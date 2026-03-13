import com.example.startdrive.shared.di.SharedFactory
import com.example.startdrive.shared.model.User
import firebase.*
import firebase.ChatMessage
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event

private var presenceUnsubscribes: MutableList<() -> Unit> = mutableListOf()
private var voiceRecorderChunks: dynamic = null
private var voiceRecorderStream: dynamic = null
private var voiceRecorder: dynamic = null
private var voiceRecorderMimeType: String = "audio/webm"
private var voiceRecordInterval: Int = 0
private var voicePlayInterval: Int = 0
private val chatScrollByContactId = mutableMapOf<String, Int>()

private fun saveChatScrollForCurrentContact() {
    val contactId = appState.selectedChatContactId ?: return
    val c = document.getElementById("sd-chat-messages")?.asDynamic()
    if (c != null) {
        val top = (c.scrollTop as? Number)?.toInt() ?: 0
        chatScrollByContactId[contactId] = top
    }
}

private fun applyScrollForChat(contactId: String) {
    val saved = chatScrollByContactId[contactId]
    val container = document.getElementById("sd-chat-messages")?.asDynamic()
    if (container != null) {
        if (saved != null) {
            window.requestAnimationFrame {
                window.requestAnimationFrame {
                    val c = document.getElementById("sd-chat-messages")?.asDynamic()
                    if (c != null) {
                        val maxScroll = ((c.scrollHeight as Number).toDouble() - (c.clientHeight as Number).toDouble()).toInt().coerceAtLeast(0)
                        c.scrollTop = saved.coerceIn(0, maxScroll)
                    }
                }
            }
        } else {
            (document.getElementById("sd-chat-messages")?.lastElementChild?.unsafeCast<dynamic>())?.scrollIntoView(js("({ block: 'end', behavior: 'smooth' })"))
        }
    }
}

private val SD_ICON_PLAY_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>"""
private val SD_ICON_PAUSE_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>"""

private fun formatVoiceDurationSec(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

/** Обновляет только UI плеера голосовых сообщений без полного рендера (чтобы не сбрасывать прокрутку). */
private fun patchVoicePlayerDOM() {
    val playingId = appState.chatPlayingVoiceId
    val currentMs = appState.chatPlayingVoiceCurrentMs
    val voiceEls = document.querySelectorAll(".sd-msg-voice")
    for (i in 0 until voiceEls.length) {
        val el = voiceEls.item(i)?.asDynamic() ?: continue
        val msgId = el.getAttribute("data-voice-id") as? String ?: continue
        val dur = ((el.getAttribute("data-voice-duration") as? String) ?: "0").toIntOrNull() ?: 0
        val isPlaying = playingId == msgId
        val ms = if (isPlaying) currentMs else 0
        val progress = if (dur > 0) (ms.toDouble() / 1000 / dur).coerceIn(0.0, 1.0) else 0.0
        val progressPct = (progress * 100).toInt()
        val currentStr = formatVoiceDurationSec((ms / 1000).coerceAtLeast(0).coerceAtMost(dur))
        val totalStr = formatVoiceDurationSec(dur)
        js("(function(el, isPlaying, progressPct, currentStr, totalStr, iconPlay, iconPause){ var btn=el.querySelector('.sd-voice-play-btn'); if(btn){ btn.innerHTML=isPlaying?iconPause:iconPlay; btn.setAttribute('title',isPlaying?'Пауза':'Воспроизвести'); btn.setAttribute('aria-label',isPlaying?'Пауза':'Воспроизвести'); } var wrap=el.querySelector('.sd-voice-progress-wrap'); if(wrap){ var bar=wrap.querySelector('.sd-voice-progress-bar'); if(isPlaying||progressPct>0){ if(!bar){ bar=document.createElement('div'); bar.className='sd-voice-progress-bar'; var ref=wrap.querySelector('.sd-voice-times'); if(ref) wrap.insertBefore(bar,ref); else wrap.appendChild(bar); } bar.style.width=progressPct+'%'; } else if(bar) bar.remove(); } var cur=el.querySelector('.sd-voice-current'); if(cur) cur.textContent=currentStr; var tot=el.querySelector('.sd-voice-total'); if(tot) tot.textContent=totalStr; })")(
            el, isPlaying, progressPct, currentStr, totalStr, SD_ICON_PLAY_SVG, SD_ICON_PAUSE_SVG
        )
    }
}

private fun subscribeChatPresence(contactIds: List<String>) {
    presenceUnsubscribes.forEach { it() }
    presenceUnsubscribes.clear()
    updateState { chatContactOnlineIds = emptySet() }
    contactIds.forEach { id ->
        val unsub = subscribePresence(id) { online ->
            updateState {
                val newSet = chatContactOnlineIds.toMutableSet()
                if (online) newSet.add(id) else newSet.remove(id)
                chatContactOnlineIds = newSet
            }
        }
        presenceUnsubscribes.add(unsub)
    }
}

fun main() {
    window.onload = onload@ { _: Event ->
        val root = document.getElementById("root") ?: return@onload
        initFirebase()

        var lastRenderedTabIndex: Int? = null

        fun render() {
            val state = appState
            val networkBanner = state.networkError?.let { msg ->
                """<div class="sd-network-error" id="sd-network-error"><span>$msg</span> <button type="button" id="sd-dismiss-network-error" class="sd-btn-inline">Закрыть</button></div>"""
            } ?: ""
            val loadingOverlay = if (state.loading) """<div class="sd-loading-overlay" id="sd-loading-overlay"><div class="sd-spinner"></div><p>Загрузка…</p></div>""" else ""
            val panelScreen = state.screen == AppScreen.Admin || state.screen == AppScreen.Instructor || state.screen == AppScreen.Cadet
            val sdCard = (root.unsafeCast<dynamic>().querySelector("#sd-card")) as? org.w3c.dom.Element
            if (panelScreen && state.user != null && sdCard != null && state.networkError == null && !state.loading) {
                val tabs = when (state.screen) {
                    AppScreen.Admin -> listOf("Главная", "Баланс", "Чат", "История")
                    AppScreen.Instructor -> listOf("Главная", "Запись", "Чат", "Билеты", "История", "Настройки")
                    else -> listOf("Главная", "Запись", "Чат", "Билеты", "История", "Настройки")
                }
                /* Состояние секций главной (развёрнуто/свёрнуто) сохраняем только при уходе с вкладки в обработчике клика по другой вкладке; при рендере не перезаписываем — в карточке может быть контент другой вкладки */
                if (state.screen == AppScreen.Admin && state.selectedTabIndex == 1) {
                    val historyDetails = sdCard.querySelector("details[data-balance-section=\"history\"]")
                    updateState { balanceHistorySectionOpen = (historyDetails?.unsafeCast<dynamic>()?.open == true) }
                }
                val (tabButtons, tabContent) = getPanelTabButtonsAndContent(state.user!!, tabs)
                if (lastRenderedTabIndex != state.selectedTabIndex) {
                    (root.unsafeCast<dynamic>().querySelector("nav.sd-tabs") as? org.w3c.dom.Element)?.innerHTML = tabButtons
                    lastRenderedTabIndex = state.selectedTabIndex
                }
                val cardContent = if (state.pddExamMode && state.pddQuestions.isNotEmpty()) renderTicketsTabContent() else tabContent
                sdCard.innerHTML = cardContent
                if (state.pddScrollToSignDetail && state.pddCategoryId == "signs" && state.pddSelectedSign != null) {
                    (sdCard.querySelector("#sd-pdd-sign-detail-scroll") as? org.w3c.dom.Element)?.let { el ->
                        el.unsafeCast<dynamic>().scrollIntoView(js("({ block: 'start', behavior: 'smooth' })"))
                    }
                    updateState { pddScrollToSignDetail = false }
                }
                attachListeners(root)
                return
            }
            lastRenderedTabIndex = null
            val html = when (state.screen) {
                AppScreen.Login -> renderLogin(state.error, state.loading)
                AppScreen.Register -> renderRegister(state.error, state.loading)
                AppScreen.PendingApproval -> renderPendingApproval()
                AppScreen.ProfileNotFound -> renderProfileNotFound(state.error ?: "Профиль не найден.")
                AppScreen.Admin -> renderPanel(state.user!!, "Администратор", listOf("Главная", "Баланс", "Чат", "История"))
                AppScreen.Instructor -> renderPanel(state.user!!, "Инструктор", listOf("Главная", "Запись", "Чат", "Билеты", "История", "Настройки"))
                AppScreen.Cadet -> renderPanel(state.user!!, "Курсант", listOf("Главная", "Запись", "Чат", "Билеты", "История", "Настройки"))
            }
            root.innerHTML = networkBanner + loadingOverlay + html
            attachListeners(root)
        }

        var renderScheduled = false
        fun scheduleRender() {
            if (renderScheduled) return
            renderScheduled = true
            window.requestAnimationFrame {
                render()
                renderScheduled = false
            }
        }

        onStateChanged = { scheduleRender() }

        setupPanelClickDelegation(root)

        onAuthStateChanged { uid ->
            if (uid == null) {
                updateState { screen = AppScreen.Login; user = null; error = null }
                return@onAuthStateChanged
            }
            updateState { loading = true; error = null }
            getCurrentUser { user, errorMsg ->
                updateState { loading = false; networkError = null }
                if (user == null) {
                    updateState {
                        screen = AppScreen.ProfileNotFound
                        this.user = null
                        error = errorMsg ?: "Профиль не найден в базе."
                    }
                    return@getCurrentUser
                }
                updateState { this.user = user; error = null; networkError = null }
                updateState {
                    screen = when (user.role) {
                        "admin" -> AppScreen.Admin
                        "instructor" -> if (user.isActive) AppScreen.Instructor else AppScreen.PendingApproval
                        "cadet" -> if (user.isActive) AppScreen.Cadet else AppScreen.PendingApproval
                        else -> AppScreen.PendingApproval
                    }
                }
                setPresence(user.id, true)
            }
        }

        render()
    } as (Event) -> dynamic
}

private fun launchConfetti() {
    val container = document.createElement("div")
    container.setAttribute("class", "sd-confetti-container")
    document.body?.appendChild(container)
    val colors = arrayOf("#ff4081","#ff6d00","#ffd600","#00e676","#2979ff","#d500f9","#00bcd4","#ff1744")
    val shapes = arrayOf("square","rect","circle")
    for (i in 0 until 90) {
        val p = document.createElement("div")
        val color = colors[i % colors.size]
        val shape = shapes[i % shapes.size]
        val left = (kotlin.random.Random.nextDouble() * 100)
        val delay = (kotlin.random.Random.nextDouble() * 2.5)
        val dur = 2.0 + kotlin.random.Random.nextDouble() * 2.0
        val size = 6 + kotlin.random.Random.nextInt(6)
        val drift = -40 + kotlin.random.Random.nextInt(80)
        val rot = kotlin.random.Random.nextInt(720)
        val w = if (shape == "rect") (size * 2.2).toInt() else size
        val br = if (shape == "circle") "50%" else "2px"
        p.setAttribute("style",
            "position:absolute;top:-12px;left:${left}%;width:${w}px;height:${size}px;" +
            "background:$color;border-radius:$br;opacity:0.92;" +
            "animation:sd-confetti-fall ${dur}s ease-in ${delay}s forwards;" +
            "--sd-drift:${drift}px;--sd-rot:${rot}deg;")
        container.appendChild(p)
    }
    window.setTimeout({ document.body?.removeChild(container) }, 5500)
}

private fun renderLogin(error: String?, loading: Boolean): String {
    val err = if (error != null) """<div class="sd-auth-error"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>$error</div>""" else ""
    val btn = if (loading) """<span class="sd-auth-spinner"></span>Вход…""" else "Войти"
    return """
        <div class="sd-auth-page">
            <div class="sd-auth-panel">
                <div class="sd-auth-panel-deco1"></div>
                <div class="sd-auth-panel-deco2"></div>
                <div class="sd-auth-panel-deco3"></div>
                <div class="sd-auth-panel-inner">
                    <div class="sd-auth-panel-logo-wrap">
                        <div class="sd-auth-logo-gloss">
                            <img src="startdrive-logo.png" alt="StartDrive" class="sd-auth-logo-img" />
                            <div class="sd-auth-logo-gloss-layer"></div>
                        </div>
                    </div>
                    <p class="sd-auth-panel-desc">Управляйте расписанием, общайтесь с инструктором и следите за своим прогрессом.</p>
                    <ul class="sd-auth-features">
                        <li><svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>Запись на занятия онлайн</li>
                        <li><svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>Чат с инструктором</li>
                        <li><svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>История занятий и баланс</li>
                    </ul>
                </div>
            </div>
            <div class="sd-auth-form-side">
                <div class="sd-auth-form-wrap">
                    <h2 class="sd-auth-title">Вход</h2>
                    <p class="sd-auth-subtitle">Введите данные своего аккаунта</p>
                    $err
                    <div class="sd-auth-field">
                        <label class="sd-auth-label">Email</label>
                        <div class="sd-auth-input-wrap">
                            <span class="sd-auth-field-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg></span>
                            <input type="email" id="sd-email" class="sd-auth-input" placeholder="you@example.com" autocomplete="email" />
                        </div>
                    </div>
                    <div class="sd-auth-field">
                        <label class="sd-auth-label">Пароль</label>
                        <div class="sd-auth-input-wrap">
                            <span class="sd-auth-field-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></span>
                            <input type="password" id="sd-password" class="sd-auth-input" placeholder="••••••••" autocomplete="current-password" />
                        </div>
                    </div>
                    <label class="sd-auth-toggle-row">
                        <span>Оставаться в системе</span>
                        <span class="sd-auth-toggle">
                            <input type="checkbox" id="sd-stay" checked />
                            <span class="sd-auth-toggle-track"><span class="sd-auth-toggle-thumb"></span></span>
                        </span>
                    </label>
                    <button type="button" id="sd-btn-signin" class="sd-auth-btn sd-auth-btn-primary" ${if (loading) "disabled" else ""}>$btn</button>
                    <p class="sd-auth-switch">Нет аккаунта? <button type="button" id="sd-btn-register" class="sd-auth-link">Зарегистрироваться</button></p>
                </div>
            </div>
        </div>
    """.trimIndent()
}

private fun renderRegister(error: String?, loading: Boolean): String {
    val err = if (error != null) """<div class="sd-auth-error"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>$error</div>""" else ""
    val btn = if (loading) """<span class="sd-auth-spinner"></span>Регистрация…""" else "Зарегистрироваться"
    return """
        <div class="sd-auth-page">
            <div class="sd-auth-panel">
                <div class="sd-auth-panel-deco1"></div>
                <div class="sd-auth-panel-deco2"></div>
                <div class="sd-auth-panel-deco3"></div>
                <div class="sd-auth-panel-inner">
                    <div class="sd-auth-panel-logo-wrap">
                        <div class="sd-auth-logo-gloss">
                            <img src="startdrive-logo.png" alt="StartDrive" class="sd-auth-logo-img" />
                            <div class="sd-auth-logo-gloss-layer"></div>
                        </div>
                    </div>
                    <p class="sd-auth-panel-desc">Всего несколько шагов — и личный кабинет уже готов к работе.</p>
                    <ul class="sd-auth-features">
                        <li><svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>Быстрая регистрация</li>
                        <li><svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>Три роли: курсант, инструктор, администратор</li>
                        <li><svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>Безопасный вход через Firebase</li>
                    </ul>
                </div>
            </div>
            <div class="sd-auth-form-side">
                <div class="sd-auth-form-wrap">
                    <h2 class="sd-auth-title">Регистрация</h2>
                    <p class="sd-auth-subtitle">Заполните данные для создания аккаунта</p>
                    $err
                    <div class="sd-auth-field">
                        <label class="sd-auth-label">ФИО</label>
                        <div class="sd-auth-input-wrap">
                            <span class="sd-auth-field-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>
                            <input type="text" id="sd-fullName" class="sd-auth-input" placeholder="Иванов Иван Иванович" autocomplete="name" />
                        </div>
                    </div>
                    <div class="sd-auth-field">
                        <label class="sd-auth-label">Email</label>
                        <div class="sd-auth-input-wrap">
                            <span class="sd-auth-field-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg></span>
                            <input type="email" id="sd-reg-email" class="sd-auth-input" placeholder="you@example.com" autocomplete="email" />
                        </div>
                    </div>
                    <div class="sd-auth-row2">
                        <div class="sd-auth-field">
                            <label class="sd-auth-label">Телефон</label>
                            <div class="sd-auth-input-wrap">
                                <span class="sd-auth-field-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.5 2 2 0 0 1 3.59 1.31h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 9a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/></svg></span>
                                <input type="tel" id="sd-phone" class="sd-auth-input" placeholder="+7 900 000-00-00" autocomplete="tel" />
                            </div>
                        </div>
                        <div class="sd-auth-field">
                            <label class="sd-auth-label">Роль</label>
                            <div class="sd-auth-input-wrap">
                                <span class="sd-auth-field-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>
                                <select id="sd-role" class="sd-auth-input sd-auth-select">
                                    <option value="cadet">Курсант</option>
                                    <option value="instructor">Инструктор</option>
                                    <option value="admin">Администратор</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="sd-auth-field">
                        <label class="sd-auth-label">Пароль</label>
                        <div class="sd-auth-input-wrap">
                            <span class="sd-auth-field-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></span>
                            <input type="password" id="sd-reg-password" class="sd-auth-input" placeholder="Минимум 6 символов" autocomplete="new-password" />
                        </div>
                    </div>
                    <button type="button" id="sd-btn-do-register" class="sd-auth-btn sd-auth-btn-primary" ${if (loading) "disabled" else ""}>$btn</button>
                    <p class="sd-auth-switch">Уже есть аккаунт? <button type="button" id="sd-btn-back" class="sd-auth-link">Войти</button></p>
                </div>
            </div>
        </div>
    """.trimIndent()
}

private fun renderPendingApproval(): String = """
    <header class="sd-header">
        <h1>StartDrive</h1>
        <p>Ожидание подтверждения</p>
    </header>
    <main class="sd-content">
        <div class="sd-card">
            <h2>Ожидание подтверждения</h2>
            <p>Ваша заявка на регистрацию отправлена. Администратор активирует ваш аккаунт. После активации вы сможете войти.</p>
            <button type="button" id="sd-btn-check" class="sd-btn sd-btn-primary">Проверить снова</button>
            <button type="button" id="sd-btn-signout-pending" class="sd-btn sd-btn-secondary">Выйти</button>
        </div>
    </main>
""".trimIndent()

private fun renderProfileNotFound(message: String): String = """
    <header class="sd-header">
        <h1>StartDrive</h1>
        <p>Профиль не найден</p>
    </header>
    <main class="sd-content">
        <div class="sd-card">
            <h2>Не удалось загрузить профиль</h2>
            <p class="sd-error">$message</p>
            <p>Если вы входите с теми же данными, что и в приложении — убедитесь, что в Firebase Console в Firestore есть документ в коллекции <strong>users</strong> с id вашего пользователя (UID из Authentication).</p>
            <button type="button" id="sd-btn-signout-profile-not-found" class="sd-btn sd-btn-primary">Выйти</button>
        </div>
    </main>
""".trimIndent()

private fun renderChatTabContent(currentUser: User): String {
    val contactId = appState.selectedChatContactId
    val contacts = appState.chatContacts
    val loading = appState.chatContactsLoading
    val messages = appState.chatMessages
    val myId = currentUser.id
    if (contactId != null) {
        val contact = contacts.find { it.id == contactId } ?: return """<p class="sd-error">Контакт не найден.</p>"""
        val iconCheck = """<svg class="sd-msg-check" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>"""
        val iconSendSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>"""
        val iconBackSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>"""
        val iconMicSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>"""
        val iconStopSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="6" width="12" height="12"/></svg>"""
        val iconPlaySvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>"""
        val iconPauseSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>"""
        val contactInitials = contact.initials().ifEmpty { "?" }.escapeHtml()
        val contactAvatarBg = avatarColorForId(contact.id).escapeHtml()
        fun formatVoiceDuration(sec: Int): String {
            val m = sec / 60
            val s = sec % 60
            return "$m:${s.toString().padStart(2, '0')}"
        }
        val msgsHtml = messages.joinToString("") { msg ->
            val isMe = msg.senderId == myId
            val cls = if (isMe) "sd-msg sd-msg-me" else "sd-msg sd-msg-them"
            val timeStr = formatMessageDateTime(msg.timestamp).escapeHtml()
            val statusHtml = if (isMe) {
                val isRead = msg.status == "read"
                val checks = if (isRead) "$iconCheck$iconCheck" else iconCheck
                val checkClass = if (isRead) "sd-msg-checks sd-msg-checks-read" else "sd-msg-checks sd-msg-checks-sent"
                """<span class="$checkClass" title="${if (isRead) "Прочитано" else "Доставлено"}">$checks</span>"""
            } else ""
            val timeRow = """<span class="sd-msg-time">$timeStr</span>"""
            if (msg.isVoice && !msg.voiceUrl.isNullOrBlank() && (msg.voiceDurationSec ?: 0) > 0) {
                val dur = msg.voiceDurationSec!!
                val totalStr = formatVoiceDuration(dur).escapeHtml()
                val isPlaying = appState.chatPlayingVoiceId == msg.id
                val currentMs = if (msg.id == appState.chatPlayingVoiceId) appState.chatPlayingVoiceCurrentMs else 0
                val progress = if (dur > 0) (currentMs.toDouble() / 1000 / dur).coerceIn(0.0, 1.0) else 0.0
                val progressPct = (progress * 100).toInt()
                val currentStr = formatVoiceDuration((currentMs / 1000).coerceAtLeast(0).coerceAtMost(dur)).escapeHtml()
                """<div class="$cls sd-msg-voice" data-voice-id="${msg.id.escapeHtml()}" data-voice-url="${msg.voiceUrl.escapeHtml()}" data-voice-duration="$dur">
                    <div class="sd-voice-player">
                        <button type="button" class="sd-voice-play-btn" title="${if (isPlaying) "Пауза" else "Воспроизвести"}" aria-label="${if (isPlaying) "Пауза" else "Воспроизвести"}">${if (isPlaying) iconPauseSvg else iconPlaySvg}</button>
                        <div class="sd-voice-progress-wrap">
                            ${if (isPlaying || currentMs > 0) """<div class="sd-voice-progress-bar" style="width:${progressPct}%"></div>""" else ""}
                            <div class="sd-voice-times"><span class="sd-voice-current">$currentStr</span><span class="sd-voice-total">$totalStr</span></div>
                        </div>
                    </div>
                    <audio id="sd-voice-audio-${msg.id.escapeHtml()}" class="sd-voice-audio" preload="metadata"></audio>
                    <div class="sd-msg-footer">$timeRow$statusHtml</div>
                </div>"""
            } else {
                """<div class="$cls"><span class="sd-msg-text">${msg.text.escapeHtml()}</span><div class="sd-msg-footer">$timeRow$statusHtml</div></div>"""
            }
        }
        val recording = appState.chatVoiceRecording
        val elapsed = appState.chatVoiceRecordElapsedSec
        val recordTimeStr = "${elapsed / 60}:${(elapsed % 60).toString().padStart(2, '0')}"
        val inputRowHtml = if (recording) {
            """<div class="sd-chat-input-row sd-chat-voice-recording" id="sd-chat-voice-recording">
                <span class="sd-chat-recording-text">Запись… $recordTimeStr</span>
                <button type="button" id="sd-chat-voice-stop" class="sd-chat-voice-stop-btn" title="Остановить">$iconStopSvg</button>
            </div>"""
        } else {
            """<div class="sd-chat-input-row">
                <input type="text" id="sd-chat-input" class="sd-chat-input" placeholder="Сообщение..." maxlength="2000" />
                <button type="button" id="sd-chat-send" class="sd-chat-send-btn" title="Отправить">$iconSendSvg</button>
                <button type="button" id="sd-chat-voice-mic" class="sd-chat-voice-mic-btn" title="Голосовое сообщение">$iconMicSvg</button>
            </div>"""
        }
        return """
            <div class="sd-chat-tab">
            <div class="sd-chat-conversation">
                <div class="sd-chat-header">
                    <button type="button" id="sd-chat-back" class="sd-chat-back-btn" title="Назад" aria-label="Назад">$iconBackSvg</button>
                    <div class="sd-chat-header-avatar" style="background:$contactAvatarBg">$contactInitials</div>
                    <span class="sd-chat-contact-name">${formatShortName(contact.fullName).escapeHtml()}</span>
                </div>
                <div class="sd-chat-messages" id="sd-chat-messages">$msgsHtml</div>
                $inputRowHtml
            </div>
            </div>
        """.trimIndent()
    }
    val loadingLine = if (loading) """<p class="sd-chat-loading-text">Загрузка контактов…</p>""" else ""
    val iconRefreshSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>"""
    fun contactRow(c: User) = run {
        val initials = c.initials().ifEmpty { "?" }.escapeHtml()
        val avatarBg = avatarColorForId(c.id).escapeHtml()
        val isOnline = appState.chatContactOnlineIds.contains(c.id)
        val onlineDot = if (isOnline) """<span class="sd-chat-contact-dot sd-chat-contact-dot-online"></span>""" else """<span class="sd-chat-contact-dot sd-chat-contact-dot-offline"></span>"""
        val roleLabel = when (c.role) { "instructor" -> "Инструктор" "cadet" -> "Курсант" "admin" -> "Администратор" else -> c.role }.escapeHtml()
        val statusText = if (isOnline) "в сети" else "не в сети"
        val statusCls = if (isOnline) "sd-chat-contact-status-online" else "sd-chat-contact-status-offline"
        """<button type="button" class="sd-chat-contact" data-contact-id="${c.id.escapeHtml()}">
            <div class="sd-chat-contact-avatar-wrap">
                <span class="sd-chat-contact-avatar" style="background:$avatarBg">$initials</span>
                $onlineDot
            </div>
            <span class="sd-chat-contact-info">
                <span class="sd-chat-contact-name-row">${formatShortName(c.fullName).escapeHtml()}</span>
                <span class="sd-chat-contact-meta"><span class="sd-chat-contact-role">$roleLabel</span> · <span class="sd-chat-contact-status $statusCls">$statusText</span></span>
            </span>
        </button>"""
    }
    val instructors = contacts.filter { it.role == "instructor" }.sortedBy { it.fullName }
    val cadets = contacts.filter { it.role == "cadet" }.sortedBy { it.fullName }
    val others = contacts.filter { it.role !in listOf("instructor", "cadet") }.sortedBy { it.fullName }
    val instructorsSection = if (instructors.isEmpty()) "" else """<div class="sd-chat-contacts-group"><p class="sd-chat-contacts-group-title">Инструкторы</p><div class="sd-chat-contacts">${instructors.joinToString("") { contactRow(it) }}</div></div>"""
    val cadetsSection = if (cadets.isEmpty()) "" else """<div class="sd-chat-contacts-group"><p class="sd-chat-contacts-group-title">Курсанты</p><div class="sd-chat-contacts">${cadets.joinToString("") { contactRow(it) }}</div></div>"""
    val othersSection = if (others.isEmpty()) "" else """<div class="sd-chat-contacts-group"><p class="sd-chat-contacts-group-title">Другие</p><div class="sd-chat-contacts">${others.joinToString("") { contactRow(it) }}</div></div>"""
    val contactsBlock = if (contacts.isEmpty() && !loading) """<p class="sd-chat-empty-hint">Нет доступных контактов.</p>""" else """$instructorsSection$cadetsSection$othersSection"""
    val refreshBtnStyled = """<button type="button" id="sd-chat-refresh" class="sd-chat-refresh-btn">$iconRefreshSvg Обновить контакты</button>"""
    return """<div class="sd-chat-tab"><div class="sd-chat-list-header"><h2 class="sd-chat-title">Чат</h2>$refreshBtnStyled</div>$loadingLine$contactsBlock</div>"""
}

/** Экранирует HTML, чтобы пользовательский ввод не приводил к XSS. */
private fun String.escapeHtml(): String = this
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&#39;")

/** Формат даты и времени для сообщения (DD.MM.YYYY HH:mm). */
private fun formatMessageDateTime(timestampMs: Long): String {
    if (timestampMs <= 0L) return ""
    val d = js("(function(ts){ return new Date(ts); })").unsafeCast<(Long) -> dynamic>().invoke(timestampMs)
    val day = (d.getDate() as Int).toString().padStart(2, '0')
    val month = ((d.getMonth() as Int) + 1).toString().padStart(2, '0')
    val year = d.getFullYear() as Int
    val hours = (d.getHours() as Int).toString().padStart(2, '0')
    val minutes = (d.getMinutes() as Int).toString().padStart(2, '0')
    return "$day.$month.$year $hours:$minutes"
}

/** Цвет кружка-аватара по id контакта (у каждого контакта свой оттенок). */
private fun avatarColorForId(id: String): String {
    val h = id.hashCode().and(0x7FFF_FFFF) % 360
    val s = 55
    val l = 42
    return "hsl($h,$s%,$l%)"
}

/** Дни недели с понедельника по воскресенье (для раздела «Мой график»). */
private val WEEKDAY_NAMES = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")

/** Индекс дня недели 0=Пн .. 6=Вс по timestamp. */
private fun getWeekdayIndex(startTimeMillis: Long?): Int {
    if (startTimeMillis == null || startTimeMillis <= 0) return 0
    val fn = js("(function(ms){ var d = new Date(ms).getDay(); return (d+6)%7; })").unsafeCast<(Long) -> Int>()
    return fn(startTimeMillis)
}

/** Формат «Фамилия И.О.» для списка курсантов */
private fun formatShortName(fullName: String): String {
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    if (parts.isEmpty()) return "—"
    if (parts.size == 1) return parts[0]
    val surname = parts[0]
    val initials = parts.drop(1).map { it.firstOrNull()?.uppercase()?.plus(".") ?: "" }.joinToString("")
    return "$surname $initials"
}

private val iconPhoneSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>"""
private val iconChatSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>"""
private val iconUserPlusSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>"""
private val iconPowerSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18.36 6.64a9 9 0 1 1-12.73 0"/><line x1="12" y1="2" x2="12" y2="12"/></svg>"""
private val iconTrashSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>"""
private val iconUnlinkSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18.84 12.25l5.72-5.72a2.5 2.5 0 0 0-3.54-3.54l-5.72 5.72"/><path d="M5.16 11.75l-5.72 5.72a2.5 2.5 0 0 0 3.54 3.54l5.72-5.72"/><line x1="8" y1="16" x2="16" y2="8"/></svg>"""
private val iconUserSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>"""
private val iconPhoneLabelSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>"""
private val iconEmailLabelSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>"""
private val iconTicketSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z"/><path d="M13 5v2"/><path d="M13 17v2"/><path d="M13 11v2"/></svg>"""
private val iconInstructorSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>"""
private val iconSelectSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>"""
private val iconEyeSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>"""
private val iconCreditSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>"""
private val iconDebitSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"/></svg>"""
private val iconSetSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="9" x2="19" y2="9"/><line x1="5" y1="15" x2="19" y2="15"/></svg>"""
private val iconResetSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>"""
private val iconCalendarSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>"""
private val iconClockSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>"""
private val iconPlaySvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>"""
private val iconLateSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>"""


/** Длительность занятия вождением (минуты). */
private const val LESSON_DURATION_MINUTES = 90L
private val LESSON_DURATION_MS = LESSON_DURATION_MINUTES * 60 * 1000

/** За сколько минут до вождения активируется кнопка «Начать вождение». */
private const val START_ALLOWED_MINUTES_BEFORE = 15L

/** Категории билетов ПДД — как в Android (PddRepository.getCategories). */
private val PDD_CATEGORIES = listOf(
    "A_B" to "Категория AB",
    "C_D" to "Категория CD",
    "by_topic" to "Вопросы по разделам",
    "signs" to "Дорожные знаки",
    "markup" to "Дорожная разметка",
    "penalties" to "Штрафы",
)

/** Ключ в localStorage для статистики билета (как в Android: pdd_stats). */
private fun pddTicketStorageKey(categoryId: String, ticketName: String): String = "pdd_${categoryId}_$ticketName"

/** Парсит значение из localStorage. Формат новый: "0:2:t,1:0:f" (вопрос:индекс_ответа:верно); старый: "0:t,1:f". */
private fun getPddTicketSavedParsed(categoryId: String, ticketName: String): Map<Int, Pair<Int, Boolean>> {
    val storage = js("typeof window !== 'undefined' && window.localStorage").unsafeCast<Boolean>()
    if (!storage) return emptyMap()
    val raw = js("window.localStorage.getItem").unsafeCast<(String) -> String?>().invoke(pddTicketStorageKey(categoryId, ticketName)) ?: return emptyMap()
    val map = mutableMapOf<Int, Pair<Int, Boolean>>()
    raw.split(",").forEach { part ->
        val p = part.split(":")
        when (p.size) {
            2 -> { /* старый формат: 0:t */ val idx = p[0].toIntOrNull(); if (idx != null) map[idx] = Pair(0, p[1] == "t") }
            3 -> { /* новый: 0:2:t */ val idx = p[0].toIntOrNull(); val ansIdx = p[1].toIntOrNull(); if (idx != null && ansIdx != null) map[idx] = Pair(ansIdx, p[2] == "t") }
        }
    }
    return map
}

/** Читает из localStorage только правильность ответов (для счётчиков и цветов карточек). */
private fun getPddTicketSavedResults(categoryId: String, ticketName: String): Map<Int, Boolean> =
    getPddTicketSavedParsed(categoryId, ticketName).mapValues { it.value.second }

/** Читает из localStorage сохранённые выбранные варианты (вопрос -> индекс ответа) для восстановления при повторном открытии билета. */
private fun getPddTicketSavedSelections(categoryId: String, ticketName: String): Map<Int, Int> =
    getPddTicketSavedParsed(categoryId, ticketName).mapValues { it.value.first }

/** Возвращает (правильно, с ошибкой, без ответа) и CSS-класс для карточки билета. */
private fun getPddTicketStat(categoryId: String, ticketName: String, questionsPerTicket: Int = 20): Triple<Int, Int, Int> {
    val results = getPddTicketSavedResults(categoryId, ticketName)
    val correct = results.values.count { it }
    val incorrect = results.values.count { !it }
    val noAnswer = questionsPerTicket - results.size
    return Triple(correct, incorrect, noAnswer)
}

/** Класс фона карточки билета: зелёный — все верно, красный — есть ошибки, серый — не решал. */
private fun getPddTicketCardStatClass(categoryId: String, ticketName: String, questionsPerTicket: Int = 20): String {
    val (correct, incorrect, noAnswer) = getPddTicketStat(categoryId, ticketName, questionsPerTicket)
    return when {
        noAnswer == questionsPerTicket -> "sd-pdd-ticket-stat-gray"
        incorrect > 0 -> "sd-pdd-ticket-stat-red"
        correct == questionsPerTicket -> "sd-pdd-ticket-stat-green"
        else -> "sd-pdd-ticket-stat-gray"
    }
}

/** Сохраняет результат ответа на вопрос билета в localStorage (формат: вопрос:индекс_ответа:верно). */
private fun savePddTicketResult(categoryId: String, ticketName: String, questionIndex: Int, answerIndex: Int, isCorrect: Boolean) {
    val key = pddTicketStorageKey(categoryId, ticketName)
    val existing = getPddTicketSavedParsed(categoryId, ticketName).toMutableMap()
    existing[questionIndex] = Pair(answerIndex, isCorrect)
    val value = existing.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value.first}:${if (it.value.second) "t" else "f"}" }
    js("window.localStorage.setItem").unsafeCast<(String, String) -> Unit>().invoke(key, value)
}

/** Удаляет статистику билетов для категории (обнуление). */
private fun clearPddTicketStatsForCategory(categoryId: String) {
    val storage = js("typeof window !== 'undefined' && window.localStorage").unsafeCast<Boolean>()
    if (!storage) return
    for (n in 1..40) {
        js("window.localStorage.removeItem").unsafeCast<(String) -> Unit>().invoke(pddTicketStorageKey(categoryId, "Билет $n"))
    }
}

/** Кэш бандла, встроенного в приложение (генерируется из pdd-tickets-bundle.json). */
private var embeddedPddBundleCache: dynamic = null

/** Возвращает бандл билетов из встроенного JSON (постранично внедрён при сборке). */
private fun getEmbeddedPddBundle(): dynamic? {
    val log = js("console.log").unsafeCast<(Any?) -> Unit>()
    if (embeddedPddBundleCache != null && embeddedPddBundleCache != js("undefined")) {
        log("PDD: embedded bundle (from cache)")
        return embeddedPddBundleCache
    }
    log("PDD: trying embedded bundle...")
    return try {
        val json = PddTicketsEmbedded.json
        log("PDD: embedded json length = ${json.length}")
        if (json.isBlank()) {
            log("PDD: embedded json is blank")
            null
        } else {
            val parsed = js("JSON.parse").unsafeCast<(String) -> dynamic>().invoke(json)
            embeddedPddBundleCache = parsed
            log("PDD: embedded bundle OK")
            parsed
        }
    } catch (t: Throwable) {
        log("PDD: embedded bundle error: " + (t.message ?: t.toString()))
        null
    }
}

/** Достаёт список вопросов из уже загруженного бандла билетов. */
private fun getQuestionsFromBundle(bundle: dynamic, categoryId: String, num: Int): List<PddQuestion> {
    val log = js("console.log").unsafeCast<(Any?) -> Unit>()
    if (bundle == null || bundle == js("undefined")) {
        log("PDD: getQuestionsFromBundle bundle is null/undefined")
        return emptyList()
    }
    val arr = js("(function(b, cat, n){ var c = b[cat]; return c && c[String(n)]; })").unsafeCast<(dynamic, String, Int) -> dynamic>().invoke(bundle, categoryId, num)
    if (arr == null || arr == js("undefined")) {
        log("PDD: getQuestionsFromBundle no arr for categoryId=$categoryId num=$num (check bundle has A_B/C_D and key \"$num\")")
        return emptyList()
    }
    val jsonText = js("JSON.stringify").unsafeCast<(dynamic) -> String>().invoke(arr)
    return parseTicketJson(jsonText)
}

/** Парсит JSON билета (массив вопросов) в List<PddQuestion>. Результат JSON.parse — чистый JS-объект, без .asDynamic(). */
private fun parseTicketJson(jsonText: String): List<PddQuestion> {
    return try {
        val parsed: dynamic = js("JSON.parse").unsafeCast<(String) -> dynamic>().invoke(jsonText)
        val len = (parsed.length as Int)
        val list = mutableListOf<PddQuestion>()
        for (i in 0 until len) {
            val obj: dynamic = parsed[i]
            val answersArr: dynamic = obj.answers
            val answersLen = (answersArr.length as Int)
            val answers = (0 until answersLen).map { j ->
                val a: dynamic = answersArr[j]
                PddAnswer(
                    answerText = (a.answer_text as? String) ?: "",
                    isCorrect = (a.is_correct as? Boolean) ?: false,
                )
            }
            val topicArr: dynamic = obj.topic
            val topicLen = (topicArr.length as Int)
            val topic = (0 until topicLen).map { (topicArr[it] as? String) ?: "" }
            val img = obj.image as? String
            val image = if (img.isNullOrBlank() || img.endsWith("no_image.jpg")) null else img
            list.add(PddQuestion(
                id = (obj.id as? String) ?: "",
                title = (obj.title as? String) ?: "",
                ticketNumber = (obj.ticket_number as? String) ?: "",
                ticketCategory = (obj.ticket_category as? String) ?: "",
                image = image,
                question = (obj.question as? String) ?: "",
                answers = answers,
                correctAnswer = (obj.correct_answer as? String) ?: "",
                answerTip = (obj.answer_tip as? String) ?: "",
                topic = topic,
            ))
        }
        list
    } catch (t: Throwable) {
        js("console.log").unsafeCast<(Any?) -> Unit>().invoke("PDD: parseTicketJson error: " + (t.message ?: t.toString()))
        emptyList()
    }
}

/** Собирает все вопросы по категории из бандла (все 40 билетов). */
private fun getAllQuestionsFromBundle(bundle: dynamic, categoryId: String): List<PddQuestion> {
    val list = mutableListOf<PddQuestion>()
    for (n in 1..40) {
        list.addAll(getQuestionsFromBundle(bundle, categoryId, n))
    }
    return list
}

/** Возвращает 5 случайных вопросов по теме (первый topic из вопроса), исключая указанные id. */
private fun getAdditionalQuestionsByTopic(allQuestions: List<PddQuestion>, topicName: String, excludeIds: Set<String>): List<PddQuestion> {
    val byTopic = allQuestions.filter { q -> q.topic.firstOrNull()?.takeIf { it.isNotBlank() } == topicName && q.id !in excludeIds }
    return byTopic.shuffled().take(5)
}

/** Генерирует экзаменационный билет: 4 блока по 5 вопросов из 4 случайных билетов. Блок 0: вопросы 0–4, блок 1: 5–9, блок 2: 10–14, блок 3: 15–19. */
private fun generateExamTicket(bundle: dynamic, categoryId: String): Pair<List<PddQuestion>, List<Int>> {
    val ticketNumbers = (1..40).toList().shuffled().take(4)
    val questions = mutableListOf<PddQuestion>()
    val blockIndices = mutableListOf<Int>()
    for (block in 0..3) {
        val ticketNum = ticketNumbers[block]
        val ticketQuestions = getQuestionsFromBundle(bundle, categoryId, ticketNum)
        val startIdx = block * 5
        val endIdx = (block + 1) * 5
        for (i in startIdx until endIdx.coerceAtMost(ticketQuestions.size)) {
            questions.add(ticketQuestions[i])
            blockIndices.add(block)
        }
    }
    return Pair(questions, blockIndices)
}

private var examTimerIntervalId: Int? = null

private fun clearExamTimer() {
    examTimerIntervalId?.let { id -> js("clearInterval").unsafeCast<(Int) -> Unit>().invoke(id) }
    examTimerIntervalId = null
}

/** Запускает таймер обновления раз в секунду для отображения обратного отсчёта экзамена (основная и дополнительная части). */
private fun startExamTimer() {
    clearExamTimer()
    examTimerIntervalId = js("setInterval").unsafeCast<(Any?, Int) -> Int>().invoke({
        if (!appState.pddExamMode) return@invoke
        when (appState.pddExamPhase) {
            "main" -> updateState { }
            "additional" -> {
                val start = appState.pddExamAdditionalStartTimeMs ?: 0.0
                val dur = appState.pddExamAdditionalDurationSec
                val now = js("Date.now").unsafeCast<() -> Double>().invoke()
                if ((now - start) / 1000.0 >= dur) {
                    clearExamTimer()
                    updateState { pddExamResultPass = false; pddExamPhase = "result" }
                } else {
                    updateState { }
                }
            }
            else -> { }
        }
    }, 1000)
}

/** Предзагрузка бандла билетов при открытии категории A_B/C_D, чтобы клик по билету сработал сразу. */
private fun preloadPddBundleIfNeeded() {
    var b = appState.pddTicketsBundle
    if (b != null && b != js("undefined")) return
    b = getEmbeddedPddBundle()
    if (b != null && b != js("undefined")) {
        updateState { pddTicketsBundle = b }
        return
    }
    b = js("window.__PDD_TICKETS_BUNDLE__").unsafeCast<dynamic>()
    if (b != null && b != js("undefined")) {
        updateState { pddTicketsBundle = b }
        return
    }
    window.fetch("pdd-tickets-bundle.json").then { r: dynamic ->
        if ((r.status as Int) != 200) return@then
        r.text().then { text: dynamic ->
            val parsed = js("JSON.parse").unsafeCast<(String) -> dynamic>().invoke(text.unsafeCast<String>())
            updateState { pddTicketsBundle = parsed }
        }
    }.catch { _: dynamic ->
        val script = document.createElement("script")
        script.asDynamic().src = "pdd-tickets-bundle.js"
        script.asDynamic().onload = {
            val w = js("window.__PDD_TICKETS_BUNDLE__").unsafeCast<dynamic>()
            if (w != null && w != js("undefined")) updateState { pddTicketsBundle = w }
        }
        document.head?.appendChild(script)
    }
}

/** Обработка клика по категории ПДД: переход или загрузка данных. */
private fun handlePddCategoryClick(catId: String) {
    when (catId) {
        "A_B", "C_D" -> {
            updateState { pddCategoryId = catId }
            preloadPddBundleIfNeeded()
        }
        "signs" -> {
            updateState { pddCategoryId = catId; pddLoading = true }
            window.fetch("pdd/signs/signs.json").then { r: dynamic ->
                r.text().then { text: dynamic ->
                    val list = parseSignsJson(text.unsafeCast<String>())
                    updateState { pddSignsSections = list; pddLoading = false }
                }
            }.catch { _: dynamic -> updateState { pddLoading = false; pddSignsSections = emptyList() } }
        }
        "markup" -> {
            updateState { pddCategoryId = catId; pddLoading = true }
            window.fetch("pdd/markup/markup.json").then { r: dynamic ->
                r.text().then { text: dynamic ->
                    val list = parseMarkupJson(text.unsafeCast<String>())
                    updateState { pddMarkupSections = list; pddLoading = false }
                }
            }.catch { _: dynamic -> updateState { pddLoading = false; pddMarkupSections = emptyList() } }
        }
        "penalties" -> {
            updateState { pddCategoryId = catId; pddLoading = true }
            window.fetch("pdd/penalties/penalties.json").then { r: dynamic ->
                r.text().then { text: dynamic ->
                    val list = parsePenaltiesJson(text.unsafeCast<String>())
                    updateState { pddPenalties = list; pddLoading = false }
                }
            }.catch { _: dynamic -> updateState { pddLoading = false; pddPenalties = emptyList() } }
        }
        "by_topic" -> {
            updateState { pddCategoryId = catId; pddLoading = true }
            loadAllTicketsByTopic()
        }
        else -> updateState { pddCategoryId = catId }
    }
}

private fun parseSignsJson(jsonText: String): List<PddSignsSection> {
    return try {
        val root: dynamic = js("JSON.parse").unsafeCast<(String) -> dynamic>().invoke(jsonText)
        val keys: dynamic = js("Object.keys").unsafeCast<(dynamic) -> dynamic>().invoke(root)
        val list = mutableListOf<PddSignsSection>()
        val keysLen = (keys.length as Int)
        for (i in 0 until keysLen) {
            val sectionName = (keys[i] as? String) ?: continue
            val sectionObj: dynamic = root[sectionName]
            if (sectionObj == null || sectionObj == js("undefined")) continue
            val itemKeys: dynamic = js("Object.keys").unsafeCast<(dynamic) -> dynamic>().invoke(sectionObj)
            val items = mutableListOf<PddSignItem>()
            val itemKeysLen = (itemKeys.length as Int)
            for (j in 0 until itemKeysLen) {
                val num = (itemKeys[j] as? String) ?: continue
                val obj: dynamic = sectionObj[num]
                if (obj == null || obj == js("undefined")) continue
                val img = (obj.image as? String)?.trim() ?: ""
                val imagePath = when {
                    img.isEmpty() -> ""
                    img.startsWith("./") -> "/pdd/" + img.drop(2).replace("\\", "/")
                    else -> "/pdd/images/$img"
                }
                items.add(PddSignItem(
                    number = (obj.number as? String) ?: num,
                    title = (obj.title as? String) ?: "",
                    imagePath = imagePath,
                    description = (obj.description as? String) ?: "",
                ))
            }
            list.add(PddSignsSection(name = sectionName, items = items))
        }
        list
    } catch (_: Throwable) { emptyList() }
}

private fun parseMarkupJson(jsonText: String): List<PddMarkupSection> {
    return try {
        val root: dynamic = js("JSON.parse").unsafeCast<(String) -> dynamic>().invoke(jsonText)
        val keys: dynamic = js("Object.keys").unsafeCast<(dynamic) -> dynamic>().invoke(root)
        val list = mutableListOf<PddMarkupSection>()
        val keysLen = (keys.length as Int)
        for (i in 0 until keysLen) {
            val sectionName = (keys[i] as? String) ?: continue
            val sectionObj: dynamic = root[sectionName]
            if (sectionObj == null || sectionObj == js("undefined")) continue
            val itemKeys: dynamic = js("Object.keys").unsafeCast<(dynamic) -> dynamic>().invoke(sectionObj)
            val items = mutableListOf<PddMarkupItem>()
            val itemKeysLen = (itemKeys.length as Int)
            for (j in 0 until itemKeysLen) {
                val num = (itemKeys[j] as? String) ?: continue
                val obj: dynamic = sectionObj[num]
                if (obj == null || obj == js("undefined")) continue
                val img = (obj.image as? String)?.trim() ?: ""
                val imagePath = when {
                    img.isEmpty() -> ""
                    img.startsWith("./") -> "/pdd/" + img.drop(2).replace("\\", "/")
                    else -> "/pdd/images/$img"
                }
                items.add(PddMarkupItem(
                    number = (obj.number as? String) ?: num,
                    imagePath = imagePath,
                    description = (obj.description as? String) ?: "",
                ))
            }
            list.add(PddMarkupSection(name = sectionName, items = items))
        }
        list
    } catch (_: Throwable) { emptyList() }
}

private fun parsePenaltiesJson(jsonText: String): List<PddPenaltyItem> {
    return jsonText.lines().mapNotNull { line ->
        val trimmed = line.trim()
        if (trimmed.isBlank()) return@mapNotNull null
        try {
            val obj = js("JSON.parse").unsafeCast<(String) -> dynamic>().invoke(trimmed)
            PddPenaltyItem(
                articlePart = (obj.article_part as? String) ?: "",
                text = (obj.text as? String) ?: "",
                penalty = (obj.penalty as? String) ?: "",
            )
        } catch (_: Throwable) { null }
    }
}

private fun loadAllTicketsByTopic() {
    fun buildSectionsFromBundle(bundle: dynamic) {
        updateState { pddTicketsBundle = bundle }
        val allQuestions = mutableListOf<PddQuestion>()
        val ab: dynamic = if (bundle != null && bundle != js("undefined")) bundle["A_B"] else null
        if (ab != null && ab != js("undefined")) {
            for (n in 1..40) {
                allQuestions.addAll(getQuestionsFromBundle(bundle, "A_B", n))
            }
        }
        val order = mutableListOf<String>()
        val byTopic = mutableMapOf<String, MutableList<PddQuestion>>()
        for (q in allQuestions) {
            val topic = q.topic.firstOrNull()?.takeIf { it.isNotBlank() } ?: "Прочее"
            if (topic !in order) order.add(topic)
            byTopic.getOrPut(topic) { mutableListOf() }.add(q)
        }
        val sections = order.map { name -> PddTopicSection(name = name, questions = byTopic[name] ?: emptyList()) }
        updateState { pddByTopicSections = sections; pddLoading = false }
    }
    var bundle = appState.pddTicketsBundle
    if (bundle == null || bundle == js("undefined")) bundle = getEmbeddedPddBundle()
    if (bundle == null || bundle == js("undefined")) {
        bundle = js("window.__PDD_TICKETS_BUNDLE__").unsafeCast<dynamic>()
    }
    if (bundle != null && bundle != js("undefined")) {
        buildSectionsFromBundle(bundle)
    } else {
        loadPddBundleFromNetwork { b: dynamic? ->
            if (b != null && b != js("undefined")) buildSectionsFromBundle(b)
            else updateState { pddLoading = false; pddByTopicSections = emptyList() }
        }
    }
}

/** Пробует загрузить бандл билетов: сначала fetch JSON, при ошибке — подгрузка pdd-tickets-bundle.js. */
private fun loadPddBundleFromNetwork(callback: (dynamic?) -> Unit) {
    val log = js("console.log").unsafeCast<(Any?) -> Unit>()
    log("PDD: fetch pdd-tickets-bundle.json")
    window.fetch("pdd-tickets-bundle.json").then { r: dynamic ->
        log("PDD: fetch status = " + (r.status as Int))
        if ((r.status as Int) != 200) throw js("new Error('Not found')")
        r.text().then { text: dynamic ->
            val parsed = js("JSON.parse").unsafeCast<(String) -> dynamic>().invoke(text.unsafeCast<String>())
            log("PDD: fetch OK, parsed bundle")
            callback(parsed)
        }
    }.catch { err: dynamic ->
        log("PDD: fetch failed, loading pdd-tickets-bundle.js script")
        val script = document.createElement("script")
        script.asDynamic().src = "pdd-tickets-bundle.js"
        script.asDynamic().onload = {
            val b = js("window.__PDD_TICKETS_BUNDLE__").unsafeCast<dynamic>()
            log("PDD: script onload, bundle=" + (if (b != null && b != js("undefined")) "OK" else "null"))
            callback(b)
        }
        script.asDynamic().onerror = { log("PDD: script onerror"); callback(null) }
        document.head?.appendChild(script)
    }
}

/** Текущие дата и время в формате для min атрибута datetime-local (нельзя выбрать прошлое). */
private fun getDatetimeLocalMin(): String {
    return js("(function(){ var d=new Date(); var p=function(n){ return (n<10?'0':'')+n; }; return d.getFullYear()+'-'+p(d.getMonth()+1)+'-'+p(d.getDate())+'T'+p(d.getHours())+':'+p(d.getMinutes()); })()").unsafeCast<String>()
}

/** Проверка: занято ли время (сессии и окна по 1.5 ч). Возвращает сообщение об ошибке или null. */
private fun findOccupiedMessage(
    selectedMs: Long,
    sessions: List<DrivingSession>,
    windows: List<InstructorOpenWindow>,
    cadets: List<User>,
): String? {
    val selectedEnd = selectedMs + LESSON_DURATION_MS
    for (s in sessions) {
        if (s.status != "scheduled" && s.status != "inProgress") continue
        val startMs = s.startTimeMillis ?: continue
        val endMs = startMs + LESSON_DURATION_MS
        if (selectedMs < endMs && selectedEnd > startMs) {
            val name = cadets.find { it.id == s.cadetId }?.fullName?.takeIf { it.isNotBlank() }?.let { formatShortName(it) } ?: "Курсант"
            return "Это время занято: $name записан до ${formatTimeOnly(endMs)}"
        }
    }
    for (w in windows) {
        val startMs = w.dateTimeMillis ?: continue
        val endMs = startMs + LESSON_DURATION_MS
        if (selectedMs < endMs && selectedEnd > startMs) {
            return "Это время уже есть в свободных окнах. Выберите другое."
        }
    }
    return null
}

private fun formatDateTime(ms: Long?): String {
    if (ms == null || ms <= 0) return "—"
    val d = js("new Date(ms)")
    return js("d.toLocaleString('ru-RU')").unsafeCast<String>()
}

private fun formatDateOnly(ms: Long?): String {
    if (ms == null || ms <= 0) return "—"
    val d = js("new Date(ms)")
    return js("d.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric' })").unsafeCast<String>()
}

private fun formatTimeOnly(ms: Long?): String {
    if (ms == null || ms <= 0) return "—"
    val d = js("new Date(ms)")
    return js("d.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })").unsafeCast<String>()
}

/** Дата и время по часовому поясу Екатеринбург (UTC+5) для истории операций баланса */
private fun formatDateTimeEkaterinburg(ms: Long?): String {
    if (ms == null || ms <= 0) return "—"
    val d = js("new Date(ms)")
    return js("d.toLocaleString('ru-RU', { timeZone: 'Asia/Yekaterinburg', dateStyle: 'short', timeStyle: 'short' })").unsafeCast<String>()
}

/** Показать всплывающее сообщение (toast), исчезает через 4 с или по клику. */
private fun showToast(message: String) {
    val existing = document.querySelector(".sd-toast")
    existing?.parentNode?.removeChild(existing)
    val div = document.createElement("div")
    div.className = "sd-toast"
    div.setAttribute("role", "alert")
    div.textContent = message
    document.body?.appendChild(div)
    var timeoutId: dynamic = js("0")
    val remove = {
        div.parentNode?.removeChild(div)
        window.clearTimeout(timeoutId)
    }
    timeoutId = window.setTimeout({ remove() }, 4000)
    div.addEventListener("click", { remove() })
}

private fun renderAdminHomeContent(): String {
    val loading = appState.adminHomeLoading
    val allUsers = appState.adminHomeUsers
    val newbies = allUsers.filter { (it.role == "instructor" || it.role == "cadet") && !it.isActive }.sortedBy { it.fullName }
    val instructors = allUsers.filter { it.role == "instructor" && it.isActive }.sortedBy { it.fullName }
    val cadets = allUsers.filter { it.role == "cadet" && it.isActive }.sortedBy { it.fullName }
    val emptyLoadBtn = if (!loading && allUsers.isEmpty()) """<p>Список пуст. <button type="button" id="sd-admin-home-load" class="sd-btn sd-btn-primary">Загрузить</button></p>""" else ""
    val topSlotContent = emptyLoadBtn
    val topSlot = """<div class="sd-admin-home-top-slot">$topSlotContent</div>"""
    val newbiesCards = newbies.joinToString("") { u ->
        val roleLabel = if (u.role == "instructor") "Инструктор" else "Курсант"
        """<div class="sd-admin-card sd-admin-card-pending">
            <div class="sd-admin-card-info">
                <p class="sd-admin-card-name">${(u.fullName.ifBlank { "Имя не указано" }).escapeHtml()}</p>
                <p class="sd-admin-card-meta">${u.email.escapeHtml()}</p>
                <p class="sd-admin-card-meta">${u.phone.escapeHtml()}</p>
                <p class="sd-admin-card-meta"><span class="sd-admin-role-label">Роль при регистрации:</span> $roleLabel</p>
            </div>
            <div class="sd-admin-card-actions">
                <button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-activate="${u.id.escapeHtml()}" title="Активировать — переведёт в раздел «$roleLabel»">Активировать</button>
                <button type="button" class="sd-btn sd-btn-small sd-btn-delete" data-admin-delete="${u.id.escapeHtml()}" title="Удалить">Удалить</button>
            </div>
        </div>"""
    }
    val newbiesOpen = if (appState.adminNewbiesSectionOpen) " open" else ""
    val newbiesContent = if (newbies.isEmpty()) """<p class="sd-muted">Нет новых пользователей. После регистрации они появятся здесь; после активации перейдут в раздел «Инструкторы» или «Курсанты» по выбранной роли.</p>""" else newbiesCards
    val newbiesBlock = """<details class="sd-block sd-details-block" data-admin-section="newbies"$newbiesOpen><summary class="sd-block-title">Вновь принятые (${newbies.size})</summary><div class="sd-admin-cards">$newbiesContent</div></details>"""
    val assignInstructorId = appState.adminAssignInstructorId
    val assignCadetId = appState.adminAssignCadetId
    val assignBlock = when {
        assignInstructorId != null -> {
            val inst = instructors.find { it.id == assignInstructorId }
            val instShortName = inst?.let { formatShortName(it.fullName).escapeHtml() } ?: "—"
            val currentCadets = cadets.filter { it.assignedInstructorId == assignInstructorId }.sortedBy { it.fullName }
            val currentSectionRows = currentCadets.joinToString("") { c ->
                """<div class="sd-assign-section-row sd-assign-section-row-current"><span class="sd-assign-section-name">${formatShortName(c.fullName).escapeHtml()}</span></div>"""
            }
            val currentInstructorSection = """<div class="sd-assign-section sd-assign-section-current"><h4 class="sd-assign-section-title">Курсанты инструктора (уже назначены): $instShortName (${currentCadets.size})</h4><div class="sd-assign-section-list">${if (currentCadets.isEmpty()) "<p class=\"sd-muted\">Нет назначенных курсантов.</p>" else currentSectionRows}</div></div>"""
            val unassigned = cadets.filter { it.assignedInstructorId == null }.sortedBy { it.fullName }
            val otherInstructors = instructors.filter { it.id != assignInstructorId }.sortedBy { it.fullName }
            val newbiesRows = unassigned.joinToString("") { c ->
                """<div class="sd-assign-section-row"><span class="sd-assign-section-name">${formatShortName(c.fullName).escapeHtml()}</span><button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-assign-instructor="${assignInstructorId.escapeHtml()}" data-admin-assign-cadet="${c.id.escapeHtml()}">Назначить</button></div>"""
            }
            val newbiesSection = """<div class="sd-assign-section"><h4 class="sd-assign-section-title">Вновь принятые (${unassigned.size})</h4><div class="sd-assign-section-list">${if (unassigned.isEmpty()) "<p class=\"sd-muted\">Нет курсантов без инструктора.</p>" else newbiesRows}</div></div>"""
            val instructorsSections = otherInstructors.joinToString("") { other ->
                val otherCadets = cadets.filter { it.assignedInstructorId == other.id }.sortedBy { it.fullName }
                val rows = otherCadets.joinToString("") { c ->
                    """<div class="sd-assign-section-row"><span class="sd-assign-section-name">${formatShortName(c.fullName).escapeHtml()}</span><button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-assign-instructor="${assignInstructorId.escapeHtml()}" data-admin-assign-cadet="${c.id.escapeHtml()}">Назначить</button></div>"""
                }
                val otherShort = formatShortName(other.fullName).escapeHtml()
                """<div class="sd-assign-section"><h4 class="sd-assign-section-title">Инструктор $otherShort (${otherCadets.size})</h4><div class="sd-assign-section-list">${if (otherCadets.isEmpty()) "<p class=\"sd-muted\">Нет назначенных курсантов.</p>" else rows}</div></div>"""
            }
            """<div class="sd-assign-panel" id="sd-assign-panel"><h3 class="sd-assign-panel-title">Назначить курсанта инструктору: $instShortName</h3>$currentInstructorSection$newbiesSection$instructorsSections<p class="sd-assign-panel-actions"><button type="button" id="sd-admin-assign-cancel" class="sd-btn sd-assign-close-btn">Закрыть</button></p></div>"""
        }
        assignCadetId != null -> {
            val cadet = cadets.find { it.id == assignCadetId }
            val cadetShortName = cadet?.let { formatShortName(it.fullName).escapeHtml() } ?: "—"
            val instRows = instructors.joinToString("") { inst ->
                """<div class="sd-assign-section-row"><span class="sd-assign-section-name">${formatShortName(inst.fullName).escapeHtml()}</span><button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-assign-instructor="${inst.id.escapeHtml()}" data-admin-assign-cadet="${assignCadetId.escapeHtml()}">Назначить</button></div>"""
            }
            """<div class="sd-assign-panel" id="sd-assign-panel"><h3 class="sd-assign-panel-title">Назначить курсанта: $cadetShortName</h3><div class="sd-assign-section"><h4 class="sd-assign-section-title">Выберите инструктора</h4><div class="sd-assign-section-list">$instRows</div></div><p class="sd-assign-panel-actions"><button type="button" id="sd-admin-assign-cancel" class="sd-btn sd-assign-close-btn">Закрыть</button></p></div>"""
        }
        else -> ""
    }
    val instCards = instructors.joinToString("") { u ->
        val assignedCadets = cadets.filter { it.assignedInstructorId == u.id }.sortedBy { it.fullName }
        val initials = u.fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        val phoneHrefInst = if (u.phone.isNotBlank()) "tel:${u.phone.escapeHtml()}" else "#"
        val phoneDisabled = if (u.phone.isBlank()) " sd-ucard-icon-btn-disabled" else ""
        """<div class="sd-ucard sd-ucard-instructor">
            <div class="sd-ucard-accent-bar"></div>
            <div class="sd-ucard-top">
                <div class="sd-ucard-avatar sd-ucard-avatar-blue">$initials</div>
                <div class="sd-ucard-head">
                    <p class="sd-ucard-name">${formatShortName(u.fullName).escapeHtml()}</p>
                    <span class="sd-ucard-badge sd-ucard-badge-blue">Инструктор</span>
                </div>
                <div class="sd-ucard-quick">
                    <a href="$phoneHrefInst" class="sd-ucard-icon-btn$phoneDisabled" title="Позвонить">$iconPhoneSvg</a>
                    <button type="button" class="sd-ucard-icon-btn sd-admin-open-chat" data-contact-id="${u.id.escapeHtml()}" title="Чат">$iconChatSvg</button>
                </div>
            </div>
            <div class="sd-ucard-rows">
                <div class="sd-ucard-row"><span class="sd-ucard-row-icon">$iconPhoneLabelSvg</span>${(u.phone.ifBlank { "Телефон не указан" }).escapeHtml()}</div>
                <div class="sd-ucard-row"><span class="sd-ucard-row-icon">$iconEmailLabelSvg</span>${(u.email.ifBlank { "—" }).escapeHtml()}</div>
                <div class="sd-ucard-row"><span class="sd-ucard-row-icon">$iconTicketSvg</span>Талоны: <strong>${u.balance}</strong></div>
                <div class="sd-ucard-row sd-ucard-row-stretch"><span class="sd-ucard-row-icon">$iconInstructorSvg</span>Курсантов: <strong>${assignedCadets.size}</strong><button type="button" class="sd-ucard-tag-btn sd-instructor-cadets-toggle" data-instructor-cadets-modal="${u.id.escapeHtml()}" title="Показать курсантов">$iconEyeSvg</button></div>
            </div>
            <div class="sd-ucard-footer">
                <button type="button" class="sd-ucard-foot-btn sd-ucard-foot-btn-assign" data-admin-assign="${u.id.escapeHtml()}" title="Назначить курсанта">$iconUserPlusSvg</button>
                <button type="button" class="sd-ucard-foot-btn ${if (u.isActive) "sd-ucard-foot-btn-deact" else "sd-ucard-foot-btn-act"}" data-admin-activate="${u.id.escapeHtml()}" data-admin-active="${u.isActive}" title="${if (u.isActive) "Деактивировать" else "Активировать"}">$iconPowerSvg</button>
                <button type="button" class="sd-ucard-foot-btn sd-ucard-foot-btn-danger" data-admin-delete="${u.id.escapeHtml()}" title="Удалить">$iconTrashSvg</button>
            </div>
        </div>"""
    }
    val cadetCards = cadets.joinToString("") { u ->
        val instId = u.assignedInstructorId
        val instName = instId?.let { id -> instructors.find { it.id == id }?.let { formatShortName(it.fullName) } ?: "—" } ?: "—"
        val displayInstText = if (instId != null) instName.escapeHtml() else "Не назначен"
        val phoneHrefCadet = if (u.phone.isNotBlank()) "tel:${u.phone.escapeHtml()}" else "#"
        val phoneDisabledCadet = if (u.phone.isBlank()) " sd-ucard-icon-btn-disabled" else ""
        val initials = u.fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        val unlinkOrAssign = if (instId != null)
            """<button type="button" class="sd-ucard-tag-btn sd-ucard-tag-btn-warn sd-admin-unlink-right" data-admin-unlink-instructor="${instId.escapeHtml()}" data-admin-unlink-cadet="${u.id.escapeHtml()}" title="Отвязать инструктора">$iconUnlinkSvg</button>"""
        else
            """<button type="button" class="sd-ucard-tag-btn sd-admin-assign-cadet-btn" data-admin-assign-cadet="${u.id.escapeHtml()}" title="Назначить инструктора">$iconUserPlusSvg</button>"""
        """<div class="sd-ucard sd-ucard-cadet">
            <div class="sd-ucard-accent-bar"></div>
            <div class="sd-ucard-top">
                <div class="sd-ucard-avatar sd-ucard-avatar-teal">$initials</div>
                <div class="sd-ucard-head">
                    <p class="sd-ucard-name">${formatShortName(u.fullName).escapeHtml()}</p>
                    <span class="sd-ucard-badge sd-ucard-badge-teal">Курсант</span>
                </div>
                <div class="sd-ucard-quick">
                    <a href="$phoneHrefCadet" class="sd-ucard-icon-btn$phoneDisabledCadet" title="Позвонить">$iconPhoneSvg</a>
                    <button type="button" class="sd-ucard-icon-btn sd-admin-open-chat" data-contact-id="${u.id.escapeHtml()}" title="Чат">$iconChatSvg</button>
                </div>
            </div>
            <div class="sd-ucard-rows">
                <div class="sd-ucard-row"><span class="sd-ucard-row-icon">$iconPhoneLabelSvg</span>${(u.phone.ifBlank { "Телефон не указан" }).escapeHtml()}</div>
                <div class="sd-ucard-row"><span class="sd-ucard-row-icon">$iconEmailLabelSvg</span>${(u.email.ifBlank { "—" }).escapeHtml()}</div>
                <div class="sd-ucard-row sd-ucard-row-stretch"><span class="sd-ucard-row-icon">$iconInstructorSvg</span>Инструктор: <strong>$displayInstText</strong>$unlinkOrAssign</div>
            </div>
            <div class="sd-ucard-footer">
                <button type="button" class="sd-ucard-foot-btn ${if (u.isActive) "sd-ucard-foot-btn-deact" else "sd-ucard-foot-btn-act"}" data-admin-activate="${u.id.escapeHtml()}" data-admin-active="${u.isActive}" title="${if (u.isActive) "Деактивировать" else "Активировать"}">$iconPowerSvg</button>
                <button type="button" class="sd-ucard-foot-btn sd-ucard-foot-btn-danger" data-admin-delete="${u.id.escapeHtml()}" title="Удалить">$iconTrashSvg</button>
            </div>
        </div>"""
    }
    val instOpen = if (appState.adminInstructorsSectionOpen) " open" else ""
    val cadetOpen = if (appState.adminCadetsSectionOpen) " open" else ""
    val modalId = appState.adminInstructorCadetsModalId
    val cadetsModalHtml = if (modalId != null) {
        val inst = instructors.find { it.id == modalId }
        val modalCadets = cadets.filter { it.assignedInstructorId == modalId }.sortedBy { it.fullName }
        val instName = (inst?.fullName ?: "—").escapeHtml()
        val listItems = modalCadets.joinToString("") { c ->
            """<li class="sd-instructor-cadet-name">${formatShortName(c.fullName).escapeHtml()}</li>"""
        }
        """<div class="sd-modal-overlay" id="sd-admin-cadets-modal-overlay"><div class="sd-modal sd-admin-cadets-modal"><h3 class="sd-modal-title">Курсанты инструктора: $instName</h3><ul class="sd-instructor-cadets-list">$listItems</ul><p class="sd-modal-actions"><button type="button" id="sd-admin-cadets-modal-close" class="sd-btn sd-assign-close-btn">Закрыть</button></p></div></div>"""
    } else ""
    return """<h2>Главная</h2>$topSlot$newbiesBlock<details class="sd-block sd-details-block" data-admin-section="instructors"$instOpen><summary class="sd-block-title">Инструкторы (${instructors.size})</summary><div class="sd-admin-cards">$instCards</div></details>$assignBlock<details class="sd-block sd-details-block" data-admin-section="cadets"$cadetOpen><summary class="sd-block-title">Курсанты (${cadets.size})</summary><div class="sd-admin-cards">$cadetCards</div></details>$cadetsModalHtml"""
}

private fun renderInstructorHomeContent(user: User, version: String): String {
    val loading = appState.recordingLoading
    val cadets = appState.instructorCadets.sortedBy { it.fullName }
    val sessions = appState.recordingSessions.filter { it.status == "scheduled" || it.status == "inProgress" }.take(20)
    val allSessions = appState.recordingSessions
    val loadingLine = if (loading) """<p class="sd-loading-text">Загрузка…</p>""" else ""
    val cadetsListHtml = if (cadets.isEmpty()) """<p class="sd-muted">Нет назначенных курсантов</p>""" else cadets.joinToString("") { c ->
        val completedCount = allSessions.count { it.cadetId == c.id && it.status == "completed" }
        val initials = c.fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        val phoneDisplay = (c.phone.ifBlank { "—" }).escapeHtml()
        val phoneHref = if (c.phone.isNotBlank()) "tel:${c.phone.escapeHtml()}" else "#"
        val phoneClass = if (c.phone.isNotBlank()) "sd-btn sd-btn-circle sd-btn-phone" else "sd-btn sd-btn-circle sd-btn-phone sd-btn-disabled"
        val svgChat = """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>"""
        val svgPhone = """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 1.18h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.77a16 16 0 0 0 6 6l.96-.96a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7a2 2 0 0 1 1.72 2.02z"/></svg>"""
        val svgPhoneRow = """<svg viewBox="0 0 24 24" fill="currentColor" width="13" height="13" style="opacity:0.6;flex-shrink:0"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 1.18h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.77a16 16 0 0 0 6 6l.96-.96a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7a2 2 0 0 1 1.72 2.02z"/></svg>"""
        val svgCar = """<svg viewBox="0 0 24 24" fill="currentColor" width="13" height="13" style="opacity:0.6;flex-shrink:0"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/></svg>"""
        val svgTicket = """<svg viewBox="0 0 24 24" fill="currentColor" width="13" height="13" style="opacity:0.6;flex-shrink:0"><path d="M22 10V6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v4c1.1 0 2 .9 2 2s-.9 2-2 2v4c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2v-4c-1.1 0-2-.9-2-2s.9-2 2-2zm-2-1.46c-1.19.69-2 1.99-2 3.46s.81 2.77 2 3.46V18H4v-2.54c1.19-.69 2-1.99 2-3.46 0-1.48-.8-2.77-2-3.46L4 6h16v2.54z"/></svg>"""
        val phoneDisabled = if (c.phone.isBlank()) " sd-cadet-icon-btn-disabled" else ""
        """<div class="sd-cadet-card">
            <div class="sd-cadet-accent-bar"></div>
            <div class="sd-cadet-top">
                <div class="sd-cadet-avatar">$initials</div>
                <div class="sd-cadet-head">
                    <p class="sd-cadet-name">${(c.fullName.ifBlank { "—" }).escapeHtml()}</p>
                    <span class="sd-cadet-badge">Курсант</span>
                </div>
                <div class="sd-cadet-quick">
                    <button type="button" class="sd-cadet-icon-btn sd-cadet-chat-btn" data-contact-id="${c.id.escapeHtml()}" title="Чат">$svgChat</button>
                    <a href="$phoneHref" class="sd-cadet-icon-btn$phoneDisabled" title="Позвонить">$svgPhone</a>
                </div>
            </div>
            <div class="sd-cadet-rows">
                <div class="sd-cadet-row">$svgPhoneRow $phoneDisplay</div>
                <div class="sd-cadet-row">$svgCar $completedCount вождений</div>
                <div class="sd-cadet-row">$svgTicket ${c.balance} талонов</div>
            </div>
        </div>"""
    }
    val sessionsByWeekday = sessions.groupBy { getWeekdayIndex(it.startTimeMillis) }
    val scheduleSectionsHtml = if (sessions.isEmpty()) {
        """<p class="sd-muted">Нет записанных на вождение курсантов</p>"""
    } else {
        WEEKDAY_NAMES.mapIndexed { index, dayName ->
            val daySessions = (sessionsByWeekday[index] ?: emptyList()).sortedBy { it.startTimeMillis ?: 0L }
            val count = daySessions.size
            val cardsHtml = daySessions.joinToString("") { s ->
                val cadetName = cadets.find { it.id == s.cadetId }?.fullName?.takeIf { it.isNotBlank() }?.let { formatShortName(it) } ?: "Курсант (${s.cadetId.take(8)})"
                val dateStr = formatDateOnly(s.startTimeMillis)
                val timeStr = formatTimeOnly(s.startTimeMillis)
                val startMs = s.startTimeMillis ?: 0L
                val bookedByCadet = s.openWindowId.isNotBlank()
                val statusText = when {
                    bookedByCadet && !s.instructorConfirmed -> "Курсант забронировал — ожидает вашего подтверждения"
                    !s.instructorConfirmed -> "Ожидает подтверждения записи курсантом"
                    s.status == "inProgress" -> "В процессе"
                    else -> "Подтверждён"
                }
                val showConfirm = bookedByCadet && !s.instructorConfirmed
                val showStart = s.status == "scheduled" && s.instructorConfirmed
                val confirmBtn = if (showConfirm) """<button type="button" class="sd-btn sd-btn-small sd-btn-primary sd-home-schedule-confirm" data-session-id="${s.id.escapeHtml()}" title="Подтвердить бронь">$iconSelectSvg Подтвердить</button>""" else ""
                val startBtn = if (showStart) """<button type="button" class="sd-btn sd-btn-small sd-btn-primary sd-home-schedule-start" data-session-id="${s.id.escapeHtml()}" data-start-ms="$startMs" title="Начать вождение (доступно за 15 мин)">$iconPlaySvg Начать</button>""" else ""
                val lateBtn = if (s.status == "scheduled") """<button type="button" class="sd-btn sd-btn-small sd-btn-late sd-home-schedule-late" data-session-id="${s.id.escapeHtml()}" title="Опаздываю">$iconLateSvg Опаздываю</button>""" else ""
                """<div class="sd-schedule-card" data-session-id="${s.id.escapeHtml()}" data-start-ms="$startMs">
                <div class="sd-schedule-card-body">
                    <div class="sd-schedule-card-row">$iconCalendarSvg <span class="sd-schedule-card-label">Дата:</span> $dateStr</div>
                    <div class="sd-schedule-card-row">$iconClockSvg <span class="sd-schedule-card-label">Время:</span> $timeStr</div>
                    <div class="sd-schedule-card-row">$iconUserSvg <span class="sd-schedule-card-label">Курсант:</span> ${cadetName.escapeHtml()}</div>
                    <div class="sd-schedule-card-status">$statusText</div>
                </div>
                <div class="sd-schedule-card-actions">$confirmBtn $startBtn $lateBtn <button type="button" class="sd-btn sd-btn-small sd-btn-delete sd-home-schedule-cancel" data-session-id="${s.id.escapeHtml()}">$iconTrashSvg Отменить</button></div>
            </div>"""
            }
            """<details class="sd-schedule-day" ${if (count > 0) "open" else ""}><summary class="sd-schedule-day-title">$dayName ($count)</summary><div class="sd-schedule-list">$cardsHtml</div></details>"""
        }.joinToString("")
    }
    val runningLateModalHtml = """<div class="sd-modal-overlay sd-hidden" id="sd-running-late-modal"><div class="sd-modal"><h3 class="sd-modal-title">Опаздываю</h3><p>Выберите задержку:</p><div class="sd-running-late-options"><label class="sd-radio"><input type="radio" name="sd-late-mins" value="5" /> 5 мин.</label><label class="sd-radio"><input type="radio" name="sd-late-mins" value="10" /> 10 мин.</label><label class="sd-radio"><input type="radio" name="sd-late-mins" value="15" /> 15 мин.</label></div><p class="sd-modal-actions"><button type="button" id="sd-running-late-confirm" class="sd-btn sd-btn-primary">Подтвердить</button><button type="button" id="sd-running-late-cancel" class="sd-btn sd-btn-secondary">Отмена</button></p></div></div>"""
    val instrInitials = user.fullName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }
    val instrAvatarHue = (user.fullName.hashCode() and 0x7FFFFFFF) % 360
    val instrAvatarBg = "hsl($instrAvatarHue,50%,32%)"
    val svgEmail = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>"""
    val svgPhoneP = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/></svg>"""
    val svgTicketP = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M22 10V6c0-1.1-.9-2-2-2H4c-1.1 0-1.99.9-1.99 2v4c1.1 0 1.99.9 1.99 2s-.89 2-2 2v4c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2v-4c-1.1 0-2-.9-2-2s.9-2 2-2zm-2-1.46c-1.19.69-2 1.99-2 3.46s.81 2.77 2 3.46V18H4v-2.54c1.19-.69 2-1.99 2-3.46 0-1.48-.8-2.77-1.99-3.46L4 6h16v2.54z"/></svg>"""
    val profileCard = """
        <div class="sd-profile-card">
            <div class="sd-profile-accent-bar"></div>
            <div class="sd-profile-card-shimmer" aria-hidden="true"></div>
            <div class="sd-profile-hero">
                <div class="sd-profile-avatar" style="background:$instrAvatarBg">$instrInitials</div>
                <div class="sd-profile-hero-info">
                    <p class="sd-profile-fullname">${(user.fullName.ifBlank { "—" }).escapeHtml()}</p>
                    <span class="sd-profile-role-badge">Инструктор</span>
                </div>
            </div>
            <div class="sd-profile-info-rows">
                <div class="sd-profile-info-row"><span class="sd-profile-info-icon">$svgEmail</span><span class="sd-profile-info-label">Email</span><span class="sd-profile-info-value">${(user.email.ifBlank { "—" }).escapeHtml()}</span></div>
                <div class="sd-profile-info-row"><span class="sd-profile-info-icon">$svgPhoneP</span><span class="sd-profile-info-label">Телефон</span><span class="sd-profile-info-value">${(user.phone.ifBlank { "—" }).escapeHtml()}</span></div>
                <div class="sd-profile-info-row sd-profile-info-row-balance"><span class="sd-profile-info-icon">$svgTicketP</span><span class="sd-profile-info-label">Талоны</span><span class="sd-balance-badge">${user.balance}</span></div>
            </div>
        </div>"""
    return """<h2>Главная</h2>
        $profileCard
        <div class="sd-block"><h3 class="sd-block-title">Мои курсанты (${cadets.size})</h3><div class="sd-cadet-cards">$cadetsListHtml</div></div>
        $loadingLine
        <div class="sd-block sd-block-schedule"><h3 class="sd-block-title">Мой график</h3><div class="sd-schedule-days">$scheduleSectionsHtml</div>$runningLateModalHtml</div>
        <p class="sd-version">Версия: $version</p>"""
}

private fun renderCadetHomeContent(user: User, version: String): String {
    val inst = appState.cadetInstructor
    val instText = when {
        inst != null -> inst.fullName.escapeHtml()
        user.assignedInstructorId != null -> "загрузка…"
        else -> "не назначен"
    }
    val loading = appState.recordingLoading
    val sessions = appState.recordingSessions.filter { it.status == "scheduled" }.take(20)
    val loadingLine = if (loading) """<p class="sd-loading-text">Загрузка…</p>""" else ""
    val sessList = sessions.joinToString("") { """<div class="sd-record-row">${formatDateTime(it.startTimeMillis)} — запланировано</div>""" }
    return """<h2>Главная</h2>
        <div class="sd-home-card"><strong>Баланс:</strong> ${user.balance} талонов.</div>
        <div class="sd-home-card"><strong>Мой инструктор:</strong> $instText</div>
        $loadingLine
        <div class="sd-block"><h3 class="sd-block-title">Моё вождение</h3><div class="sd-list">$sessList</div></div>
        <p class="sd-version">Версия: $version</p>"""
}

private fun renderRecordingTabContent(user: User): String {
    val loading = appState.recordingLoading
    val windows = appState.recordingOpenWindows
    val sessions = appState.recordingSessions
    val cadets = appState.instructorCadets
    val loadingLine = if (loading) """<p class="sd-loading-text">Загрузка… <button type="button" id="sd-stop-loading" class="sd-btn sd-btn-small sd-btn-secondary">Показать пусто</button></p>""" else ""
    return when (user.role) {
        "instructor" -> {
            val scheduledSessions = sessions.filter { it.status == "scheduled" || it.status == "inProgress" }.take(50)
            val assignedList = if (scheduledSessions.isEmpty()) """<p class="sd-muted">Нет записей</p>""" else scheduledSessions.joinToString("") { s ->
                val cadetName = cadets.find { it.id == s.cadetId }?.fullName?.takeIf { it.isNotBlank() }?.let { formatShortName(it) } ?: "Курсант (${s.cadetId.take(8)})"
                val dateStr = formatDateOnly(s.startTimeMillis)
                val timeStr = formatTimeOnly(s.startTimeMillis)
                val bookedByCadet = s.openWindowId.isNotBlank()
                val statusText = when {
                    bookedByCadet && !s.instructorConfirmed -> "Курсант забронировал — ожидает вашего подтверждения"
                    !s.instructorConfirmed -> "Ожидает подтверждения записи курсантом"
                    else -> "Подтверждён"
                }
                val showConfirm = bookedByCadet && !s.instructorConfirmed
                val confirmBtn = if (showConfirm) """<button type="button" class="sd-btn sd-btn-small sd-btn-primary sd-recording-confirm-session" data-session-id="${s.id.escapeHtml()}">Подтвердить</button>""" else ""
                """<div class="sd-recording-assigned-card">
                    <div class="sd-recording-assigned-head"><strong>${cadetName.escapeHtml()}</strong></div>
                    <div class="sd-recording-assigned-datetime">$dateStr, $timeStr</div>
                    <div class="sd-recording-assigned-status">$statusText</div>
                    <div class="sd-recording-assigned-actions">$confirmBtn <button type="button" class="sd-btn sd-btn-small sd-btn-delete sd-recording-cancel-session" data-session-id="${s.id.escapeHtml()}">Отменить</button></div>
                </div>"""
            }
            val freeWindows = windows.filter { it.status == "free" }
            val freeWindowsList = if (freeWindows.isEmpty()) """<p class="sd-muted">Нет свободных окон</p>""" else freeWindows.joinToString("") { w ->
                val dt = formatDateTime(w.dateTimeMillis)
                """<div class="sd-recording-window-row"><span class="sd-recording-window-dt">$dt</span><span class="sd-recording-window-status">свободно</span><button type="button" class="sd-btn sd-btn-small sd-btn-delete" data-window-id="${w.id.escapeHtml()}">Удалить</button></div>"""
            }
            val cadetOptions = cadets.sortedBy { it.fullName }.joinToString("") { c ->
                """<option value="${c.id.escapeHtml()}">${formatShortName(c.fullName).escapeHtml()}</option>"""
            }
            """<h2>Запись</h2>$loadingLine
               <div class="sd-recording-block">
                 <h3 class="sd-recording-section-title">Записанные на вождение</h3>
                 <div class="sd-recording-assigned-list">$assignedList</div>
               </div>
               <div class="sd-recording-block">
                 <h3 class="sd-recording-section-title">Свободные окна</h3>
                 <div class="sd-list">$freeWindowsList</div>
               </div>
               <div class="sd-recording-block">
                 <h3 class="sd-recording-section-title">Записать на вождение</h3>
                 <p><label>Курсант</label><select id="sd-recording-book-cadet" class="sd-input"><option value="">Выберите курсанта</option>$cadetOptions</select></p>
                 <p><label>Дата и время</label><input type="datetime-local" id="sd-recording-book-dt" class="sd-input" min="" /></p>
                 <p><button type="button" id="sd-recording-book-btn" class="sd-btn sd-btn-primary">Записать</button></p>
               </div>
               <div class="sd-recording-block">
                 <h3 class="sd-recording-section-title">Добавить окно</h3>
                 <p><label>Дата и время</label><input type="datetime-local" id="sd-recording-add-dt" class="sd-input" min="" /></p>
                 <p><button type="button" id="sd-recording-add-btn" class="sd-btn sd-btn-primary">Подтвердить</button></p>
               </div>"""
        }
        "cadet" -> {
            val slotsHtml = windows.joinToString("") { w ->
                """<div class="sd-record-row"><span>${formatDateTime(w.dateTimeMillis)}</span> <button type="button" class="sd-btn sd-btn-primary sd-btn-small" data-window-id="${w.id.escapeHtml()}">Записаться</button></div>"""
            }
            val myRecords = sessions.filter { it.status == "scheduled" }.take(10).joinToString("") { """<div class="sd-record-row">${formatDateTime(it.startTimeMillis)} — ${it.status}</div>""" }
            """<h2>Запись на вождение</h2>$loadingLine
               <div class="sd-recording-block"><h3 class="sd-recording-section-title">Свободные слоты</h3><div class="sd-list">$slotsHtml</div></div>
               <div class="sd-recording-block"><h3 class="sd-recording-section-title">Мои записи</h3><div class="sd-list">$myRecords</div></div>"""
        }
        else -> """<h2>Запись</h2><p>Доступно инструктору и курсанту.</p>"""
    }
}

private fun renderHistoryTabContent(user: User): String {
    val loadingLine = if (appState.historyLoading) """<p class="sd-loading-text">Загрузка…</p>""" else ""
    val sessions = appState.historySessions.take(30)
    val balance = appState.historyBalance.take(50)

    fun sessionStatusLabel(s: String) = when (s) {
        "scheduled" -> "Запланировано"; "completed" -> "Завершено"; "cancelled" -> "Отменено"; else -> s
    }

    fun historyDayBlock(date: String, count: Int, rowsHtml: String) =
        """<div class="sd-history-day">
            <div class="sd-history-day-header"><span class="sd-history-day-date">$date</span><span class="sd-history-day-count">$count зап.</span></div>
            <div class="sd-history-day-list">$rowsHtml</div>
        </div>"""

    fun sessionsGroupedHtml(): String {
        if (sessions.isEmpty()) return """<p class="sd-history-empty">Нет записей</p>"""
        val byDate = sessions.sortedByDescending { it.startTimeMillis ?: 0L }.groupBy { formatDateOnly(it.startTimeMillis) }
        val days = byDate.entries.joinToString("") { (date, entries) ->
            val rows = entries.joinToString("") { s ->
                val rating = if (s.instructorRating > 0) """<span class="sd-history-rating">★${s.instructorRating}</span>""" else ""
                val statusClass = when (s.status) { "completed" -> "sd-hist-done"; "cancelled" -> "sd-hist-cancel"; else -> "" }
                """<div class="sd-history-row"><span class="sd-history-time">${formatTimeOnly(s.startTimeMillis)}</span><span class="sd-history-status $statusClass">${sessionStatusLabel(s.status).escapeHtml()}</span>$rating</div>"""
            }
            historyDayBlock(date, entries.size, rows)
        }
        return """<div class="sd-history-by-date">$days</div>"""
    }

    fun balanceGroupedHtml(withUser: Boolean): String {
        if (balance.isEmpty()) return """<p class="sd-history-empty">Нет записей</p>"""
        val users = appState.balanceAdminUsers
        val byDate = balance.sortedByDescending { it.timestampMillis ?: 0L }.groupBy { formatDateOnly(it.timestampMillis) }
        val days = byDate.entries.joinToString("") { (date, entries) ->
            val rows = entries.joinToString("") { b ->
                val typeStr = when (b.type) { "credit" -> "+ "; "debit" -> "− "; "set" -> "= "; else -> "" }
                val typeClass = when (b.type) { "credit" -> "sd-hist-credit"; "debit" -> "sd-hist-debit"; else -> "" }
                val whoHtml = if (withUser) {
                    val name = users.find { it.id == b.userId }?.let { formatShortName(it.fullName) } ?: b.userId.take(8) + "…"
                    """<span class="sd-history-who">${name.escapeHtml()}</span>"""
                } else ""
                """<div class="sd-history-row"><span class="sd-history-time">${formatTimeOnly(b.timestampMillis)}</span>$whoHtml<span class="sd-history-amount $typeClass">$typeStr${b.amount} тал.</span></div>"""
            }
            historyDayBlock(date, entries.size, rows)
        }
        return """<div class="sd-history-by-date">$days</div>"""
    }

    if (user.role == "admin") {
        return """<h2>История</h2>$loadingLine<div class="sd-block"><h3 class="sd-block-title">Зачисления и списания (${balance.size})</h3>${balanceGroupedHtml(true)}</div>"""
    }

    return """<h2>История</h2>$loadingLine
        <div class="sd-block"><h3 class="sd-block-title">Занятия (${sessions.size})</h3>${sessionsGroupedHtml()}</div>
        <div class="sd-block"><h3 class="sd-block-title">Операции по балансу (${balance.size})</h3>${balanceGroupedHtml(false)}</div>"""
}

private fun renderTicketsTabContent(): String {
    val catId = appState.pddCategoryId
    val ticketName = appState.pddTicketName
    val questions = appState.pddQuestions
    val loading = appState.pddLoading
    val finished = appState.pddFinished
    val currentIdx = appState.pddCurrentIndex
    val userSelections = appState.pddUserSelections

    if (catId == null) {
        val categoriesHtml = PDD_CATEGORIES.joinToString("") { (id, title) ->
            val iconHtml = when (id) {
                "A_B" -> """<span class="sd-ticket-category-badge">AB</span>"""
                "C_D" -> """<span class="sd-ticket-category-badge">CD</span>"""
                else  -> """<span class="sd-ticket-category-icon">$iconTicketSvg</span>"""
            }
            """<div class="sd-ticket-category-card sd-ticket-category-clickable" data-pdd-category="${id.escapeHtml()}">
                $iconHtml
                <span class="sd-ticket-category-title">${title.escapeHtml()}</span>
            </div>"""
        }
        return """<h2>Билеты ПДД</h2>
        <div class="sd-tickets-content">
            <div class="sd-ticket-categories">$categoriesHtml</div>
        </div>"""
    }

    if (catId == "exam") {
        updateState {
            pddCategoryId = null
            pddExamMode = false
            pddExamCategoryForBundle = null
            pddExamStartTimeMs = null
            pddExamBlockIndices = emptyList()
            pddExamPhase = "main"
            pddExamAdditionalQuestions = emptyList()
            pddExamAdditionalBlockIndices = emptyList()
            pddExamAdditionalCurrentIndex = 0
            pddExamAdditionalUserSelections = emptyMap()
            pddExamAdditionalStartTimeMs = null
            pddExamResultPass = null
        }
        val categoriesHtml = PDD_CATEGORIES.joinToString("") { (id, title) ->
            val iconHtml = when (id) {
                "A_B" -> """<span class="sd-ticket-category-badge">AB</span>"""
                "C_D" -> """<span class="sd-ticket-category-badge">CD</span>"""
                else  -> """<span class="sd-ticket-category-icon">$iconTicketSvg</span>"""
            }
            """<div class="sd-ticket-category-card sd-ticket-category-clickable" data-pdd-category="${id.escapeHtml()}">
                $iconHtml
                <span class="sd-ticket-category-title">${title.escapeHtml()}</span>
            </div>"""
        }
        return """<h2>Билеты ПДД</h2>
        <div class="sd-tickets-content">
            <div class="sd-ticket-categories">$categoriesHtml</div>
        </div>"""
    }

    if (catId == "A_B" || catId == "C_D") {
        if (ticketName == null) {
            val categoryTitle = PDD_CATEGORIES.find { it.first == catId }?.second ?: catId
            val tickets = (1..40).map { "Билет $it" }
            val ticketsHtml = tickets.joinToString("") { name ->
                val statClass = getPddTicketCardStatClass(catId, name)
                val (correct, incorrect, noAnswer) = getPddTicketStat(catId, name)
                """<div class="sd-ticket-category-card sd-ticket-category-clickable sd-pdd-ticket $statClass" data-pdd-ticket="${name.escapeHtml()}" data-pdd-category="${catId.escapeHtml()}">
                    <span class="sd-ticket-category-icon">$iconTicketSvg</span>
                    <span class="sd-ticket-category-title">${name.escapeHtml()}</span>
                    <span class="sd-pdd-ticket-stats"><span class="sd-pdd-stat sd-pdd-stat-ok">$correct</span><span class="sd-pdd-stat sd-pdd-stat-err">$incorrect</span><span class="sd-pdd-stat sd-pdd-stat-na">$noAnswer</span></span>
                </div>"""
            }
            return """<h2>Билеты ПДД</h2>
            <div class="sd-tickets-content">
                <button type="button" class="sd-btn sd-btn-secondary sd-pdd-back-categories">← К категориям</button>
                <div class="sd-pdd-tickets-header">
                    <h3 class="sd-pdd-subtitle">$categoryTitle</h3>
                    <button type="button" class="sd-btn sd-btn-secondary sd-pdd-reset-stats" data-pdd-reset-category="${catId.escapeHtml()}">Обнулить статистику</button>
                </div>
                <p class="sd-tickets-intro">Выберите билет (1–40). Зелёный — решено без ошибок, красный — с ошибками, серый — не решал.</p>
                <div class="sd-ticket-categories">$ticketsHtml</div>
                ${if (appState.pddResetConfirmCategory == catId) """
                <div class="sd-pdd-reset-modal-overlay" id="sd-pdd-reset-modal">
                    <div class="sd-pdd-reset-modal">
                        <p class="sd-pdd-reset-modal-text">Подтвердите действие: Вы уверены, что хотите обнулить статистику решений экзаменационных билетов?</p>
                        <div class="sd-pdd-reset-modal-buttons">
                            <button type="button" class="sd-btn sd-btn-primary sd-pdd-reset-confirm-yes" data-pdd-reset-category="${catId.escapeHtml()}">Да</button>
                            <button type="button" class="sd-btn sd-btn-secondary sd-pdd-reset-confirm-no">Нет</button>
                        </div>
                    </div>
                </div>
                """ else ""}
            </div>"""
        }

        if (loading) {
            return """<h2>Билеты ПДД</h2><div class="sd-tickets-content"><p class="sd-loading-text">Загрузка билета…</p></div>"""
        }

        if (questions.isEmpty()) {
            return """<h2>Билеты ПДД</h2><div class="sd-tickets-content"><p class="sd-muted">Не удалось загрузить билет.</p><button type="button" class="sd-btn sd-btn-secondary sd-pdd-back-tickets">← К списку билетов</button></div>"""
        }

        if (finished) {
            val correctCount = userSelections.entries.count { (qIdx, ansIdx) ->
                questions.getOrNull(qIdx)?.answers?.getOrNull(ansIdx)?.isCorrect == true
            }
            val total = questions.size
            val pct = if (total > 0) (correctCount * 100 / total) else 0
            val pass = pct >= 80
            return """<h2>Билеты ПДД</h2>
            <div class="sd-tickets-content">
                <div class="sd-pdd-result ${if (pass) "sd-pdd-result-pass" else "sd-pdd-result-fail"}">
                    <p class="sd-pdd-result-title">${ticketName.escapeHtml()}</p>
                    <p class="sd-pdd-result-score">Правильно: $correctCount из $total ($pct%)</p>
                    <p class="sd-pdd-result-verdict">${if (pass) "Сдано" else "Не сдано (нужно не менее 80%)"}</p>
                </div>
                <div class="sd-pdd-result-actions">
                    <button type="button" class="sd-btn sd-btn-secondary sd-pdd-back-to-questions">К вопросам</button>
                    <button type="button" class="sd-btn sd-btn-primary sd-pdd-back-tickets">← К списку билетов</button>
                </div>
            </div>"""
        }

        val q = questions.getOrNull(currentIdx) ?: return ""
        val selectedAns = userSelections[currentIdx]
        val answered = selectedAns != null
        val correctIdx = q.answers.indexOfFirst { it.isCorrect }
        val answersHtml = q.answers.mapIndexed { idx, a ->
            val isCorrectAnswer = a.isCorrect
            val isSelectedWrong = answered && selectedAns == idx && !isCorrectAnswer
            val showCorrect = answered && isCorrectAnswer
            val cls = buildString {
                append("sd-pdd-answer")
                if (selectedAns == idx) append(" sd-pdd-answer-selected")
                if (showCorrect) append(" sd-pdd-answer-correct")
                if (isSelectedWrong) append(" sd-pdd-answer-wrong")
            }
            /* Один выбор на вопрос. При повторном открытии ошибочные не подгружаются в userSelections — даём один повторный выбор. */
            val disabledAttr = if (answered) " disabled" else ""
            """<button type="button" class="$cls" data-pdd-answer-index="$idx" data-pdd-question-index="$currentIdx"$disabledAttr>${(idx + 1).toString().escapeHtml()}. ${a.answerText.escapeHtml()}</button>"""
        }.joinToString("")
        val savedResults = if (catId != null && ticketName != null) getPddTicketSavedResults(catId, ticketName) else emptyMap()
        val questionNavHtml = questions.indices.joinToString("") { idx ->
            val selIdx = userSelections[idx]
            val savedCorrect = savedResults[idx]
            val answeredHere = selIdx != null || savedCorrect != null
            val isCorrectHere = when {
                savedCorrect == true -> true
                savedCorrect == false -> false
                selIdx != null -> questions.getOrNull(idx)?.answers?.getOrNull(selIdx)?.isCorrect == true
                else -> null
            }
            val isCurrent = idx == currentIdx
            val navCls = buildString {
                append("sd-pdd-question-nav-item")
                if (isCurrent) append(" sd-pdd-question-nav-current")
                if (answeredHere && isCorrectHere != null) {
                    if (isCorrectHere) append(" sd-pdd-question-nav-correct")
                    else append(" sd-pdd-question-nav-wrong")
                }
            }
            """<button type="button" class="$navCls" data-pdd-go-question="$idx" title="Вопрос ${idx + 1}">${idx + 1}</button>"""
        }
        val imageSrc = q.image?.let { img ->
            if (img.startsWith("./")) "pdd/${img.drop(2)}" else "pdd/images/$img"
        }
        val imageHtml = if (imageSrc != null) """<div class="sd-pdd-question-image"><img src="${imageSrc.escapeHtml()}" alt="" loading="lazy" /></div>""" else ""
        val explanationHtml = if (answered && q.correctAnswer.isNotBlank()) {
            val tipHtml = if (q.answerTip.isNotBlank()) """<p class="sd-pdd-answer-tip">${q.answerTip.escapeHtml()}</p>""" else ""
            """<div class="sd-pdd-explanation">
                <p class="sd-pdd-correct-answer">${q.correctAnswer.escapeHtml()}</p>
                $tipHtml
            </div>"""
        } else ""
        val progress = "${currentIdx + 1} / ${questions.size}"
        val nextDisabled = !answered
        val nextBtn = """<button type="button" class="sd-btn sd-btn-primary sd-pdd-next" ${if (nextDisabled) "disabled" else ""} data-pdd-question-index="$currentIdx">${if (currentIdx == questions.size - 1) "Завершить" else "Далее"}</button>"""
        val backLabel = if (catId == "by_topic") "← К разделам" else "← К списку билетов"
        return """<h2>Билеты ПДД</h2>
        <div class="sd-tickets-content">
            <button type="button" class="sd-btn sd-btn-secondary sd-pdd-back-tickets">$backLabel</button>
            <p class="sd-pdd-progress">$progress</p>
            <div class="sd-pdd-question-nav" aria-label="Выбор вопроса">$questionNavHtml</div>
            <div class="sd-pdd-question-block">
                <p class="sd-pdd-question-text">${q.question.escapeHtml()}</p>
                $imageHtml
                <div class="sd-pdd-answers">$answersHtml</div>
                $explanationHtml
                <div class="sd-pdd-next-row">$nextBtn</div>
            </div>
        </div>"""
    }

    if (catId == "signs") {
        val categoryTitle = PDD_CATEGORIES.find { it.first == catId }?.second ?: catId
        if (loading) return """<h2>Билеты ПДД</h2><div class="sd-tickets-content"><p class="sd-loading-text">Загрузка…</p></div>"""
        val selected = appState.pddSelectedSign
        if (selected != null) {
            val sections = appState.pddSignsSections
            val secIdx = appState.pddSelectedSignSectionIndex
            val itemIdx = appState.pddSelectedSignItemIndex
            val nextSign: PddSignItem? = when {
                secIdx < 0 || itemIdx < 0 || sections.isEmpty() -> null
                itemIdx + 1 < sections.getOrNull(secIdx)?.items?.size ?: 0 -> sections[secIdx].items[itemIdx + 1]
                secIdx + 1 < sections.size && sections[secIdx + 1].items.isNotEmpty() -> sections[secIdx + 1].items[0]
                else -> null
            }
            val nextButtonHtml = if (nextSign != null) {
                val nextSecIdx = if (itemIdx + 1 < sections.getOrNull(secIdx)?.items?.size ?: 0) secIdx else secIdx + 1
                val nextItemIdx = if (itemIdx + 1 < sections.getOrNull(secIdx)?.items?.size ?: 0) itemIdx + 1 else 0
                """<div class="sd-pdd-sign-detail-next">
                    <button type="button" class="sd-btn sd-btn-primary sd-pdd-sign-next" data-next-section-index="$nextSecIdx" data-next-item-index="$nextItemIdx">
                        К следующему знаку: ${nextSign.number.escapeHtml()} ${nextSign.title.escapeHtml()}
                    </button>
                </div>"""
            } else ""
            val sectionName = sections.getOrNull(secIdx)?.name ?: categoryTitle
            val groupTitle = if (secIdx >= 0) "${secIdx + 1}) ${sectionName}" else categoryTitle
            val imgSrc = if (selected.imagePath.isNotBlank()) """<img src="${selected.imagePath.escapeHtml()}" alt="" class="sd-pdd-sign-detail-img" />""" else ""
            return """<h2>Билеты ПДД</h2>
        <div class="sd-tickets-content sd-pdd-sign-detail">
            <button type="button" class="sd-btn sd-btn-secondary sd-pdd-back-signs">← К списку знаков</button>
            <h3 class="sd-pdd-subtitle">${groupTitle.escapeHtml()}</h3>
            <div class="sd-pdd-sign-detail-card" id="sd-pdd-sign-detail-scroll">
                $imgSrc
                <div class="sd-pdd-sign-detail-body">
                    <p class="sd-pdd-sign-detail-title"><strong>${selected.number.escapeHtml()} ${selected.title.escapeHtml()}</strong></p>
                    <div class="sd-pdd-sign-detail-desc">${selected.description.escapeHtml()}</div>
                    $nextButtonHtml
                </div>
            </div>
        </div>"""
        }
        val sections = appState.pddSignsSections
        val sectionsHtml = sections.mapIndexed { secIdx, sec ->
            val itemsHtml = sec.items.mapIndexed { itemIdx, item ->
                val imgSrc = if (item.imagePath.isNotBlank()) """<img src="${item.imagePath.escapeHtml()}" alt="" class="sd-pdd-sign-img" loading="lazy" />""" else ""
                """<button type="button" class="sd-pdd-sign-card" data-sign-number="${item.number.escapeHtml()}" data-section-index="$secIdx" data-item-index="$itemIdx">
                    $imgSrc
                    <span class="sd-pdd-sign-card-title">${item.number.escapeHtml()} ${item.title.escapeHtml()}</span>
                </button>"""
            }.joinToString("")
            """<details class="sd-pdd-section" open><summary class="sd-pdd-section-title">${secIdx + 1}) ${sec.name.escapeHtml()} (${sec.items.size})</summary><div class="sd-pdd-section-list">$itemsHtml</div></details>"""
        }.joinToString("")
        return """<h2>Билеты ПДД</h2>
        <div class="sd-tickets-content">
            <button type="button" class="sd-btn sd-btn-secondary sd-pdd-back-categories">← К категориям</button>
            <h3 class="sd-pdd-subtitle">$categoryTitle</h3>
            <p class="sd-pdd-signs-intro">Нажмите на знак, чтобы открыть пояснение.</p>
            <div class="sd-pdd-sections">$sectionsHtml</div>
        </div>"""
    }

    if (catId == "markup") {
        val categoryTitle = PDD_CATEGORIES.find { it.first == catId }?.second ?: catId
        if (loading) return """<h2>Билеты ПДД</h2><div class="sd-tickets-content"><p class="sd-loading-text">Загрузка…</p></div>"""
        val sections = appState.pddMarkupSections
        val sectionsHtml = sections.joinToString("") { sec ->
            val itemsHtml = sec.items.joinToString("") { item ->
                val imgSrc = if (item.imagePath.isNotBlank()) """<img src="${item.imagePath.escapeHtml()}" alt="" class="sd-pdd-markup-img" loading="lazy" />""" else ""
                """<div class="sd-pdd-markup-item">
                    $imgSrc
                    <div class="sd-pdd-markup-info"><strong>${item.number.escapeHtml()}</strong><p class="sd-pdd-markup-desc">${item.description.escapeHtml()}</p></div>
                </div>"""
            }
            """<details class="sd-pdd-section"><summary class="sd-pdd-section-title">${sec.name.escapeHtml()} (${sec.items.size})</summary><div class="sd-pdd-section-list">$itemsHtml</div></details>"""
        }
        return """<h2>Билеты ПДД</h2>
        <div class="sd-tickets-content">
            <button type="button" class="sd-btn sd-btn-secondary sd-pdd-back-categories">← К категориям</button>
            <h3 class="sd-pdd-subtitle">$categoryTitle</h3>
            <div class="sd-pdd-sections">$sectionsHtml</div>
        </div>"""
    }

    if (catId == "penalties") {
        val categoryTitle = PDD_CATEGORIES.find { it.first == catId }?.second ?: catId
        if (loading) return """<h2>Билеты ПДД</h2><div class="sd-tickets-content"><p class="sd-loading-text">Загрузка…</p></div>"""
        val list = appState.pddPenalties
        val itemsHtml = list.joinToString("") { p ->
            """<div class="sd-pdd-penalty-item">
                <span class="sd-pdd-penalty-art">${p.articlePart.escapeHtml()}</span>
                <p class="sd-pdd-penalty-text">${p.text.escapeHtml()}</p>
                <p class="sd-pdd-penalty-penalty">${p.penalty.escapeHtml()}</p>
            </div>"""
        }
        return """<h2>Билеты ПДД</h2>
        <div class="sd-tickets-content">
            <button type="button" class="sd-btn sd-btn-secondary sd-pdd-back-categories">← К категориям</button>
            <h3 class="sd-pdd-subtitle">$categoryTitle</h3>
            <div class="sd-pdd-penalties-list">$itemsHtml</div>
        </div>"""
    }

    if (catId == "by_topic") {
        val categoryTitle = PDD_CATEGORIES.find { it.first == catId }?.second ?: catId
        if (loading) return """<h2>Билеты ПДД</h2><div class="sd-tickets-content"><p class="sd-loading-text">Загрузка вопросов по разделам…</p></div>"""
        if (questions.isNotEmpty() && finished) {
            val correctCount = userSelections.entries.count { (qIdx, ansIdx) ->
                questions.getOrNull(qIdx)?.answers?.getOrNull(ansIdx)?.isCorrect == true
            }
            val total = questions.size
            val pct = if (total > 0) (correctCount * 100 / total) else 0
            val pass = pct >= 80
            return """<h2>Билеты ПДД</h2>
            <div class="sd-tickets-content">
                <div class="sd-pdd-result ${if (pass) "sd-pdd-result-pass" else "sd-pdd-result-fail"}">
                    <p class="sd-pdd-result-title">${(ticketName ?: "Тест по разделу").escapeHtml()}</p>
                    <p class="sd-pdd-result-score">Правильно: $correctCount из $total ($pct%)</p>
                    <p class="sd-pdd-result-verdict">${if (pass) "Сдано" else "Не сдано (нужно не менее 80%)"}</p>
                </div>
                <div class="sd-pdd-result-actions">
                    <button type="button" class="sd-btn sd-btn-secondary sd-pdd-back-to-questions">К вопросам</button>
                    <button type="button" class="sd-btn sd-btn-primary sd-pdd-back-tickets">← К разделам</button>
                </div>
            </div>"""
        }
        if (questions.isNotEmpty() && !finished) {
            val q = questions.getOrNull(currentIdx) ?: return ""
            val selectedAns = userSelections[currentIdx]
            val answered = selectedAns != null
            val answersHtml = q.answers.mapIndexed { idx, a ->
                val cls = buildString {
                    append("sd-pdd-answer")
                    if (selectedAns == idx) append(" sd-pdd-answer-selected")
                    if (answered && a.isCorrect) append(" sd-pdd-answer-correct")
                    if (answered && selectedAns == idx && !a.isCorrect) append(" sd-pdd-answer-wrong")
                }
                /* Один выбор на вопрос; ошибочные при повторном открытии не в userSelections — один повторный выбор. */
                val disabledAttr = if (answered) " disabled" else ""
                """<button type="button" class="$cls" data-pdd-answer-index="$idx" data-pdd-question-index="$currentIdx"$disabledAttr>${(idx + 1).toString().escapeHtml()}. ${a.answerText.escapeHtml()}</button>"""
            }.joinToString("")
            val savedResultsTopic = getPddTicketSavedResults("by_topic", appState.pddTicketName ?: "")
            val questionNavHtml = questions.indices.joinToString("") { idx ->
                val selIdx = userSelections[idx]
                val savedCorrectTopic = savedResultsTopic[idx]
                val answeredHere = selIdx != null || savedCorrectTopic != null
                val isCorrectHere = when {
                    savedCorrectTopic == true -> true
                    savedCorrectTopic == false -> false
                    selIdx != null -> questions.getOrNull(idx)?.answers?.getOrNull(selIdx)?.isCorrect == true
                    else -> null
                }
                val isCurrent = idx == currentIdx
                val navCls = buildString {
                    append("sd-pdd-question-nav-item")
                    if (isCurrent) append(" sd-pdd-question-nav-current")
                    if (answeredHere && isCorrectHere != null) {
                        if (isCorrectHere) append(" sd-pdd-question-nav-correct")
                        else append(" sd-pdd-question-nav-wrong")
                    }
                }
                """<button type="button" class="$navCls" data-pdd-go-question="$idx" title="Вопрос ${idx + 1}">${idx + 1}</button>"""
            }
            val imageSrc = q.image?.let { img -> if (img.startsWith("./")) "pdd/${img.drop(2)}" else "pdd/images/$img" }
            val imageHtml = if (imageSrc != null) """<div class="sd-pdd-question-image"><img src="${imageSrc.escapeHtml()}" alt="" loading="lazy" /></div>""" else ""
            val explanationHtml = if (answered && q.correctAnswer.isNotBlank()) {
                val tipHtml = if (q.answerTip.isNotBlank()) """<p class="sd-pdd-answer-tip">${q.answerTip.escapeHtml()}</p>""" else ""
                """<div class="sd-pdd-explanation"><p class="sd-pdd-correct-answer">${q.correctAnswer.escapeHtml()}</p>$tipHtml</div>"""
            } else ""
            val nextDisabled = !answered
            val nextBtn = """<button type="button" class="sd-btn sd-btn-primary sd-pdd-next" ${if (nextDisabled) "disabled" else ""} data-pdd-question-index="$currentIdx">${if (currentIdx == questions.size - 1) "Завершить" else "Далее"}</button>"""
            return """<h2>Билеты ПДД</h2>
        <div class="sd-tickets-content">
            <button type="button" class="sd-btn sd-btn-secondary sd-pdd-back-tickets">← К разделам</button>
            <p class="sd-pdd-progress">Вопрос ${currentIdx + 1} из ${questions.size}</p>
            <div class="sd-pdd-question-nav" aria-label="Выбор вопроса">$questionNavHtml</div>
            <div class="sd-pdd-question-block">
                <p class="sd-pdd-question-text">${q.question.escapeHtml()}</p>
                $imageHtml
                <div class="sd-pdd-answers">$answersHtml</div>
                $explanationHtml
                <div class="sd-pdd-next-row">$nextBtn</div>
            </div>
        </div>"""
        }
        val sections = appState.pddByTopicSections
        val sectionsHtml = sections.mapIndexed { secIdx, sec ->
            val count = sec.questions.size
            val topicTicketName = "По разделу: ${sec.name}"
            val statClass = getPddTicketCardStatClass("by_topic", topicTicketName, count)
            val (correct, incorrect, noAnswer) = getPddTicketStat("by_topic", topicTicketName, count)
            """<button type="button" class="sd-pdd-section sd-pdd-section-clickable $statClass" data-pdd-topic-index="$secIdx">
                <span class="sd-pdd-section-title">${sec.name.escapeHtml()} ($count вопросов)</span>
                <span class="sd-pdd-ticket-stats"><span class="sd-pdd-stat sd-pdd-stat-ok">$correct</span><span class="sd-pdd-stat sd-pdd-stat-err">$incorrect</span><span class="sd-pdd-stat sd-pdd-stat-na">$noAnswer</span></span>
            </button>"""
        }.joinToString("")
        return """<h2>Билеты ПДД</h2>
        <div class="sd-tickets-content">
            <button type="button" class="sd-btn sd-btn-secondary sd-pdd-back-categories">← К категориям</button>
            <h3 class="sd-pdd-subtitle">$categoryTitle</h3>
            <p class="sd-tickets-intro">Вопросы по разделам из билетов категории AB. Нажмите на раздел, чтобы открыть тест.</p>
            <div class="sd-pdd-sections">$sectionsHtml</div>
        </div>"""
    }

    return renderTicketsTabContentCategoriesOnly()
}

private fun renderTicketsTabContentCategoriesOnly(): String {
    val categoriesHtml = PDD_CATEGORIES.joinToString("") { (id, title) ->
        val iconHtml = when (id) {
            "A_B" -> """<span class="sd-ticket-category-badge">AB</span>"""
            "C_D" -> """<span class="sd-ticket-category-badge">CD</span>"""
            else  -> """<span class="sd-ticket-category-icon">$iconTicketSvg</span>"""
        }
        """<div class="sd-ticket-category-card" data-pdd-category="${id.escapeHtml()}">
            $iconHtml
            <span class="sd-ticket-category-title">${title.escapeHtml()}</span>
        </div>"""
    }
    return """<h2>Билеты ПДД</h2><div class="sd-tickets-content"><div class="sd-ticket-categories">$categoriesHtml</div></div>"""
}

private fun renderSettingsTabContent(user: User): String =
    """<h2>Настройки</h2>
       <label>ФИО</label><input type="text" id="sd-settings-fullName" class="sd-input" value="${user.fullName.escapeHtml()}" />
       <label>Телефон</label><input type="tel" id="sd-settings-phone" class="sd-input" value="${user.phone.escapeHtml()}" />
       <button type="button" id="sd-settings-save" class="sd-btn sd-btn-primary">Сохранить профиль</button>
       <p style="margin-top:16px">Сменить пароль:</p>
       <label>Новый пароль</label><input type="password" id="sd-settings-newpassword" class="sd-input" placeholder="мин. 6 символов" />
       <button type="button" id="sd-settings-password" class="sd-btn sd-btn-secondary">Сменить пароль</button>"""

private fun renderBalanceTabContent(user: User): String {
    if (user.role != "admin") return """<h2>Баланс</h2><p>Ваш баланс: ${user.balance} талонов.</p>"""
    val loadingLine = if (appState.balanceAdminLoading) """<p class="sd-loading-text">Загрузка… <button type="button" id="sd-stop-balance-loading" class="sd-btn sd-btn-small sd-btn-secondary">Показать пусто</button></p>""" else ""
    val users = appState.balanceAdminUsers
    val emptyBalanceBtn = if (!appState.balanceAdminLoading && users.isEmpty()) """<p>Список пуст. <button type="button" id="sd-balance-load" class="sd-btn sd-btn-primary">Загрузить</button></p>""" else ""
    val selectedId = appState.balanceAdminSelectedUserId
    val selectedUser = users.find { it.id == selectedId }
    val instructors = users.filter { it.role == "instructor" }
    val cadets = users.filter { it.role == "cadet" }
    val balanceCardHtml = { u: User ->
        val initials = u.fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        val roleClass = if (u.role == "instructor") "sd-bcard-instructor" else "sd-bcard-cadet"
        val avatarClass = if (u.role == "instructor") "sd-ucard-avatar-blue" else "sd-ucard-avatar-teal"
        val selectedClass = if (u.id == selectedId) " sd-bcard-selected" else ""
        """<div class="sd-bcard $roleClass$selectedClass">
            <div class="sd-ucard-avatar $avatarClass">$initials</div>
            <div class="sd-bcard-info">
                <p class="sd-bcard-name">${formatShortName(u.fullName).escapeHtml()}</p>
                <span class="sd-bcard-balance">$iconTicketSvg ${u.balance} талонов</span>
            </div>
            <button type="button" class="sd-bcard-select-btn" data-balance-select="${u.id.escapeHtml()}" title="Выбрать">$iconSelectSvg</button>
        </div>"""
    }
    val instRows = instructors.joinToString("") { balanceCardHtml(it) }
    val cadetRows = cadets.joinToString("") { balanceCardHtml(it) }
    val selectedBlock = if (selectedUser != null) run {
        val isInst = selectedUser.role == "instructor"
        val cardCls = if (isInst) "sd-balance-sel-instructor" else "sd-balance-sel-cadet"
        val avatarCls = if (isInst) "sd-ucard-avatar-blue" else "sd-ucard-avatar-teal"
        val roleLabel = if (isInst) "Инструктор" else "Курсант"
        val initials = selectedUser.fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        val shortName = formatShortName(selectedUser.fullName).escapeHtml()
        val bal = selectedUser.balance
        fun ticketWord(n: Int) = when {
            n % 100 in 11..14 -> "талонов"
            n % 10 == 1 -> "талон"
            n % 10 in 2..4 -> "талона"
            else -> "талонов"
        }
        """<div class="sd-balance-sel $cardCls" id="sd-balance-selected-block">
            <div class="sd-balance-sel-top">
                <div class="sd-ucard-avatar $avatarCls">$initials</div>
                <div class="sd-balance-sel-info">
                    <p class="sd-balance-sel-name">$shortName</p>
                    <span class="sd-balance-sel-role">$roleLabel</span>
                </div>
                <div class="sd-balance-sel-counter">
                    <span class="sd-balance-sel-count">$bal</span>
                    <span class="sd-balance-sel-count-label">${ticketWord(bal)}</span>
                </div>
            </div>
            <div class="sd-balance-sel-input-row">
                <span class="sd-balance-sel-input-label">Количество талонов</span>
                <input type="number" id="sd-balance-amount" class="sd-balance-sel-input" value="0" min="0" />
            </div>
            <div class="sd-balance-selected-actions">
                <button type="button" id="sd-balance-credit" class="sd-btn sd-balance-btn sd-balance-btn-credit">$iconCreditSvg Зачислить</button>
                <button type="button" id="sd-balance-debit" class="sd-btn sd-balance-btn sd-balance-btn-debit">$iconDebitSvg Списать</button>
                <button type="button" id="sd-balance-set" class="sd-btn sd-balance-btn sd-balance-btn-set">$iconSetSvg Установить</button>
                <button type="button" id="sd-balance-clear-selection" class="sd-btn sd-balance-btn sd-balance-btn-clear">$iconResetSvg Сбросить</button>
            </div>
        </div>"""
    } else ""
    val history = appState.balanceAdminHistory.take(50)
    val typeLabel = { t: String -> when (t) { "credit" -> "зачислено"; "debit" -> "списано"; "set" -> "установлено"; else -> t } }
    fun ticketWord(n: Int): String {
        val a = n % 100
        if (a in 11..14) return "талонов"
        return when (n % 10) { 1 -> "талон"; 2, 3, 4 -> "талона"; else -> "талонов" }
    }
    val sortedHistory = history.sortedByDescending { it.timestampMillis ?: 0L }
    val byDate = sortedHistory.groupBy { formatDateTimeEkaterinburg(it.timestampMillis).substringBefore(", ") }
    val historyRows = byDate.entries.joinToString("") { (dateStr, entries) ->
        val rows = entries.joinToString("") { b ->
            val userName = users.find { it.id == b.userId }?.fullName ?: b.userId.take(8) + "…"
            val label = typeLabel(b.type)
            val tail = "${b.amount} ${ticketWord(b.amount)}"
            """<div class="sd-record-row"><span class="sd-balance-history-time">${formatDateTimeEkaterinburg(b.timestampMillis).substringAfter(", ").ifEmpty { "—" }}</span> — <strong>${userName.escapeHtml()}</strong>: $label $tail</div>"""
        }
        """<div class="sd-balance-history-day"><p class="sd-balance-history-day-title">$dateStr</p><div class="sd-balance-history-day-list">$rows</div></div>"""
    }
    val historyEmptyMsg = if (history.isEmpty()) """<p class="sd-muted sd-balance-history-empty">Нет записей. Выполните зачисление или списание по выбранному пользователю — операции появятся здесь.</p>""" else ""
    val historyOpen = if (appState.balanceHistorySectionOpen) " open" else ""
    val historyDetailsContent = if (history.isEmpty()) historyEmptyMsg else """<div class="sd-balance-history-by-date">$historyRows</div>"""
    val historyBlock = """<details class="sd-block sd-details-block" data-balance-section="history"$historyOpen><summary class="sd-block-title">История операций (${history.size})</summary>$historyDetailsContent</details>"""
    return """<h2>Баланс</h2>$loadingLine$emptyBalanceBtn
        <div class="sd-block" id="sd-balance-instructors-block"><h3 class="sd-block-title">Инструкторы (${instructors.size})</h3><div id="sd-balance-instructors-list" class="sd-balance-cards">$instRows</div></div>
        <div class="sd-block" id="sd-balance-cadets-block"><h3 class="sd-block-title">Курсанты (${cadets.size})</h3><div id="sd-balance-cadets-list" class="sd-balance-cards">$cadetRows</div></div>
        $selectedBlock
        $historyBlock"""
}

/** Возвращает (кнопки вкладок, контент вкладки) для текущего выбора. */
private fun getPanelTabButtonsAndContent(user: User, tabs: List<String>): Pair<String, String> {
    val appInfo = SharedFactory.getAppInfoRepository().getAppInfo()
    val selected = appState.selectedTabIndex.coerceIn(0, tabs.size - 1)
    val tabIconMap = mapOf(
        "Главная"  to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>""",
        "Баланс"   to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>""",
        "Чат"      to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>""",
        "История"  to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>""",
        "Запись"   to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>""",
        "Билеты"   to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>""",
        "Настройки" to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>"""
    )
    val tabButtons = tabs.mapIndexed { i, name ->
        val cls = if (i == selected) "sd-tab sd-active" else "sd-tab"
        val icon = tabIconMap[name] ?: """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg>"""
        """<button type="button" class="$cls" data-tab="$i"><span class="sd-tab-icon">$icon</span><span class="sd-tab-label">$name</span></button>"""
    }.joinToString("")
    val tabName = tabs[selected]
    val tabContent = when (tabName) {
        "Главная" -> when (user.role) {
            "admin" -> renderAdminHomeContent()
            "instructor" -> renderInstructorHomeContent(user, appInfo.version)
            "cadet" -> renderCadetHomeContent(user, appInfo.version)
            else -> """<h2>$tabName</h2><p>Баланс: ${user.balance} талонов.</p><p>Версия: ${appInfo.version}</p>"""
        }
        "Чат" -> renderChatTabContent(user)
        "Баланс" -> renderBalanceTabContent(user)
        "Запись", "Запись на вождение" -> renderRecordingTabContent(user)
        "История" -> renderHistoryTabContent(user)
        "Билеты" -> renderTicketsTabContent()
        "ПДД" -> """<h2>$tabName</h2><p>Правила дорожного движения — полный функционал в приложении.</p><p><a href="https://play.google.com/store/apps/details?id=com.example.startdrive" target="_blank" rel="noopener">Приложение StartDrive</a> · <a href="https://pdd.ru/" target="_blank" rel="noopener">ПДД РФ (pdd.ru)</a></p>"""
        "Настройки" -> renderSettingsTabContent(user)
        else -> """<h2>$tabName</h2><p>Раздел в разработке.</p>"""
    }
    return Pair(tabButtons, tabContent)
}

private fun renderPanel(user: User, roleTitle: String, tabs: List<String>): String {
    val (tabButtons, tabContent) = getPanelTabButtonsAndContent(user, tabs)
    return """
        <header class="sd-header sd-panel-header">
            <div class="sd-header-text">
                <h1>StartDrive · $roleTitle</h1>
                <p>${formatShortName(user.fullName)} · ${user.email}</p>
            </div>
            <button type="button" id="sd-btn-signout" class="sd-btn sd-btn-signout" title="Выйти">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" width="18" height="18">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
                <span class="sd-signout-label">Выйти</span>
            </button>
        </header>
        <main class="sd-content">
            <div class="sd-card" id="sd-card">
                $tabContent
            </div>
        </main>
        <nav class="sd-tabs">$tabButtons</nav>
    """.trimIndent()
}

/** Принудительно обновляет контент #sd-card по текущему appState (для панели инструктора/курсанта). */
private fun refreshPanelCardContent(root: org.w3c.dom.Element) {
    val user = appState.user ?: return
    val tabs = when (appState.screen) {
        AppScreen.Admin -> listOf("Главная", "Баланс", "Чат", "История")
        AppScreen.Instructor -> listOf("Главная", "Запись", "Чат", "Билеты", "История", "Настройки")
        else -> listOf("Главная", "Запись", "Чат", "Билеты", "История", "Настройки")
    }
    val (_, tabContent) = getPanelTabButtonsAndContent(user, tabs)
    (root.querySelector("#sd-card") as? org.w3c.dom.Element)?.innerHTML = tabContent
    attachListeners(root)
}

/** Обновляет только контент вкладки «Билеты» в #sd-card (экзамен/билеты). */
private fun refreshTicketsCardContent(root: org.w3c.dom.Element) {
    val content = renderTicketsTabContent()
    (root.querySelector("#sd-card") as? org.w3c.dom.Element)?.innerHTML = content
    attachListeners(root)
}

/** Запускает экзамен ПДД по категории (A_B или C_D). Вызывается из клика по [data-pdd-exam-category] или по «Категория AB/CD» с главного экрана. */
private fun runPddExam(examCat: String, root: org.w3c.dom.Element) {
    val log = js("console.log").unsafeCast<(Any?) -> Unit>()
    log("PDD: runPddExam cat=$examCat")
    fun applyExam(b: dynamic, r: org.w3c.dom.Element) {
        try {
            val (examQuestions, blockIndices) = generateExamTicket(b, examCat)
            log("PDD: exam ticket generated questions=${examQuestions.size}")
            val now = js("Date.now").unsafeCast<() -> Double>().invoke()
            val tabsList = when (appState.screen) {
                AppScreen.Instructor -> listOf("Главная", "Запись", "Чат", "Билеты", "История", "Настройки")
                else -> listOf("Главная", "Запись", "Чат", "Билеты", "История", "Настройки")
            }
            val ticketsTabIndex = tabsList.indexOf("Билеты").coerceAtLeast(0)
            updateState {
                selectedTabIndex = ticketsTabIndex
                pddCategoryId = "exam"
                pddTicketsBundle = b
                pddExamMode = true; pddExamCategoryForBundle = examCat
                pddExamStartTimeMs = now; pddExamBlockIndices = blockIndices
                pddExamPhase = "main"
                pddQuestions = examQuestions; pddCurrentIndex = 0; pddUserSelections = emptyMap()
                pddFinished = false; pddLoading = false
                pddTicketName = "Экзамен (${if (examCat == "A_B") "AB" else "CD"})"
            }
            startExamTimer()
            forceFullPanelRender(r, useExamContent = true)
            log("PDD: forceFullPanelRender done")
        } catch (t: Throwable) {
            js("console.error").unsafeCast<(Any?) -> Unit>().invoke("PDD exam start error: " + (t.message ?: t.toString()))
            updateState { pddExamCategoryForBundle = null }
        }
    }
    var bundle: dynamic = getEmbeddedPddBundle()
    if (bundle == null || bundle == js("undefined")) bundle = appState.pddTicketsBundle
    if (bundle == null || bundle == js("undefined")) bundle = js("window.__PDD_TICKETS_BUNDLE__").unsafeCast<dynamic>()
    val hasBundle = js("(function(b, cat){ return b != null && b !== undefined && b[cat]; })").unsafeCast<(dynamic, String) -> Boolean>().invoke(bundle, examCat)
    if (hasBundle) {
        applyExam(bundle, root)
    } else {
        updateState { pddCategoryId = "exam"; pddExamCategoryForBundle = examCat; pddLoading = true }
        loadPddBundleFromNetwork { b: dynamic? ->
            val ok = js("(function(x){ return x != null && x !== undefined; })").unsafeCast<(dynamic) -> Boolean>().invoke(b)
            if (ok && b != null) (document.getElementById("root") as? org.w3c.dom.Element)?.let { r -> applyExam(b, r) }
            else updateState { pddLoading = false; pddExamCategoryForBundle = null }
        }
    }
}

/** Полная перерисовка панели. useExamContent = true — принудительно подставить экран экзамена в карточку. */
private fun forceFullPanelRender(root: org.w3c.dom.Element, useExamContent: Boolean = false) {
    val user = appState.user ?: return
    val networkBanner = appState.networkError?.let { msg ->
        """<div class="sd-network-error" id="sd-network-error"><span>$msg</span> <button type="button" id="sd-dismiss-network-error" class="sd-btn-inline">Закрыть</button></div>"""
    } ?: ""
    val loadingOverlay = if (appState.loading) """<div class="sd-loading-overlay" id="sd-loading-overlay"><div class="sd-spinner"></div><p>Загрузка…</p></div>""" else ""
    val roleTitle = when (appState.screen) {
        AppScreen.Admin -> "Администратор"
        AppScreen.Instructor -> "Инструктор"
        AppScreen.Cadet -> "Курсант"
        else -> ""
    }
    val tabs = when (appState.screen) {
        AppScreen.Admin -> listOf("Главная", "Баланс", "Чат", "История")
        AppScreen.Instructor -> listOf("Главная", "Запись", "Чат", "Билеты", "История", "Настройки")
        else -> listOf("Главная", "Запись", "Чат", "Билеты", "История", "Настройки")
    }
    val (tabButtons, tabContentFromTabs) = getPanelTabButtonsAndContent(user, tabs)
    val tabContent = if (useExamContent) renderTicketsTabContent() else tabContentFromTabs
    val panelHtml = """
        <header class="sd-header sd-panel-header">
            <div class="sd-header-text">
                <h1>StartDrive · $roleTitle</h1>
                <p>${formatShortName(user.fullName)} · ${user.email}</p>
            </div>
            <button type="button" id="sd-btn-signout" class="sd-btn sd-btn-signout" title="Выйти">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" width="18" height="18">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
                <span class="sd-signout-label">Выйти</span>
            </button>
        </header>
        <main class="sd-content">
            <div class="sd-card" id="sd-card">
                $tabContent
            </div>
        </main>
        <nav class="sd-tabs">$tabButtons</nav>
    """.trimIndent()
    root.innerHTML = networkBanner + loadingOverlay + panelHtml
    if (useExamContent) {
        val card = root.querySelector("#sd-card") as? org.w3c.dom.Element
        if (card != null) {
            card.innerHTML = renderTicketsTabContent()
        }
    }
    attachListeners(root)
    root.querySelector("#sd-card")?.unsafeCast<dynamic>()?.scrollIntoView(js("({ block: 'start', behavior: 'auto' })"))
}

private fun setupPanelClickDelegation(root: org.w3c.dom.Element) {
    root.addEventListener("click", { e: dynamic ->
        if (appState.screen != AppScreen.Admin && appState.screen != AppScreen.Instructor && appState.screen != AppScreen.Cadet) return@addEventListener
        val target = e?.target as? org.w3c.dom.Element ?: return@addEventListener
        val closestHelper = js("(function(el, sel) { return el && el.closest ? el.closest(sel) : null; })").unsafeCast<(Any?, String) -> Any?>()
        val closest = { s: String ->
            try {
                closestHelper(target, s) as? org.w3c.dom.Element
            } catch (_: Throwable) { null }
        }
        if (appState.user?.role == "instructor" || appState.user?.role == "cadet") {
            val pddExamBack = closest(".sd-pdd-exam-back")
            if (pddExamBack != null) {
                val examStartMs = appState.pddExamStartTimeMs ?: 0.0
                val nowMs = js("Date.now").unsafeCast<() -> Double>().invoke()
                if (examStartMs > 0.0 && (nowMs - examStartMs) < 500.0) return@addEventListener
                clearExamTimer()
                updateState {
                    pddExamMode = false; pddExamCategoryForBundle = null; pddExamStartTimeMs = null
                    pddExamBlockIndices = emptyList(); pddExamPhase = "main"
                    pddExamAdditionalQuestions = emptyList(); pddExamAdditionalBlockIndices = emptyList()
                    pddExamAdditionalCurrentIndex = 0; pddExamAdditionalUserSelections = emptyMap()
                    pddExamAdditionalStartTimeMs = null; pddExamAdditionalDurationSec = 300
                    pddExamResultPass = null
                    pddCategoryId = null
                    pddTicketName = null; pddQuestions = emptyList(); pddCurrentIndex = 0
                    pddUserSelections = emptyMap(); pddFinished = false
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddExamCategory = closest("[data-pdd-exam-category]")
            if (pddExamCategory != null) {
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                (e as? org.w3c.dom.events.Event)?.stopPropagation()
                val examCat = pddExamCategory.getAttribute("data-pdd-exam-category") ?: return@addEventListener
                runPddExam(examCat, root)
                return@addEventListener
            }
            val pddBackCat = closest(".sd-pdd-back-categories")
            val clickedBackButton = pddBackCat != null && (target == pddBackCat || js("(function(a,b){ return a===b || (a&&b&&a.contains&&a.contains(b)); })").unsafeCast<(Any?, Any?) -> Boolean>().invoke(pddBackCat, target))
            if (clickedBackButton) {
                updateState {
                    pddCategoryId = null; pddTicketName = null; pddQuestions = emptyList()
                    pddCurrentIndex = 0; pddUserSelections = emptyMap(); pddFinished = false
                    pddExamMode = false; pddExamCategoryForBundle = null; pddExamStartTimeMs = null
                    pddExamBlockIndices = emptyList(); pddExamPhase = "main"
                    pddExamAdditionalQuestions = emptyList(); pddExamAdditionalBlockIndices = emptyList()
                    pddExamAdditionalCurrentIndex = 0; pddExamAdditionalUserSelections = emptyMap(); pddExamResultPass = null
                    pddSignsSections = emptyList(); pddSelectedSign = null; pddSelectedSignSectionIndex = -1; pddSelectedSignItemIndex = -1; pddResetConfirmCategory = null; pddMarkupSections = emptyList(); pddPenalties = emptyList(); pddByTopicSections = emptyList()
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddTicket = closest(".sd-pdd-ticket")
            if (pddTicket != null) {
                val ticketName = pddTicket.getAttribute("data-pdd-ticket") ?: return@addEventListener
                val categoryId = pddTicket.getAttribute("data-pdd-category") ?: return@addEventListener
                updateState { pddTicketName = ticketName; pddLoading = true }
                val num = ticketName.removePrefix("Билет ").toIntOrNull() ?: 1
                val log = js("console.log").unsafeCast<(Any?) -> Unit>()
                log("PDD: click ticket categoryId=$categoryId num=$num")
                fun applyTicket(b: dynamic) {
                    val questions = getQuestionsFromBundle(b, categoryId, num)
                    log("PDD: applyTicket questions.size=${questions.size}")
                    val parsed = getPddTicketSavedParsed(categoryId, ticketName)
                    val savedSelections = parsed.filter { it.value.second }.mapValues { it.value.first } /* только правильные — чтобы ошибочные можно было решить один раз заново */
                    updateState {
                        pddTicketsBundle = b
                        pddQuestions = questions; pddCurrentIndex = 0; pddUserSelections = savedSelections
                        pddFinished = false; pddLoading = false
                    }
                }
                var bundle = appState.pddTicketsBundle
                if (bundle == null || bundle == js("undefined")) {
                    log("PDD: no bundle in state, trying embedded")
                    bundle = getEmbeddedPddBundle()
                } else log("PDD: bundle from state")
                if (bundle == null || bundle == js("undefined")) {
                    log("PDD: trying window.__PDD_TICKETS_BUNDLE__")
                    bundle = js("window.__PDD_TICKETS_BUNDLE__").unsafeCast<dynamic>()
                }
                if (bundle != null && bundle != js("undefined")) {
                    log("PDD: using bundle, applying")
                    applyTicket(bundle)
                } else {
                    log("PDD: no bundle, loading from network")
                    loadPddBundleFromNetwork { b: dynamic? ->
                        if (b != null && b != js("undefined")) {
                            log("PDD: network load OK, applying")
                            applyTicket(b)
                        } else {
                            log("PDD: network load failed")
                            updateState { pddLoading = false; pddQuestions = emptyList() }
                        }
                    }
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddCategoryCard = closest(".sd-ticket-category-clickable[data-pdd-category]:not(.sd-pdd-ticket)")
            if (pddCategoryCard != null) {
                val catId = pddCategoryCard.getAttribute("data-pdd-category")
                if (catId != null && catId.isNotBlank()) {
                    handlePddCategoryClick(catId)
                    (e as? org.w3c.dom.events.Event)?.preventDefault()
                    (e as? org.w3c.dom.events.Event)?.stopPropagation()
                    return@addEventListener
                }
            }
            val pddBackTickets = closest(".sd-pdd-back-tickets")
            if (pddBackTickets != null) {
                updateState {
                    pddTicketName = null; pddQuestions = emptyList(); pddCurrentIndex = 0
                    pddUserSelections = emptyMap(); pddFinished = false
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddBackSigns = closest(".sd-pdd-back-signs")
            if (pddBackSigns != null) {
                updateState { pddSelectedSign = null }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val signCard = closest(".sd-pdd-sign-card")
            if (signCard != null) {
                val num = signCard.getAttribute("data-sign-number") ?: return@addEventListener
                val secIdx = signCard.getAttribute("data-section-index")?.toIntOrNull() ?: -1
                val itemIdx = signCard.getAttribute("data-item-index")?.toIntOrNull() ?: -1
                val sections = appState.pddSignsSections
                val item = if (secIdx in sections.indices && itemIdx in sections[secIdx].items.indices) sections[secIdx].items[itemIdx] else sections.flatMap { it.items }.firstOrNull { it.number == num }
                if (item != null) {
                    updateState {
                        pddSelectedSign = item
                        pddSelectedSignSectionIndex = if (secIdx in sections.indices) secIdx else -1
                        pddSelectedSignItemIndex = if (secIdx in sections.indices && itemIdx in sections[secIdx].items.indices) itemIdx else -1
                    }
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val signNextBtn = closest(".sd-pdd-sign-next")
            if (signNextBtn != null) {
                val nextSecIdx = signNextBtn.getAttribute("data-next-section-index")?.toIntOrNull() ?: -1
                val nextItemIdx = signNextBtn.getAttribute("data-next-item-index")?.toIntOrNull() ?: -1
                val sections = appState.pddSignsSections
                if (nextSecIdx in sections.indices && nextItemIdx in sections[nextSecIdx].items.indices) {
                    val nextItem = sections[nextSecIdx].items[nextItemIdx]
                    updateState {
                        pddSelectedSign = nextItem
                        pddSelectedSignSectionIndex = nextSecIdx
                        pddSelectedSignItemIndex = nextItemIdx
                        pddScrollToSignDetail = true
                    }
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddGoQuestion = closest(".sd-pdd-question-nav-item")
            if (pddGoQuestion != null) {
                val idx = pddGoQuestion.getAttribute("data-pdd-go-question")?.toIntOrNull() ?: return@addEventListener
                updateState { pddCurrentIndex = idx }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddBackToQuestions = closest(".sd-pdd-back-to-questions")
            if (pddBackToQuestions != null) {
                updateState { pddFinished = false; pddCurrentIndex = 0 }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddExamAdditionalAnswer = closest(".sd-pdd-answer[data-pdd-exam-additional]")
            if (pddExamAdditionalAnswer != null && pddExamAdditionalAnswer.getAttribute("disabled") == null) {
                val answerIndex = pddExamAdditionalAnswer.getAttribute("data-pdd-answer-index")?.toIntOrNull() ?: return@addEventListener
                val questionIndex = pddExamAdditionalAnswer.getAttribute("data-pdd-question-index")?.toIntOrNull() ?: return@addEventListener
                updateState {
                    val m = pddExamAdditionalUserSelections.toMutableMap()
                    m[questionIndex] = answerIndex
                    pddExamAdditionalUserSelections = m
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddExamMainAnswer = closest(".sd-pdd-answer[data-pdd-exam-main]")
            if (pddExamMainAnswer != null && pddExamMainAnswer.getAttribute("disabled") == null) {
                val answerIndex = pddExamMainAnswer.getAttribute("data-pdd-answer-index")?.toIntOrNull() ?: return@addEventListener
                val questionIndex = pddExamMainAnswer.getAttribute("data-pdd-question-index")?.toIntOrNull() ?: return@addEventListener
                updateState {
                    val m = pddUserSelections.toMutableMap()
                    m[questionIndex] = answerIndex
                    pddUserSelections = m
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddAnswer = closest(".sd-pdd-answer")
            if (pddAnswer != null) {
                if (pddAnswer.getAttribute("disabled") != null) return@addEventListener /* ответ уже выбран, менять нельзя */
                val answerIndex = pddAnswer.getAttribute("data-pdd-answer-index")?.toIntOrNull() ?: return@addEventListener
                val questionIndex = pddAnswer.getAttribute("data-pdd-question-index")?.toIntOrNull() ?: return@addEventListener
                updateState {
                    val m = pddUserSelections.toMutableMap()
                    m[questionIndex] = answerIndex
                    pddUserSelections = m
                }
                /* Сохраняем статистику билета при каждом выборе/смене ответа (A_B/C_D) */
                val catId = appState.pddCategoryId
                val tName = appState.pddTicketName
                if (catId != null && tName != null && (catId == "A_B" || catId == "C_D" || catId == "by_topic")) {
                    val q = appState.pddQuestions.getOrNull(questionIndex)
                    val isCorrect = q?.answers?.getOrNull(answerIndex)?.isCorrect == true
                    savePddTicketResult(catId, tName, questionIndex, answerIndex, isCorrect)
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddNext = closest(".sd-pdd-next")
            if (pddNext != null && pddNext.getAttribute("disabled") == null) {
            val isExamAdditional = pddNext.getAttribute("data-pdd-exam-additional") != null
            val isExamMain = pddNext.getAttribute("data-pdd-exam-main") != null
            if (isExamAdditional) {
                val addStartMs = appState.pddExamAdditionalStartTimeMs ?: 0.0
                val addDurationSec = appState.pddExamAdditionalDurationSec
                val addNow = js("Date.now").unsafeCast<() -> Double>().invoke()
                val addTimeOver = ((addNow - addStartMs) / 1000.0).toInt() >= addDurationSec
                if (addTimeOver) {
                    clearExamTimer()
                    updateState { pddExamResultPass = false; pddExamPhase = "result" }
                    (e as? org.w3c.dom.events.Event)?.preventDefault()
                    return@addEventListener
                }
                val addIdx = appState.pddExamAdditionalCurrentIndex
                val addTotal = appState.pddExamAdditionalQuestions.size
                val addSel = appState.pddExamAdditionalUserSelections[addIdx]
                if (addSel != null) {
                    if (addIdx >= addTotal - 1) {
                        val addQuestions = appState.pddExamAdditionalQuestions
                        val allCorrect = addQuestions.indices.all { qIdx ->
                            val s = appState.pddExamAdditionalUserSelections[qIdx] ?: return@all false
                            addQuestions.getOrNull(qIdx)?.answers?.getOrNull(s)?.isCorrect == true
                        }
                        clearExamTimer()
                        updateState { pddExamResultPass = allCorrect; pddExamPhase = "result" }
                    } else {
                        updateState { pddExamAdditionalCurrentIndex = addIdx + 1 }
                    }
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
                if (isExamMain && appState.pddExamMode) {
                    val startTime = appState.pddExamStartTimeMs ?: 0.0
                    val now = js("Date.now").unsafeCast<() -> Double>().invoke()
                    val elapsedSec = ((now - startTime) / 1000.0).toInt()
                    val timeOver = elapsedSec >= 20 * 60
                    if (timeOver) {
                        updateState { pddExamResultPass = false; pddExamPhase = "result" }
                        (e as? org.w3c.dom.events.Event)?.preventDefault()
                        return@addEventListener
                    }
                    val idx = appState.pddCurrentIndex
                    val total = appState.pddQuestions.size
                    val sel = appState.pddUserSelections[idx]
                    if (sel != null) {
                        if (idx < total - 1) {
                            updateState { pddCurrentIndex = idx + 1 }
                        } else {
                            val blockIndices = appState.pddExamBlockIndices
                            val questions = appState.pddQuestions
                            val userSelections = appState.pddUserSelections
                            val errorsPerBlock = IntArray(4)
                            for (i in 0 until 20) {
                                val bi = blockIndices.getOrNull(i) ?: 0
                                val s = userSelections[i] ?: continue
                                val correct = questions.getOrNull(i)?.answers?.getOrNull(s)?.isCorrect == true
                                if (!correct) errorsPerBlock[bi]++
                            }
                            val totalErrors = errorsPerBlock.sum()
                            val twoOrMoreInBlock = errorsPerBlock.any { it >= 2 }
                            when {
                                timeOver || totalErrors >= 3 || twoOrMoreInBlock -> {
                                    clearExamTimer()
                                    updateState { pddExamResultPass = false; pddExamPhase = "result" }
                                }
                                totalErrors == 0 -> {
                                    clearExamTimer()
                                    updateState { pddExamResultPass = true; pddExamPhase = "result" }
                                }
                                else -> {
                                    val blocksWithOneError = errorsPerBlock.mapIndexed { b, c -> b to c }.filter { it.second == 1 }.map { it.first }
                                    val bundle = appState.pddTicketsBundle
                                    val catForBundle = appState.pddExamCategoryForBundle ?: "A_B"
                                    val allQ = if (bundle != null && bundle != js("undefined")) getAllQuestionsFromBundle(bundle, catForBundle) else emptyList()
                                    val mainIds = questions.map { it.id }.toSet()
                                    var excludeIds = mainIds
                                    val additionalList = mutableListOf<PddQuestion>()
                                    val additionalBlockList = mutableListOf<Int>()
                                    for (blockIdx in blocksWithOneError) {
                                        val wrongQ = questions.withIndex().firstOrNull { blockIndices.getOrNull(it.index) == blockIdx && userSelections[it.index] != null && questions[it.index].answers.getOrNull(userSelections[it.index]!!)?.isCorrect != true }
                                        val topicName = wrongQ?.let { (_, q) -> q.topic.firstOrNull()?.takeIf { it.isNotBlank() } ?: "Прочее" } ?: "Прочее"
                                        val five = getAdditionalQuestionsByTopic(allQ, topicName, excludeIds)
                                        additionalList.addAll(five)
                                        repeat(five.size) { additionalBlockList.add(blockIdx) }
                                        excludeIds = excludeIds + five.map { it.id }
                                    }
                                    val addDurationSec = if (additionalList.size <= 5) 5 * 60 else 10 * 60
                                    val addStartMs = js("Date.now").unsafeCast<() -> Double>().invoke()
                                    updateState {
                                        pddExamAdditionalQuestions = additionalList
                                        pddExamAdditionalBlockIndices = additionalBlockList
                                        pddExamAdditionalCurrentIndex = 0
                                        pddExamAdditionalUserSelections = emptyMap()
                                        pddExamAdditionalStartTimeMs = addStartMs
                                        pddExamAdditionalDurationSec = addDurationSec
                                        pddExamPhase = "additional"
                                    }
                                }
                            }
                        }
                    }
                    (e as? org.w3c.dom.events.Event)?.preventDefault()
                    return@addEventListener
                }
                val idx = appState.pddCurrentIndex
                val total = appState.pddQuestions.size
                val sel = appState.pddUserSelections[idx]
                val catId = appState.pddCategoryId
                val tName = appState.pddTicketName
                if (sel != null && catId != null && tName != null && (catId == "A_B" || catId == "C_D")) {
                    val q = appState.pddQuestions.getOrNull(idx)
                    val isCorrect = q?.answers?.getOrNull(sel)?.isCorrect == true
                    savePddTicketResult(catId, tName, idx, sel, isCorrect)
                }
                if (sel != null) {
                    if (idx >= total - 1) updateState { pddFinished = true }
                    else updateState { pddCurrentIndex = idx + 1 }
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddResetStats = closest(".sd-pdd-reset-stats")
            if (pddResetStats != null) {
                val resetCat = pddResetStats.getAttribute("data-pdd-reset-category") ?: return@addEventListener
                updateState { pddResetConfirmCategory = resetCat }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddResetConfirmYes = closest(".sd-pdd-reset-confirm-yes")
            if (pddResetConfirmYes != null) {
                val resetCat = pddResetConfirmYes.getAttribute("data-pdd-reset-category") ?: return@addEventListener
                clearPddTicketStatsForCategory(resetCat)
                updateState { pddStatsVersion = pddStatsVersion + 1; pddResetConfirmCategory = null }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddResetConfirmNo = closest(".sd-pdd-reset-confirm-no")
            if (pddResetConfirmNo != null) {
                updateState { pddResetConfirmCategory = null }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
            val pddTopicSection = closest(".sd-pdd-section-clickable[data-pdd-topic-index]")
            if (pddTopicSection != null) {
                val idxStr = pddTopicSection.getAttribute("data-pdd-topic-index") ?: return@addEventListener
                val secIdx = idxStr.toIntOrNull() ?: return@addEventListener
                val sections = appState.pddByTopicSections
                val sec = sections.getOrNull(secIdx) ?: return@addEventListener
                val ticketNameTopic = "По разделу: ${sec.name}"
                val parsedTopic = getPddTicketSavedParsed("by_topic", ticketNameTopic)
                val savedSelectionsTopic = parsedTopic.filter { it.value.second }.mapValues { it.value.first }
                updateState {
                    pddQuestions = sec.questions
                    pddTicketName = ticketNameTopic
                    pddCurrentIndex = 0
                    pddUserSelections = savedSelectionsTopic
                    pddFinished = false
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                return@addEventListener
            }
        }
        val summaryInMainSection = closest("details[data-admin-section] summary")
        if (summaryInMainSection != null && appState.user?.role == "admin") {
            val detailsEl = summaryInMainSection.parentElement ?: return@addEventListener
            window.setTimeout({
                val cardEl = document.getElementById("sd-card") as? org.w3c.dom.Element
                if (cardEl != null) {
                    val newbiesDetails = cardEl.querySelector("details[data-admin-section=\"newbies\"]")
                    val instDetails = cardEl.querySelector("details[data-admin-section=\"instructors\"]")
                    val cadetDetails = cardEl.querySelector("details[data-admin-section=\"cadets\"]")
                    updateState {
                        adminNewbiesSectionOpen = (newbiesDetails?.unsafeCast<dynamic>()?.open == true)
                        adminInstructorsSectionOpen = (instDetails?.unsafeCast<dynamic>()?.open == true)
                        adminCadetsSectionOpen = (cadetDetails?.unsafeCast<dynamic>()?.open == true)
                    }
                }
            }, 0)
            return@addEventListener
        }
        val cadetsToggleBtn = closest(".sd-instructor-cadets-toggle")
        if (cadetsToggleBtn != null) {
            val instId = cadetsToggleBtn.getAttribute("data-instructor-cadets-modal") ?: return@addEventListener
            updateState { adminInstructorCadetsModalId = instId }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val modalCloseBtn = closest("#sd-admin-cadets-modal-close")
        if (modalCloseBtn != null) {
            updateState { adminInstructorCadetsModalId = null }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val modalOverlay = if (target.id == "sd-admin-cadets-modal-overlay") target else null
        if (modalOverlay != null) {
            updateState { adminInstructorCadetsModalId = null }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val tabBtn = closest(".sd-tab")
        if (tabBtn != null) {
            val idx = tabBtn.getAttribute("data-tab")?.toIntOrNull() ?: return@addEventListener
            var newbiesOpen = appState.adminNewbiesSectionOpen
            var instOpen = appState.adminInstructorsSectionOpen
            var cadetsOpen = appState.adminCadetsSectionOpen
            if (appState.user?.role == "admin" && appState.selectedTabIndex == 0 && idx != 0) {
                val cardEl = document.getElementById("sd-card") as? org.w3c.dom.Element
                if (cardEl != null) {
                    val newbiesDetails = cardEl.querySelector("details[data-admin-section=\"newbies\"]")
                    val instDetails = cardEl.querySelector("details[data-admin-section=\"instructors\"]")
                    val cadetDetails = cardEl.querySelector("details[data-admin-section=\"cadets\"]")
                    newbiesOpen = (newbiesDetails?.unsafeCast<dynamic>()?.open == true)
                    instOpen = (instDetails?.unsafeCast<dynamic>()?.open == true)
                    cadetsOpen = (cadetDetails?.unsafeCast<dynamic>()?.open == true)
                }
            }
            if (idx != 2) {
                saveChatScrollForCurrentContact()
                updateState { selectedChatContactId = null; chatMessages = emptyList() }
                unsubscribeChat()
            }
            val nOpen = newbiesOpen
            val iOpen = instOpen
            val cOpen = cadetsOpen
            updateState {
                selectedTabIndex = idx
                recordingLoading = false
                historyLoading = false
                balanceAdminLoading = false
                chatContactsLoading = false
                adminHomeLoading = false
                if (appState.user?.role == "admin" && idx != 0) {
                    adminNewbiesSectionOpen = nOpen
                    adminInstructorsSectionOpen = iOpen
                    adminCadetsSectionOpen = cOpen
                }
            }
            val user = appState.user ?: return@addEventListener
            when (idx) {
                0 -> if (user.role == "admin" && !appState.adminHomeLoading) {
                    updateState { adminHomeLoading = true; networkError = null }
                    val tid = window.setTimeout({ updateState { adminHomeLoading = false } }, 8000)
                    getUsersWithError { list, err ->
                        window.clearTimeout(tid)
                        updateState {
                            adminHomeUsers = list; balanceAdminUsers = list; adminHomeLoading = false
                            if (err != null) networkError = err
                        }
                    }
                }
                1 -> if (user.role == "admin" && !appState.balanceAdminLoading) {
                    updateState { balanceAdminLoading = true }
                    val tid = window.setTimeout({ updateState { balanceAdminLoading = false } }, 8000)
                    getUsers { list ->
                        loadBalanceHistoryForUsers(list.map { it.id }) { hist ->
                            window.clearTimeout(tid)
                            updateState { balanceAdminUsers = list; adminHomeUsers = list; balanceAdminHistory = hist; balanceAdminLoading = false }
                        }
                    }
                }
                2 -> if (appState.chatContacts.isEmpty() && !appState.chatContactsLoading) {
                    updateState { chatContactsLoading = true }
                    val chatTid = window.setTimeout({ updateState { chatContactsLoading = false } }, 5000)
                    getUsersForChat(user) { list ->
                        window.clearTimeout(chatTid)
                        updateState { chatContacts = list; chatContactsLoading = false }
                        subscribeChatPresence(list.map { it.id })
                    }
                }
                else -> { }
            }
            if ((idx == 0 || idx == 1) && (user.role == "instructor" || user.role == "cadet") && !appState.recordingLoading) {
                updateState { recordingLoading = true }
                val tid = window.setTimeout({ updateState { recordingLoading = false } }, 8000)
                if (user.role == "instructor") {
                    getOpenWindowsForInstructor(user.id) { wins ->
                        getSessionsForInstructor(user.id) { sess ->
                            window.clearTimeout(tid)
                            updateState { recordingOpenWindows = wins; recordingSessions = sess; recordingLoading = false }
                            getUsers { list -> updateState { instructorCadets = list.filter { user.assignedCadets.contains(it.id) } } }
                        }
                    }
                } else {
                    val instId = user.assignedInstructorId ?: ""
                    getOpenWindowsForCadet(instId) { wins ->
                        getSessionsForCadet(user.id) { sess ->
                            window.clearTimeout(tid)
                            updateState { recordingOpenWindows = wins; recordingSessions = sess; recordingLoading = false }
                            user.assignedInstructorId?.let { id -> getUserById(id) { inst -> updateState { cadetInstructor = inst } } }
                        }
                    }
                }
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val btnActivate = closest("[data-admin-activate]")
        if (btnActivate != null) {
            val userId = btnActivate.getAttribute("data-admin-activate") ?: return@addEventListener
            val currentlyActive = btnActivate.getAttribute("data-admin-active") == "true"
            setActive(userId, !currentlyActive) { err ->
                if (err != null) updateState { networkError = err }
                else getUsers { list -> updateState { adminHomeUsers = list } }
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val btnAssign = closest("[data-admin-assign]")
        if (btnAssign != null) {
            val id = btnAssign.getAttribute("data-admin-assign") ?: return@addEventListener
            updateState { adminAssignInstructorId = id }
            window.setTimeout({
                document.getElementById("sd-assign-panel")?.scrollIntoView(js("({ block: 'start', behavior: 'smooth' })"))
            }, 150)
            e.preventDefault(); (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val btnAssignCadetFromCard = closest(".sd-admin-assign-cadet-btn")
        if (btnAssignCadetFromCard != null) {
            val cadetId = btnAssignCadetFromCard.getAttribute("data-admin-assign-cadet") ?: return@addEventListener
            updateState { adminAssignCadetId = cadetId; adminAssignInstructorId = null }
            window.setTimeout({
                document.getElementById("sd-assign-panel")?.scrollIntoView(js("({ block: 'start', behavior: 'smooth' })"))
            }, 150)
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val btnAssignCadet = closest("[data-admin-assign-instructor]")
        if (btnAssignCadet != null) {
            val instId = btnAssignCadet.getAttribute("data-admin-assign-instructor") ?: return@addEventListener
            val cadetId = btnAssignCadet.getAttribute("data-admin-assign-cadet") ?: return@addEventListener
            assignCadetToInstructor(instId, cadetId) { err ->
                if (err != null) updateState { networkError = err }
                else getUsers { list -> updateState { adminHomeUsers = list; adminAssignCadetId = null } }
            }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val btnUnlink = closest("[data-admin-unlink-instructor]")
        if (btnUnlink != null) {
            val instId = btnUnlink.getAttribute("data-admin-unlink-instructor") ?: return@addEventListener
            val cadetId = btnUnlink.getAttribute("data-admin-unlink-cadet") ?: return@addEventListener
            removeCadetFromInstructor(instId, cadetId) { err ->
                if (err != null) updateState { networkError = err }
                else getUsers { list -> updateState { adminHomeUsers = list } }
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val btnDelete = closest("[data-admin-delete]")
        if (btnDelete != null) {
            val userId = btnDelete.getAttribute("data-admin-delete") ?: return@addEventListener
            if (!window.confirm("Удалить пользователя из базы? Это не удалит аккаунт Firebase Auth.")) return@addEventListener
            deleteUser(userId) { err ->
                if (err != null) updateState { networkError = err }
                else getUsers { list -> updateState { adminHomeUsers = list } }
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val btnBalanceSelect = closest("[data-balance-select]")
        if (btnBalanceSelect != null) {
            val id = btnBalanceSelect.getAttribute("data-balance-select") ?: return@addEventListener
            updateState { balanceAdminSelectedUserId = id }
            window.setTimeout({
                document.getElementById("sd-balance-selected-block")?.scrollIntoView(js("({ block: 'start', behavior: 'smooth' })"))
            }, 150)
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val chatContact = closest(".sd-chat-contact")
        if (chatContact != null) {
            val contactId = chatContact.getAttribute("data-contact-id") ?: return@addEventListener
            val uid = appState.user?.id ?: return@addEventListener
            saveChatScrollForCurrentContact()
            updateState { selectedChatContactId = contactId; chatMessages = emptyList() }
            unsubscribeChat()
            subscribeMessages(chatRoomId(uid, contactId)) { list ->
                updateState { chatMessages = list }
                window.setTimeout({ applyScrollForChat(contactId) }, 100)
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val adminOpenChat = closest(".sd-admin-open-chat")
        if (adminOpenChat != null) {
            val contactId = adminOpenChat.getAttribute("data-contact-id") ?: return@addEventListener
            val uid = appState.user?.id ?: return@addEventListener
            saveChatScrollForCurrentContact()
            updateState { selectedTabIndex = 2; selectedChatContactId = contactId; chatMessages = emptyList() }
            unsubscribeChat()
            subscribeMessages(chatRoomId(uid, contactId)) { list ->
                updateState { chatMessages = list }
                window.setTimeout({ applyScrollForChat(contactId) }, 100)
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val cadetChatBtn = closest(".sd-cadet-chat-btn")
        if (cadetChatBtn != null) {
            val contactId = cadetChatBtn.getAttribute("data-contact-id") ?: return@addEventListener
            val uid = appState.user?.id ?: return@addEventListener
            saveChatScrollForCurrentContact()
            updateState { selectedTabIndex = 2; selectedChatContactId = contactId; chatMessages = emptyList() }
            unsubscribeChat()
            subscribeMessages(chatRoomId(uid, contactId)) { list ->
                updateState { chatMessages = list }
                window.setTimeout({ applyScrollForChat(contactId) }, 100)
            }
            e.preventDefault(); e.stopPropagation()
        }
    }, true)
}

private fun attachListeners(root: org.w3c.dom.Element) {
            document.getElementById("sd-dismiss-network-error")?.addEventListener("click", {
        updateState { networkError = null }
    })
    document.getElementById("sd-stop-loading")?.addEventListener("click", {
        updateState { recordingLoading = false }
    })
    document.getElementById("sd-stop-history-loading")?.addEventListener("click", {
        updateState { historyLoading = false }
    })
    document.getElementById("sd-stop-balance-loading")?.addEventListener("click", {
        updateState { balanceAdminLoading = false }
    })
    document.getElementById("sd-chat-stop-loading")?.addEventListener("click", {
        updateState { chatContactsLoading = false }
    })
    document.getElementById("sd-chat-refresh")?.addEventListener("click", {
        val u = appState.user ?: return@addEventListener
        updateState { chatContacts = emptyList(); chatContactsLoading = true }
        getUsersForChat(u) { list ->
            updateState { chatContacts = list; chatContactsLoading = false }
            subscribeChatPresence(list.map { it.id })
        }
    })
    document.getElementById("sd-admin-home-load")?.addEventListener("click", {
        updateState { adminHomeLoading = true; networkError = null }
        val tid = window.setTimeout({ updateState { adminHomeLoading = false } }, 8000)
        getUsersWithError { list, err ->
            window.clearTimeout(tid)
            updateState {
                adminHomeUsers = list; balanceAdminUsers = list; adminHomeLoading = false
                if (err != null) networkError = err
            }
        }
    })
    document.getElementById("sd-balance-load")?.addEventListener("click", {
        updateState { balanceAdminLoading = true }
        val tid = window.setTimeout({ updateState { balanceAdminLoading = false } }, 8000)
        getUsers { list ->
            loadBalanceHistoryForUsers(list.map { it.id }) { hist ->
                window.clearTimeout(tid)
                updateState { balanceAdminUsers = list; adminHomeUsers = list; balanceAdminHistory = hist; balanceAdminLoading = false }
            }
        }
    })
    document.getElementById("sd-admin-assign-cancel")?.addEventListener("click", {
        updateState { adminAssignInstructorId = null; adminAssignCadetId = null }
    })
    when (appState.screen) {
        AppScreen.Login -> {
            document.getElementById("sd-btn-signin")?.addEventListener("click", {
                val email = ((document.getElementById("sd-email") as? HTMLInputElement)?.value ?: "").trim()
                val password = (document.getElementById("sd-password") as? HTMLInputElement)?.value ?: ""
                if (email.isBlank() || password.isBlank()) {
                    updateState { error = "Введите email и пароль" }
                    return@addEventListener
                }
                updateState { loading = true; error = null }
                signIn(email, password)
                    .then {
                        launchConfetti()
                        updateState { loading = false }
                    }
                    .catch { e ->
                        val code = e.asDynamic().code as? String
                        val rawMsg = (e.asDynamic().message as? String) ?: ""
                        val msg = when (code) {
                            "auth/invalid-credential", "auth/wrong-password", "auth/user-not-found" ->
                                "Неверный email или пароль. Используйте учётную запись из приложения или зарегистрируйтесь."
                            "auth/invalid-email" -> "Некорректный email."
                            "auth/too-many-requests" -> "Слишком много попыток. Подождите и попробуйте снова."
                            "auth/network-request-failed" -> "Нет соединения с интернетом."
                            else -> rawMsg.ifBlank { "Ошибка входа" }
                        }
                        if (code == "auth/network-request-failed") updateState { networkError = "Нет соединения с интернетом." }
                        updateState { loading = false; error = msg }
                    }
            })
            document.getElementById("sd-btn-register")?.addEventListener("click", {
                updateState { screen = AppScreen.Register; error = null }
            })
        }
        AppScreen.Register -> {
            document.getElementById("sd-btn-do-register")?.addEventListener("click", {
                val fullName = ((document.getElementById("sd-fullName") as? HTMLInputElement)?.value ?: "").trim()
                val email = ((document.getElementById("sd-reg-email") as? HTMLInputElement)?.value ?: "").trim()
                val phone = ((document.getElementById("sd-phone") as? HTMLInputElement)?.value ?: "").trim()
                val password = (document.getElementById("sd-reg-password") as? HTMLInputElement)?.value ?: ""
                val role = (document.getElementById("sd-role") as? HTMLSelectElement)?.value ?: "cadet"
                if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                    updateState { error = "Заполните обязательные поля" }
                    return@addEventListener
                }
                if (password.length < 6) {
                    updateState { error = "Пароль должен быть не короче 6 символов" }
                    return@addEventListener
                }
                updateState { loading = true; error = null }
                register(fullName, email, phone, password, role)
                    .then {
                        updateState { loading = false }
                    }
                    .catch { e ->
                        val code = e.asDynamic().code as? String
                        val rawMsg = (e.asDynamic().message as? String) ?: ""
                        val msg = when (code) {
                            "auth/email-already-in-use" -> "Этот email уже зарегистрирован. Войдите или восстановите пароль."
                            "auth/invalid-email" -> "Некорректный email."
                            "auth/weak-password" -> "Пароль слишком простой (минимум 6 символов)."
                            "auth/network-request-failed" -> "Нет соединения с интернетом."
                            else -> rawMsg.ifBlank { "Ошибка регистрации" }
                        }
                        if (code == "auth/network-request-failed") updateState { networkError = "Нет соединения с интернетом." }
                        updateState { loading = false; error = msg }
                    }
            })
            document.getElementById("sd-btn-back")?.addEventListener("click", {
                updateState { screen = AppScreen.Login; error = null }
            })
        }
        AppScreen.PendingApproval -> {
            document.getElementById("sd-btn-check")?.addEventListener("click", {
                val uid = getCurrentUserId() ?: return@addEventListener
                updateState { loading = true }
                getCurrentUser { user, _ ->
                    updateState { loading = false }
                    if (user != null && user.isActive) {
                        updateState {
                            this.user = user
                            screen = when (user.role) {
                                "admin" -> AppScreen.Admin
                                "instructor" -> AppScreen.Instructor
                                "cadet" -> AppScreen.Cadet
                                else -> AppScreen.PendingApproval
                            }
                        }
                    }
                }
            })
            document.getElementById("sd-btn-signout-pending")?.addEventListener("click", {
                signOutAndClearPresence()
            })
        }
        AppScreen.ProfileNotFound -> {
            document.getElementById("sd-btn-signout-profile-not-found")?.addEventListener("click", {
                signOutAndClearPresence()
            })
        }
        AppScreen.Admin, AppScreen.Instructor, AppScreen.Cadet -> {
            val uid = appState.user?.id
            if (appState.selectedTabIndex != 2) {
                unsubscribeChat()
            }
            window.setTimeout({
                if (appState.selectedTabIndex == 2 && uid != null) {
                    if (appState.chatContacts.isEmpty() && !appState.chatContactsLoading) {
                        updateState { chatContactsLoading = true }
                        val chatTid = window.setTimeout({ updateState { chatContactsLoading = false } }, 5000)
                        getUsersForChat(appState.user!!) { list ->
                            window.clearTimeout(chatTid)
                            updateState { chatContacts = list; chatContactsLoading = false }
                            subscribeChatPresence(list.map { it.id })
                        }
                    }
                }
                val usr = appState.user ?: return@setTimeout
                when {
                (usr.role == "instructor" && (appState.selectedTabIndex == 0 || appState.selectedTabIndex == 1)) -> {
                    if (!appState.recordingLoading && appState.recordingSessions.isEmpty() && appState.recordingOpenWindows.isEmpty()) {
                        updateState { recordingLoading = true }
                        val tid = window.setTimeout({ updateState { recordingLoading = false } }, 8000)
                        getOpenWindowsForInstructor(usr.id) { wins ->
                            getSessionsForInstructor(usr.id) { sess ->
                                window.clearTimeout(tid)
                                updateState { recordingOpenWindows = wins; recordingSessions = sess; recordingLoading = false }
                                getUsers { list -> updateState { instructorCadets = list.filter { usr.assignedCadets.contains(it.id) } } }
                            }
                        }
                    }
                    if (appState.instructorCadets.isEmpty()) {
                        getCurrentUser { freshUser, _ ->
                            val ids = freshUser?.assignedCadets ?: usr.assignedCadets
                            getUsers { list -> updateState { instructorCadets = list.filter { ids.contains(it.id) }; if (freshUser != null) this.user = freshUser } }
                        }
                    }
                }
                (usr.role == "cadet" && (appState.selectedTabIndex == 0 || appState.selectedTabIndex == 1)) -> {
                    if (!appState.recordingLoading && appState.recordingSessions.isEmpty()) {
                        updateState { recordingLoading = true }
                        val tid = window.setTimeout({ updateState { recordingLoading = false } }, 8000)
                        val instId = usr.assignedInstructorId ?: ""
                        getOpenWindowsForCadet(instId) { wins ->
                            getSessionsForCadet(usr.id) { sess ->
                                window.clearTimeout(tid)
                                updateState { recordingOpenWindows = wins; recordingSessions = sess; recordingLoading = false }
                                usr.assignedInstructorId?.let { id -> getUserById(id) { inst -> updateState { cadetInstructor = inst } } }
                            }
                        }
                    }
                }
                usr.role == "instructor" && appState.selectedTabIndex == 4 -> {
                    if (!appState.historyLoading && appState.historySessions.isEmpty()) {
                        updateState { historyLoading = true }
                        val tid = window.setTimeout({ updateState { historyLoading = false } }, 8000)
                        getSessionsForInstructor(usr.id) { sess ->
                            getBalanceHistory(usr.id) { hist ->
                                window.clearTimeout(tid)
                                updateState { historySessions = sess; historyBalance = hist; historyLoading = false }
                            }
                        }
                    }
                }
                usr.role == "cadet" && appState.selectedTabIndex == 4 -> {
                    if (!appState.historyLoading && appState.historySessions.isEmpty()) {
                        updateState { historyLoading = true }
                        val tid = window.setTimeout({ updateState { historyLoading = false } }, 8000)
                        getSessionsForCadet(usr.id) { sess ->
                            getBalanceHistory(usr.id) { hist ->
                                window.clearTimeout(tid)
                                updateState { historySessions = sess; historyBalance = hist; historyLoading = false }
                            }
                        }
                    }
                }
                usr.role == "admin" && appState.selectedTabIndex == 3 -> {
                    if (!appState.historyLoading && appState.historyBalance.isEmpty()) {
                        updateState { historyLoading = true }
                        val tid = window.setTimeout({ updateState { historyLoading = false } }, 8000)
                        getUsers { list ->
                            loadBalanceHistoryForUsers(list.map { it.id }) { hist ->
                                window.clearTimeout(tid)
                                updateState { balanceAdminUsers = list; historyBalance = hist; historyLoading = false }
                            }
                        }
                    }
                }
                usr.role == "admin" && appState.selectedTabIndex == 0 -> {
                    if (!appState.adminHomeLoading) {
                        updateState { adminHomeLoading = true; networkError = null }
                        val tid = window.setTimeout({ updateState { adminHomeLoading = false } }, 8000)
                        getUsersWithError { list, err ->
                            window.clearTimeout(tid)
                            updateState {
                                adminHomeUsers = list; balanceAdminUsers = list; adminHomeLoading = false
                                if (err != null) networkError = err
                            }
                        }
                    }
                }
                usr.role == "admin" && appState.selectedTabIndex == 1 -> {
                    if (!appState.balanceAdminLoading && appState.balanceAdminUsers.isEmpty()) {
                        updateState { balanceAdminLoading = true }
                        val tid = window.setTimeout({ updateState { balanceAdminLoading = false } }, 8000)
                        getUsers { list ->
                            loadBalanceHistoryForUsers(list.map { it.id }) { hist ->
                                window.clearTimeout(tid)
                                updateState { balanceAdminUsers = list; adminHomeUsers = list; balanceAdminHistory = hist; balanceAdminLoading = false }
                            }
                        }
                    }
                }
                }
            }, 0)
            val u = appState.user
            u?.let { usr ->
                document.getElementById("sd-btn-signout")?.addEventListener("click", {
                signOutAndClearPresence()
            })
            if (usr.role == "instructor") {
                val homeConfirmNodes = root.querySelectorAll(".sd-home-schedule-confirm")
                for (k in 0 until homeConfirmNodes.length) {
                    val btn = homeConfirmNodes.item(k) as? org.w3c.dom.Element ?: continue
                    val sessionId = btn.getAttribute("data-session-id") ?: continue
                    btn.addEventListener("click", {
                        confirmBookingByInstructor(sessionId) { err ->
                            if (err != null) updateState { networkError = err }
                            else getSessionsForInstructor(usr.id) { sess -> updateState { recordingSessions = sess } }
                        }
                    })
                }
                val homeStartNodes = root.querySelectorAll(".sd-home-schedule-start")
                val startThresholdMs = START_ALLOWED_MINUTES_BEFORE * 60 * 1000
                for (k in 0 until homeStartNodes.length) {
                    val btn = homeStartNodes.item(k) as? org.w3c.dom.Element ?: continue
                    val sessionId = btn.getAttribute("data-session-id") ?: continue
                    val startMsStr = btn.getAttribute("data-start-ms")
                    btn.addEventListener("click", {
                        val startMs = startMsStr?.toLongOrNull() ?: 0L
                        val now = js("Date.now()").unsafeCast<Double>().toLong()
                        if (now < startMs - startThresholdMs) {
                            showToast("Ещё рано! Кнопка активируется за 15 мин. до вождения.")
                            return@addEventListener
                        }
                        requestStartByInstructor(sessionId) { err ->
                            if (err != null) updateState { networkError = err }
                            else getSessionsForInstructor(usr.id) { sess -> updateState { recordingSessions = sess } }
                        }
                    })
                }
                val homeLateNodes = root.querySelectorAll(".sd-home-schedule-late")
                for (k in 0 until homeLateNodes.length) {
                    val btn = homeLateNodes.item(k) as? org.w3c.dom.Element ?: continue
                    val sessionId = btn.getAttribute("data-session-id") ?: continue
                    btn.addEventListener("click", {
                        val modal = document.getElementById("sd-running-late-modal") ?: return@addEventListener
                        modal.asDynamic().dataset["sessionId"] = sessionId
                        (root.querySelector("input[name=\"sd-late-mins\"][value=\"5\"]")?.asDynamic())?.checked = true
                        modal.classList.remove("sd-hidden")
                    })
                }
                document.getElementById("sd-running-late-confirm")?.addEventListener("click", {
                    val modal = document.getElementById("sd-running-late-modal") ?: return@addEventListener
                    val sessionId = modal.asDynamic().dataset["sessionId"] as? String ?: return@addEventListener
                    val checkedEl = root.querySelector("input[name=\"sd-late-mins\"]:checked")?.asDynamic()
                    val delay = (checkedEl?.value as? String)?.toIntOrNull() ?: run { showToast("Выберите задержку"); return@addEventListener }
                    setInstructorRunningLate(sessionId, delay) { err ->
                        if (err != null) updateState { networkError = err }
                        else {
                            getSessionsForInstructor(usr.id) { sess -> updateState { recordingSessions = sess } }
                            modal.classList.add("sd-hidden")
                        }
                    }
                })
                document.getElementById("sd-running-late-cancel")?.addEventListener("click", {
                    document.getElementById("sd-running-late-modal")?.classList?.add("sd-hidden")
                })
                val homeCancelNodes = root.querySelectorAll(".sd-home-schedule-cancel")
                for (k in 0 until homeCancelNodes.length) {
                    val btn = homeCancelNodes.item(k) as? org.w3c.dom.Element ?: continue
                    val sessionId = btn.getAttribute("data-session-id") ?: continue
                    btn.addEventListener("click", {
                        if (!window.confirm("Вы уверены, что хотите отменить вождение?")) return@addEventListener
                        cancelByInstructor(sessionId) { err ->
                            if (err != null) updateState { networkError = err }
                            else getOpenWindowsForInstructor(usr.id) { wins ->
                                getSessionsForInstructor(usr.id) { sess ->
                                    updateState { recordingOpenWindows = wins; recordingSessions = sess }
                                }
                            }
                        }
                    })
                }
            }
            document.getElementById("sd-chat-back")?.addEventListener("click", {
                try { (voiceRecorder.asDynamic()).stop() } catch (_: Throwable) { }
                val stream = voiceRecorderStream
                if (stream != null) {
                    js("(function(s){ if(s&&s.getTracks) s.getTracks().forEach(function(t){ t.stop(); }); })").unsafeCast<(dynamic) -> Unit>().invoke(stream)
                }
                if (voiceRecordInterval != 0) { window.clearInterval(voiceRecordInterval); voiceRecordInterval = 0 }
                if (voicePlayInterval != 0) { window.clearInterval(voicePlayInterval); voicePlayInterval = 0 }
                saveChatScrollForCurrentContact()
                updateState { selectedChatContactId = null; chatMessages = emptyList(); chatVoiceRecording = false; chatPlayingVoiceId = null }
                unsubscribeChat()
            })
            val chatInput = document.getElementById("sd-chat-input") as? HTMLInputElement
            document.getElementById("sd-chat-send")?.addEventListener("click", {
                sendChatMessage(chatInput, uid)
            })
            chatInput?.addEventListener("keypress", { e: dynamic ->
                if (e?.key == "Enter") sendChatMessage(chatInput, uid)
            })
            document.getElementById("sd-chat-voice-mic")?.addEventListener("click", {
                startVoiceRecording(uid)
            })
            document.getElementById("sd-chat-voice-stop")?.addEventListener("click", {
                stopVoiceRecordingAndSend(uid)
            })
            if (document.getElementById("sd-chat-voice-recording") != null) {
                if (voiceRecordInterval != 0) window.clearInterval(voiceRecordInterval)
                voiceRecordInterval = window.setInterval({
                    val start = appState.chatVoiceRecordStartMs
                    if (start > 0) updateState { chatVoiceRecordElapsedSec = ((js("Date.now()").unsafeCast<Double>() - start) / 1000.0).toInt().coerceAtLeast(0) }
                }, 1000).unsafeCast<Int>()
            }
            val playBtns = root.querySelectorAll(".sd-voice-play-btn")
            for (i in 0 until playBtns.length) {
                val btn = playBtns.item(i) as? org.w3c.dom.Element ?: continue
                val msgEl = (btn.asDynamic().closest(".sd-msg-voice")) as? org.w3c.dom.Element ?: continue
                val msgId = msgEl.getAttribute("data-voice-id") ?: continue
                val voiceUrl = msgEl.getAttribute("data-voice-url") ?: continue
                val durationSec = (msgEl.getAttribute("data-voice-duration") ?: "0").toIntOrNull() ?: 0
                val audioEl = document.getElementById("sd-voice-audio-$msgId")
                if (audioEl != null) {
                    btn.addEventListener("click", {
                        toggleVoicePlay(audioEl.asDynamic(), msgId, voiceUrl, durationSec)
                    })
                }
            }
            val progressWraps = root.querySelectorAll(".sd-voice-progress-wrap")
            for (i in 0 until progressWraps.length) {
                val wrap = progressWraps.item(i) as? org.w3c.dom.Element ?: continue
                val msgEl = (wrap.asDynamic().closest(".sd-msg-voice")) as? org.w3c.dom.Element ?: continue
                val msgId = msgEl.getAttribute("data-voice-id") ?: continue
                val voiceUrl = msgEl.getAttribute("data-voice-url") ?: continue
                val durationSec = (msgEl.getAttribute("data-voice-duration") ?: "0").toIntOrNull() ?: 0
                attachVoiceProgressDrag(wrap, msgId, voiceUrl, durationSec)
            }
            val playingId = appState.chatPlayingVoiceId
            if (playingId != null && voicePlayInterval == 0) {
                val durAttr = root.querySelector(".sd-msg-voice[data-voice-id=\"$playingId\"]")?.getAttribute("data-voice-duration") ?: "0"
                val durationSec = durAttr.toIntOrNull() ?: 0
                voicePlayInterval = window.setInterval({
                    val a = document.getElementById("sd-global-voice-audio")?.asDynamic()
                    if (a != null && a.paused == false) {
                        appState.chatPlayingVoiceCurrentMs = ((a.currentTime as Double) * 1000).toInt()
                        patchVoicePlayerDOM()
                    } else if (a != null && a.ended == true) {
                        window.clearInterval(voicePlayInterval)
                        voicePlayInterval = 0
                        appState.chatPlayingVoiceId = null
                        appState.chatPlayingVoiceCurrentMs = durationSec * 1000
                        patchVoicePlayerDOM()
                    }
                }, 200).unsafeCast<Int>()
            }
            document.getElementById("sd-recording-book-dt")?.setAttribute("min", getDatetimeLocalMin())
            document.getElementById("sd-recording-add-dt")?.setAttribute("min", getDatetimeLocalMin())
            document.getElementById("sd-recording-add-btn")?.addEventListener("click", {
                val input = document.getElementById("sd-recording-add-dt") as? HTMLInputElement
                val v = input?.value ?: ""
                if (v.isBlank()) { updateState { networkError = "Укажите дату и время" }; return@addEventListener }
                val dateFn = js("function(s){ return new Date(s).getTime(); }").unsafeCast<(String) -> Number>()
                val ms = dateFn(v).toLong()
                if (ms <= 0) return@addEventListener
                val nowMs = js("Date.now()").unsafeCast<Double>().toLong()
                if (ms < nowMs) { showToast("Нельзя выбрать прошедшую дату и время"); return@addEventListener }
                val occupied = findOccupiedMessage(ms, appState.recordingSessions, appState.recordingOpenWindows, appState.instructorCadets)
                if (occupied != null) { showToast(occupied); return@addEventListener }
                addOpenWindow(usr.id, ms) { _, err ->
                    if (err != null) updateState { networkError = err }
                    else {
                        getOpenWindowsForInstructor(usr.id) { wins ->
                            getSessionsForInstructor(usr.id) { sess ->
                                updateState { recordingOpenWindows = wins; recordingSessions = sess; networkError = null }
                            }
                        }
                        input?.value = ""
                    }
                }
            })
            document.getElementById("sd-recording-book-btn")?.addEventListener("click", {
                val cadetSelect = document.getElementById("sd-recording-book-cadet") as? HTMLSelectElement
                val cadetId = cadetSelect?.value?.takeIf { it.isNotBlank() } ?: run { updateState { networkError = "Выберите курсанта" }; return@addEventListener }
                val input = document.getElementById("sd-recording-book-dt") as? HTMLInputElement
                val v = input?.value ?: ""
                if (v.isBlank()) { updateState { networkError = "Укажите дату и время" }; return@addEventListener }
                val dateFn = js("function(s){ return new Date(s).getTime(); }").unsafeCast<(String) -> Number>()
                val ms = dateFn(v).toLong()
                if (ms <= 0) return@addEventListener
                val nowMs = js("Date.now()").unsafeCast<Double>().toLong()
                if (ms < nowMs) { showToast("Нельзя выбрать прошедшую дату и время"); return@addEventListener }
                val occupied = findOccupiedMessage(ms, appState.recordingSessions, appState.recordingOpenWindows, appState.instructorCadets)
                if (occupied != null) { showToast(occupied); return@addEventListener }
                val cadet = appState.instructorCadets.find { it.id == cadetId }
                if (cadet == null) { updateState { networkError = "Курсант не найден" }; return@addEventListener }
                if (cadet.balance <= 0) { updateState { networkError = "У курсанта 0 талонов, запись невозможна" }; return@addEventListener }
                val scheduledCount = appState.recordingSessions.count { it.cadetId == cadetId && (it.status == "scheduled" || it.status == "inProgress") }
                if (scheduledCount >= cadet.balance) { updateState { networkError = "По балансу курсанта уже запланировано макс. вождений" }; return@addEventListener }
                createSession(usr.id, cadetId, ms, null) { _, err ->
                    if (err != null) updateState { networkError = err }
                    else {
                        getSessionsForInstructor(usr.id) { sess ->
                            updateState { recordingSessions = sess; networkError = null }
                        }
                        input?.value = ""
                    }
                }
            })
            val confirmSessionNodes = root.querySelectorAll(".sd-recording-confirm-session")
            for (k in 0 until confirmSessionNodes.length) {
                val btn = confirmSessionNodes.item(k) as? org.w3c.dom.Element ?: continue
                val sessionId = btn.getAttribute("data-session-id") ?: continue
                btn.addEventListener("click", {
                    confirmBookingByInstructor(sessionId) { err ->
                        if (err != null) updateState { networkError = err }
                        else getSessionsForInstructor(usr.id) { sess -> updateState { recordingSessions = sess } }
                    }
                })
            }
            val cancelSessionNodes = root.querySelectorAll(".sd-recording-cancel-session")
            for (k in 0 until cancelSessionNodes.length) {
                val btn = cancelSessionNodes.item(k) as? org.w3c.dom.Element ?: continue
                val sessionId = btn.getAttribute("data-session-id") ?: continue
                btn.addEventListener("click", {
                    if (!window.confirm("Вы уверены, что хотите отменить вождение?")) return@addEventListener
                    cancelByInstructor(sessionId) { err ->
                        if (err != null) updateState { networkError = err }
                        else getOpenWindowsForInstructor(usr.id) { wins ->
                            getSessionsForInstructor(usr.id) { sess ->
                                updateState { recordingOpenWindows = wins; recordingSessions = sess }
                            }
                        }
                    }
                })
            }
            val delNodes = root.querySelectorAll(".sd-btn-delete[data-window-id]")
            for (k in 0 until delNodes.length) {
                val btn = delNodes.item(k) as? org.w3c.dom.Element ?: continue
                btn.addEventListener("click", {
                    val wid = btn.getAttribute("data-window-id") ?: return@addEventListener
                    deleteOpenWindow(wid) { err ->
                        if (err != null) updateState { networkError = err }
                        else getOpenWindowsForInstructor(usr.id) { wins ->
                            updateState { recordingOpenWindows = wins }
                        }
                    }
                })
            }
            val bookNodes = root.querySelectorAll(".sd-list .sd-btn-small[data-window-id]")
            for (k in 0 until bookNodes.length) {
                val btn = bookNodes.item(k) as? org.w3c.dom.Element ?: continue
                val wid = btn.getAttribute("data-window-id") ?: continue
                btn.addEventListener("click", {
                    bookWindow(wid, usr.id) { err ->
                        if (err != null) updateState { networkError = err }
                        else {
                            val instId = usr.assignedInstructorId ?: ""
                            getOpenWindowsForCadet(instId) { wins ->
                                getSessionsForCadet(usr.id) { sess ->
                                    updateState { recordingOpenWindows = wins; recordingSessions = sess }
                                }
                            }
                        }
                    }
                })
            }
            document.getElementById("sd-balance-clear-selection")?.addEventListener("click", {
                updateState { balanceAdminSelectedUserId = null }
            })
            fun doBalanceOp(type: String) {
                val targetId = appState.balanceAdminSelectedUserId ?: return
                val input = document.getElementById("sd-balance-amount") as? HTMLInputElement
                val amount = (input?.value ?: "0").toIntOrNull() ?: 0
                if (amount < 0) return
                updateBalance(targetId, type, amount, usr.id) { err ->
                    if (err != null) updateState { networkError = err }
                    else getUsers { list ->
                        loadBalanceHistoryForUsers(list.map { it.id }) { hist ->
                            updateState { balanceAdminUsers = list; balanceAdminHistory = hist }
                        }
                    }
                }
            }
            document.getElementById("sd-balance-credit")?.addEventListener("click", { doBalanceOp("credit") })
            document.getElementById("sd-balance-debit")?.addEventListener("click", { doBalanceOp("debit") })
            document.getElementById("sd-balance-set")?.addEventListener("click", { doBalanceOp("set") })
            document.getElementById("sd-settings-save")?.addEventListener("click", {
                val fullName = ((document.getElementById("sd-settings-fullName") as? HTMLInputElement)?.value ?: "").trim()
                val phone = ((document.getElementById("sd-settings-phone") as? HTMLInputElement)?.value ?: "").trim()
                updateProfile(usr.id, fullName, phone) { err ->
                    if (err != null) updateState { networkError = err }
                    else getCurrentUser { newUser, _ ->
                        if (newUser != null) updateState { user = newUser }
                    }
                }
            })
            document.getElementById("sd-settings-password")?.addEventListener("click", {
                val newPass = (document.getElementById("sd-settings-newpassword") as? HTMLInputElement)?.value ?: ""
                if (newPass.length < 6) { updateState { networkError = "Пароль не менее 6 символов" }; return@addEventListener }
                changePassword(newPass)
                    .then { updateState { networkError = null }; (document.getElementById("sd-settings-newpassword") as? HTMLInputElement)?.value = "" }
                    .catch { e -> updateState { networkError = (e.asDynamic().message as? String) ?: "Ошибка смены пароля" } }
            })
            }
        }
    }

    root.querySelectorAll(".sd-ticket-category-card[data-pdd-category]").let { nodeList ->
        for (k in 0 until nodeList.length) {
            val el = nodeList.item(k) as? org.w3c.dom.Element ?: continue
            val catId = el.getAttribute("data-pdd-category") ?: continue
            el.addEventListener("click", {
                if (appState.user?.role == "instructor" || appState.user?.role == "cadet") {
                    handlePddCategoryClick(catId)
                }
            })
        }
    }
}

private fun sendChatMessage(chatInput: HTMLInputElement?, uid: String?) {
    val text = (chatInput?.value ?: "").trim()
    if (text.isBlank() || uid == null) return
    val contactId = appState.selectedChatContactId ?: return
    val roomId = chatRoomId(uid, contactId)
    sendMessage(roomId, uid, text)
        .then {
            chatInput?.value = ""
        }
        .catch { _ ->
            updateState { networkError = "Не удалось отправить сообщение." }
        }
}

private fun startVoiceRecording(uid: String?) {
    if (uid == null || appState.selectedChatContactId == null) return
    val nav = js("navigator").unsafeCast<dynamic>()
    val mediaDevices = nav.mediaDevices
    if (mediaDevices == null || mediaDevices.getUserMedia == null) {
        updateState { networkError = "Нужен доступ к микрофону для голосовых сообщений." }
        return
    }
    voiceRecorderChunks = js("[]")
    val constraints = js("({ audio: true })")
    mediaDevices.getUserMedia(constraints).then { stream: dynamic ->
        voiceRecorderStream = stream
        val Recorder = js("window.MediaRecorder")
        if (Recorder == undefined) {
            updateState { networkError = "MediaRecorder не поддерживается в этом браузере." }
            js("(function(s){ if(s&&s.getTracks) s.getTracks().forEach(function(t){ t.stop(); }); })").unsafeCast<(dynamic) -> Unit>().invoke(stream)
            return@then
        }
        val mime = js("(function(){ if(typeof MediaRecorder!=='undefined'&&MediaRecorder.isTypeSupported&&MediaRecorder.isTypeSupported('audio/webm;codecs=opus')) return 'audio/webm;codecs=opus'; if(typeof MediaRecorder!=='undefined'&&MediaRecorder.isTypeSupported&&MediaRecorder.isTypeSupported('audio/webm')) return 'audio/webm'; return 'audio/webm'; })()").unsafeCast<String>()
        voiceRecorderMimeType = mime
        val opts = js("({ mimeType: mime })")
        val recorder = js("new MediaRecorder(stream, opts)").unsafeCast<dynamic>()
        window.asDynamic().__sdVoiceChunks = voiceRecorderChunks
        recorder.ondataavailable = { e: dynamic ->
            val data = e?.data
            if (data != null) {
                window.asDynamic().__sdVoiceChunkData = data
                js("(function(){ var a=window.__sdVoiceChunks,d=window.__sdVoiceChunkData; if(a&&d){ a.push(d); } })()")
            }
        }
        recorder.onstop = { _: dynamic ->
            val chunks = voiceRecorderChunks
            val mimeType = voiceRecorderMimeType
            val stopStream = voiceRecorderStream
            js("(function(s){ if(s&&s.getTracks) s.getTracks().forEach(function(t){ t.stop(); }); })").unsafeCast<(dynamic) -> Unit>().invoke(stopStream)
            voiceRecorderStream = null
            voiceRecorder = null
            voiceRecorderChunks = null
            if (voiceRecordInterval != 0) { window.clearInterval(voiceRecordInterval); voiceRecordInterval = 0 }
            updateState { chatVoiceRecording = false; chatVoiceRecordElapsedSec = 0 }
            if (chunks != null) {
                val blob = js("(function(c,t){ return new Blob(c, { type: t || 'audio/webm' }); })").unsafeCast<(dynamic, String) -> dynamic>().invoke(chunks, mimeType)
                val roomId = chatRoomId(uid, appState.selectedChatContactId!!)
                val startMs = appState.chatVoiceRecordStartMs
                val durationSec = ((js("Date.now()").unsafeCast<Double>() - startMs) / 1000.0).toInt().coerceAtLeast(1)
                sendVoiceMessage(roomId, uid, blob, durationSec)
                    .then {
                        subscribeMessages(roomId) { list -> updateState { chatMessages = list } }
                        window.setTimeout({
                            (document.getElementById("sd-chat-messages")?.lastElementChild?.unsafeCast<dynamic>())?.scrollIntoView(js("({ block: 'end', behavior: 'smooth' })"))
                        }, 100)
                    }
                    .catch { e: dynamic ->
                        updateState { networkError = "Не удалось отправить голосовое: ${(e?.message as? String) ?: "ошибка"}" }
                    }
            }
        }
        recorder.start(1000)
        voiceRecorder = recorder
        updateState { chatVoiceRecording = true; chatVoiceRecordStartMs = js("Date.now()").unsafeCast<Double>(); chatVoiceRecordElapsedSec = 0 }
    }.catch { _: dynamic ->
        updateState { networkError = "Нужен доступ к микрофону для голосовых сообщений." }
    }
}

private fun stopVoiceRecordingAndSend(uid: String?) {
    val rec = voiceRecorder
    if (rec != null) {
        try {
            if (js("rec.state").unsafeCast<String>() == "recording") rec.stop()
        } catch (_: Throwable) { }
    }
}

private fun getOrCreateGlobalVoiceAudio(): dynamic {
    var el = document.getElementById("sd-global-voice-audio")
    if (el == null) {
        el = document.createElement("audio")
        el.id = "sd-global-voice-audio"
        el.setAttribute("class", "sd-voice-audio")
        el.setAttribute("preload", "metadata")
        document.body?.appendChild(el)
    }
    return el.asDynamic()
}

private fun toggleVoicePlay(audioEl: dynamic, msgId: String, voiceUrl: String, durationSec: Int) {
    val globalAudio = getOrCreateGlobalVoiceAudio()
    val currentlyPlaying = appState.chatPlayingVoiceId
    if (currentlyPlaying == msgId) {
        globalAudio.pause()
        if (voicePlayInterval != 0) { window.clearInterval(voicePlayInterval); voicePlayInterval = 0 }
        appState.chatPlayingVoiceId = null
        appState.chatPlayingVoiceCurrentMs = 0
        patchVoicePlayerDOM()
        return
    }
    if (currentlyPlaying != null) {
        globalAudio.pause()
        if (voicePlayInterval != 0) { window.clearInterval(voicePlayInterval); voicePlayInterval = 0 }
    }
    if (voiceUrl.isBlank() || (!voiceUrl.startsWith("http") && !voiceUrl.startsWith("data:"))) return
    globalAudio.src = voiceUrl
    globalAudio.currentTime = 0.0
    val playPromise = globalAudio.play()
    if (playPromise != null) {
        js("(function(p){ if(p&&typeof p.catch==='function') p.catch(function(){}); })").unsafeCast<(dynamic) -> Unit>().invoke(playPromise)
    }
    appState.chatPlayingVoiceId = msgId
    appState.chatPlayingVoiceCurrentMs = 0
    patchVoicePlayerDOM()
    globalAudio.onended = {
        if (voicePlayInterval != 0) { window.clearInterval(voicePlayInterval); voicePlayInterval = 0 }
        appState.chatPlayingVoiceId = null
        appState.chatPlayingVoiceCurrentMs = durationSec * 1000
        patchVoicePlayerDOM()
    }
    voicePlayInterval = window.setInterval({
        val a = document.getElementById("sd-global-voice-audio")?.asDynamic()
        if (a != null && a.paused == false) {
            appState.chatPlayingVoiceCurrentMs = ((a.currentTime as Double) * 1000).toInt()
            patchVoicePlayerDOM()
        } else if (a != null && a.ended == true) {
            window.clearInterval(voicePlayInterval)
            voicePlayInterval = 0
            appState.chatPlayingVoiceId = null
            appState.chatPlayingVoiceCurrentMs = durationSec * 1000
            patchVoicePlayerDOM()
        }
    }, 200).unsafeCast<Int>()
}

private fun seekToVoicePosition(msgId: String, voiceUrl: String, durationSec: Int, ratio: Double) {
    val r = ratio.coerceIn(0.0, 1.0)
    val globalAudio = getOrCreateGlobalVoiceAudio()
    val currentMs = (r * durationSec * 1000).toInt()
    if (appState.chatPlayingVoiceId == msgId) {
        globalAudio.currentTime = r * durationSec
        appState.chatPlayingVoiceCurrentMs = currentMs
        patchVoicePlayerDOM()
    } else {
        if (voiceUrl.isBlank() || (!voiceUrl.startsWith("http") && !voiceUrl.startsWith("data:"))) return
        globalAudio.src = voiceUrl
        globalAudio.currentTime = r * durationSec
        appState.chatPlayingVoiceId = msgId
        appState.chatPlayingVoiceCurrentMs = currentMs
        patchVoicePlayerDOM()
    }
}

private fun attachVoiceProgressDrag(wrap: org.w3c.dom.Element, msgId: String, voiceUrl: String, durationSec: Int) {
    window.asDynamic().__sdSeekVoice = { a: String, b: String, c: Int, d: Double -> seekToVoicePosition(a, b, c, d) }
    val setupDrag = js("(function(wrap, msgId, voiceUrl, durationSec){ var getRatio=function(cx){ var r=wrap.getBoundingClientRect(); if(r.width<=0)return 0; var x=cx-r.left; return Math.max(0,Math.min(1,x/r.width)); }; var onDown=function(e){ e.preventDefault(); var cx=e.clientX!=null?e.clientX:e.touches[0].clientX; if(window.__sdSeekVoice) window.__sdSeekVoice(msgId,voiceUrl,durationSec,getRatio(cx)); var onMove=function(ev){ ev.preventDefault(); var x=ev.clientX!=null?ev.clientX:(ev.touches&&ev.touches[0]?ev.touches[0].clientX:0); if(window.__sdSeekVoice) window.__sdSeekVoice(msgId,voiceUrl,durationSec,getRatio(x)); }; var onUp=function(){ document.removeEventListener('mousemove',onMove); document.removeEventListener('mouseup',onUp); document.removeEventListener('touchmove',onMove); document.removeEventListener('touchend',onUp); }; document.addEventListener('mousemove',onMove); document.addEventListener('mouseup',onUp); document.addEventListener('touchmove',onMove); document.addEventListener('touchend',onUp); }; wrap.addEventListener('mousedown',onDown); wrap.addEventListener('touchstart',onDown,{passive:false}); })")
    setupDrag.unsafeCast<(dynamic, dynamic, dynamic, dynamic) -> Unit>().invoke(wrap, msgId, voiceUrl, durationSec)
}
