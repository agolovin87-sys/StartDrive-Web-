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
                sdCard.innerHTML = tabContent
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
        val contactInitials = contact.initials().ifEmpty { "?" }.escapeHtml()
        val contactAvatarBg = avatarColorForId(contact.id).escapeHtml()
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
            """<div class="$cls"><span class="sd-msg-text">${msg.text.escapeHtml()}</span><div class="sd-msg-footer">$timeRow$statusHtml</div></div>"""
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
                <div class="sd-chat-input-row">
                    <input type="text" id="sd-chat-input" class="sd-chat-input" placeholder="Сообщение..." maxlength="2000" />
                    <button type="button" id="sd-chat-send" class="sd-chat-send-btn" title="Отправить">$iconSendSvg</button>
                </div>
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

private fun String.escapeHtml(): String = this

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
        """<div class="sd-cadet-card">
            <p class="sd-cadet-card-title">Карточка курсанта:</p>
            <div class="sd-cadet-card-body">
                <div class="sd-cadet-avatar">$initials</div>
                <div class="sd-cadet-info">
                    <p class="sd-cadet-name">${c.fullName.escapeHtml()}</p>
                    <p class="sd-cadet-row"><span class="sd-cadet-label">Телефон:</span> $phoneDisplay</p>
                    <p class="sd-cadet-row"><span class="sd-cadet-label">Вождений:</span> $completedCount</p>
                    <p class="sd-cadet-row"><span class="sd-cadet-label">Баланс:</span> ${c.balance} талонов</p>
                </div>
            </div>
            <div class="sd-cadet-card-actions">
                <button type="button" class="sd-btn sd-btn-circle sd-btn-chat sd-cadet-chat-btn" data-contact-id="${c.id.escapeHtml()}" title="Чат">Чат</button>
                <a href="$phoneHref" class="$phoneClass" title="Позвонить">Телефон</a>
            </div>
        </div>"""
    }
    val sessList = sessions.joinToString("") { s ->
        """<div class="sd-record-row"><span>${formatDateTime(s.startTimeMillis)}</span> — ${s.status}</div>"""
    }
    val iconPerson = """<span class="sd-profile-icon" aria-hidden="true"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg></span>"""
    val iconEmail = """<span class="sd-profile-icon" aria-hidden="true"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg></span>"""
    val iconPhone = """<span class="sd-profile-icon" aria-hidden="true"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/></svg></span>"""
    val iconBadge = """<span class="sd-profile-icon" aria-hidden="true"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M20 7h-5V4c0-1.1-.9-2-2-2h-2C9.9 2 9 2.9 9 4v3H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2zm-9-3h2v3h-2V4zm9 16H4V9h5c0 1.1.9 2 2 2h2c1.1 0 2-.9 2-2h5v11zm-9-4l2 2 4-4"/></svg></span>"""
    val iconTicket = """<span class="sd-profile-icon" aria-hidden="true"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M22 10V6c0-1.1-.9-2-2-2H4c-1.1 0-1.99.9-1.99 2v4c1.1 0 1.99.9 1.99 2s-.89 2-2 2v4c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2v-4c-1.1 0-2-.9-2-2s.9-2 2-2zm-2-1.46c-1.19.69-2 1.99-2 3.46s.81 2.77 2 3.46V18H4v-2.54c1.19-.69 2-1.99 2-3.46 0-1.48-.8-2.77-1.99-3.46L4 6h16v2.54z"/></svg></span>"""
    val profileCard = """
        <div class="sd-profile-card">
            <div class="sd-profile-card-bg"></div>
            <div class="sd-profile-card-overlay"></div>
            <div class="sd-profile-card-shimmer" aria-hidden="true"></div>
            <div class="sd-profile-card-inner">
                <h3 class="sd-profile-card-title">$iconPerson Профиль инструктора</h3>
                <div class="sd-profile-row">$iconPerson<span class="sd-profile-label">ФИО:</span><span class="sd-profile-value">${(user.fullName.ifBlank { "—" }).escapeHtml()}</span></div>
                <div class="sd-profile-row">$iconEmail<span class="sd-profile-label">Email:</span><span class="sd-profile-value">${(user.email.ifBlank { "—" }).escapeHtml()}</span></div>
                <div class="sd-profile-row">$iconPhone<span class="sd-profile-label">Тел.:</span><span class="sd-profile-value">${(user.phone.ifBlank { "—" }).escapeHtml()}</span></div>
                <div class="sd-profile-row">$iconBadge<span class="sd-profile-label">Роль:</span><span class="sd-profile-value">Инструктор</span></div>
                <div class="sd-profile-row sd-profile-row-balance">$iconTicket<span class="sd-profile-label">Баланс талонов:</span><span class="sd-profile-value sd-balance-badge">${user.balance}</span></div>
            </div>
        </div>"""
    return """<h2>Главная</h2>
        $profileCard
        <div class="sd-block"><h3 class="sd-block-title">Мои курсанты (${cadets.size})</h3><div class="sd-cadet-cards">$cadetsListHtml</div></div>
        $loadingLine
        <div class="sd-block"><h3 class="sd-block-title">Мой график</h3><div class="sd-list">$sessList</div></div>
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
    val loadingLine = if (loading) """<p class="sd-loading-text">Загрузка… <button type="button" id="sd-stop-loading" class="sd-btn sd-btn-small sd-btn-secondary">Показать пусто</button></p>""" else ""
    return when (user.role) {
        "instructor" -> {
            val list = windows.joinToString("") { w ->
                val dt = formatDateTime(w.dateTimeMillis)
                val status = if (w.status == "booked") " (забронировано)" else ""
                """<div class="sd-record-row"><span>$dt</span> $status <button type="button" class="sd-btn sd-btn-small sd-btn-delete" data-window-id="${w.id.escapeHtml()}">Удалить</button></div>"""
            }
            val sessionsList = sessions.filter { it.status == "scheduled" || it.status == "inProgress" }.take(20).joinToString("") { s ->
                """<div class="sd-record-row"><span>${formatDateTime(s.startTimeMillis)}</span> — ${s.status}</div>"""
            }
            """<h2>Запись</h2>$loadingLine
               <div class="sd-recording-section"><h3>Свободные окна</h3><div class="sd-list">$list</div></div>
               <div class="sd-recording-section"><h3>Добавить окно</h3><p><input type="datetime-local" id="sd-new-window-dt" class="sd-input" /> <button type="button" id="sd-add-window" class="sd-btn sd-btn-primary">Добавить</button></p></div>
               <div class="sd-recording-section"><h3>Ближайшие занятия</h3><div class="sd-list">$sessionsList</div></div>"""
        }
        "cadet" -> {
            val slotsHtml = windows.joinToString("") { w ->
                """<div class="sd-record-row"><span>${formatDateTime(w.dateTimeMillis)}</span> <button type="button" class="sd-btn sd-btn-primary sd-btn-small" data-window-id="${w.id.escapeHtml()}">Записаться</button></div>"""
            }
            val myRecords = sessions.filter { it.status == "scheduled" }.take(10).joinToString("") { """<div class="sd-record-row">${formatDateTime(it.startTimeMillis)} — ${it.status}</div>""" }
            """<h2>Запись на вождение</h2>$loadingLine
               <div class="sd-recording-section"><h3>Свободные слоты</h3><div class="sd-list">$slotsHtml</div></div>
               <div class="sd-recording-section"><h3>Мои записи</h3><div class="sd-list">$myRecords</div></div>"""
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
        "Билеты" -> """<h2>Билеты ПДД</h2><div class="sd-tickets-content"><p>Билеты ПДД — в приложении: темы, знаки, разметка, штрафы, билеты.</p><p><a href="https://play.google.com/store/apps/details?id=com.example.startdrive" target="_blank" rel="noopener">Скачать приложение StartDrive (Google Play)</a></p><p><a href="https://pdd.ru/" target="_blank" rel="noopener">ПДД РФ на pdd.ru</a></p></div>"""
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
            <h1>StartDrive · $roleTitle</h1>
            <p>${formatShortName(user.fullName)} · ${user.email}</p>
            <button type="button" id="sd-btn-signout" class="sd-btn sd-btn-signout">Выйти</button>
        </header>
        <main class="sd-content">
            <div class="sd-card" id="sd-card">
                $tabContent
            </div>
        </main>
        <nav class="sd-tabs">$tabButtons</nav>
    """.trimIndent()
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
            updateState { selectedChatContactId = contactId; chatMessages = emptyList() }
            unsubscribeChat()
            subscribeMessages(chatRoomId(uid, contactId)) { list ->
                updateState { chatMessages = list }
                window.setTimeout({
                    (document.getElementById("sd-chat-messages")?.lastElementChild?.unsafeCast<dynamic>())?.scrollIntoView(js("({ block: 'end', behavior: 'smooth' })"))
                }, 100)
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val adminOpenChat = closest(".sd-admin-open-chat")
        if (adminOpenChat != null) {
            val contactId = adminOpenChat.getAttribute("data-contact-id") ?: return@addEventListener
            val uid = appState.user?.id ?: return@addEventListener
            updateState { selectedTabIndex = 2; selectedChatContactId = contactId; chatMessages = emptyList() }
            unsubscribeChat()
            subscribeMessages(chatRoomId(uid, contactId)) { list ->
                updateState { chatMessages = list }
                window.setTimeout({
                    (document.getElementById("sd-chat-messages")?.lastElementChild?.unsafeCast<dynamic>())?.scrollIntoView(js("({ block: 'end', behavior: 'smooth' })"))
                }, 100)
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val cadetChatBtn = closest(".sd-cadet-chat-btn")
        if (cadetChatBtn != null) {
            val contactId = cadetChatBtn.getAttribute("data-contact-id") ?: return@addEventListener
            val uid = appState.user?.id ?: return@addEventListener
            updateState { selectedTabIndex = 2; selectedChatContactId = contactId; chatMessages = emptyList() }
            unsubscribeChat()
            subscribeMessages(chatRoomId(uid, contactId)) { list ->
                updateState { chatMessages = list }
                window.setTimeout({
                    (document.getElementById("sd-chat-messages")?.lastElementChild?.unsafeCast<dynamic>())?.scrollIntoView(js("({ block: 'end', behavior: 'smooth' })"))
                }, 100)
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
                signOut()
            })
        }
        AppScreen.ProfileNotFound -> {
            document.getElementById("sd-btn-signout-profile-not-found")?.addEventListener("click", {
                signOut()
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
                signOut()
            })
            document.getElementById("sd-chat-back")?.addEventListener("click", {
                updateState { selectedChatContactId = null; chatMessages = emptyList() }
                unsubscribeChat()
            })
            val chatInput = document.getElementById("sd-chat-input") as? HTMLInputElement
            document.getElementById("sd-chat-send")?.addEventListener("click", {
                sendChatMessage(chatInput, uid)
            })
            chatInput?.addEventListener("keypress", { e: dynamic ->
                if (e?.key == "Enter") sendChatMessage(chatInput, uid)
            })
            document.getElementById("sd-add-window")?.addEventListener("click", {
                val input = document.getElementById("sd-new-window-dt") as? HTMLInputElement
                val v = input?.value ?: ""
                if (v.isBlank()) return@addEventListener
                val dateFn = js("function(s){ return new Date(s).getTime(); }").unsafeCast<(String) -> Number>()
                val ms = dateFn(v).toLong()
                if (ms <= 0) return@addEventListener
                addOpenWindow(usr.id, ms) { _, err ->
                    if (err != null) updateState { networkError = err }
                    else getOpenWindowsForInstructor(usr.id) { wins ->
                        getSessionsForInstructor(usr.id) { sess ->
                            updateState { recordingOpenWindows = wins; recordingSessions = sess }
                        }
                    }
                }
            })
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
