import com.example.startdrive.shared.di.SharedFactory
import com.example.startdrive.shared.model.User
import firebase.*
import firebase.ChatMessage
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent

private var presenceUnsubscribes: MutableList<() -> Unit> = mutableListOf()
private var voicePlayInterval: Int = 0
private var chatMsgLongPressTimerId: Int = 0
/** Поля калькулятора «талоны × руб» на экране профиля: null = подставить текущий баланс талонов. */
private var instructorProfileCalcTokensOverride: String? = null
private var instructorProfileCalcRubInput: String = ""
/** Узел калькулятора вынесен из-под innerHTML #sd-card, чтобы ввод не моргал. */
private var instructorProfileEarnedCalcMountEl: Element? = null
/** Отложенное обновление суммы в руб. (один кадр), чтобы не дёргать DOM на каждый символ. */
private var instructorEarnedRubRafId: Int = 0
/** Запланированный render() из main (blur калькулятора — подтянуть статистику без мигания при вводе). */
private var scheduleAppRender: (() -> Unit)? = null
private var chatFileViewerEscapeListenerBound: Boolean = false
private var adminStoragePendingClearKind: String? = null
private var adminStoragePendingContactId: String? = null
private var adminStoragePendingGroupId: String? = null
private val chatScrollByContactId = mutableMapOf<String, Int>()
private val chatScrollByGroupId = mutableMapOf<String, Int>()
private val chatNotifUnsubByGroupId = mutableMapOf<String, () -> Unit>()
private var cadetNotificationAudio: dynamic = null
private var cadetNotificationAudioUnlocked: Boolean = false
private var cadetNotificationAudioContext: dynamic = null
private var chatMessageAudio: dynamic = null
/** Отписка от onSnapshot документа users/{uid} (баланс в профиле в реальном времени). */
private var userProfileFirestoreUnsubscribe: (() -> Unit)? = null
/** Лента admin_events для веб-админа (уведомления без FCM). */
private var adminEventsUnsubscribe: (() -> Unit)? = null

/** Черновик аватара группы до «Сохранить»/«Создать» (загрузка в Storage после записи в Firestore). */
private var groupChatAvatarPendingDataUrl: String? = null
private var groupChatAvatarPendingRemove: Boolean = false

/** Состояние кропа аватара группы (общее, чтобы не дублировать слушатели на root). */
private val groupModalCropState = doubleArrayOf(0.0, 0.0, 1.0)
private val groupModalCropDataUrlHolder = mutableListOf<String?>(null)
private var groupModalCropDragging = false
private var groupModalCropPinching = false
private var groupModalCropDragStartX = 0.0
private var groupModalCropDragStartY = 0.0
private var groupModalCropDragOffsetStartX = 0.0
private var groupModalCropDragOffsetStartY = 0.0
private var groupModalCropPinchStartDist = 0.0
private var groupModalCropPinchStartScale = 0.0
private var groupChatAvatarRootListenersBound = false

/** Сигнатура списка курсантов для блока форм: при частичном обновлении вкладки «Запись» пересобираем только стабильные поля, если изменился состав курсантов. */
private var lastInstructorRecordingCadetSigForStable: String? = null
/** Держим состояние модалки «Начать раньше», чтобы она не закрывалась при render()/polling. */
private var startEarlyModalSessionId: String? = null
private var startEarlyModalMinutesLeft: Int = 0
private var completeEarlyModalSessionId: String? = null
private var completeEarlyModalText: String = ""
private var runningLateModalSessionId: String? = null
private var homeCancelUnconfirmedModalSessionId: String? = null
private var cancelReasonModalSessionId: String? = null
private var recDeleteWindowModalWindowId: String? = null
private var instructorRateCadetModalSessionId: String? = null
private var instructorRateCadetModalCadetName: String = ""
/** Чтобы не сбрасывать радио «оценка курсанту» при каждом poll/render после выбора пользователя. */
private var instructorRateModalRadioInitSessionId: String? = null
/** Первый снимок сессий инструктора без звука; дальше — при изменениях со стороны курсанта/сервера. */
private var instructorSessionSoundBaselineReady: Boolean = false

private fun resetInstructorSessionSoundBaseline() {
    instructorSessionSoundBaselineReady = false
}

private fun instructorCadetsSignature(cadets: List<User>): String {
    return cadets.map { "${it.id}:${it.balance}" }.sorted().joinToString(",")
}

private fun clearGroupChatAvatarPending() {
    groupChatAvatarPendingDataUrl = null
    groupChatAvatarPendingRemove = false
}

private fun groupChatNameInitials(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }

/** После addChatGroup/updateChatGroup — загрузить/удалить аватар по черновику. */
private fun applyGroupAvatarAfterFirebaseSave(groupId: String, onDone: () -> Unit) {
    when {
        !groupChatAvatarPendingDataUrl.isNullOrBlank() ->
            uploadChatGroupAvatar(groupId, groupChatAvatarPendingDataUrl!!) { err ->
                if (err != null) updateState { networkError = err }
                else onDone()
            }
        groupChatAvatarPendingRemove ->
            removeChatGroupAvatar(groupId) { err ->
                if (err != null) updateState { networkError = err }
                else onDone()
            }
        else -> onDone()
    }
}

private fun saveChatScrollForCurrentContact() {
    val contactId = appState.selectedChatContactId
    val groupId = appState.selectedChatGroupId
    val c = document.getElementById("sd-chat-messages")?.asDynamic() ?: return
    val top = (c.scrollTop as? Number)?.toInt() ?: 0
    when {
        groupId != null -> chatScrollByGroupId[groupId] = top
        contactId != null -> chatScrollByContactId[contactId] = top
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
            scrollChatToBottom()
        }
    }
}

/** Прокручивает окно чата к последнему сообщению мгновенно (без анимации). */
internal fun scrollChatToBottom() {
    val c = document.getElementById("sd-chat-messages")?.asDynamic()
    if (c != null) {
        val maxScroll = ((c.scrollHeight as Number).toDouble() - (c.clientHeight as Number).toDouble()).toInt().coerceAtLeast(0)
        c.scrollTop = maxScroll
    }
}

private val SD_ICON_PLAY_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>"""
private val SD_ICON_PAUSE_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>"""

private val SD_ICON_BALANCE_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M4 10v7h3v-7H4zm6 0v7h3v-7h-3zM2 22h19v-3H2v3zm14-12v7h3v-7h-3zm-4.5-9L2 6v2h19V6l-9.5-5z"/></svg>"""
private val SD_ICON_CREDIT_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M20 12l-1.41-1.41L13 16.17V4h-2v12.17l-5.58-5.59L4 12l8 8 8-8z"/></svg>"""
private val SD_ICON_DEBIT_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M4 12l1.41 1.41L11 7.83V20h2V7.83l5.58 5.59L20 12l-8-8-8 8z"/></svg>"""
private val SD_ICON_DRIVING_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/></svg>"""
private val SD_ICON_CHECK_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>"""
private val SD_ICON_CLOSE_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>"""
private val SD_ICON_OTHER_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M19.14 12.94c.04-.31.06-.63.06-.94 0-.31-.02-.63-.06-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.04.31-.06.63-.06.94s.02.63.06.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>"""
private val SD_ICON_NOTIFICATION_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>"""
private val SD_ICON_BACK_CHEVRON_SVG = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>"""

private fun formatVoiceDurationSec(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

/** Обновляет только UI плеера голосовых сообщений без полного рендера (чтобы не сбрасывать прокрутку). */
private fun patchVoicePlayerDOM() {
    val playingId = appState.chatPlayingVoiceId
    val currentMs = appState.chatPlayingVoiceCurrentMs
    val playbackPaused = appState.chatVoicePlaybackPaused
    val voiceEls = document.querySelectorAll(".sd-msg-voice")
    for (i in 0 until voiceEls.length) {
        val el = voiceEls.item(i)?.asDynamic() ?: continue
        val msgId = el.getAttribute("data-voice-id") as? String ?: continue
        val dur = ((el.getAttribute("data-voice-duration") as? String) ?: "0").toIntOrNull() ?: 0
        val isActiveClip = playingId == msgId
        val isPlayingNow = isActiveClip && !playbackPaused
        val ms = if (isActiveClip) currentMs else 0
        val progress = if (dur > 0) (ms.toDouble() / 1000 / dur).coerceIn(0.0, 1.0) else 0.0
        val progressPct = (progress * 100).toInt()
        val currentStr = formatVoiceDurationSec((ms / 1000).coerceAtLeast(0).coerceAtMost(dur))
        val totalStr = formatVoiceDurationSec(dur)
        js("(function(el, isPlayingNow, progressPct, currentStr, totalStr, iconPlay, iconPause){ var btn=el.querySelector('.sd-voice-play-btn'); if(btn){ btn.innerHTML=isPlayingNow?iconPause:iconPlay; btn.setAttribute('title',isPlayingNow?'Пауза':'Воспроизвести'); btn.setAttribute('aria-label',isPlayingNow?'Пауза':'Воспроизвести'); } var wrap=el.querySelector('.sd-voice-progress-wrap'); if(wrap){ var bar=wrap.querySelector('.sd-voice-progress-bar'); if(isPlayingNow||progressPct>0){ if(!bar){ bar=document.createElement('div'); bar.className='sd-voice-progress-bar'; var ref=wrap.querySelector('.sd-voice-times'); if(ref) wrap.insertBefore(bar,ref); else wrap.appendChild(bar); } bar.style.width=progressPct+'%'; } else if(bar) bar.remove(); } var cur=el.querySelector('.sd-voice-current'); if(cur) cur.textContent=currentStr; var tot=el.querySelector('.sd-voice-total'); if(tot) tot.textContent=totalStr; })")(
            el, isPlayingNow, progressPct, currentStr, totalStr, SD_ICON_PLAY_SVG, SD_ICON_PAUSE_SVG
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

/** Преобразует сырое сообщение об ошибке (например "Failed to fetch") в понятное пользователю. */
fun friendlyNetworkError(raw: String?): String {
    if (raw == null) return ""
    return when {
        raw.contains("Failed to fetch", ignoreCase = true) || raw.contains("NetworkError", ignoreCase = true) ->
            "Нет соединения с интернетом. Проверьте подключение и повторите."
        else -> raw
    }
}

/** Красную плашку сверху показываем только для реальных сетевых ошибок. */
private fun shouldShowTopNetworkBanner(raw: String?): Boolean {
    if (raw.isNullOrBlank()) return false
    return raw.contains("Failed to fetch", ignoreCase = true) ||
        raw.contains("NetworkError", ignoreCase = true) ||
        raw.contains("Нет соединения с интернетом", ignoreCase = true)
}

fun main() {
    window.onload = onload@ { _: Event ->
        run {
            val ua = js("navigator.userAgent").unsafeCast<String>()
            val androidLike = ua.contains("Android", ignoreCase = true) ||
                (ua.contains("Linux", ignoreCase = true) && ua.contains("Mobile", ignoreCase = true)) ||
                ua.contains("SamsungBrowser", ignoreCase = true)
            if (androidLike) {
                document.documentElement?.classList?.add("sd-android")
            }
        }
        val root = document.getElementById("root") ?: return@onload
        initFirebase()

        var lastRenderedTabIndex: Int? = null
        /** Чтобы не пересоздавать DOM вкладок при каждом updateState — иначе «дергается» иконка Главная. */
        var lastTabButtonsMarkup: String? = null

        fun render() {
            val state = appState
            val networkBanner = state.networkError?.takeIf { shouldShowTopNetworkBanner(it) }?.let { msg ->
                val friendly = friendlyNetworkError(msg)
                """<div class="sd-network-error" id="sd-network-error"><span>$friendly</span> <button type="button" id="sd-network-retry" class="sd-btn-inline sd-btn-inline-primary">Повторить</button> <button type="button" id="sd-dismiss-network-error" class="sd-btn-inline">Закрыть</button></div>"""
            } ?: ""
            val loadingOverlay = if (state.loading) """<div class="sd-loading-overlay" id="sd-loading-overlay"><div class="sd-spinner"></div><p>Загрузка…</p></div>""" else ""
            val panelScreen = state.screen == AppScreen.Admin || state.screen == AppScreen.Instructor || state.screen == AppScreen.Cadet
            val sdCard = (root.unsafeCast<dynamic>().querySelector("#sd-card")) as? org.w3c.dom.Element
            if (panelScreen && state.user != null && sdCard != null && state.networkError == null && !state.loading) {
                val tabs = when (state.screen) {
                    AppScreen.Admin -> listOf("Главная", "Баланс", "Расписание", "Чат", "История")
                    AppScreen.Instructor -> listOf("Главная", "Запись", "Чат", "Билеты", "История")
                    else -> listOf("Главная", "Запись", "Чат", "Билеты", "История")
                }
                /* Состояние секций главной (развёрнуто/свёрнуто) сохраняем при переключении вкладок и перед рендером текущей вкладки */
                if (state.screen == AppScreen.Admin && state.selectedTabIndex == 1) {
                    val historyDetails = sdCard.querySelector("details[data-balance-section=\"history\"]")
                    updateState { balanceHistorySectionOpen = (historyDetails?.unsafeCast<dynamic>()?.open == true) }
                }
                if (state.screen == AppScreen.Instructor && state.selectedTabIndex == 0) {
                    val myCadetsDetails = sdCard.querySelector("details[data-instructor-my-cadets]")
                    if (myCadetsDetails != null) {
                        updateState { instructorMyCadetsSectionOpen = (myCadetsDetails.unsafeCast<dynamic>().open == true) }
                    }
                }
                val (tabButtons, tabContent) = getPanelTabButtonsAndContent(state.user!!, tabs)
                if (tabButtons != lastTabButtonsMarkup) {
                    (root.unsafeCast<dynamic>().querySelector("nav.sd-tabs") as? org.w3c.dom.Element)?.innerHTML = tabButtons
                    lastTabButtonsMarkup = tabButtons
                }
                lastRenderedTabIndex = state.selectedTabIndex
                if (state.screen == AppScreen.Instructor || state.screen == AppScreen.Cadet || state.screen == AppScreen.Admin) {
                    val notifBtn = root.unsafeCast<dynamic>().querySelector("#sd-btn-notifications") as? org.w3c.dom.Element
                    notifBtn?.setAttribute("class", notificationButtonHtmlClass())
                    (notifBtn?.querySelector(".sd-btn-notif-wrap") as? org.w3c.dom.Element)?.innerHTML = getNotificationButtonWrapHtml()
                }
                val cardContent = when {
                    state.notificationsViewOpen -> """<div class="sd-notif-full-view"><p class="sd-notif-back-row"><button type="button" id="sd-notif-back" class="sd-btn sd-btn-secondary">← Назад</button></p>${renderNotificationsTabContent(state.user!!)}</div>"""
                    state.pddExamMode && state.pddQuestions.isNotEmpty() -> renderTicketsTabContent()
                    else -> tabContent
                }
                val canPatchInstructorRecording =
                    state.screen == AppScreen.Instructor &&
                    state.selectedTabIndex == 1 &&
                    !state.notificationsViewOpen &&
                    !(state.pddExamMode && state.pddQuestions.isNotEmpty())
                if (canPatchInstructorRecording) {
                    val stable = sdCard.querySelector("#sd-instructor-rec-forms-stable")
                    val dyn = sdCard.querySelector("#sd-instructor-rec-dynamic")
                    if (stable != null && dyn != null) {
                        val u = state.user!!
                        dyn.innerHTML = renderInstructorRecordingDynamicPanelHtml(u)
                        patchInstructorRecordingDatetimeInputsMin(sdCard)
                        val cadSig = instructorCadetsSignature(state.instructorCadets)
                        if (cadSig != lastInstructorRecordingCadetSigForStable) {
                            stable.innerHTML = renderInstructorRecordingFormsPanelHtml()
                            lastInstructorRecordingCadetSigForStable = cadSig
                        }
                        attachListeners(root)
                        appendSoundSettingsModalIfNeeded(root)
                        appendAdminCadetGroupModalsIfNeeded(root)
                        appendChatGroupModalsIfNeeded(root)
                        appendInstructorCancelReasonModalIfNeeded(root)
                        appendInstructorRunningLateModalIfNeeded(root)
                        appendInstructorRateCadetModalIfNeeded(root)
                        appendRecCancelSessionConfirmModalIfNeeded(root)
                        appendAllowSoundBarIfNeeded(root)
                        syncChatVoiceHostOverlay()
                        window.requestAnimationFrame { syncChatVoiceHostOverlay() }
                        return
                    }
                }
                lastInstructorRecordingCadetSigForStable = null
                val skipFullCardRedraw =
                    state.screen == AppScreen.Instructor &&
                    state.selectedTabIndex == 0 &&
                    state.instructorHomeSubView == "profile" &&
                    !state.notificationsViewOpen &&
                    !(state.pddExamMode && state.pddQuestions.isNotEmpty()) &&
                    isInstructorEarnedCalcInputFocused()
                if (!skipFullCardRedraw) {
                    detachInstructorProfileEarnedCalcMount()
                    sdCard.innerHTML = cardContent
                    // Эффект "джина" между вкладками отключен: мгновенная смена контента.
                    reattachInstructorProfileEarnedCalcMount(root)
                } else {
                    reattachInstructorProfileEarnedCalcMount(root)
                }
                syncChatVoiceHostOverlay()
                window.requestAnimationFrame { syncChatVoiceHostOverlay() }
                if (canPatchInstructorRecording && sdCard.querySelector("#sd-instructor-rec-forms-stable") != null) {
                    lastInstructorRecordingCadetSigForStable = instructorCadetsSignature(state.instructorCadets)
                }
                if (state.selectedTabIndex == chatTabIndexForRole(state.user?.role) && (state.selectedChatContactId != null || state.selectedChatGroupId != null || (state.chatAdminCorrespondenceMode && state.chatAdminCorrespondenceSubjectId != null && state.chatAdminCorrespondencePeerId != null)) && !state.chatSettingsOpen) {
                    val chatContainer = document.getElementById("sd-chat-messages")?.asDynamic()
                    if (chatContainer != null) {
                        val maxScroll = ((chatContainer.scrollHeight as Number).toDouble() - (chatContainer.clientHeight as Number).toDouble()).toInt().coerceAtLeast(0)
                        chatContainer.scrollTop = maxScroll
                    }
                }
                if (state.pddScrollToSignDetail && state.pddCategoryId == "signs" && state.pddSelectedSign != null) {
                    (sdCard.querySelector("#sd-pdd-sign-detail-scroll") as? org.w3c.dom.Element)?.let { el ->
                        el.unsafeCast<dynamic>().scrollIntoView(js("({ block: 'start', behavior: 'smooth' })"))
                    }
                    updateState { pddScrollToSignDetail = false }
                }
                attachListeners(root)
                appendSoundSettingsModalIfNeeded(root)
                appendAdminCadetGroupModalsIfNeeded(root)
                appendChatGroupModalsIfNeeded(root)
                appendInstructorCancelReasonModalIfNeeded(root)
                appendInstructorRunningLateModalIfNeeded(root)
                appendInstructorRateCadetModalIfNeeded(root)
                appendRecCancelSessionConfirmModalIfNeeded(root)
                appendAllowSoundBarIfNeeded(root)
                return
            }
            lastRenderedTabIndex = null
            lastTabButtonsMarkup = null
            val html = when (state.screen) {
                AppScreen.Login -> renderLogin(state.error, state.loading)
                AppScreen.Register -> renderRegister(state.error, state.loading)
                AppScreen.PendingApproval -> renderPendingApproval()
                AppScreen.ProfileNotFound -> renderProfileNotFound(state.error ?: "Профиль не найден.")
                AppScreen.Admin -> renderPanel(state.user!!, "Администратор", listOf("Главная", "Баланс", "Расписание", "Чат", "История"))
                AppScreen.Instructor -> renderPanel(state.user!!, "Инструктор", listOf("Главная", "Запись", "Чат", "Билеты", "История"))
                AppScreen.Cadet -> renderPanel(state.user!!, "Курсант", listOf("Главная", "Запись", "Чат", "Билеты", "История"))
            }
            root.innerHTML = networkBanner + loadingOverlay + html
            syncChatVoiceHostOverlay()
            window.requestAnimationFrame { syncChatVoiceHostOverlay() }
            attachListeners(root)
            appendSoundSettingsModalIfNeeded(root)
            appendAdminCadetGroupModalsIfNeeded(root)
            appendChatGroupModalsIfNeeded(root)
            appendInstructorCancelReasonModalIfNeeded(root)
            appendInstructorRunningLateModalIfNeeded(root)
            appendInstructorRateCadetModalIfNeeded(root)
            appendRecCancelSessionConfirmModalIfNeeded(root)
            appendAllowSoundBarIfNeeded(root)
        }

        var renderScheduled = false
        fun scheduleRender() {
            if (renderScheduled) return
            renderScheduled = true
            window.requestAnimationFrame {
                render()
                renderScheduled = false
                startDrivingTimers()
            }
        }

        scheduleAppRender = { scheduleRender() }

        onStateChanged = { scheduleRender() }

        setupPanelClickDelegation(root)
        // Делегируем клик/тап по кнопке push на root, чтобы работало даже после перерисовок.
        val pushDelegatedHandler = { ev: Event ->
            val targetEl = ev.target as? Element
            if (targetEl != null) {
                val hitEnable = targetEl.asDynamic().closest?.invoke("#sd-notif-enable-push")
                val hitTest = targetEl.asDynamic().closest?.invoke("#sd-notif-test-push")
                if (hitEnable != null && hitEnable != js("undefined")) {
                    ev.preventDefault()
                    enableWebPushFromUi()
                } else if (hitTest != null && hitTest != js("undefined")) {
                    ev.preventDefault()
                    testPushFromUi()
                }
            }
        }
        root.addEventListener("click", pushDelegatedHandler)
        root.addEventListener("touchend", pushDelegatedHandler)

        onAuthStateChanged { uid ->
            if (uid == null) {
                try {
                    userProfileFirestoreUnsubscribe?.invoke()
                } catch (_: Throwable) { }
                userProfileFirestoreUnsubscribe = null
                try {
                    adminEventsUnsubscribe?.invoke()
                } catch (_: Throwable) { }
                adminEventsUnsubscribe = null
                updateState {
                    screen = AppScreen.Login
                    user = null
                    error = null
                    instructorHomeSubView = "main"
                    instructorProfileWeekOffset = 0
                    cadetHomeSubView = "main"
                    // Сбрасываем данные, чтобы не было "утечек" между пользователями при смене аккаунта.
                    historySessions = emptyList()
                    historyBalance = emptyList()
                    historyUsers = emptyList()
                    historyLoading = false
                }
                return@onAuthStateChanged
            }
            // Если пользователь сменился в той же вкладке — очистить историю/кэш до загрузки новых данных.
            val prevUid = appState.user?.id
            if (prevUid != null && prevUid != uid) {
                updateState {
                    historySessions = emptyList()
                    historyBalance = emptyList()
                    historyUsers = emptyList()
                    historyLoading = true
                }
            }
            updateState { loading = true; error = null }
            getCurrentUser { user, errorMsg ->
                updateState { loading = false; networkError = null }
                if (user == null) {
                    try {
                        userProfileFirestoreUnsubscribe?.invoke()
                    } catch (_: Throwable) { }
                    userProfileFirestoreUnsubscribe = null
                    try {
                        adminEventsUnsubscribe?.invoke()
                    } catch (_: Throwable) { }
                    adminEventsUnsubscribe = null
                    updateState {
                        screen = AppScreen.ProfileNotFound
                        this.user = null
                        error = errorMsg ?: "Профиль не найден в базе."
                    }
                    return@getCurrentUser
                }
                val loadedNotifications = loadNotificationsFromStorage(user.id)
                updateState {
                    this.user = user
                    error = null
                    networkError = null
                    notifications = loadedNotifications
                    notificationsReadCount = loadedNotifications.size
                    screen = when (user.role) {
                        "admin" -> AppScreen.Admin
                        "instructor" -> if (user.isActive) AppScreen.Instructor else AppScreen.PendingApproval
                        "cadet" -> if (user.isActive) AppScreen.Cadet else AppScreen.PendingApproval
                        else -> AppScreen.PendingApproval
                    }
                    showSoundSettingsModal = (user.role == "cadet" && user.isActive && isFirstRunSoundSetting())
                }
                setPresence(user.id, true)
                initWebPushForCurrentUser(user.id, onForegroundMessage = { title, body ->
                    val t = title.trim()
                    val b = body.trim()
                    val txt = when {
                        t.isNotEmpty() && b.isNotEmpty() -> "$t\n$b"
                        b.isNotEmpty() -> b
                        else -> t
                    }
                    if (txt.isNotBlank()) showNotification(txt)
                })
                try {
                    userProfileFirestoreUnsubscribe?.invoke()
                } catch (_: Throwable) { }
                userProfileFirestoreUnsubscribe = null
                userProfileFirestoreUnsubscribe = subscribeCurrentUserDocument { newUser ->
                    if (appState.user == newUser) return@subscribeCurrentUserDocument
                    updateState { this.user = newUser }
                }
                try {
                    adminEventsUnsubscribe?.invoke()
                } catch (_: Throwable) { }
                adminEventsUnsubscribe = null
                if (user.role == "admin") {
                    adminEventsUnsubscribe = subscribeAdminEventsFeed { text, eventId ->
                        showNotification(text, eventId)
                    }
                }
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
                        <li><svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>Две роли: курсант, инструктор</li>
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

/** Аватар собеседника в пузыре чата (для группы — по пользователю). */
private fun chatOtherUserAvatarHtml(u: User): String {
    val initials = u.initials().ifEmpty { "?" }.escapeHtml()
    val bg = avatarColorForId(u.id).escapeHtml()
    val url = u.chatAvatarUrl?.takeIf { it.isNotBlank() }
    return if (url != null) """<div class="sd-msg-them-avatar sd-avatar-wrap" data-initials="$initials" data-bg="$bg"><img src="${url.escapeHtml()}" alt="" class="sd-msg-avatar-img sd-avatar-img" decoding="async" data-user-id="${u.id.escapeHtml()}" /></div>"""
    else """<div class="sd-msg-them-avatar" style="background:$bg"><span class="sd-msg-them-avatar-initials">$initials</span></div>"""
}

/** Сообщение с файлом — показывать как картинку в чате (превью). */
private fun chatFileMessageIsImage(msg: ChatMessage): Boolean {
    val mime = msg.fileMime?.trim()?.lowercase() ?: ""
    if (mime.startsWith("image/")) return true
    val name = (msg.fileName ?: "").lowercase()
    return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
        name.endsWith(".gif") || name.endsWith(".webp") || name.endsWith(".bmp") ||
        name.endsWith(".svg") || name.endsWith(".avif") || name.endsWith(".heic") || name.endsWith(".heif")
}

/** Пузырь сообщения с вложением (файл). */
private fun chatFileMessageBubbleHtml(
    msg: ChatMessage,
    cls: String,
    replyTargetClass: String,
    replyHtml: String,
    timeRow: String,
    statusHtml: String,
    otherAvatarHtml: String,
    myAvatarHtml: String,
    isMe: Boolean,
    senderLabel: String,
): String {
    val url = (msg.fileUrl ?: "").escapeHtml()
    val fn = (msg.fileName ?: "файл").escapeHtml()
    val fnAttr = (msg.fileName ?: "файл").escapeHtml()
    val isImage = chatFileMessageIsImage(msg)
    val fileRow = if (isImage && url.isNotBlank()) {
        """<div class="sd-msg-file-row sd-msg-file-row-image">
            <a href="$url" class="sd-msg-file-image-link sd-chat-file-in-app" rel="noopener noreferrer" title="$fn" data-file-name="$fnAttr">
                <img src="$url" alt="$fn" class="sd-msg-file-image" loading="lazy" decoding="async" />
            </a>
            <a href="$url" class="sd-msg-file-link sd-msg-file-name-under sd-chat-file-in-app" rel="noopener noreferrer" data-file-name="$fnAttr">📎 $fn</a>
        </div>"""
    } else {
        """<div class="sd-msg-file-row"><a href="$url" class="sd-msg-file-link sd-chat-file-in-app" rel="noopener noreferrer" data-file-name="$fnAttr" download="$fn">📎 $fn</a></div>"""
    }
    val caption = msg.text.trim().takeIf { it.isNotBlank() }?.let {
        """<div class="sd-msg-text sd-msg-file-caption">${it.escapeHtml()}</div>"""
    } ?: ""
    return """<div class="$cls sd-msg-file${if (isImage) " sd-msg-file-is-image" else ""}$replyTargetClass" data-msg-id="${msg.id.escapeHtml()}" data-msg-text="${msg.text.escapeHtml()}">${if (isMe) "" else otherAvatarHtml}<div class="sd-msg-bubble-wrap">$senderLabel$replyHtml$fileRow$caption<div class="sd-msg-footer">$timeRow$statusHtml</div></div>${if (isMe) myAvatarHtml else ""}</div>"""
}

/** Оверлей просмотра вложения из чата (крестик — назад в чат). Создаётся один раз на body. */
private fun ensureChatFileViewerOverlay() {
    if (document.getElementById("sd-chat-file-viewer") != null) return
    val wrap = document.createElement("div")
    wrap.id = "sd-chat-file-viewer"
    wrap.className = "sd-chat-file-viewer sd-hidden"
    wrap.setAttribute("aria-hidden", "true")
    wrap.setAttribute("role", "dialog")
    wrap.innerHTML = """
        <button type="button" id="sd-chat-file-viewer-close" class="sd-chat-file-viewer-close" title="Закрыть" aria-label="Закрыть">$SD_ICON_CLOSE_SVG</button>
        <div class="sd-chat-file-viewer-inner">
            <img id="sd-chat-file-viewer-img" class="sd-hidden sd-chat-file-viewer-img-el" alt="" />
            <iframe id="sd-chat-file-viewer-frame" class="sd-hidden sd-chat-file-viewer-frame-el" title="Файл"></iframe>
            <div id="sd-chat-file-viewer-extra" class="sd-chat-file-viewer-extra sd-hidden">
                <p id="sd-chat-file-viewer-filename" class="sd-chat-file-viewer-filename"></p>
                <a id="sd-chat-file-viewer-open-new" class="sd-btn sd-btn-secondary sd-chat-file-viewer-open-new" href="#" target="_blank" rel="noopener noreferrer">Открыть в новой вкладке</a>
            </div>
        </div>
    """.trimIndent()
    document.body?.appendChild(wrap)
    document.getElementById("sd-chat-file-viewer-close")?.addEventListener("click", { closeChatFileViewer() })
    wrap.addEventListener("click", { e: dynamic ->
        if (e?.target === wrap) closeChatFileViewer()
    })
    if (!chatFileViewerEscapeListenerBound) {
        chatFileViewerEscapeListenerBound = true
        document.addEventListener("keydown", { e: dynamic ->
            if (e?.key != "Escape") return@addEventListener
            val v = document.getElementById("sd-chat-file-viewer") ?: return@addEventListener
            if (v.classList.contains("sd-hidden")) return@addEventListener
            closeChatFileViewer()
            (e as? org.w3c.dom.events.Event)?.preventDefault()
        }, true)
    }
}

private fun openChatFileViewer(fileUrl: String, displayName: String, isImage: Boolean) {
    ensureChatFileViewerOverlay()
    val overlay = document.getElementById("sd-chat-file-viewer") ?: return
    val img = document.getElementById("sd-chat-file-viewer-img") as? org.w3c.dom.HTMLImageElement
    val frame = document.getElementById("sd-chat-file-viewer-frame") as? org.w3c.dom.HTMLIFrameElement
    val extra = document.getElementById("sd-chat-file-viewer-extra")
    val fname = document.getElementById("sd-chat-file-viewer-filename")
    val openNew = document.getElementById("sd-chat-file-viewer-open-new") as? org.w3c.dom.HTMLAnchorElement
    if (isImage && img != null) {
        img.classList.remove("sd-hidden")
        img.src = fileUrl
        img.alt = displayName
        frame?.classList?.add("sd-hidden")
        try {
            frame?.removeAttribute("src")
        } catch (_: Throwable) {
        }
        frame?.setAttribute("src", "about:blank")
        extra?.classList?.add("sd-hidden")
    } else {
        img?.classList?.add("sd-hidden")
        try {
            img?.removeAttribute("src")
        } catch (_: Throwable) {
        }
        frame?.classList?.remove("sd-hidden")
        frame?.setAttribute("src", fileUrl)
        fname?.textContent = displayName
        openNew?.href = fileUrl
        extra?.classList?.remove("sd-hidden")
    }
    overlay.classList.remove("sd-hidden")
    overlay.setAttribute("aria-hidden", "false")
    val bodyStyle = (document.body as? org.w3c.dom.HTMLElement)?.asDynamic().style
    bodyStyle.overflow = "hidden"
}

private fun closeChatFileViewer() {
    val overlay = document.getElementById("sd-chat-file-viewer") ?: return
    val img = document.getElementById("sd-chat-file-viewer-img") as? org.w3c.dom.HTMLImageElement
    val frame = document.getElementById("sd-chat-file-viewer-frame") as? org.w3c.dom.HTMLIFrameElement
    img?.classList?.add("sd-hidden")
    try {
        img?.removeAttribute("src")
    } catch (_: Throwable) {
    }
    frame?.classList?.add("sd-hidden")
    frame?.setAttribute("src", "about:blank")
    document.getElementById("sd-chat-file-viewer-extra")?.classList?.add("sd-hidden")
    overlay.classList.add("sd-hidden")
    overlay.setAttribute("aria-hidden", "true")
    val bodyStyle = (document.body as? org.w3c.dom.HTMLElement)?.asDynamic().style
    bodyStyle.overflow = ""
}

private fun renderChatTabContent(currentUser: User): String {
    if (currentUser.role == "admin" && appState.chatAdminCorrespondenceMode) {
        return renderAdminCorrespondenceChatTab(currentUser)
    }
    val contactId = appState.selectedChatContactId
    val contacts = appState.chatContacts
    val loading = appState.chatContactsLoading
    val messages = appState.chatMessages
    val myId = currentUser.id
    val groupId = appState.selectedChatGroupId
    if (groupId != null) {
        val grp = appState.chatGroups.find { it.id == groupId } ?: return """<p class="sd-error">Группа не найдена.</p>"""
        if (myId !in grp.memberIds) return """<p class="sd-error">У вас нет доступа к этой группе.</p>"""
        val iconCheck = """<svg class="sd-msg-check" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>"""
        val iconSendSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>"""
        val iconBackSvg = SD_ICON_BACK_CHEVRON_SVG
        val iconMicSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>"""
        val iconAttachSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>"""
        val iconPlaySvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>"""
        val iconPauseSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>"""
        fun formatVoiceDuration(sec: Int): String {
            val m = sec / 60
            val s = sec % 60
            return "$m:${s.toString().padStart(2, '0')}"
        }
        val myAvatarUrl = appState.user?.chatAvatarUrl?.takeIf { it.isNotBlank() } ?: getChatAvatarDataUrl(myId)
        val myInitials = appState.user?.let { it.initials().ifEmpty { "?" }.escapeHtml() } ?: "?"
        val myAvatarBg = avatarColorForId(myId).escapeHtml()
        val myAvatarHtml = if (myAvatarUrl != null) """<div class="sd-msg-my-avatar sd-avatar-wrap" data-initials="$myInitials" data-bg="$myAvatarBg"><img src="${myAvatarUrl.escapeHtml()}" alt="" class="sd-msg-avatar-img sd-avatar-img" decoding="async" data-user-id="${myId.escapeHtml()}" /></div>""" else ""
        val visibleMessages = messages.filterNot { it.deletedForUserIds.contains(myId) }
        val msgsHtml = visibleMessages.joinToString("") { msg ->
            val isMe = msg.senderId == myId
            val themUser = if (!isMe) contacts.find { it.id == msg.senderId } else null
            val otherAvatarHtml = if (themUser != null) chatOtherUserAvatarHtml(themUser) else """<div class="sd-msg-them-avatar"><span class="sd-msg-them-avatar-initials">?</span></div>"""
            val senderLabel = if (!isMe) """<div class="sd-msg-group-sender">${formatShortName(themUser?.fullName ?: "Участник").escapeHtml()}</div>""" else ""
            val cls = if (isMe) "sd-msg sd-msg-me" else "sd-msg sd-msg-them"
            val timeStr = formatMessageDateTime(msg.timestamp).escapeHtml()
            val replyTargetClass = if (appState.chatReplyToMessageId == msg.id) " sd-msg-reply-target" else ""
            val statusHtml = if (isMe) {
                val isRead = msg.status == "read"
                val checks = if (isRead) """<span class="sd-msg-check-wrap">$iconCheck</span><span class="sd-msg-check-wrap">$iconCheck</span>""" else """<span class="sd-msg-check-wrap">$iconCheck</span>"""
                val checkClass = if (isRead) "sd-msg-checks sd-msg-checks-read" else "sd-msg-checks sd-msg-checks-sent"
                """<span class="$checkClass" title="${if (isRead) "Прочитано" else "Доставлено"}">$checks</span>"""
            } else ""
            val timeRow = """<span class="sd-msg-time">$timeStr</span>"""
            val replyHtml = msg.replyToText?.takeIf { it.isNotBlank() }?.let { rText ->
                """<div class="sd-msg-reply-snippet">${rText.escapeHtml()}</div>"""
            } ?: ""
            if (msg.isVoice && !msg.voiceUrl.isNullOrBlank() && (msg.voiceDurationSec ?: 0) > 0) {
                val dur = msg.voiceDurationSec!!
                val totalStr = formatVoiceDuration(dur).escapeHtml()
                val isPlaying = appState.chatPlayingVoiceId == msg.id && !appState.chatVoicePlaybackPaused
                val currentMs = if (msg.id == appState.chatPlayingVoiceId) appState.chatPlayingVoiceCurrentMs else 0
                val progress = if (dur > 0) (currentMs.toDouble() / 1000 / dur).coerceIn(0.0, 1.0) else 0.0
                val progressPct = (progress * 100).toInt()
                val currentStr = formatVoiceDuration((currentMs / 1000).coerceAtLeast(0).coerceAtMost(dur)).escapeHtml()
                """<div class="$cls sd-msg-voice$replyTargetClass" data-msg-id="${msg.id.escapeHtml()}" data-msg-text="${msg.text.escapeHtml()}" data-voice-id="${msg.id.escapeHtml()}" data-voice-url="${msg.voiceUrl.escapeHtml()}" data-voice-duration="$dur">
                    ${if (isMe) "" else otherAvatarHtml}
                    <div class="sd-msg-bubble-wrap">
                    $senderLabel
                    $replyHtml
                    <div class="sd-voice-player">
                        <button type="button" class="sd-voice-play-btn" title="${if (isPlaying) "Пауза" else "Воспроизвести"}" aria-label="${if (isPlaying) "Пауза" else "Воспроизвести"}">${if (isPlaying) iconPauseSvg else iconPlaySvg}</button>
                        <div class="sd-voice-progress-wrap">
                            ${if (isPlaying || currentMs > 0) """<div class="sd-voice-progress-bar" style="width:${progressPct}%"></div>""" else ""}
                            <div class="sd-voice-times"><span class="sd-voice-current">$currentStr</span><span class="sd-voice-total">$totalStr</span></div>
                        </div>
                    </div>
                    <audio id="sd-voice-audio-${msg.id.escapeHtml()}" class="sd-voice-audio" preload="metadata"></audio>
                    <div class="sd-msg-footer">$timeRow$statusHtml</div>
                    </div>
                    ${if (isMe) myAvatarHtml else ""}
                </div>"""
            } else if (msg.isFile && !msg.fileUrl.isNullOrBlank()) {
                chatFileMessageBubbleHtml(msg, cls, replyTargetClass, replyHtml, timeRow, statusHtml, otherAvatarHtml, myAvatarHtml, isMe, senderLabel)
            } else {
                """<div class="$cls$replyTargetClass" data-msg-id="${msg.id.escapeHtml()}" data-msg-text="${msg.text.escapeHtml()}">${if (isMe) "" else otherAvatarHtml}<div class="sd-msg-bubble-wrap">$senderLabel<span class="sd-msg-text">${msg.text.escapeHtml()}</span><div class="sd-msg-footer">$timeRow$statusHtml</div></div>${if (isMe) myAvatarHtml else ""}</div>"""
            }
        }
        val recording = appState.chatVoiceRecording
        val reviewReady = appState.chatVoiceReviewReady
        val chatFileAttachHtml = """<input type="file" id="sd-chat-file-input" class="sd-hidden" aria-hidden="true" />
                    <button type="button" id="sd-chat-file-btn" class="sd-chat-attach-btn" title="Прикрепить файл" aria-label="Прикрепить файл">$iconAttachSvg</button>"""
        val voiceRowHtml = buildChatVoiceInputRowInnerHtml()
        val inputRowHtml = when {
            voiceRowHtml != null && chatVoiceIsWebAndroidLike() -> chatVoicePlaceholderRowHtml()
            voiceRowHtml != null -> voiceRowHtml
            else -> {
                val replyComposerHtml = appState.chatReplyToText?.takeIf { it.isNotBlank() }?.let { txt ->
                    """<div class="sd-chat-reply-preview" id="sd-chat-reply-preview">
                        <span class="sd-chat-reply-preview-text">${txt.escapeHtml()}</span>
                        <button type="button" id="sd-chat-reply-cancel" class="sd-chat-reply-cancel-btn" title="Отменить ответ">$SD_ICON_CLOSE_SVG</button>
                    </div>"""
                } ?: ""
                """<div class="sd-chat-input-row">
                    $replyComposerHtml
                    $chatFileAttachHtml
                    <input type="text" id="sd-chat-input" class="sd-chat-input" placeholder="" maxlength="2000" aria-label="Сообщение" />
                    <button type="button" id="sd-chat-send" class="sd-chat-send-btn" title="Отправить">$iconSendSvg</button>
                    <button type="button" id="sd-chat-voice-mic" class="sd-chat-voice-mic-btn" title="Запись голосового сообщения" aria-label="Запись голоса">$iconMicSvg</button>
                </div>"""
            }
        }
        val adminGroupBtns = if (currentUser.role == "admin") """<span class="sd-chat-group-actions"><button type="button" id="sd-chat-group-edit" class="sd-chat-create-group-btn">Редактировать</button><button type="button" id="sd-chat-group-delete" class="sd-chat-create-group-btn sd-chat-create-group-btn--danger">Удалить группу</button></span>""" else ""
        val conversationClass = when {
            recording -> "sd-chat-conversation sd-chat-conversation--voice-recording"
            reviewReady -> "sd-chat-conversation sd-chat-conversation--voice-preview"
            else -> "sd-chat-conversation"
        }
        val grpAvatarBg = avatarColorForId(grp.id).escapeHtml()
        val grpInitials = groupChatNameInitials(grp.name).escapeHtml()
        val grpHeaderAvatarHtml = grp.chatAvatarUrl?.takeIf { it.isNotBlank() }?.let { url ->
            """<div class="sd-chat-header-avatar sd-avatar-wrap" data-initials="$grpInitials" data-bg="$grpAvatarBg"><img src="${url.escapeHtml()}" alt="" class="sd-avatar-img" decoding="async" data-group-id="${grp.id.escapeHtml()}" /></div>"""
        } ?: """<div class="sd-chat-header-avatar" style="background:$grpAvatarBg">$grpInitials</div>"""
        return """
            <div class="sd-chat-tab">
            <div class="$conversationClass">
                <div class="sd-chat-header sd-chat-header-group">
                    <button type="button" id="sd-chat-back" class="sd-chat-back-btn" title="Назад" aria-label="Назад">$iconBackSvg</button>
                    $grpHeaderAvatarHtml
                    <span class="sd-chat-contact-name">${"Группа: ${grp.name} (${grp.memberIds.size} ${participantsWord(grp.memberIds.size)})".escapeHtml()}</span>
                    $adminGroupBtns
                </div>
                <div class="sd-chat-messages" id="sd-chat-messages">$msgsHtml</div>
                $inputRowHtml
            </div>
            </div>
        """.trimIndent()
    }
    if (contactId != null) {
        /* Список контактов может прийти позже выбранного id — иначе рендер обрывался без поля ввода/микрофона. */
        val contact = contacts.find { it.id == contactId }
            ?: User(id = contactId, fullName = "Контакт", role = "", isActive = true)
        val iconCheck = """<svg class="sd-msg-check" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>"""
        val iconSendSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>"""
        val iconBackSvg = SD_ICON_BACK_CHEVRON_SVG
        val iconMicSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>"""
        val iconAttachSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>"""
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
        val myAvatarUrl = appState.user?.chatAvatarUrl?.takeIf { it.isNotBlank() } ?: getChatAvatarDataUrl(myId)
        val myInitials = appState.user?.let { it.initials().ifEmpty { "?" }.escapeHtml() } ?: "?"
        val myAvatarBg = avatarColorForId(myId).escapeHtml()
        val myAvatarHtml = if (myAvatarUrl != null) """<div class="sd-msg-my-avatar sd-avatar-wrap" data-initials="$myInitials" data-bg="$myAvatarBg"><img src="${myAvatarUrl.escapeHtml()}" alt="" class="sd-msg-avatar-img sd-avatar-img" decoding="async" data-user-id="${myId.escapeHtml()}" /></div>""" else ""
        val otherAvatarHtml = run {
            val url = contact.chatAvatarUrl?.takeIf { it.isNotBlank() }
            if (url != null) """<div class="sd-msg-them-avatar sd-avatar-wrap" data-initials="$contactInitials" data-bg="${contactAvatarBg}"><img src="${url.escapeHtml()}" alt="" class="sd-msg-avatar-img sd-avatar-img" decoding="async" data-user-id="${contact.id.escapeHtml()}" /></div>"""
            else """<div class="sd-msg-them-avatar" style="background:$contactAvatarBg"><span class="sd-msg-them-avatar-initials">$contactInitials</span></div>"""
        }
        val visibleMessages = messages.filterNot { it.deletedForUserIds.contains(myId) }
        val msgsHtml = visibleMessages.joinToString("") { msg ->
            val isMe = msg.senderId == myId
            val cls = if (isMe) "sd-msg sd-msg-me" else "sd-msg sd-msg-them"
            val timeStr = formatMessageDateTime(msg.timestamp).escapeHtml()
            val replyTargetClass = if (appState.chatReplyToMessageId == msg.id) " sd-msg-reply-target" else ""
            val statusHtml = if (isMe) {
                val isRead = msg.status == "read"
                val checks = if (isRead) """<span class="sd-msg-check-wrap">$iconCheck</span><span class="sd-msg-check-wrap">$iconCheck</span>""" else """<span class="sd-msg-check-wrap">$iconCheck</span>"""
                val checkClass = if (isRead) "sd-msg-checks sd-msg-checks-read" else "sd-msg-checks sd-msg-checks-sent"
                """<span class="$checkClass" title="${if (isRead) "Прочитано" else "Доставлено"}">$checks</span>"""
            } else ""
            val timeRow = """<span class="sd-msg-time">$timeStr</span>"""
            val replyHtml = msg.replyToText?.takeIf { it.isNotBlank() }?.let { rText ->
                """<div class="sd-msg-reply-snippet">${rText.escapeHtml()}</div>"""
            } ?: ""
            if (msg.isVoice && !msg.voiceUrl.isNullOrBlank() && (msg.voiceDurationSec ?: 0) > 0) {
                val dur = msg.voiceDurationSec!!
                val totalStr = formatVoiceDuration(dur).escapeHtml()
                val isPlaying = appState.chatPlayingVoiceId == msg.id && !appState.chatVoicePlaybackPaused
                val currentMs = if (msg.id == appState.chatPlayingVoiceId) appState.chatPlayingVoiceCurrentMs else 0
                val progress = if (dur > 0) (currentMs.toDouble() / 1000 / dur).coerceIn(0.0, 1.0) else 0.0
                val progressPct = (progress * 100).toInt()
                val currentStr = formatVoiceDuration((currentMs / 1000).coerceAtLeast(0).coerceAtMost(dur)).escapeHtml()
                """<div class="$cls sd-msg-voice$replyTargetClass" data-msg-id="${msg.id.escapeHtml()}" data-msg-text="${msg.text.escapeHtml()}" data-voice-id="${msg.id.escapeHtml()}" data-voice-url="${msg.voiceUrl.escapeHtml()}" data-voice-duration="$dur">
                    ${if (isMe) "" else otherAvatarHtml}
                    <div class="sd-msg-bubble-wrap">
                    $replyHtml
                    <div class="sd-voice-player">
                        <button type="button" class="sd-voice-play-btn" title="${if (isPlaying) "Пауза" else "Воспроизвести"}" aria-label="${if (isPlaying) "Пауза" else "Воспроизвести"}">${if (isPlaying) iconPauseSvg else iconPlaySvg}</button>
                        <div class="sd-voice-progress-wrap">
                            ${if (isPlaying || currentMs > 0) """<div class="sd-voice-progress-bar" style="width:${progressPct}%"></div>""" else ""}
                            <div class="sd-voice-times"><span class="sd-voice-current">$currentStr</span><span class="sd-voice-total">$totalStr</span></div>
                        </div>
                    </div>
                    <audio id="sd-voice-audio-${msg.id.escapeHtml()}" class="sd-voice-audio" preload="metadata"></audio>
                    <div class="sd-msg-footer">$timeRow$statusHtml</div>
                    </div>
                    ${if (isMe) myAvatarHtml else ""}
                </div>"""
            } else if (msg.isFile && !msg.fileUrl.isNullOrBlank()) {
                chatFileMessageBubbleHtml(msg, cls, replyTargetClass, replyHtml, timeRow, statusHtml, otherAvatarHtml, myAvatarHtml, isMe, "")
            } else {
                """<div class="$cls$replyTargetClass" data-msg-id="${msg.id.escapeHtml()}" data-msg-text="${msg.text.escapeHtml()}">${if (isMe) "" else otherAvatarHtml}<div class="sd-msg-bubble-wrap"><span class="sd-msg-text">${msg.text.escapeHtml()}</span><div class="sd-msg-footer">$timeRow$statusHtml</div></div>${if (isMe) myAvatarHtml else ""}</div>"""
            }
        }
        val recording = appState.chatVoiceRecording
        val reviewReady = appState.chatVoiceReviewReady
        val chatFileAttachHtml = """<input type="file" id="sd-chat-file-input" class="sd-hidden" aria-hidden="true" />
                    <button type="button" id="sd-chat-file-btn" class="sd-chat-attach-btn" title="Прикрепить файл" aria-label="Прикрепить файл">$iconAttachSvg</button>"""
        val voiceRowHtml = buildChatVoiceInputRowInnerHtml()
        val inputRowHtml = when {
            voiceRowHtml != null && chatVoiceIsWebAndroidLike() -> chatVoicePlaceholderRowHtml()
            voiceRowHtml != null -> voiceRowHtml
            else -> {
                val replyComposerHtml = appState.chatReplyToText?.takeIf { it.isNotBlank() }?.let { txt ->
                    """<div class="sd-chat-reply-preview" id="sd-chat-reply-preview">
                        <span class="sd-chat-reply-preview-text">${txt.escapeHtml()}</span>
                        <button type="button" id="sd-chat-reply-cancel" class="sd-chat-reply-cancel-btn" title="Отменить ответ">$SD_ICON_CLOSE_SVG</button>
                    </div>"""
                } ?: ""
                """<div class="sd-chat-input-row">
                    $replyComposerHtml
                    $chatFileAttachHtml
                    <input type="text" id="sd-chat-input" class="sd-chat-input" placeholder="" maxlength="2000" aria-label="Сообщение" />
                    <button type="button" id="sd-chat-send" class="sd-chat-send-btn" title="Отправить">$iconSendSvg</button>
                    <button type="button" id="sd-chat-voice-mic" class="sd-chat-voice-mic-btn" title="Запись голосового сообщения" aria-label="Запись голоса">$iconMicSvg</button>
                </div>"""
            }
        }
        val conversationClass = when {
            recording -> "sd-chat-conversation sd-chat-conversation--voice-recording"
            reviewReady -> "sd-chat-conversation sd-chat-conversation--voice-preview"
            else -> "sd-chat-conversation"
        }
        return """
            <div class="sd-chat-tab">
            <div class="$conversationClass">
                <div class="sd-chat-header">
                    <button type="button" id="sd-chat-back" class="sd-chat-back-btn" title="Назад" aria-label="Назад">$iconBackSvg</button>
                    ${contact.chatAvatarUrl?.takeIf { it.isNotBlank() }?.let { url -> """<div class="sd-chat-header-avatar sd-avatar-wrap" data-initials="$contactInitials" data-bg="${contactAvatarBg}"><img src="${url.escapeHtml()}" alt="" class="sd-avatar-img" decoding="async" data-user-id="${contact.id.escapeHtml()}" /></div>""" } ?: """<div class="sd-chat-header-avatar" style="background:$contactAvatarBg">$contactInitials</div>"""}
                    <span class="sd-chat-contact-name">${formatShortName(contact.fullName).escapeHtml()}</span>
                </div>
                <div class="sd-chat-messages" id="sd-chat-messages">$msgsHtml</div>
                $inputRowHtml
            </div>
            </div>
        """.trimIndent()
    }
    val loadingLine = if (loading) """<p class="sd-chat-loading-text">Загрузка контактов…</p>""" else ""
    val iconRefreshSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>"""
    fun contactRow(c: User) = run {
        val initials = c.initials().ifEmpty { "?" }.escapeHtml()
        val avatarBg = avatarColorForId(c.id).escapeHtml()
        val contactAvatarHtml = c.chatAvatarUrl?.takeIf { it.isNotBlank() }?.let { url ->
            """<span class="sd-chat-contact-avatar sd-avatar-wrap" data-initials="$initials" data-bg="$avatarBg"><img src="${url.escapeHtml()}" alt="" class="sd-avatar-img" decoding="async" data-user-id="${c.id.escapeHtml()}" /></span>"""
        } ?: """<span class="sd-chat-contact-avatar" style="background:$avatarBg">$initials</span>"""
        val isOnline = appState.chatContactOnlineIds.contains(c.id)
        val onlineDot = if (isOnline) """<span class="sd-chat-contact-dot sd-chat-contact-dot-online"></span>""" else """<span class="sd-chat-contact-dot sd-chat-contact-dot-offline"></span>"""
        val roleLabel = when (c.role) { "instructor" -> "Инструктор" "cadet" -> "Курсант" "admin" -> "Администратор" else -> c.role }.escapeHtml()
        val statusText = if (isOnline) "в сети" else "не в сети"
        val statusCls = if (isOnline) "sd-chat-contact-status-online" else "sd-chat-contact-status-offline"
        val unread = appState.chatUnreadCounts[c.id] ?: 0
        val unreadBadge = if (unread > 0) """<span class="sd-chat-contact-unread">${if (unread > 99) "99+" else unread.toString()}</span>""" else ""
        """<button type="button" class="sd-chat-contact" data-contact-id="${c.id.escapeHtml()}">
            <div class="sd-chat-contact-avatar-wrap">
                $contactAvatarHtml
                $onlineDot
            </div>
            <span class="sd-chat-contact-info">
                <span class="sd-chat-contact-name-row">${formatShortName(c.fullName).escapeHtml()}</span>
                <span class="sd-chat-contact-meta"><span class="sd-chat-contact-role">$roleLabel</span> · <span class="sd-chat-contact-status $statusCls">$statusText</span></span>
            </span>
            $unreadBadge
        </button>"""
    }
    val chatGroupsList = appState.chatGroups.sortedBy { it.name.lowercase() }
    fun groupRow(g: ChatGroup) = run {
        val gKey = chatUnreadKeyForGroup(g.id)
        val unread = appState.chatUnreadCounts[gKey] ?: 0
        val unreadBadge = if (unread > 0) """<span class="sd-chat-contact-unread">${if (unread > 99) "99+" else unread.toString()}</span>""" else ""
        val gInitials = groupChatNameInitials(g.name).escapeHtml()
        val gBg = avatarColorForId(g.id).escapeHtml()
        val gAvatarHtml = g.chatAvatarUrl?.takeIf { it.isNotBlank() }?.let { url ->
            """<span class="sd-chat-contact-avatar sd-avatar-wrap" data-initials="$gInitials" data-bg="$gBg"><img src="${url.escapeHtml()}" alt="" class="sd-avatar-img" decoding="async" data-group-id="${g.id.escapeHtml()}" /></span>"""
        } ?: """<span class="sd-chat-contact-avatar" style="background:$gBg">$gInitials</span>"""
        """<button type="button" class="sd-chat-group-row" data-chat-group-id="${g.id.escapeHtml()}">
            <div class="sd-chat-contact-avatar-wrap">$gAvatarHtml</div>
            <span class="sd-chat-group-label">Группа: ${g.name.escapeHtml()} (${g.memberIds.size} ${participantsWord(g.memberIds.size)})</span>
            $unreadBadge
        </button>"""
    }
    val groupsSection = if (chatGroupsList.isEmpty()) "" else """<div class="sd-chat-contacts-group"><p class="sd-chat-contacts-group-title">Группы</p><div class="sd-chat-contacts">${chatGroupsList.joinToString("") { groupRow(it) }}</div></div>"""
    val instructors = contacts.filter { it.role == "instructor" }.sortedBy { it.fullName }
    val cadets = contacts.filter { it.role == "cadet" }.sortedBy { it.fullName }
    val others = contacts.filter { it.role !in listOf("instructor", "cadet") }.sortedBy { it.fullName }
    val instructorsSection = if (instructors.isEmpty()) "" else """<div class="sd-chat-contacts-group"><p class="sd-chat-contacts-group-title">Инструкторы</p><div class="sd-chat-contacts">${instructors.joinToString("") { contactRow(it) }}</div></div>"""
    val cadetsSection = if (cadets.isEmpty()) "" else """<div class="sd-chat-contacts-group"><p class="sd-chat-contacts-group-title">Курсанты</p><div class="sd-chat-contacts">${cadets.joinToString("") { contactRow(it) }}</div></div>"""
    val othersSection = if (others.isEmpty()) "" else """<div class="sd-chat-contacts-group"><p class="sd-chat-contacts-group-title">Другие</p><div class="sd-chat-contacts">${others.joinToString("") { contactRow(it) }}</div></div>"""
    val contactsBlock = if (contacts.isEmpty() && chatGroupsList.isEmpty() && !loading) """<p class="sd-chat-empty-hint">Нет доступных контактов.</p>""" else """$groupsSection$instructorsSection$cadetsSection$othersSection"""
    val iconSettingsSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>"""
    val corrBtnActive = if (appState.chatAdminCorrespondenceMode) " sd-chat-correspondence-btn--active" else ""
    val adminCorrBtn = if (currentUser.role == "admin") """<button type="button" id="sd-chat-admin-correspondence" class="sd-chat-create-group-btn sd-chat-create-group-btn--icon sd-chat-settings-btn sd-chat-correspondence-btn$corrBtnActive" title="Переписка" aria-label="Переписка">$iconCorrespondenceSvg</button>""" else ""
    val createGroupBtn = if (currentUser.role == "admin") """<button type="button" id="sd-chat-create-group" class="sd-chat-create-group-btn" title="Создать групповой чат" aria-label="Создать группу">$iconCreateGroupSvg Создать группу</button>""" else ""
    val settingsBtnHtml = """<button type="button" id="sd-chat-settings-btn" class="sd-chat-create-group-btn sd-chat-create-group-btn--icon sd-chat-settings-btn" title="Настройки" aria-label="Настройки">$iconSettingsSvg</button>"""
    val refreshBtnStyled = """<button type="button" id="sd-chat-refresh" class="sd-chat-create-group-btn" title="Обновить список контактов" aria-label="Обновить контакты">$iconRefreshSvg Обновить контакты</button>"""
    return """<div class="sd-chat-tab"><div class="sd-chat-list-header"><h2 class="sd-chat-title">Чат</h2><div class="sd-chat-header-actions">$adminCorrBtn$createGroupBtn$settingsBtnHtml$refreshBtnStyled</div></div>$loadingLine$contactsBlock</div>"""
}

private fun renderAdminCorrespondencePickRow(c: User, dataAttrName: String): String {
    val initials = c.initials().ifEmpty { "?" }.escapeHtml()
    val avatarBg = avatarColorForId(c.id).escapeHtml()
    val contactAvatarHtml = c.chatAvatarUrl?.takeIf { it.isNotBlank() }?.let { url ->
        """<span class="sd-chat-contact-avatar sd-avatar-wrap" data-initials="$initials" data-bg="$avatarBg"><img src="${url.escapeHtml()}" alt="" class="sd-avatar-img" decoding="async" data-user-id="${c.id.escapeHtml()}" /></span>"""
    } ?: """<span class="sd-chat-contact-avatar" style="background:$avatarBg">$initials</span>"""
    val isOnline = appState.chatContactOnlineIds.contains(c.id)
    val onlineDot = if (isOnline) """<span class="sd-chat-contact-dot sd-chat-contact-dot-online"></span>""" else """<span class="sd-chat-contact-dot sd-chat-contact-dot-offline"></span>"""
    val roleLabel = when (c.role) { "instructor" -> "Инструктор" "cadet" -> "Курсант" "admin" -> "Администратор" else -> c.role }.escapeHtml()
    val statusText = if (isOnline) "в сети" else "не в сети"
    val statusCls = if (isOnline) "sd-chat-contact-status-online" else "sd-chat-contact-status-offline"
    return """<button type="button" class="sd-chat-admin-corr-pick" $dataAttrName="${c.id.escapeHtml()}">
            <div class="sd-chat-contact-avatar-wrap">
                $contactAvatarHtml
                $onlineDot
            </div>
            <span class="sd-chat-contact-info">
                <span class="sd-chat-contact-name-row">${formatShortName(c.fullName).escapeHtml()}</span>
                <span class="sd-chat-contact-meta"><span class="sd-chat-contact-role">$roleLabel</span> · <span class="sd-chat-contact-status $statusCls">$statusText</span></span>
            </span>
        </button>"""
}

private fun adminCorrMyAvatarHtml(u: User): String {
    val myInitials = u.initials().ifEmpty { "?" }.escapeHtml()
    val myAvatarBg = avatarColorForId(u.id).escapeHtml()
    val myAvatarUrl = u.chatAvatarUrl?.takeIf { it.isNotBlank() } ?: getChatAvatarDataUrl(u.id)
    return if (myAvatarUrl != null) """<div class="sd-msg-my-avatar sd-avatar-wrap" data-initials="$myInitials" data-bg="$myAvatarBg"><img src="${myAvatarUrl.escapeHtml()}" alt="" class="sd-msg-avatar-img sd-avatar-img" decoding="async" data-user-id="${u.id.escapeHtml()}" /></div>""" else ""
}

private fun userForAdminCorr(contacts: List<User>, id: String): User =
    contacts.find { it.id == id } ?: User(id = id, fullName = "Пользователь", role = "")

private fun renderAdminCorrespondenceMessagesHtml(
    subjectId: String,
    peerId: String,
    contacts: List<User>,
    messages: List<ChatMessage>,
): String {
    val subjectUser = userForAdminCorr(contacts, subjectId)
    val iconCheck = """<svg class="sd-msg-check" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>"""
    val iconPlaySvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>"""
    val iconPauseSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>"""
    fun formatVoiceDuration(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "$m:${s.toString().padStart(2, '0')}"
    }
    val myAvatarSubject = adminCorrMyAvatarHtml(subjectUser)
    return messages.joinToString("") { msg ->
        val isMe = msg.senderId == subjectId
        val senderUser = userForAdminCorr(contacts, msg.senderId)
        val otherAvatarHtml = chatOtherUserAvatarHtml(senderUser)
        val cls = if (isMe) "sd-msg sd-msg-me" else "sd-msg sd-msg-them"
        val timeStr = formatMessageDateTime(msg.timestamp).escapeHtml()
        val replyTargetClass = ""
        val statusHtml = if (isMe) {
            val isRead = msg.status == "read"
            val checks = if (isRead) """<span class="sd-msg-check-wrap">$iconCheck</span><span class="sd-msg-check-wrap">$iconCheck</span>""" else """<span class="sd-msg-check-wrap">$iconCheck</span>"""
            val checkClass = if (isRead) "sd-msg-checks sd-msg-checks-read" else "sd-msg-checks sd-msg-checks-sent"
            """<span class="$checkClass" title="${if (isRead) "Прочитано" else "Доставлено"}">$checks</span>"""
        } else ""
        val timeRow = """<span class="sd-msg-time">$timeStr</span>"""
        val replyHtml = msg.replyToText?.takeIf { it.isNotBlank() }?.let { rText ->
            """<div class="sd-msg-reply-snippet">${rText.escapeHtml()}</div>"""
        } ?: ""
        if (msg.isVoice && !msg.voiceUrl.isNullOrBlank() && (msg.voiceDurationSec ?: 0) > 0) {
            val dur = msg.voiceDurationSec!!
            val totalStr = formatVoiceDuration(dur).escapeHtml()
            val isPlaying = appState.chatPlayingVoiceId == msg.id && !appState.chatVoicePlaybackPaused
            val currentMs = if (msg.id == appState.chatPlayingVoiceId) appState.chatPlayingVoiceCurrentMs else 0
            val progress = if (dur > 0) (currentMs.toDouble() / 1000 / dur).coerceIn(0.0, 1.0) else 0.0
            val progressPct = (progress * 100).toInt()
            val currentStr = formatVoiceDuration((currentMs / 1000).coerceAtLeast(0).coerceAtMost(dur)).escapeHtml()
            """<div class="$cls sd-msg-voice$replyTargetClass" data-msg-id="${msg.id.escapeHtml()}" data-msg-text="${msg.text.escapeHtml()}" data-voice-id="${msg.id.escapeHtml()}" data-voice-url="${msg.voiceUrl.escapeHtml()}" data-voice-duration="$dur">
                ${if (isMe) "" else otherAvatarHtml}
                <div class="sd-msg-bubble-wrap">
                $replyHtml
                <div class="sd-voice-player">
                    <button type="button" class="sd-voice-play-btn" title="${if (isPlaying) "Пауза" else "Воспроизвести"}" aria-label="${if (isPlaying) "Пауза" else "Воспроизвести"}">${if (isPlaying) iconPauseSvg else iconPlaySvg}</button>
                    <div class="sd-voice-progress-wrap">
                        ${if (isPlaying || currentMs > 0) """<div class="sd-voice-progress-bar" style="width:${progressPct}%"></div>""" else ""}
                        <div class="sd-voice-times"><span class="sd-voice-current">$currentStr</span><span class="sd-voice-total">$totalStr</span></div>
                    </div>
                </div>
                <audio id="sd-voice-audio-${msg.id.escapeHtml()}" class="sd-voice-audio" preload="metadata"></audio>
                <div class="sd-msg-footer">$timeRow$statusHtml</div>
                </div>
                ${if (isMe) myAvatarSubject else ""}
            </div>"""
        } else if (msg.isFile && !msg.fileUrl.isNullOrBlank()) {
            chatFileMessageBubbleHtml(msg, cls, replyTargetClass, replyHtml, timeRow, statusHtml, otherAvatarHtml, myAvatarSubject, isMe, "")
        } else {
            """<div class="$cls$replyTargetClass" data-msg-id="${msg.id.escapeHtml()}" data-msg-text="${msg.text.escapeHtml()}">${if (isMe) "" else otherAvatarHtml}<div class="sd-msg-bubble-wrap"><span class="sd-msg-text">${msg.text.escapeHtml()}</span><div class="sd-msg-footer">$timeRow$statusHtml</div></div>${if (isMe) myAvatarSubject else ""}</div>"""
        }
    }
}

private fun renderAdminCorrespondenceChatTab(currentUser: User): String {
    val contacts = appState.chatContacts
    val loading = appState.chatContactsLoading
    val subjectId = appState.chatAdminCorrespondenceSubjectId
    val peerId = appState.chatAdminCorrespondencePeerId
    val messages = appState.chatMessages
    val iconBackSvg = SD_ICON_BACK_CHEVRON_SVG
    val loadingLine = if (loading) """<p class="sd-chat-loading-text">Загрузка контактов…</p>""" else ""
    val corrActive = if (appState.chatAdminCorrespondenceMode) " sd-chat-correspondence-btn--active" else ""
    val adminCorrBtn = """<button type="button" id="sd-chat-admin-correspondence" class="sd-chat-create-group-btn sd-chat-create-group-btn--icon sd-chat-settings-btn sd-chat-correspondence-btn$corrActive" title="Переписка" aria-label="Переписка">$iconCorrespondenceSvg</button>"""
    val createGroupBtn = """<button type="button" id="sd-chat-create-group" class="sd-chat-create-group-btn" title="Создать групповой чат" aria-label="Создать группу">$iconCreateGroupSvg Создать группу</button>"""
    val iconSettingsSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>"""
    val settingsBtnHtml = """<button type="button" id="sd-chat-settings-btn" class="sd-chat-create-group-btn sd-chat-create-group-btn--icon sd-chat-settings-btn" title="Настройки" aria-label="Настройки">$iconSettingsSvg</button>"""
    val iconRefreshSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>"""
    val refreshBtnStyled = """<button type="button" id="sd-chat-refresh" class="sd-chat-create-group-btn" title="Обновить список контактов" aria-label="Обновить контакты">$iconRefreshSvg Обновить контакты</button>"""
    val listHeader = """<div class="sd-chat-list-header"><h2 class="sd-chat-title">Чат</h2><div class="sd-chat-header-actions">$adminCorrBtn$createGroupBtn$settingsBtnHtml$refreshBtnStyled</div></div>"""
    if (subjectId != null && peerId != null) {
        val sName = userForAdminCorr(contacts, subjectId).let { formatShortName(it.fullName) }.escapeHtml()
        val pName = userForAdminCorr(contacts, peerId).let { formatShortName(it.fullName) }.escapeHtml()
        val msgsHtml = renderAdminCorrespondenceMessagesHtml(subjectId, peerId, contacts, messages)
        return """
            <div class="sd-chat-tab">
            <div class="sd-chat-conversation sd-chat-admin-corr-conversation">
                <div class="sd-chat-header">
                    <button type="button" id="sd-chat-back" class="sd-chat-back-btn" title="Назад" aria-label="Назад">$iconBackSvg</button>
                    <span class="sd-chat-contact-name sd-chat-admin-corr-title">$sName ↔ $pName</span>
                </div>
                <div class="sd-chat-messages" id="sd-chat-messages">$msgsHtml</div>
                <div class="sd-chat-input-row sd-chat-admin-corr-readonly"><span class="sd-chat-admin-corr-readonly-text">Просмотр переписки (только чтение)</span></div>
            </div>
            </div>
        """.trimIndent()
    }
    if (subjectId != null) {
        val peers = contacts.filter { it.id != subjectId }.sortedBy { formatShortName(it.fullName).lowercase() }
        val subjName = userForAdminCorr(contacts, subjectId).let { formatShortName(it.fullName) }.escapeHtml()
        val listHtml = peers.joinToString("") { renderAdminCorrespondencePickRow(it, "data-admin-corr-peer-id") }
        val emptyPeers = if (peers.isEmpty()) """<p class="sd-chat-empty-hint">Нет других контактов для выбора.</p>""" else ""
        return """<div class="sd-chat-tab">$listHeader$loadingLine<p class="sd-chat-admin-corr-hint">Переписка: <strong>$subjName</strong> — выберите собеседника</p>$emptyPeers<div class="sd-chat-contacts sd-chat-admin-corr-list">$listHtml</div></div>"""
    }
    val sorted = contacts.sortedBy { formatShortName(it.fullName).lowercase() }
    val listHtml = sorted.joinToString("") { renderAdminCorrespondencePickRow(it, "data-admin-corr-subject-id") }
    val emptyList = if (sorted.isEmpty() && !loading) """<p class="sd-chat-empty-hint">Нет контактов. Обновите список.</p>""" else ""
    return """<div class="sd-chat-tab">$listHeader$loadingLine<p class="sd-chat-admin-corr-hint">Выберите пользователя, чью переписку с другими контактами нужно просмотреть</p>$emptyList<div class="sd-chat-contacts sd-chat-admin-corr-list">$listHtml</div></div>"""
}

/** Экранирует HTML, чтобы пользовательский ввод не приводил к XSS. В т.ч. для атрибутов (data-msg-text). */
private fun String.escapeHtml(): String = this
    .replace("&", "&amp;")
    .replace("\r\n", "&#10;")
    .replace("\n", "&#10;")
    .replace("\r", "&#10;")
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

private const val LAST_BALANCE_TS_KEY_PREFIX = "sd_last_balance_ts_"
/** Уже показанные уведомления по балансу (id документа или составной ключ), чтобы не дублировать одно списание/зачисление. */
private const val BALANCE_NOTIFIED_KEYS_PREFIX = "sd_balance_notified_keys_"
private const val MAX_BALANCE_NOTIFIED_KEYS = 200

private fun balanceHistoryEntryNotifyKey(e: BalanceHistoryEntry): String =
    if (e.id.isNotBlank()) e.id else "${e.type}|${e.amount}|${e.timestampMillis}|${e.performedBy}"

private fun loadBalanceNotifiedKeys(userId: String): MutableSet<String> {
    return try {
        val raw = window.asDynamic().localStorage?.getItem(BALANCE_NOTIFIED_KEYS_PREFIX + userId) as? String ?: return mutableSetOf()
        if (raw.isBlank()) return mutableSetOf()
        val arr = js("(function(r){ return JSON.parse(r); })").unsafeCast<(String) -> dynamic>().invoke(raw)
        val len = (arr?.length as? Number)?.toInt() ?: 0
        val getAt = js("(function(a, i){ return a[i]; })").unsafeCast<(Any, Int) -> Any?>()
        (0 until len).mapNotNull { i ->
            (if (arr != null) getAt(arr, i) else null as Any?)?.unsafeCast<dynamic>()?.toString()?.takeIf { it.isNotBlank() }
        }.toMutableSet()
    } catch (_: Throwable) { mutableSetOf() }
}

private fun saveBalanceNotifiedKeys(userId: String, ids: Set<String>) {
    try {
        val list = ids.toList().takeLast(MAX_BALANCE_NOTIFIED_KEYS)
        val json = "[" + list.joinToString(",") { "\"" + escapeJsonString(it) + "\"" } + "]"
        window.asDynamic().localStorage?.setItem(BALANCE_NOTIFIED_KEYS_PREFIX + userId, json)
    } catch (_: Throwable) { }
}

/** Склонение «талон»: 1 талон, 2 талона, 5 талонов. */
private fun ticketWord(n: Int): String {
    val a = n % 100
    if (a in 11..14) return "талонов"
    return when (n % 10) { 1 -> "талон"; 2, 3, 4 -> "талона"; else -> "талонов" }
}

/** Склонение «участник»: 1 участник, 2 участника, 5 участников. */
private fun participantsWord(n: Int): String {
    val a = n % 100
    if (a in 11..14) return "участников"
    return when (n % 10) { 1 -> "участник"; 2, 3, 4 -> "участника"; else -> "участников" }
}

/** Показывает тосты о новых операциях по балансу для указанного пользователя (зачисление/списание/установка). Одно уведомление на запись: дедуп по id Firestore + составной ключ; повторный вызов не дублирует уже показанные. */
private fun notifyNewBalanceOpsForUser(userId: String, entries: List<BalanceHistoryEntry>, users: List<User>) {
    if (entries.isEmpty()) return
    val storage = window.asDynamic().localStorage
    val keyTs = LAST_BALANCE_TS_KEY_PREFIX + userId
    val lastStr = storage?.getItem(keyTs) as? String
    val lastSeen = lastStr?.toLongOrNull() ?: 0L
    val sorted = entries.sortedBy { it.timestampMillis ?: 0L }
    val maxTsInEntries = sorted.maxOfOrNull { it.timestampMillis ?: 0L } ?: 0L
    if (maxTsInEntries <= lastSeen) return
    val seenInBatch = mutableSetOf<String>()
    val deduped = sorted.filter { e ->
        val k = balanceHistoryEntryNotifyKey(e)
        if (k in seenInBatch) false else {
            seenInBatch.add(k)
            true
        }
    }
    val notifiedKeys = loadBalanceNotifiedKeys(userId)
    var keysChanged = false
    for (e in deduped) {
        val ts = e.timestampMillis ?: 0L
        if (ts <= lastSeen) continue
        val notifyKey = balanceHistoryEntryNotifyKey(e)
        if (notifyKey in notifiedKeys) continue
        val performer = users.find { it.id == e.performedBy }
        val performerShortName = performer?.fullName?.takeIf { it.isNotBlank() }?.let { formatShortName(it) }
        val performerRole = performer?.role ?: ""
        val creditFrom = if (performerRole == "admin") "администратор" else (performerShortName ?: "Пользователь")
        val debitBy = when (performerRole) {
            "instructor" -> "инструктором ${performerShortName ?: "—"}"
            "admin" -> "администратором ${performerShortName ?: "—"}"
            else -> performerShortName?.let { "пользователем $it" } ?: "пользователем"
        }
        val setBy = when (performerRole) {
            "admin" -> "администратором"
            "instructor" -> "инструктором ${performerShortName ?: "—"}"
            else -> performerShortName?.let { "пользователем $it" } ?: "пользователем"
        }
        val dt = if (ts > 0L) formatMessageDateTime(ts) else ""
        val tw = ticketWord(e.amount)
        val msg = when (e.type) {
            "credit" -> "Вам зачислено ${e.amount} $tw от $creditFrom. Дата и время: $dt"
            "debit" -> "У вас списано ${e.amount} $tw $debitBy. Дата и время: $dt"
            "set" -> "Ваш баланс установлен на ${e.amount} $tw $setBy. Дата и время: $dt"
            else -> null
        }
        if (msg != null) {
            showNotification(msg)
            notifiedKeys.add(notifyKey)
            keysChanged = true
        }
    }
    if (keysChanged) {
        saveBalanceNotifiedKeys(userId, notifiedKeys)
    }
    if (maxTsInEntries > lastSeen) {
        storage?.setItem(keyTs, maxTsInEntries.toString())
    }
}

/** Удобный помощник: уведомления о новых операциях по балансу для текущего пользователя. */
private fun notifyNewBalanceOpsForCurrentUser(entries: List<BalanceHistoryEntry>, users: List<User>? = null) {
    val uid = appState.user?.id ?: return
    val baseList = when {
        users != null && users.isNotEmpty() -> users
        appState.historyUsers.isNotEmpty() -> appState.historyUsers
        appState.user != null -> listOf(appState.user!!)
        else -> emptyList()
    }
    val userList = if (appState.user?.role == "instructor" && appState.instructorCadets.isNotEmpty())
        (baseList + appState.instructorCadets).distinctBy { it.id }
    else
        baseList
    notifyNewBalanceOpsForUser(uid, entries, userList)
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
private val iconCarSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 14 10s-4.7.6-5.5 1.1C4.2 11.3 2 12.1 2 13v3c0 .6.4 1 1 1h2"/><circle cx="7" cy="17" r="2"/><path d="M9 17h6"/><circle cx="17" cy="17" r="2"/></svg>"""
private val iconGroupSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>"""
/** Та же иконка, что у кнопки «Создать группу» во вкладке «Чат». */
private val iconCreateGroupSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/></svg>"""
/** Два пузырька — просмотр переписки между пользователями (админ). */
private val iconCorrespondenceSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/><path d="M13 7H4a2 2 0 0 0-2 2v12l4-4h9a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z"/></svg>"""
private val iconSelectSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>"""
private val iconEyeSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>"""
private val iconCreditSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>"""
private val iconDebitSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"/></svg>"""
private val iconSetSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="9" x2="19" y2="9"/><line x1="5" y1="15" x2="19" y2="15"/></svg>"""
private val iconResetSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>"""
private val iconCalendarSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>"""
private val iconClockSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>"""
private val iconPlaySvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>"""
/** Плей для синей кнопки «Прослушать» в настройках уведомлений. */
private val iconPlaySvgWhite = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" aria-hidden="true"><polygon points="5 3 19 12 5 21 5 3" fill="#ffffff"/></svg>"""
private val iconPauseSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>"""
private val iconStopSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="6" width="12" height="12" rx="1"/></svg>"""
private val iconLateSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>"""


/** Длительность занятия вождением (минуты). */
private const val LESSON_DURATION_MINUTES = 90L
private val LESSON_DURATION_MS = LESSON_DURATION_MINUTES * 60 * 1000

/** Варианты учебного ТС (Firestore `users.trainingVehicle` для инструктора). */
private const val TRAINING_VEHICLE_LADA = "Lada Granta Х180ТА102"
private const val TRAINING_VEHICLE_RENAULT = "Renault Logan А525ВО102"

private fun trainingVehicleLabelOrDash(u: User): String =
    u.trainingVehicle?.trim()?.takeIf { it.isNotBlank() } ?: "—"

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
private var drivingTimerIntervalId: Int? = null
private var cadetWindowsPollIntervalId: Int = 0
private var instructorSessionsPollIntervalId: Int = 0
private var adminSchedulePollIntervalId: Int = 0
/** Сессии, по которым уже вызвано завершение по таймеру (чтобы не вызывать повторно). */
private val sessionCompletionRequestedIds = mutableSetOf<String>()

private fun clearDrivingTimer() {
    drivingTimerIntervalId?.let { id -> js("clearInterval").unsafeCast<(Int) -> Unit>().invoke(id) }
    drivingTimerIntervalId = null
}

/** Один кадр отрисовки таймера вождения (остаток, полоса, ползунок). */
private fun drivingTimerTick() {
    val blocks = document.querySelectorAll(".sd-sch-timer-block")
    val totalMs = (LESSON_DURATION_MS).toDouble()
    val now = js("Date.now()").unsafeCast<Double>().toLong()
    for (i in 0 until blocks.length) {
        val el = blocks.item(i) as? org.w3c.dom.Element ?: continue
        val paused = el.getAttribute("data-paused") == "true"
        val remaining = if (paused) {
            (el.getAttribute("data-remaining-ms") ?: "0").toLongOrNull() ?: 0L
        } else {
            val endMs = (el.getAttribute("data-end-ms") ?: "0").toLongOrNull() ?: 0L
            (endMs - now).coerceAtLeast(0L)
        }
        val valEl = el.querySelector(".sd-sch-timer-value")
        val fillEl = el.querySelector(".sd-sch-timer-bar-fill")
        val thumbEl = el.querySelector(".sd-sch-timer-bar-thumb") as? org.w3c.dom.HTMLElement
        if (remaining <= 0 && !paused) {
            valEl?.textContent = "Завершено"
            (fillEl as? org.w3c.dom.HTMLElement)?.style?.width = "0%"
            thumbEl?.style?.left = "0%"
            val sessionId = el.getAttribute("data-session-id")
            if (sessionId != null && sessionId !in sessionCompletionRequestedIds) {
                sessionCompletionRequestedIds.add(sessionId)
                completeDrivingSession(sessionId) { err ->
                    if (err != null) {
                        sessionCompletionRequestedIds.remove(sessionId)
                        updateState { networkError = err }
                        return@completeDrivingSession
                    }
                    val uid = appState.user?.id ?: return@completeDrivingSession
                    if (appState.user?.role == "instructor") {
                        getOpenWindowsForInstructor(uid) { wins ->
                            getSessionsForInstructor(uid) { sess ->
                                getBalanceHistory(uid) { hist ->
                                    updateState { recordingOpenWindows = wins; recordingSessions = sess; historySessions = sess; historyBalance = hist }
                                    getCurrentUser { newUser, _ -> if (newUser != null) updateState { user = newUser } }
                                    notifyNewBalanceOpsForCurrentUser(hist)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val pct = (remaining.toDouble() / totalMs * 100.0).coerceIn(0.0, 100.0)
            val mins = remaining / 60000L
            val secs = (remaining % 60000L) / 1000L
            val mm = if (mins < 10) "0$mins" else "$mins"
            val ss = if (secs < 10) "0$secs" else "$secs"
            valEl?.textContent = if (paused) "$mm:$ss (пауза)" else "$mm:$ss"
            (fillEl as? org.w3c.dom.HTMLElement)?.style?.width = "$pct%"
            thumbEl?.style?.left = "$pct%"
        }
    }
}

/**
 * Запускает интервал обновления таймера вождения. Не перезапускает интервал при каждом рендере —
 * иначе DOM с --:-- ждёт до 1 с до первого тика и ползунок «прыгает» влево.
 */
private fun startDrivingTimers() {
    val blocks = document.querySelectorAll(".sd-sch-timer-block")
    if (blocks.length == 0) {
        clearDrivingTimer()
        return
    }
    if (drivingTimerIntervalId != null) {
        drivingTimerTick()
        return
    }
    drivingTimerIntervalId = js("setInterval").unsafeCast<(Any?, Int) -> Int>().invoke({
        drivingTimerTick()
    }, 1000)
    drivingTimerTick()
}

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

/** Сигнатура списков для опроса: не вызывать updateState, если данные не изменились — иначе перерисовка рвёт открытый select/datetime.
 * Важно включать все поля, от которых зависит UI «Моё вождение» / график инструктора: иначе при смене только
 * startRequestedByInstructor / cadetConfirmed / таймера сессии сигнатура не меняется и кнопки/таймер не появляются до перезахода.
 */
private fun recordingPollSignature(wins: List<InstructorOpenWindow>, sess: List<DrivingSession>): String {
    val w = wins.sortedBy { it.id }.joinToString(";") { "${it.id}:${it.dateTimeMillis}:${it.status}" }
    val s = sess.sortedBy { it.id }.joinToString(";") {
        "${it.id}:${it.status}:${it.startTimeMillis}:${it.cadetId}:${it.instructorConfirmed}:${it.openWindowId}:" +
            "${it.startRequestedByInstructor}:${it.cadetConfirmed}:${it.actualStartMs}:${it.sessionIsActive}:${it.sessionPausedAt}"
    }
    return "$w|$s"
}

/** Сигнатура карты расписания админа для бейджа и опроса (без лишних перерисовок). */
private fun computeAdminScheduleSignature(map: Map<String, List<DrivingSession>>): String {
    return map.entries.sortedBy { it.key }.joinToString("|") { (iid, list) ->
        val s = list.sortedBy { it.id }.joinToString(";") {
            "${it.id}:${it.status}:${it.startTimeMillis}:${it.cadetId}:${it.cadetConfirmed}:${it.openWindowId}"
        }
        "$iid:$s"
    }
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

private fun isModalOpenById(id: String): Boolean =
    document.getElementById(id)?.classList?.contains("sd-hidden") == false

private fun syncStartEarlyModalVisibility() {
    val modal = document.getElementById("sd-start-early-modal") ?: return
    val sid = startEarlyModalSessionId
    if (sid.isNullOrBlank()) {
        modal.classList.add("sd-hidden")
        return
    }
    modal.asDynamic().dataset["sessionId"] = sid
    document.getElementById("sd-start-early-mins")?.textContent = startEarlyModalMinutesLeft.toString()
    modal.classList.remove("sd-hidden")
}

private fun syncInstructorActionModalsVisibility() {
    fun syncSimple(id: String, key: String, value: String?) {
        val m = document.getElementById(id) ?: return
        if (value.isNullOrBlank()) {
            m.classList.add("sd-hidden")
        } else {
            m.asDynamic().dataset[key] = value
            m.classList.remove("sd-hidden")
        }
    }
    syncSimple("sd-running-late-modal", "sessionId", runningLateModalSessionId)
    syncSimple("sd-home-cancel-unconfirmed-modal", "sessionId", homeCancelUnconfirmedModalSessionId)
    syncSimple("sd-instructor-cancel-reason-modal", "sessionId", cancelReasonModalSessionId)
    syncSimple("sd-rec-delete-window-confirm-modal", "windowId", recDeleteWindowModalWindowId)
    syncSimple("sd-instructor-rate-cadet-modal", "sessionId", instructorRateCadetModalSessionId)

    val complete = document.getElementById("sd-complete-early-modal")
    if (completeEarlyModalSessionId.isNullOrBlank()) {
        complete?.classList?.add("sd-hidden")
    } else {
        // dataset — DOMStringMap: без .set() и без asDynamic() на dataset (не Kotlin-объект)
        val c = complete
        if (c != null) {
            c.asDynamic().dataset["sessionId"] = completeEarlyModalSessionId
        }
        document.getElementById("sd-complete-early-text")?.textContent = completeEarlyModalText
        complete?.classList?.remove("sd-hidden")
    }
    document.getElementById("sd-instructor-rate-cadet-name")?.textContent = instructorRateCadetModalCadetName
    val rateSid = instructorRateCadetModalSessionId
    if (rateSid.isNullOrBlank()) {
        instructorRateModalRadioInitSessionId = null
    } else {
        val m = document.getElementById("sd-instructor-rate-cadet-modal")
        if (m != null && !m.classList.contains("sd-hidden")) {
            if (instructorRateModalRadioInitSessionId != rateSid) {
                instructorRateModalRadioInitSessionId = rateSid
                val rateInputs = document.querySelectorAll("input[name=sd-instructor-rate]")
                for (j in 0 until rateInputs.length) {
                    (rateInputs.item(j) as? HTMLInputElement)?.checked = false
                }
            }
        }
    }
}

private fun formatTimeOnly(ms: Long?): String {
    if (ms == null || ms <= 0) return "—"
    val d = js("new Date(ms)")
    return js("d.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })").unsafeCast<String>()
}

private fun weekdayNameLongRu(ms: Long?): String {
    if (ms == null || ms <= 0) return "—"
    val d = js("new Date(ms)")
    return js("d.toLocaleDateString('ru-RU', { weekday: 'long' })").unsafeCast<String>()
}

private fun weekdayNameShortRu(ms: Long?): String {
    if (ms == null || ms <= 0) return "—"
    val d = js("new Date(ms)")
    val idx = when ((d.getDay() as Int).coerceIn(0, 6)) {
        0 -> 6
        else -> (d.getDay() as Int) - 1
    }
    return listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")[idx]
}

/** Ключ сортировки ГГГГ-ММ для группировки истории по месяцам. */
private fun monthSortKeyFromMillis(ms: Long): String {
    if (ms <= 0) return "0000-00"
    val d = js("new Date(ms)")
    val y = d.getFullYear() as Int
    val m = (d.getMonth() as Int) + 1
    return "$y-${m.toString().padStart(2, '0')}"
}

/** Подзаголовок месяца в истории: «март, 26г.» */
private fun monthYearTitleRu(ms: Long): String {
    if (ms <= 0) return "—"
    val d = js("new Date(ms)")
    val months = listOf(
        "январь", "февраль", "март", "апрель", "май", "июнь",
        "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь",
    )
    val mi = (d.getMonth() as Int).coerceIn(0, 11)
    val y = d.getFullYear() as Int
    val yy = (y % 100).toString().padStart(2, '0')
    return "${months[mi]}, ${yy}г."
}

/** Последовательное удаление сессий истории (завершённые/отменённые). */
private fun deleteInstructorHistorySessionsSequential(sessionIds: List<String>, onDone: (String?) -> Unit) {
    if (sessionIds.isEmpty()) {
        onDone(null)
        return
    }
    var i = 0
    fun step() {
        deleteDrivingSession(sessionIds[i]) { err ->
            if (err != null) {
                onDone(err)
                return@deleteDrivingSession
            }
            i++
            if (i >= sessionIds.size) onDone(null) else step()
        }
    }
    step()
}

/** Нижняя панель вкладок по роли (индексы должны совпадать везде). */
private fun getTabsForUserRole(role: String): List<String> = when (role) {
    "admin" -> listOf("Главная", "Баланс", "Расписание", "Чат", "История")
    "instructor" -> listOf("Главная", "Запись", "Чат", "Билеты", "История")
    "cadet" -> listOf("Главная", "Запись", "Чат", "Билеты", "История")
    else -> listOf("Главная")
}

/** Индекс вкладки «Чат» (у админа 3, у инструктора/курсанта 2). Нельзя сравнивать с константой 2. */
private fun chatTabIndexForRole(role: String?): Int =
    getTabsForUserRole(role ?: "").indexOf("Чат").takeIf { it >= 0 } ?: 2

/** Краткое «на ДД.ММ в ЧЧ:ММ» для текста уведомлений (пустая строка если ms не задано). */
private fun formatDayTimeShort(ms: Long?): String {
    if (ms == null || ms <= 0) return ""
    return " на ${formatDateOnly(ms)} в ${formatTimeOnly(ms)}"
}

/** Дата и время по часовому поясу Екатеринбург (UTC+5) для истории операций баланса */
private fun formatDateTimeEkaterinburg(ms: Long?): String {
    if (ms == null || ms <= 0) return "—"
    val d = js("new Date(ms)")
    return js("d.toLocaleString('ru-RU', { timeZone: 'Asia/Yekaterinburg', dateStyle: 'short', timeStyle: 'short' })").unsafeCast<String>()
}

/** Показать всплывающее сообщение (toast), исчезает через 4 с или по клику. */
private fun showToast(message: String) {
    showToastWithDuration(message, 4000)
}

/** Toast с заданной длительностью и опциональным классом (например sd-toast-important). */
private fun showToastWithDuration(message: String, durationMs: Int, extraClass: String? = null) {
    val existing = document.querySelector(".sd-toast")
    existing?.parentNode?.removeChild(existing)
    val div = document.createElement("div")
    div.className = "sd-toast" + (if (extraClass != null) " $extraClass" else "")
    div.setAttribute("role", "alert")
    div.textContent = message
    document.body?.appendChild(div)
    var timeoutId: dynamic = js("0")
    val remove = {
        div.parentNode?.removeChild(div)
        window.clearTimeout(timeoutId)
    }
    timeoutId = window.setTimeout({ remove() }, durationMs)
    div.addEventListener("click", { remove() })
}

/** Звуковое и текстовое уведомление курсанту: инструктор добавил свободное окно. Обновляет экран, переключает на вкладку «Запись» и сохраняет в «Уведомления». */
private fun showCadetNewWindowNotification(windowDateMs: Long? = null) {
    val dayTime = formatDayTimeShort(windowDateMs)
    val text = "Инструктор добавил свободное окно${dayTime}. Можно записаться на вождение."
    val now = js("Date.now()").unsafeCast<Double>().toLong()
    val newList = appState.notifications + AppNotification(dateTimeMs = now, text = text)
    updateState {
        notifications = newList
        selectedTabIndex = 1
        chatSettingsOpen = false
    }
    appState.user?.id?.let { uid ->
        saveNotificationsToStorage(uid, newList)
    }
    showToastWithDuration(text, 8000, "sd-toast-important")
    try {
        val perm = js("(function(){ return typeof Notification !== 'undefined' ? Notification.permission : 'denied'; })").unsafeCast<() -> String>().invoke()
        if (perm == "granted") {
            js("(function(t){ try { new Notification('StartDrive', { body: t, tag: 'sd-new-window' }); } catch(e) {} })").unsafeCast<(String) -> Unit>().invoke(text)
        } else if (perm == "default") {
            js("(function(t){ try { Notification.requestPermission().then(function(p){ if(p==='granted') new Notification('StartDrive', { body: t, tag: 'sd-new-window' }); }); } catch(e) {} })").unsafeCast<(String) -> Unit>().invoke(text)
        }
    } catch (_: Throwable) { }
    if (getSoundNotificationsEnabled() != false) {
        playInstruktorDobavilOknoSound()
        playCadetNewWindowVoiceNotification()
    }
}

private const val NOTIFICATIONS_STORAGE_KEY_PREFIX = "sd_notifications_"
/** Id сессий вождения, о которых курсант уже получил уведомление «Инструктор записал вас…» (чтобы показывать и сохранять при первом открытии, не только по опросу). */
private const val CADET_INSTRUCTOR_BOOKING_SESSION_IDS_PREFIX = "sd_cadet_instructor_booking_session_ids_"
private const val MAX_CADET_INSTRUCTOR_BOOKING_SESSION_IDS = 500

/** Ключ в localStorage: включены ли звуковые уведомления (true/false). Отсутствие ключа = первый запуск. */
private const val SOUND_NOTIFICATIONS_ENABLED_KEY = "sd_sound_notifications_enabled"

/** Читает сохранённый выбор пользователя: null = ещё не выбирал (первый запуск), true/false = включено/выключено. */
private fun getSoundNotificationsEnabled(): Boolean? {
    val raw = window.asDynamic().localStorage?.getItem(SOUND_NOTIFICATIONS_ENABLED_KEY) as? String ?: return null
    return when (raw) {
        "true" -> true
        "false" -> false
        else -> null
    }
}

/** Сохраняет выбор пользователя по звуковым уведомлениям в localStorage. */
private fun setSoundNotificationsEnabled(enabled: Boolean) {
    try {
        window.asDynamic().localStorage?.setItem(SOUND_NOTIFICATIONS_ENABLED_KEY, if (enabled) "true" else "false")
    } catch (_: Throwable) { }
}

/** true, если пользователь ещё не делал выбор (ключ в localStorage отсутствует). */
private fun isFirstRunSoundSetting(): Boolean = getSoundNotificationsEnabled() == null

/** HTML модального окна «Настройки звуковых уведомлений» (первый запуск). */
private fun renderSoundSettingsModalHtml(): String = """
    <div class="sd-modal-overlay sd-sound-settings-overlay" id="sd-sound-settings-modal">
        <div class="sd-modal sd-sound-settings-modal">
            <h3 class="sd-modal-title">Звуковые уведомления</h3>
            <p class="sd-sound-settings-text">Включить автоматическое воспроизведение звука и голоса?</p>
            <p class="sd-modal-actions sd-sound-settings-actions">
                <button type="button" id="sd-sound-enable-btn" class="sd-btn sd-btn-primary">Включить</button>
                <button type="button" id="sd-sound-disable-btn" class="sd-btn sd-btn-secondary">Отключить</button>
            </p>
        </div>
    </div>
""".trimIndent()

/** Удаляет модальное окно настроек звука из DOM. */
private fun removeSoundSettingsModal() {
    document.getElementById("sd-sound-settings-modal")?.let { el ->
        el.parentNode?.removeChild(el)
    }
}

/** Добавляет модальное окно настроек звука в root и вешает обработчики кнопок (если флаг показа включён). */
private fun appendSoundSettingsModalIfNeeded(root: org.w3c.dom.Element) {
    if (!appState.showSoundSettingsModal) return
    val wrap = document.createElement("div")
    wrap.innerHTML = renderSoundSettingsModalHtml()
    val modal = wrap.firstElementChild ?: return
    root.appendChild(modal)
    val enableBtn = modal.querySelector("#sd-sound-enable-btn")
    val disableBtn = modal.querySelector("#sd-sound-disable-btn")
    enableBtn?.addEventListener("click", {
        setSoundNotificationsEnabled(true)
        unlockCadetNotificationAudio()
        updateState { showSoundSettingsModal = false }
        removeSoundSettingsModal()
    })
    disableBtn?.addEventListener("click", {
        setSoundNotificationsEnabled(false)
        updateState { showSoundSettingsModal = false }
        removeSoundSettingsModal()
    })
}

/** Показать кнопку «Разрешить звук» курсанту. При включённом звуке в настройках полосу не показываем — разблокировка по первому нажатию в интерфейсе. */
private fun appendAllowSoundBarIfNeeded(root: org.w3c.dom.Element) {
    if (appState.user?.role != "cadet") return
    if (getSoundNotificationsEnabled() == true) return
    if (getSoundNotificationsEnabled() != true) return
    if (appState.soundAudioUnlocked) return
    if (root.querySelector("#sd-allow-sound-bar") != null) return
    val bar = document.createElement("div")
    bar.id = "sd-allow-sound-bar"
    bar.className = "sd-allow-sound-bar"
    bar.innerHTML = """
        <p class="sd-allow-sound-text">Браузер требует одно нажатие для воспроизведения звука уведомлений.</p>
        <button type="button" id="sd-allow-sound-btn" class="sd-btn sd-btn-primary sd-allow-sound-btn">🔊 Разрешить звук</button>
    """.trimIndent()
    root.appendChild(bar)
    val allowBtn = bar.querySelector("#sd-allow-sound-btn")
    allowBtn?.addEventListener("click", { ev: Event ->
        ev.preventDefault()
        ev.stopPropagation()
        updateState { soundAudioUnlocked = true }
        unlockCadetNotificationAudio()
        root.querySelectorAll(".sd-allow-sound-bar").let { list ->
            for (i in 0 until list.length) {
                list.item(i)?.let { node -> node.parentNode?.removeChild(node) }
            }
        }
    })
}

private fun loadNotificationsFromStorage(userId: String): List<AppNotification> {
    return try {
        val raw = window.asDynamic().localStorage?.getItem(NOTIFICATIONS_STORAGE_KEY_PREFIX + userId) as? String ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        val arr = js("(function(r){ return JSON.parse(r); })").unsafeCast<(String) -> dynamic>().invoke(raw)
        val len = (arr?.length as? Number)?.toInt() ?: 0
        val getAt = js("(function(a, i){ return a[i]; })").unsafeCast<(Any, Int) -> Any?>()
        (0 until len).mapNotNull { i ->
            val obj = (if (arr != null) getAt(arr, i) else null) ?: return@mapNotNull null
            val o = obj.unsafeCast<dynamic>()
            val ms = when (val v = o.dateTimeMs) {
                is Number -> v.toLong()
                else -> (v as? String)?.toLongOrNull() ?: return@mapNotNull null
            }
            val t = (o.text as? String) ?: ""
            val sid = (o.sourceId as? String)?.takeIf { it.isNotBlank() }
            AppNotification(dateTimeMs = ms, text = t, sourceId = sid)
        }
    } catch (_: Throwable) { emptyList() }
}

/** Экранирование строки для JSON (кавычки и обратные слэши). */
private fun escapeJsonString(s: String): String {
    return s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n")
}

private fun saveNotificationsToStorage(userId: String, list: List<AppNotification>) {
    try {
        val key = NOTIFICATIONS_STORAGE_KEY_PREFIX + userId
        val json = "[" + list.joinToString(",") { n ->
            val sidPart = n.sourceId?.let { ",\"sourceId\":\"${escapeJsonString(it)}\"" } ?: ""
            """{"dateTimeMs":${n.dateTimeMs},"text":"${escapeJsonString(n.text)}"$sidPart}"""
        } + "]"
        window.asDynamic().localStorage?.setItem(key, json)
    } catch (_: Throwable) { }
}

private fun loadCadetInstructorBookingNotifiedSessionIds(userId: String): MutableSet<String> {
    return try {
        val raw = window.asDynamic().localStorage?.getItem(CADET_INSTRUCTOR_BOOKING_SESSION_IDS_PREFIX + userId) as? String ?: return mutableSetOf()
        if (raw.isBlank()) return mutableSetOf()
        val arr = js("(function(r){ return JSON.parse(r); })").unsafeCast<(String) -> dynamic>().invoke(raw)
        val len = (arr?.length as? Number)?.toInt() ?: 0
        val getAt = js("(function(a, i){ return a[i]; })").unsafeCast<(Any, Int) -> Any?>()
        (0 until len).mapNotNull { i ->
            (if (arr != null) getAt(arr, i) else null)?.unsafeCast<dynamic>()?.toString()?.takeIf { it.isNotBlank() }
        }.toMutableSet()
    } catch (_: Throwable) { mutableSetOf() }
}

private fun saveCadetInstructorBookingNotifiedSessionIds(userId: String, ids: MutableSet<String>) {
    try {
        val list = ids.toList().takeLast(MAX_CADET_INSTRUCTOR_BOOKING_SESSION_IDS)
        val json = "[" + list.joinToString(",") { "\"" + escapeJsonString(it) + "\"" } + "]"
        window.asDynamic().localStorage?.setItem(CADET_INSTRUCTOR_BOOKING_SESSION_IDS_PREFIX + userId, json)
    } catch (_: Throwable) { }
}

/**
 * Одна строка на один [sourceId] (если задан) или на одинаковый текст — оставляем запись с более поздним временем.
 * Убирает дубли: live admin_events + загрузка из Firestore с другим dateTimeMs и т.п.
 */
private fun notificationDedupeKey(n: AppNotification): String =
    n.sourceId?.takeIf { it.isNotBlank() } ?: "text:${n.text.trim()}"

private fun dedupeNotificationsByTextKeepNewest(items: List<AppNotification>): List<AppNotification> {
    if (items.isEmpty()) return emptyList()
    return items
        .groupBy { notificationDedupeKey(it) }
        .values
        .map { group -> group.maxBy { it.dateTimeMs } }
        .sortedByDescending { it.dateTimeMs }
}

/** Показать уведомление снизу экрана и сохранить в список уведомлений (и в localStorage). */
private fun showNotification(text: String, sourceId: String? = null) {
    val now = js("Date.now()").unsafeCast<Double>().toLong()
    val trimmed = text.trim()
    val before = appState.notifications
    val newList = dedupeNotificationsByTextKeepNewest(
        before + AppNotification(dateTimeMs = now, text = text, sourceId = sourceId?.takeIf { it.isNotBlank() }),
    )
    if (newList == before) return
    val duplicateContent = when {
        !sourceId.isNullOrBlank() -> before.any { it.sourceId == sourceId }
        else -> before.any { it.text.trim() == trimmed }
    }
    val duplicateTextOnly = duplicateContent && newList.size == before.size
    updateState {
        notifications = newList
        if (notificationsViewOpen) notificationsReadCount = newList.size
    }
    appState.user?.id?.let { uid ->
        saveNotificationsToStorage(uid, newList)
    }
    if (!duplicateTextOnly) showToast(text)
}

/** Явный запуск включения web push из UI вкладки уведомлений. */
private fun enableWebPushFromUi() {
    val uid = appState.user?.id
    (document.getElementById("sd-notif-enable-push") as? HTMLButtonElement)?.let { btn ->
        btn.textContent = "Проверка..."
        btn.disabled = true
    }
    if (uid.isNullOrBlank()) {
        showToast("Сначала войдите в аккаунт")
        (document.getElementById("sd-notif-enable-push") as? HTMLButtonElement)?.let { btn ->
            btn.textContent = "Включить push"
            btn.disabled = false
        }
        return
    }
    initWebPushForCurrentUser(uid, { _, _ -> Unit }) { ok, msg ->
        showToast(if (ok) msg else "Push: $msg")
        (document.getElementById("sd-notif-enable-push") as? HTMLButtonElement)?.let { btn ->
            btn.textContent = "Включить push"
            btn.disabled = false
        }
    }
}

/** Отправить тестовый FCM на свои токены (веб + приложение). */
private fun testPushFromUi() {
    val uid = appState.user?.id
    (document.getElementById("sd-notif-test-push") as? HTMLButtonElement)?.let { btn ->
        btn.textContent = "Отправка..."
        btn.disabled = true
    }
    if (uid.isNullOrBlank()) {
        showToast("Сначала войдите в аккаунт")
        (document.getElementById("sd-notif-test-push") as? HTMLButtonElement)?.let { btn ->
            btn.textContent = "Тест push"
            btn.disabled = false
        }
        return
    }
    sendTestPush { err, sent ->
        when {
            err == null -> showToast("Тест отправлен ($sent)")
            else -> showToast("Push: $err${if (sent > 0) " (частично: $sent)" else ""}")
        }
        (document.getElementById("sd-notif-test-push") as? HTMLButtonElement)?.let { btn ->
            btn.textContent = "Тест push"
            btn.disabled = false
        }
    }
}

/** Базовый URL для ресурсов (звуки) — от origin, чтобы всегда грузить ваш MP3. */
private fun getResourceBaseUrl(): String {
    val origin = kotlinx.browser.window.location.origin
    return if (origin.endsWith("/")) origin else origin + "/"
}

/** Полный URL вашего звука: instruktor_dobavil_okno.mp3 из «Фото для приложения/звуки». */
/** Список звуков уведомлений (имя файла без .mp3). По умолчанию iphone1. */
private val NOTIFICATION_SOUND_OPTIONS = listOf(
    "iphone1", "iphone2", "netflix", "oreo", "intriga", "telegram", "signalizacia", "droid", "dudka", "oldspice", "kupola", "du-hast-1", "du-hast-2"
)
private const val NOTIFICATION_SOUND_STORAGE_KEY = "sd_notification_sound"
private const val CHAT_AVATAR_STORAGE_KEY_PREFIX = "sd_chat_avatar_"
private const val CHAT_ADMIN_FILE_MAX_BYTES = 8 * 1024 * 1024

private fun getNotificationSoundFilename(): String =
    (window.asDynamic().localStorage?.getItem(NOTIFICATION_SOUND_STORAGE_KEY) as? String)?.takeIf { it in NOTIFICATION_SOUND_OPTIONS } ?: "iphone1"

private fun getCadetNotificationSoundUrl(): String =
    getResourceBaseUrl() + "sounds/" + getNotificationSoundFilename() + ".mp3"

/** Один общий Audio для звука входящего сообщения в чате; разблокируется по первому клику. Использует выбранный в настройках звук уведомлений. */
private fun getOrCreateChatMessageAudio(): dynamic {
    if (chatMessageAudio != null && chatMessageAudio != js("undefined")) return chatMessageAudio
    val audio = document.createElement("audio").asDynamic()
    audio.preload = "auto"
    audio.volume = 0.7
    audio.src = getCadetNotificationSoundUrl()
    chatMessageAudio = audio
    return audio
}

/** Один общий Audio для звука «Инструктор добавил свободное окно»; разблокируется по первому клику пользователя. */
private fun getOrCreateCadetNotificationAudio(): dynamic {
    if (cadetNotificationAudio != null && cadetNotificationAudio != js("undefined")) return cadetNotificationAudio
    val audio = document.createElement("audio").asDynamic()
    audio.preload = "auto"
    audio.volume = 1.0
    audio.src = getCadetNotificationSoundUrl()
    val el = audio.unsafeCast<org.w3c.dom.HTMLElement>()
    el.setAttribute("playsinline", "true")
    el.setAttribute("webkit-playsinline", "true")
    cadetNotificationAudio = audio
    return audio
}

/** Разблокировать воспроизведение звука уведомления (нужен один раз после клика/касания пользователя из-за политики autoplay). */
private fun unlockCadetNotificationAudio() {
    if (cadetNotificationAudioUnlocked) return
    cadetNotificationAudioUnlocked = true
    try {
        val audio = getOrCreateCadetNotificationAudio()
        audio.currentTime = 0.0
        audio.play()?.then({
            audio.pause()
            audio.currentTime = 0.0
        })?.catch { _ -> Unit }
        val chatAudio = getOrCreateChatMessageAudio()
        chatAudio.currentTime = 0.0
        chatAudio.play()?.then({
            chatAudio.pause()
            chatAudio.currentTime = 0.0
        })?.catch { _ -> Unit }
        if (cadetNotificationAudioContext == null || cadetNotificationAudioContext == js("undefined")) {
            cadetNotificationAudioContext = js("new (window.AudioContext || window.webkitAudioContext)()").unsafeCast<dynamic>()
        }
        val ctx = cadetNotificationAudioContext
        if (ctx != null) ctx.asDynamic().resume()?.catch { _ -> Unit }
    } catch (_: Throwable) { }
}

/** Разблокировать аудио для уведомлений чата (один контекст с курсантским). Вызывать при клике пользователя, чтобы браузер разрешил звук. */
private fun unlockChatNotificationAudio() {
    try {
        if (cadetNotificationAudioContext == null || cadetNotificationAudioContext == js("undefined")) {
            cadetNotificationAudioContext = js("new (window.AudioContext || window.webkitAudioContext)()").unsafeCast<dynamic>()
        }
        cadetNotificationAudioContext?.asDynamic()?.resume()?.catch { _: dynamic -> Unit }
    } catch (_: Throwable) { }
}

/** Звук нового сообщения в чате: выбранный в настройках звук уведомлений. */
private fun playChatMessageSound() {
    try {
        val audio = getOrCreateChatMessageAudio()
        audio.src = getCadetNotificationSoundUrl()
        audio.currentTime = 0.0
        audio.volume = 0.7
        audio.play()?.catch { _: dynamic -> Unit }
    } catch (_: Throwable) { }
}

/** Звук уведомления инструктора о событиях вождения (тот же файл, что выбран в настройках уведомлений). */
private fun playInstructorSessionEventSound() {
    if (getSoundNotificationsEnabled() == false) return
    try {
        if (cadetNotificationAudioContext != null && cadetNotificationAudioContext != js("undefined")) {
            cadetNotificationAudioContext.asDynamic().resume()?.catch { _: dynamic -> Unit }
        }
        val audio = getOrCreateCadetNotificationAudio()
        audio.src = getCadetNotificationSoundUrl()
        audio.currentTime = 0.0
        audio.volume = 1.0
        audio.play()?.catch { _: dynamic -> Unit }
    } catch (_: Throwable) { }
}

/**
 * Сравнивает старый и новый список сессий инструктора и воспроизводит звук при:
 * бронь курсантом, подтверждение записи, подтверждение начала, старт вождения (таймер), отмена курсантом, завершение.
 */
private fun maybePlayInstructorSessionSounds(oldS: List<DrivingSession>, newS: List<DrivingSession>) {
    if (getSoundNotificationsEnabled() == false) return
    if (!instructorSessionSoundBaselineReady) {
        instructorSessionSoundBaselineReady = true
        return
    }
    val oldById = oldS.associateBy { it.id }
    for (s in newS) {
        val prev = oldById[s.id]
        if (prev == null) {
            if (s.status == "scheduled" && s.openWindowId.isNotBlank()) {
                playInstructorSessionEventSound()
            }
            continue
        }
        val becameInProgress = prev.status != "inProgress" && s.status == "inProgress"
        if (!prev.instructorConfirmed && s.instructorConfirmed) {
            playInstructorSessionEventSound()
        }
        if (!prev.cadetConfirmed && s.cadetConfirmed && !becameInProgress) {
            playInstructorSessionEventSound()
        }
        if (becameInProgress) {
            playInstructorSessionEventSound()
        }
        if (prev.status != "cancelledByCadet" && s.status == "cancelledByCadet") {
            playInstructorSessionEventSound()
        }
        if (prev.status != "completed" && s.status == "completed") {
            playInstructorSessionEventSound()
        }
    }
}

private val chatNotifUnsubByContactId = mutableMapOf<String, () -> Unit>()

private fun chatLastSeenKey(uid: String, contactId: String): String = "sd_chat_last_seen_${uid}_$contactId"

private fun getChatLastSeenMs(uid: String, contactId: String): Long {
    return try {
        val v = js("window.localStorage.getItem(arguments[0])")
            .unsafeCast<(String) -> String?>()
            .invoke(chatLastSeenKey(uid, contactId))
        v?.toLongOrNull() ?: 0L
    } catch (_: Throwable) { 0L }
}

private fun setChatLastSeenMs(uid: String, contactId: String, ms: Long) {
    try {
        js("window.localStorage.setItem(arguments[0], arguments[1])")
            .unsafeCast<(String, String) -> Unit>()
            .invoke(chatLastSeenKey(uid, contactId), ms.toString())
    } catch (_: Throwable) { }
}

private fun setupChatNotificationsForContact(uid: String, contactId: String) {
    chatNotifUnsubByContactId.remove(contactId)?.invoke()
    val db = getDatabase() ?: return
    val roomId = chatRoomId(uid, contactId)
    val lastSeen = getChatLastSeenMs(uid, contactId)
    val ref = db.ref("${com.example.startdrive.shared.FirebasePaths.CHATS}/$roomId/${com.example.startdrive.shared.FirebasePaths.MESSAGES}")
        .orderByChild("timestamp")
        .startAt((lastSeen + 1).toDouble())
    val subscribeTimeMs = js("Date.now()").unsafeCast<Double>().toLong()
    val listener: (dynamic) -> Unit = listener@{ snap: dynamic ->
        val m = snap?.`val`()
        val senderId = (m?.senderId as? String) ?: ""
        val tsAny = m?.timestamp
        val ts = when (tsAny) {
            is Number -> tsAny.toLong()
            else -> (tsAny?.unsafeCast<Double>())?.toLong() ?: 0L
        }
        if (ts <= 0L) return@listener
        val isInitialLoad = ts < subscribeTimeMs - 2000
        if (isInitialLoad) return@listener
        val isViewingThisChat = (appState.selectedTabIndex == chatTabIndexForRole(appState.user?.role) && appState.selectedChatContactId == contactId && !appState.chatSettingsOpen)
        if (isViewingThisChat) {
            setChatLastSeenMs(uid, contactId, ts)
            updateState { if (chatUnreadCounts.containsKey(contactId)) chatUnreadCounts = chatUnreadCounts - contactId }
        } else if (senderId.isNotBlank() && senderId != uid) {
            updateState {
                val prev = chatUnreadCounts[contactId] ?: 0
                chatUnreadCounts = chatUnreadCounts + (contactId to (prev + 1))
            }
            if (getSoundNotificationsEnabled() != false) {
                unlockChatNotificationAudio()
                playChatMessageSound()
            }
        }
    }
    ref.on("child_added", listener)
    chatNotifUnsubByContactId[contactId] = {
        try { ref.off("child_added", listener) } catch (_: Throwable) { }
    }
}

private fun clearChatUnread(uid: String, contactId: String) {
    val lastTs = appState.chatMessages.lastOrNull()?.timestamp ?: js("Date.now()").unsafeCast<Double>().toLong()
    setChatLastSeenMs(uid, contactId, lastTs)
    updateState { if (chatUnreadCounts.containsKey(contactId)) chatUnreadCounts = chatUnreadCounts - contactId }
    setupChatNotificationsForContact(uid, contactId)
}

private fun chatUnreadKeyForGroup(groupId: String) = "g_$groupId"

private fun chatLastSeenKeyGroup(uid: String, groupId: String) = "sd_chat_last_seen_${uid}_g_$groupId"

private fun getChatLastSeenMsForGroup(uid: String, groupId: String): Long {
    return try {
        val v = js("window.localStorage.getItem(arguments[0])")
            .unsafeCast<(String) -> String?>()
            .invoke(chatLastSeenKeyGroup(uid, groupId))
        v?.toLongOrNull() ?: 0L
    } catch (_: Throwable) { 0L }
}

private fun setChatLastSeenMsForGroup(uid: String, groupId: String, ms: Long) {
    try {
        js("window.localStorage.setItem(arguments[0], arguments[1])")
            .unsafeCast<(String, String) -> Unit>()
            .invoke(chatLastSeenKeyGroup(uid, groupId), ms.toString())
    } catch (_: Throwable) { }
}

private fun setupChatNotificationsForGroup(uid: String, groupId: String) {
    chatNotifUnsubByGroupId.remove(groupId)?.invoke()
    val db = getDatabase() ?: return
    val roomId = groupChatRoomId(groupId)
    val lastSeen = getChatLastSeenMsForGroup(uid, groupId)
    val ref = db.ref("${com.example.startdrive.shared.FirebasePaths.CHATS}/$roomId/${com.example.startdrive.shared.FirebasePaths.MESSAGES}")
        .orderByChild("timestamp")
        .startAt((lastSeen + 1).toDouble())
    val subscribeTimeMs = js("Date.now()").unsafeCast<Double>().toLong()
    val gKey = chatUnreadKeyForGroup(groupId)
    val listener: (dynamic) -> Unit = listener@{ snap: dynamic ->
        val m = snap?.`val`()
        val senderId = (m?.senderId as? String) ?: ""
        val tsAny = m?.timestamp
        val ts = when (tsAny) {
            is Number -> tsAny.toLong()
            else -> (tsAny?.unsafeCast<Double>())?.toLong() ?: 0L
        }
        if (ts <= 0L) return@listener
        val isInitialLoad = ts < subscribeTimeMs - 2000
        if (isInitialLoad) return@listener
        val isViewingThisChat = (appState.selectedTabIndex == chatTabIndexForRole(appState.user?.role) && appState.selectedChatGroupId == groupId && !appState.chatSettingsOpen)
        if (isViewingThisChat) {
            setChatLastSeenMsForGroup(uid, groupId, ts)
            updateState { if (chatUnreadCounts.containsKey(gKey)) chatUnreadCounts = chatUnreadCounts - gKey }
        } else if (senderId.isNotBlank() && senderId != uid) {
            updateState {
                val prev = chatUnreadCounts[gKey] ?: 0
                chatUnreadCounts = chatUnreadCounts + (gKey to (prev + 1))
            }
            if (getSoundNotificationsEnabled() != false) {
                unlockChatNotificationAudio()
                playChatMessageSound()
            }
        }
    }
    ref.on("child_added", listener)
    chatNotifUnsubByGroupId[groupId] = {
        try { ref.off("child_added", listener) } catch (_: Throwable) { }
    }
}

private fun subscribeChatNotifications(uid: String, contacts: List<User>) {
    val ids = contacts.map { it.id }.filter { it.isNotBlank() }.toSet()
    val toRemove = chatNotifUnsubByContactId.keys.filter { it !in ids }
    toRemove.forEach { id -> chatNotifUnsubByContactId.remove(id)?.invoke() }
    ids.forEach { cid -> setupChatNotificationsForContact(uid, cid) }
    val gIds = appState.chatGroups.map { it.id }.filter { it.isNotBlank() }.toSet()
    val toRemoveG = chatNotifUnsubByGroupId.keys.filter { it !in gIds }
    toRemoveG.forEach { id -> chatNotifUnsubByGroupId.remove(id)?.invoke() }
    gIds.forEach { gid -> setupChatNotificationsForGroup(uid, gid) }
}

private fun clearChatUnreadForGroup(uid: String, groupId: String) {
    val gKey = chatUnreadKeyForGroup(groupId)
    val lastTs = appState.chatMessages.lastOrNull()?.timestamp ?: js("Date.now()").unsafeCast<Double>().toLong()
    setChatLastSeenMsForGroup(uid, groupId, lastTs)
    updateState { if (chatUnreadCounts.containsKey(gKey)) chatUnreadCounts = chatUnreadCounts - gKey }
    setupChatNotificationsForGroup(uid, groupId)
}

/** Короткий звуковой сигнал через Web Audio API (два «динь-динь», всегда слышно). */
private fun playCadetNotificationBeep() {
    try {
        var ctx = cadetNotificationAudioContext
        if (ctx == null || ctx == js("undefined")) {
            ctx = js("new (window.AudioContext || window.webkitAudioContext)()").unsafeCast<dynamic>()
            cadetNotificationAudioContext = ctx
        }
        val c = ctx.asDynamic()
        js("(function(ctx){ function beep(delay){ var o=ctx.createOscillator(),g=ctx.createGain(); o.connect(g); g.connect(ctx.destination); o.frequency.value=880; o.type='sine'; g.gain.value=0.5; o.start(ctx.currentTime+delay); o.stop(ctx.currentTime+delay+0.12); } function run(){ beep(0); beep(0.18); } var r=ctx.resume; if(r){ r.call(ctx).then(run).catch(run); } else run(); })").unsafeCast<(dynamic) -> Unit>().invoke(c)
    } catch (_: Throwable) { }
}

/** Голосовое уведомление через TTS (Web Speech API): «Инструктор добавил свободное окно». */
private fun playCadetNewWindowVoiceNotification() {
    try {
        val synth = kotlinx.browser.window.asDynamic().speechSynthesis
        if (synth == null || synth == js("undefined")) return
        val utterance = js("new SpeechSynthesisUtterance()").unsafeCast<dynamic>()
        utterance.text = "Инструктор добавил свободное окно. Можно записаться на вождение."
        utterance.lang = "ru-RU"
        utterance.rate = 0.95
        utterance.volume = 1.0
        js("(function(s,u){ try { s.speak(u); } catch(e) {} })").unsafeCast<(dynamic, dynamic) -> Unit>().invoke(synth, utterance)
    } catch (_: Throwable) { }
}

/** Воспроизвести звуковое уведомление: только ваш файл instruktor_dobavil_okno.mp3 (без TTS и без бипа). */
private fun playInstruktorDobavilOknoSound() {
    try {
        if (cadetNotificationAudioContext != null && cadetNotificationAudioContext != js("undefined")) {
            cadetNotificationAudioContext.asDynamic().resume()?.catch { _: dynamic -> Unit }
        }
        val audio = getOrCreateCadetNotificationAudio()
        audio.src = getCadetNotificationSoundUrl()
        audio.currentTime = 0.0
        audio.play()?.catch { _ -> Unit }
    } catch (_: Throwable) { }
}

private const val CADET_DRIVING_START_GREETING_MP3 = "zdravstvuyte_dly_kursanta"

/**
 * Приветствие при нажатии «Начать» у курсанта. Отдельный [HTMLAudioElement] на каждый клик —
 * жест пользователя остаётся «свежим», не делим src с уведомлениями (иначе часто тишина / гонка).
 */
private fun playCadetDrivingStartGreetingSound() {
    try {
        if (cadetNotificationAudioContext == null || cadetNotificationAudioContext == js("undefined")) {
            cadetNotificationAudioContext = js("new (window.AudioContext || window.webkitAudioContext)()").unsafeCast<dynamic>()
        }
        cadetNotificationAudioContext.asDynamic().resume()?.catch { _: dynamic -> Unit }
        val url = getResourceBaseUrl() + "sounds/" + CADET_DRIVING_START_GREETING_MP3 + ".mp3"
        val audio = document.createElement("audio").asDynamic()
        audio.preload = "auto"
        audio.volume = 1.0
        audio.muted = false
        audio.src = url
        val el = audio.unsafeCast<org.w3c.dom.HTMLElement>()
        el.setAttribute("playsinline", "true")
        el.setAttribute("webkit-playsinline", "true")
        document.body?.appendChild(el)
        val a = audio.asDynamic()
        fun tryPlay() {
            try {
                a.currentTime = 0.0
                a.play()?.catch { _: dynamic -> Unit }
            } catch (_: Throwable) { }
        }
        val playPromise = a.play()
        if (playPromise != null) {
            val onFail: () -> Unit = {
                val rs = (a.readyState as? Number)?.toInt() ?: 0
                if (rs >= 2) tryPlay()
                else {
                    val once = js("({ once: true })")
                    audio.addEventListener("canplay", { tryPlay() }, once)
                }
            }
            js("(function(p, onFail){ if(p&&typeof p.catch==='function') p.catch(function(){ onFail(); }); })").unsafeCast<(dynamic, dynamic) -> Unit>().invoke(playPromise, onFail)
        }
        val onceEnd = js("({ once: true })")
        audio.addEventListener(
            "ended",
            {
                try {
                    el.remove()
                } catch (_: Throwable) { }
            },
            onceEnd
        )
    } catch (_: Throwable) { }
}

/**
 * Курсант: новые записи на вождение инструктором — сохраняем в «Уведомления» и localStorage (как [showNotification]),
 * без дубля при первом заходе после записи (учёт по id сессии).
 */
private fun notifyCadetNewInstructorScheduledSessions(userId: String, sessions: List<DrivingSession>) {
    val scheduled = sessions.filter { it.status == "scheduled" && it.id.isNotBlank() && it.cadetId == userId }
    if (scheduled.isEmpty()) return
    val known = loadCadetInstructorBookingNotifiedSessionIds(userId)
    val newSessions = scheduled.filter { it.id !in known }.sortedBy { it.startTimeMillis ?: 0L }
    if (newSessions.isEmpty()) return
    val now = js("Date.now()").unsafeCast<Double>().toLong()
    var list = appState.notifications
    newSessions.forEachIndexed { idx, s ->
        val text = "Инструктор записал вас на вождение" + formatDayTimeShort(s.startTimeMillis)
        list = list + AppNotification(dateTimeMs = now + idx, text = text, sourceId = "instructorSession:${s.id}")
        known.add(s.id)
    }
    saveCadetInstructorBookingNotifiedSessionIds(userId, known)
    val firstText = "Инструктор записал вас на вождение" + formatDayTimeShort(newSessions.first().startTimeMillis)
    updateState {
        notifications = list
        if (notificationsViewOpen) notificationsReadCount = list.size
        selectedTabIndex = 1
        chatSettingsOpen = false
    }
    saveNotificationsToStorage(userId, list)
    showToast(firstText)
    if (getSoundNotificationsEnabled() == true) playInstruktorDobavilOknoSound()
}

/** Обновляет карту «курсант id → число завершённых вождений» для главной админки. */
private fun refreshAdminCadetCompletedDriveCounts() {
    getCompletedDrivingSessionsForAdmin { sessions ->
        val counts = sessions
            .filter { it.cadetId.isNotBlank() }
            .groupingBy { it.cadetId }
            .eachCount()
        updateState { adminCadetCompletedDriveCounts = counts }
    }
}

private fun refreshCadetGroups() {
    getCadetGroups { list -> updateState { cadetGroups = list } }
}

private fun refreshChatGroups() {
    val uid = appState.user?.id ?: return
    getChatGroupsForUser(uid) { list ->
        updateState { chatGroups = list }
        val u = appState.user ?: return@getChatGroupsForUser
        if (appState.selectedTabIndex == chatTabIndexForRole(appState.user?.role) && appState.chatContacts.isNotEmpty()) {
            subscribeChatNotifications(u.id, appState.chatContacts)
        }
    }
}

/** Вождения + группы при загрузке главной админки и после действий со списком. */
private fun refreshAdminHomeAuxiliaryData() {
    refreshAdminCadetCompletedDriveCounts()
    refreshCadetGroups()
}

private fun formatDdMmYyShort(ms: Long): String {
    if (ms <= 0L) return "—"
    val d = js("new Date(ms)").unsafeCast<dynamic>()
    val day = (d.getDate() as Int).toString().padStart(2, '0')
    val month = ((d.getMonth() as Int) + 1).toString().padStart(2, '0')
    val y = (d.getFullYear() as Int) % 100
    return "$day.$month.${y.toString().padStart(2, '0')}"
}

private fun cadetGroupDisplayTextEscaped(g: CadetGroup?): String {
    if (g == null || g.id.isBlank()) return "не выбрана"
    val n = g.numberLabel.escapeHtml()
    val df = g.dateFromMillis
    val dt = g.dateToMillis
    if (df == null || dt == null) return "№ $n · без срока"
    val from = formatDdMmYyShort(df)
    val to = formatDdMmYyShort(dt)
    if (from == "—" || to == "—") return "№ $n · без срока"
    return "№ $n · с $from по $to"
}

private fun formatMillisToIsoDate(ms: Long): String {
    val d = js("new Date(ms)").unsafeCast<dynamic>()
    val y = d.getFullYear() as Int
    val m = ((d.getMonth() as Int) + 1).toString().padStart(2, '0')
    val day = (d.getDate() as Int).toString().padStart(2, '0')
    return "$y-$m-$day"
}

/** Парсинг дд.мм.гг в локальные миллисекунды (начало дня). */
private fun parseDdMmYyToMillis(s: String): Long? {
    val t = s.trim()
    val p = t.split('.').map { it.trim() }
    if (p.size != 3) return null
    val day = p[0].toIntOrNull() ?: return null
    val month = p[1].toIntOrNull() ?: return null
    var year = p[2].toIntOrNull() ?: return null
    if (year < 100) year += 2000
    if (month !in 1..12 || day !in 1..31) return null
    val mkDate = js("(function(y,m,d){ return new Date(y,m-1,d); })").unsafeCast<(Int, Int, Int) -> dynamic>()
    val d = mkDate(year, month, day)
    if ((d.getDate() as Int) != day || (d.getMonth() as Int) != month - 1) return null
    return (d.getTime() as Double).toLong()
}

/** Значение `<input type="date">` (гггг-мм-дд) → миллисекунды начала дня. */
private fun parseIsoDateToMillis(iso: String): Long? {
    val t = iso.trim()
    if (t.isBlank()) return null
    val p = t.split('-')
    if (p.size != 3) return null
    val y = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    val d = p[2].toIntOrNull() ?: return null
    if (m !in 1..12 || d !in 1..31) return null
    val mkDate = js("(function(y,m,d){ return new Date(y,m-1,d); })").unsafeCast<(Int, Int, Int) -> dynamic>()
    val date = mkDate(y, m, d)
    if ((date.getDate() as Int) != d || (date.getMonth() as Int) != m - 1) return null
    return (date.getTime() as Double).toLong()
}

private fun removeAdminCadetGroupModalsFromRoot() {
    document.getElementById("sd-admin-add-group-overlay")?.let { el -> el.parentNode?.removeChild(el) }
    document.getElementById("sd-admin-cadet-group-picker-overlay")?.let { el -> el.parentNode?.removeChild(el) }
    document.getElementById("sd-admin-training-vehicle-overlay")?.let { el -> el.parentNode?.removeChild(el) }
}

private fun renderChatGroupModalHtml(): String {
    val isEdit = appState.adminChatGroupEditId != null
    val title = if (isEdit) "Редактировать группу" else "Создать группу"
    val btnSave = if (isEdit) "Сохранить" else "Создать"
    val nameVal = appState.adminChatGroupDraftName.escapeHtml()
    val uid = appState.user?.id ?: ""
    val editG = appState.adminChatGroupEditId?.let { eid -> appState.chatGroups.find { it.id == eid } }
    val nameForAvatarInitials = (editG?.name ?: appState.adminChatGroupDraftName).trim()
    val avatarInitialsEsc = groupChatNameInitials(nameForAvatarInitials).escapeHtml()
    val existingAvatarUrl = editG?.chatAvatarUrl?.takeIf { it.isNotBlank() }
    val groupIdEsc = (editG?.id ?: "").escapeHtml()
    val avatarPreviewInner = if (existingAvatarUrl != null) {
        """<img src="${existingAvatarUrl.escapeHtml()}" alt="" id="sd-group-chat-avatar-img" class="sd-settings-avatar-img sd-avatar-img" data-group-id="$groupIdEsc" />"""
    } else {
        """<span class="sd-settings-avatar-placeholder" id="sd-group-chat-avatar-placeholder">$avatarInitialsEsc</span>"""
    }
    val pickOpts = appState.chatContacts
        .filter { it.id !in appState.adminChatGroupDraftMemberIds && it.id != uid }
        .sortedBy { it.fullName }
        .joinToString("") { u ->
            val roleLabel = when (u.role) { "instructor" -> "Инструктор" "cadet" -> "Курсант" else -> u.role }.escapeHtml()
            """<option value="${u.id.escapeHtml()}">${formatShortName(u.fullName).escapeHtml()} · $roleLabel</option>"""
        }
    val chips = appState.adminChatGroupDraftMemberIds.joinToString("") { mid ->
        val u = appState.chatContacts.find { it.id == mid }
        val label = (u?.let { formatShortName(it.fullName) } ?: mid).escapeHtml()
        """<span class="sd-chat-group-chip">$label <button type="button" class="sd-chat-group-chip-remove" data-remove-member="${mid.escapeHtml()}" title="Удалить">×</button></span>"""
    }
    val avatarSection = """<div class="sd-settings-block sd-chat-group-avatar-block">
       <h3 class="sd-settings-block-title">Аватар группы</h3>
       <div class="sd-settings-avatar-wrap">
         <div class="sd-settings-avatar-preview" id="sd-group-chat-avatar-preview">
           $avatarPreviewInner
         </div>
         <input type="file" id="sd-group-chat-avatar-file" class="sd-settings-avatar-file" accept="image/*" />
         <label for="sd-group-chat-avatar-file" class="sd-btn sd-btn-secondary sd-settings-avatar-label">Выбрать фото</label>
         <button type="button" id="sd-group-chat-avatar-remove" class="sd-btn sd-btn-secondary sd-settings-avatar-remove">Удалить аватар</button>
       </div>
       <div class="sd-avatar-crop-editor sd-hidden" id="sd-group-chat-avatar-crop-editor">
         <p class="sd-avatar-crop-hint">Перетащите фото и измените масштаб. В круге будет виден аватар.</p>
         <div class="sd-avatar-crop-stage">
           <div class="sd-avatar-crop-overlay" id="sd-group-chat-avatar-crop-overlay" aria-hidden="true"></div>
           <div class="sd-avatar-crop-frame" id="sd-group-chat-avatar-crop-frame">
             <img id="sd-group-chat-avatar-crop-img" class="sd-avatar-crop-img" alt="" draggable="false" />
           </div>
         </div>
         <div class="sd-avatar-crop-controls">
           <div class="sd-avatar-crop-scale-row">
             <span class="sd-avatar-crop-scale-label">Масштаб</span>
             <input type="range" id="sd-group-chat-avatar-crop-scale" class="sd-avatar-crop-scale" min="0.5" max="2" step="0.05" value="1" title="Масштаб" />
           </div>
           <div class="sd-avatar-crop-buttons">
             <button type="button" id="sd-group-chat-avatar-crop-cancel" class="sd-btn sd-btn-secondary">Отмена</button>
             <button type="button" id="sd-group-chat-avatar-crop-apply" class="sd-btn sd-btn-primary">Готово</button>
           </div>
         </div>
       </div>
       </div>"""
    return """<div class="sd-modal-overlay" id="sd-chat-group-modal-overlay">
        <div class="sd-modal sd-chat-group-modal" id="sd-chat-group-modal-dialog">
            <h3 class="sd-modal-title">$title</h3>
            $avatarSection
            <label class="sd-auth-label" for="sd-chat-group-name">Название группы</label>
            <input type="text" id="sd-chat-group-name" class="sd-auth-input" maxlength="120" autocomplete="off" value="$nameVal" />
            <p class="sd-auth-label">Добавить контакты</p>
            <div class="sd-chat-group-add-row">
                <select id="sd-chat-group-pick" class="sd-auth-input sd-chat-group-pick"><option value="">— выберите контакт —</option>$pickOpts</select>
                <button type="button" id="sd-chat-group-add-member" class="sd-btn sd-btn-secondary">Добавить</button>
            </div>
            <div class="sd-chat-group-chips" id="sd-chat-group-chips">$chips</div>
            <p class="sd-modal-actions">
                <button type="button" id="sd-chat-group-cancel" class="sd-btn sd-btn-secondary">Отменить</button>
                <button type="button" id="sd-chat-group-save" class="sd-btn sd-btn-primary">$btnSave</button>
            </p>
        </div>
    </div>"""
}

private fun renderChatGroupDeleteConfirmHtml(): String {
    val gid = appState.adminChatGroupDeleteConfirmId?.escapeHtml() ?: ""
    return """<div class="sd-modal-overlay" id="sd-chat-group-delete-overlay" data-gid="$gid">
        <div class="sd-modal sd-chat-group-delete-modal">
            <h3 class="sd-modal-title">Вы уверены?</h3>
            <p class="sd-modal-actions">
                <button type="button" id="sd-chat-group-delete-yes" class="sd-btn sd-btn-danger">Да</button>
                <button type="button" id="sd-chat-group-delete-no" class="sd-btn sd-btn-secondary">Нет</button>
            </p>
        </div>
    </div>"""
}

private fun wireChatGroupModalControls(root: org.w3c.dom.Element) {
    document.getElementById("sd-chat-group-cancel")?.addEventListener("click", {
        clearGroupChatAvatarPending()
        updateState {
            adminChatGroupModalOpen = false
            adminChatGroupEditId = null
            adminChatGroupDraftName = ""
            adminChatGroupDraftMemberIds = emptyList()
        }
    })
    document.getElementById("sd-chat-group-add-member")?.addEventListener("click", {
        val sel = document.getElementById("sd-chat-group-pick") as? HTMLSelectElement ?: return@addEventListener
        val id = (sel.value ?: "").trim()
        if (id.isBlank()) return@addEventListener
        val nameInp = document.getElementById("sd-chat-group-name") as? HTMLInputElement
        val name = nameInp?.value?.trim() ?: ""
        updateState {
            if (id !in adminChatGroupDraftMemberIds) adminChatGroupDraftMemberIds = adminChatGroupDraftMemberIds + id
            adminChatGroupDraftName = name
        }
        sel.value = ""
    })
    document.getElementById("sd-chat-group-modal-overlay")?.addEventListener("click", { e: dynamic ->
        val t = e?.target as? org.w3c.dom.Element ?: return@addEventListener
        if (t.id == "sd-chat-group-modal-overlay") {
            clearGroupChatAvatarPending()
            updateState {
                adminChatGroupModalOpen = false
                adminChatGroupEditId = null
                adminChatGroupDraftName = ""
                adminChatGroupDraftMemberIds = emptyList()
            }
        }
    })
    document.getElementById("sd-chat-group-modal-dialog")?.addEventListener("click", { e: dynamic ->
        val t = e?.target as? org.w3c.dom.Element ?: return@addEventListener
        val rm = t.closest("[data-remove-member]") as? org.w3c.dom.Element ?: return@addEventListener
        val mid = rm.getAttribute("data-remove-member") ?: return@addEventListener
        val nameInp = document.getElementById("sd-chat-group-name") as? HTMLInputElement
        val name = nameInp?.value?.trim() ?: ""
        updateState {
            adminChatGroupDraftMemberIds = adminChatGroupDraftMemberIds.filter { it != mid }
            adminChatGroupDraftName = name
        }
    })
    document.getElementById("sd-chat-group-save")?.addEventListener("click", {
        val adminUid = appState.user?.id ?: return@addEventListener
        if (appState.user?.role != "admin") return@addEventListener
        val nameInp = document.getElementById("sd-chat-group-name") as? HTMLInputElement ?: return@addEventListener
        val name = nameInp.value.trim()
        if (name.isBlank()) {
            updateState { networkError = "Введите название группы." }
            return@addEventListener
        }
        val members = appState.adminChatGroupDraftMemberIds
        val editId = appState.adminChatGroupEditId
        fun closeModalSuccess() {
            refreshChatGroups()
            clearGroupChatAvatarPending()
            updateState {
                adminChatGroupModalOpen = false
                adminChatGroupEditId = null
                adminChatGroupDraftName = ""
                adminChatGroupDraftMemberIds = emptyList()
            }
        }
        if (editId != null) {
            updateChatGroup(editId, name, members, adminUid) { err ->
                if (err != null) updateState { networkError = err }
                else applyGroupAvatarAfterFirebaseSave(editId) { closeModalSuccess() }
            }
        } else {
            addChatGroup(name, members, adminUid) { err, newId ->
                if (err != null) updateState { networkError = err }
                else if (newId != null) applyGroupAvatarAfterFirebaseSave(newId) { closeModalSuccess() }
                else closeModalSuccess()
            }
        }
    })
    wireGroupChatAvatarModalControls(root)
}

private fun wireGroupChatAvatarModalControls(root: org.w3c.dom.Element) {
    val cropState = groupModalCropState
    val cropDataUrlHolder = groupModalCropDataUrlHolder
    val updateCropImgTransform = {
        document.getElementById("sd-group-chat-avatar-crop-img")?.unsafeCast<org.w3c.dom.HTMLElement>()?.style?.setProperty(
            "transform", "translate(-50%,-50%) translate(${cropState[0]}px,${cropState[1]}px) scale(${cropState[2]})"
        )
    }
    document.getElementById("sd-group-chat-avatar-file")?.addEventListener("change", { ev: dynamic ->
        val target = ev?.target?.unsafeCast<dynamic>()
        val getFirstFile = js("(function(f){ return f && f[0]; })").unsafeCast<(dynamic) -> dynamic>()
        val file = getFirstFile(target?.files)
        if (file != null) {
            val reader = js("new FileReader()").unsafeCast<dynamic>()
            reader.onload = {
                val dataUrl = reader.result as? String
                if (dataUrl != null) {
                    cropDataUrlHolder[0] = dataUrl
                    val editor = document.getElementById("sd-group-chat-avatar-crop-editor")?.unsafeCast<org.w3c.dom.HTMLElement>()
                    val cropImg = document.getElementById("sd-group-chat-avatar-crop-img")?.unsafeCast<org.w3c.dom.HTMLImageElement>()
                    val frame = document.getElementById("sd-group-chat-avatar-crop-frame")
                    val scaleInput = document.getElementById("sd-group-chat-avatar-crop-scale") as? HTMLInputElement
                    if (editor != null && cropImg != null && frame != null) {
                        editor.classList.remove("sd-hidden")
                        cropImg.src = dataUrl
                        cropImg.onload = {
                            val nw = cropImg.naturalWidth.toDouble().coerceAtLeast(1.0)
                            val nh = cropImg.naturalHeight.toDouble().coerceAtLeast(1.0)
                            val containScale = (200.0 / nw).coerceAtMost(200.0 / nh).coerceAtMost(1.0)
                            cropImg.style.setProperty("width", cropImg.naturalWidth.toString() + "px")
                            cropImg.style.setProperty("height", cropImg.naturalHeight.toString() + "px")
                            cropImg.style.setProperty("object-fit", "none")
                            cropState[0] = 0.0
                            cropState[1] = 0.0
                            cropState[2] = containScale
                            scaleInput?.value = containScale.toString()
                            scaleInput?.setAttribute("value", containScale.toString())
                            updateCropImgTransform()
                        }
                    }
                }
            }
            reader.readAsDataURL(file)
        }
    })
    fun syncCropScaleSlider() {
        (document.getElementById("sd-group-chat-avatar-crop-scale") as? HTMLInputElement)?.let { it.value = cropState[2].toString(); it.setAttribute("value", cropState[2].toString()) }
    }
    document.getElementById("sd-group-chat-avatar-crop-scale")?.addEventListener("input", {
        val input = document.getElementById("sd-group-chat-avatar-crop-scale") as? HTMLInputElement ?: return@addEventListener
        val v = input.value.toDoubleOrNull() ?: 1.0
        cropState[2] = v.coerceIn(0.5, 2.0)
        updateCropImgTransform()
    })
    fun getClientXY(e: dynamic): Pair<Double, Double> {
        val tx = (e?.touches?.get(0) ?: e)?.clientX as? Number
        val ty = (e?.touches?.get(0) ?: e)?.clientY as? Number
        return Pair((tx?.toDouble() ?: (e?.clientX as? Number)?.toDouble() ?: 0.0), (ty?.toDouble() ?: (e?.clientY as? Number)?.toDouble() ?: 0.0))
    }
    fun getTouchDistance(e: dynamic): Double {
        val t = e?.touches
        if (t == null || (t.asDynamic().length as Int) < 2) return 0.0
        val x1 = (t.asDynamic()[0].clientX as Number).toDouble()
        val y1 = (t.asDynamic()[0].clientY as Number).toDouble()
        val x2 = (t.asDynamic()[1].clientX as Number).toDouble()
        val y2 = (t.asDynamic()[1].clientY as Number).toDouble()
        return kotlin.math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    }
    document.getElementById("sd-group-chat-avatar-crop-frame")?.addEventListener("mousedown", { e: dynamic ->
        groupModalCropDragging = true
        groupModalCropPinching = false
        val (x, y) = getClientXY(e)
        groupModalCropDragStartX = x
        groupModalCropDragStartY = y
        groupModalCropDragOffsetStartX = cropState[0]
        groupModalCropDragOffsetStartY = cropState[1]
        (e as? org.w3c.dom.events.Event)?.preventDefault()
    })
    document.getElementById("sd-group-chat-avatar-crop-frame")?.addEventListener("touchstart", { e: dynamic ->
        val touchCount = (e?.touches?.asDynamic()?.length as? Int) ?: 0
        if (touchCount >= 2) {
            groupModalCropPinching = true
            groupModalCropDragging = false
            groupModalCropPinchStartDist = getTouchDistance(e).coerceAtLeast(1.0)
            groupModalCropPinchStartScale = cropState[2]
        } else {
            groupModalCropDragging = true
            groupModalCropPinching = false
            val (x, y) = getClientXY(e)
            groupModalCropDragStartX = x
            groupModalCropDragStartY = y
            groupModalCropDragOffsetStartX = cropState[0]
            groupModalCropDragOffsetStartY = cropState[1]
        }
        (e as? org.w3c.dom.events.Event)?.preventDefault()
    })
    if (!groupChatAvatarRootListenersBound) {
        groupChatAvatarRootListenersBound = true
        root.addEventListener("mousemove", { e: dynamic ->
            if (!groupModalCropDragging) return@addEventListener
            val (x, y) = getClientXY(e)
            cropState[0] = groupModalCropDragOffsetStartX + (x - groupModalCropDragStartX)
            cropState[1] = groupModalCropDragOffsetStartY + (y - groupModalCropDragStartY)
            updateCropImgTransform()
        })
        root.addEventListener("touchmove", { e: dynamic ->
            val touchCount = (e?.touches?.asDynamic()?.length as? Int) ?: 0
            if (groupModalCropPinching && touchCount >= 2) {
                val d = getTouchDistance(e).coerceAtLeast(1.0)
                val newScale = (groupModalCropPinchStartScale * d / groupModalCropPinchStartDist).coerceIn(0.5, 2.0)
                cropState[2] = newScale
                syncCropScaleSlider()
                updateCropImgTransform()
                (e as? org.w3c.dom.events.Event)?.preventDefault()
            } else if (groupModalCropDragging && touchCount >= 1) {
                val (x, y) = getClientXY(e)
                cropState[0] = groupModalCropDragOffsetStartX + (x - groupModalCropDragStartX)
                cropState[1] = groupModalCropDragOffsetStartY + (y - groupModalCropDragStartY)
                updateCropImgTransform()
                (e as? org.w3c.dom.events.Event)?.preventDefault()
            }
        })
        root.addEventListener("mouseup", { _: dynamic -> groupModalCropDragging = false })
        root.addEventListener("touchend", { e: dynamic ->
            groupModalCropDragging = false
            val touchCount = (e?.touches?.asDynamic()?.length as? Int) ?: 0
            if (touchCount < 2) groupModalCropPinching = false
        })
    }
    document.getElementById("sd-group-chat-avatar-crop-apply")?.addEventListener("click", {
        val dataUrl = cropDataUrlHolder[0] ?: return@addEventListener
        val cropImg = document.getElementById("sd-group-chat-avatar-crop-img") as? org.w3c.dom.HTMLImageElement ?: return@addEventListener
        if (cropImg.naturalWidth == 0) return@addEventListener
        val scale = cropState[2]
        val ox = cropState[0]
        val oy = cropState[1]
        val nw = cropImg.naturalWidth.toDouble()
        val nh = cropImg.naturalHeight.toDouble()
        val canvas = document.createElement("canvas").unsafeCast<org.w3c.dom.HTMLCanvasElement>()
        val size = 200
        canvas.width = size
        canvas.height = size
        val ctx = canvas.getContext("2d")?.unsafeCast<dynamic>() ?: return@addEventListener
        ctx.save()
        ctx.fillStyle = "rgba(0,0,0,0.06)"
        ctx.fillRect(0.0, 0.0, size.toDouble(), size.toDouble())
        ctx.beginPath()
        ctx.arc(size / 2.0, size / 2.0, size / 2.0, 0.0, 6.283185307179586)
        ctx.closePath()
        ctx.clip()
        ctx.translate(size / 2.0 + ox, size / 2.0 + oy)
        ctx.scale(scale, scale)
        ctx.drawImage(cropImg, -nw / 2.0, -nh / 2.0, nw, nh)
        ctx.restore()
        val resultDataUrl = try { canvas.toDataURL("image/png") } catch (_: Throwable) { "" }
        val finalDataUrl = if (resultDataUrl.isNotEmpty()) resultDataUrl else dataUrl
        document.getElementById("sd-group-chat-avatar-crop-editor")?.unsafeCast<org.w3c.dom.HTMLElement>()?.classList?.add("sd-hidden")
        cropDataUrlHolder[0] = null
        groupChatAvatarPendingRemove = false
        groupChatAvatarPendingDataUrl = finalDataUrl
        val preview = document.getElementById("sd-group-chat-avatar-preview") ?: return@addEventListener
        preview.querySelector(".sd-settings-avatar-placeholder")?.remove()
        var img = preview.querySelector(".sd-settings-avatar-img") as? org.w3c.dom.HTMLElement
        if (img == null) {
            img = document.createElement("img").unsafeCast<org.w3c.dom.HTMLElement>()
            img.className = "sd-settings-avatar-img sd-avatar-img"
            img.setAttribute("alt", "")
            img.id = "sd-group-chat-avatar-img"
            preview.appendChild(img)
        }
        img.setAttribute("src", finalDataUrl)
        document.getElementById("sd-group-chat-avatar-file")?.let { it.unsafeCast<dynamic>().value = "" }
    })
    document.getElementById("sd-group-chat-avatar-crop-cancel")?.addEventListener("click", {
        document.getElementById("sd-group-chat-avatar-crop-editor")?.unsafeCast<org.w3c.dom.HTMLElement>()?.classList?.add("sd-hidden")
        cropDataUrlHolder[0] = null
        document.getElementById("sd-group-chat-avatar-file")?.let { it.unsafeCast<dynamic>().value = "" }
    })
    document.getElementById("sd-group-chat-avatar-remove")?.addEventListener("click", {
        groupChatAvatarPendingRemove = true
        groupChatAvatarPendingDataUrl = null
        val preview = document.getElementById("sd-group-chat-avatar-preview") ?: return@addEventListener
        preview.querySelector(".sd-settings-avatar-img")?.remove()
        val nameInp = document.getElementById("sd-chat-group-name") as? HTMLInputElement
        val name = nameInp?.value?.trim() ?: ""
        val placeholder = document.createElement("span").unsafeCast<org.w3c.dom.HTMLElement>()
        placeholder.className = "sd-settings-avatar-placeholder"
        placeholder.id = "sd-group-chat-avatar-placeholder"
        placeholder.textContent = groupChatNameInitials(name)
        preview.querySelector("#sd-group-chat-avatar-placeholder")?.remove()
        preview.appendChild(placeholder)
        document.getElementById("sd-group-chat-avatar-file")?.let { it.unsafeCast<dynamic>().value = "" }
    })
}

private fun appendChatGroupModalsIfNeeded(root: org.w3c.dom.Element) {
    if (appState.user?.role != "admin") {
        document.getElementById("sd-chat-group-modal-overlay")?.remove()
        document.getElementById("sd-chat-group-delete-overlay")?.remove()
        return
    }
    val showModal = appState.adminChatGroupModalOpen || appState.adminChatGroupEditId != null
    if (showModal) {
        val ex = document.getElementById("sd-chat-group-modal-overlay")
        val sig = "${appState.adminChatGroupEditId}|${appState.adminChatGroupDraftMemberIds.joinToString(",")}"
        if (ex == null || ex.getAttribute("data-sig") != sig) {
            ex?.remove()
            val wrap = document.createElement("div")
            wrap.innerHTML = renderChatGroupModalHtml()
            val el = wrap.firstElementChild ?: return
            el.setAttribute("data-sig", sig)
            root.appendChild(el)
            wireChatGroupModalControls(root)
        }
    } else {
        document.getElementById("sd-chat-group-modal-overlay")?.remove()
    }
    val delId = appState.adminChatGroupDeleteConfirmId
    if (delId != null) {
        val ex = document.getElementById("sd-chat-group-delete-overlay")
        if (ex == null || ex.getAttribute("data-gid") != delId) {
            ex?.remove()
            val wrap = document.createElement("div")
            wrap.innerHTML = renderChatGroupDeleteConfirmHtml()
            val el = wrap.firstElementChild ?: return
            root.appendChild(el)
            document.getElementById("sd-chat-group-delete-yes")?.addEventListener("click", {
                val gid = appState.adminChatGroupDeleteConfirmId ?: return@addEventListener
                deleteChatGroup(gid) { err ->
                    if (err != null) updateState { networkError = err; adminChatGroupDeleteConfirmId = null }
                    else {
                        val wasOpen = appState.selectedChatGroupId == gid
                        refreshChatGroups()
                        updateState {
                            adminChatGroupDeleteConfirmId = null
                            if (wasOpen) {
                                selectedChatGroupId = null
                                chatMessages = emptyList()
                            }
                        }
                        unsubscribeChat()
                    }
                }
            })
            document.getElementById("sd-chat-group-delete-no")?.addEventListener("click", {
                updateState { adminChatGroupDeleteConfirmId = null }
            })
            document.getElementById("sd-chat-group-delete-overlay")?.addEventListener("click", { e: dynamic ->
                val t = e?.target as? org.w3c.dom.Element ?: return@addEventListener
                if (t.id == "sd-chat-group-delete-overlay") updateState { adminChatGroupDeleteConfirmId = null }
            })
        }
    } else {
        document.getElementById("sd-chat-group-delete-overlay")?.remove()
    }
}

/** Модалка выбора причины отмены вождения — вне #sd-card, чтобы опрос/рендер не уничтожали DOM и не вызывали мигание. */
private fun instructorCancelReasonModalOverlayHtml(): String = """<div class="sd-modal-overlay sd-hidden" id="sd-instructor-cancel-reason-modal"><div class="sd-modal"><h3 class="sd-modal-title">Выберите причину отмены:</h3><div class="sd-rate-options-instructor sd-cancel-reason-options"><label class="sd-radio"><input type="radio" name="sd-cancel-reason" value="Курсант не явился" checked /> Курсант не явился</label><label class="sd-radio"><input type="radio" name="sd-cancel-reason" value="ТС на ремонте" /> ТС на ремонте</label><label class="sd-radio"><input type="radio" name="sd-cancel-reason" value="__OTHER__" /> Другая причина</label></div><div id="sd-cancel-reason-other-wrap" class="sd-cancel-reason-other-wrap sd-hidden"><label class="sd-auth-label" for="sd-cancel-reason-other-text">Укажите причину</label><textarea id="sd-cancel-reason-other-text" class="sd-auth-input sd-cancel-reason-other-text" rows="3" maxlength="500" placeholder="Кратко опишите причину"></textarea></div><p class="sd-modal-actions"><button type="button" id="sd-instructor-cancel-reason-confirm" class="sd-btn sd-btn-primary">Подтвердить</button><button type="button" id="sd-instructor-cancel-reason-cancel" class="sd-btn sd-btn-secondary">Отмена</button></p></div></div>"""

/** Подтверждение отмены на главной, если запись ещё не подтверждена курсантом. */
private fun instructorHomeCancelUnconfirmedModalOverlayHtml(): String = """<div class="sd-modal-overlay sd-hidden" id="sd-home-cancel-unconfirmed-modal"><div class="sd-modal"><h3 class="sd-modal-title">Вы уверены?</h3><p class="sd-muted">Запись ещё не подтверждена курсантом.</p><p class="sd-modal-actions"><button type="button" id="sd-home-cancel-unconfirmed-yes" class="sd-btn sd-btn-primary">Да</button><button type="button" id="sd-home-cancel-unconfirmed-no" class="sd-btn sd-btn-secondary">Нет</button></p></div></div>"""

private fun appendInstructorCancelReasonModalIfNeeded(root: org.w3c.dom.Element) {
    if (appState.screen != AppScreen.Instructor || appState.user?.role != "instructor") {
        document.getElementById("sd-instructor-cancel-reason-modal")?.remove()
        document.getElementById("sd-home-cancel-unconfirmed-modal")?.remove()
        return
    }
    if (document.getElementById("sd-instructor-cancel-reason-modal") == null) {
        val wrap = document.createElement("div")
        wrap.innerHTML = instructorCancelReasonModalOverlayHtml()
        val el = wrap.firstElementChild ?: return
        root.appendChild(el)
        wireInstructorCancelReasonModalListeners(root)
    }
    if (document.getElementById("sd-home-cancel-unconfirmed-modal") == null) {
        val wrap2 = document.createElement("div")
        wrap2.innerHTML = instructorHomeCancelUnconfirmedModalOverlayHtml()
        val el2 = wrap2.firstElementChild ?: return
        root.appendChild(el2)
        wireInstructorHomeCancelUnconfirmedModalListeners()
    }
}

private fun wireInstructorHomeCancelUnconfirmedModalListeners() {
    document.getElementById("sd-home-cancel-unconfirmed-yes")?.addEventListener("click", {
        val usr = appState.user ?: return@addEventListener
        if (usr.role != "instructor") return@addEventListener
        val modal = document.getElementById("sd-home-cancel-unconfirmed-modal") ?: return@addEventListener
        val sessionId = modal.asDynamic().dataset["sessionId"] as? String ?: return@addEventListener
        homeCancelUnconfirmedModalSessionId = null
        syncInstructorActionModalsVisibility()
        val session = appState.recordingSessions.find { it.id == sessionId }
        val startMs = session?.startTimeMillis
        val cadetShortName = appState.instructorCadets.find { it.id == session?.cadetId }?.fullName?.let { formatShortName(it) } ?: "Курсант"
        cancelByInstructor(sessionId, "—") { err ->
            if (err != null) updateState { networkError = err }
            else {
                showNotification("Вождение отменено" + formatDayTimeShort(startMs) + ". Курсант: $cadetShortName")
                getOpenWindowsForInstructor(usr.id) { wins ->
                    getSessionsForInstructor(usr.id) { sess ->
                        getBalanceHistory(usr.id) { hist ->
                            updateState { recordingOpenWindows = wins; recordingSessions = sess; historySessions = sess; historyBalance = hist }
                            notifyNewBalanceOpsForCurrentUser(hist)
                        }
                    }
                }
            }
        }
    })
    document.getElementById("sd-home-cancel-unconfirmed-no")?.addEventListener("click", {
        homeCancelUnconfirmedModalSessionId = null
        syncInstructorActionModalsVisibility()
    })
}

private fun wireInstructorCancelReasonModalListeners(root: org.w3c.dom.Element) {
    document.getElementById("sd-instructor-cancel-reason-modal")?.addEventListener("change", { ev ->
        val target = ev.target as? HTMLInputElement ?: return@addEventListener
        if (target.name == "sd-cancel-reason") {
            val otherWrap = document.getElementById("sd-cancel-reason-other-wrap")
            if (target.value == "__OTHER__") {
                otherWrap?.classList?.remove("sd-hidden")
            } else {
                otherWrap?.classList?.add("sd-hidden")
            }
        }
        if (target.name != "sd-cancel-reason" || target.value != "Курсант не явился") return@addEventListener
        val modal = document.getElementById("sd-instructor-cancel-reason-modal") ?: return@addEventListener
        val sessionId = modal.asDynamic().dataset["sessionId"] as? String ?: return@addEventListener
        val session = appState.recordingSessions.find { it.id == sessionId } ?: return@addEventListener
        val now = js("Date.now()").unsafeCast<Double>().toLong()
        val startMs = session.startTimeMillis ?: 0L
        val timeNotReached = startMs > 0 && now < startMs
        if (timeNotReached) {
            showNotification("Выбрать причину не возможно, еще не настало время вождения!")
            document.querySelector("input[name=sd-cancel-reason][value=\"ТС на ремонте\"]")?.unsafeCast<HTMLInputElement>()?.let { it.checked = true }
            document.getElementById("sd-cancel-reason-other-wrap")?.classList?.add("sd-hidden")
            return@addEventListener
        }
        if (session.instructorConfirmed != true) {
            showNotification("Выбрать причину не возможно, курсант не подтвердил вождение!")
            document.querySelector("input[name=sd-cancel-reason][value=\"ТС на ремонте\"]")?.unsafeCast<HTMLInputElement>()?.let { it.checked = true }
            document.getElementById("sd-cancel-reason-other-wrap")?.classList?.add("sd-hidden")
            return@addEventListener
        }
    })
    document.getElementById("sd-instructor-cancel-reason-confirm")?.addEventListener("click", {
        val usr = appState.user ?: return@addEventListener
        if (usr.role != "instructor") return@addEventListener
        val modal = document.getElementById("sd-instructor-cancel-reason-modal") ?: return@addEventListener
        val sessionId = modal.asDynamic().dataset["sessionId"] as? String ?: return@addEventListener
        val selected = document.querySelector("input[name=sd-cancel-reason]:checked") as? HTMLInputElement
        val rawVal = selected?.value?.takeIf { it.isNotBlank() }
        val reason = when (rawVal) {
            "__OTHER__" -> {
                val t = (document.getElementById("sd-cancel-reason-other-text") as? HTMLTextAreaElement)?.value?.trim()
                if (t.isNullOrBlank()) "Другая причина" else t
            }
            null -> "—"
            else -> rawVal
        }
        cancelReasonModalSessionId = null
        syncInstructorActionModalsVisibility()
        document.getElementById("sd-cancel-reason-other-wrap")?.classList?.add("sd-hidden")
        (document.getElementById("sd-cancel-reason-other-text") as? HTMLTextAreaElement)?.value = ""
        val session = appState.recordingSessions.find { it.id == sessionId }
        val startMs = session?.startTimeMillis
        val cadetShortName = appState.instructorCadets.find { it.id == session?.cadetId }?.fullName?.let { formatShortName(it) } ?: "Курсант"
        cancelByInstructor(sessionId, reason) { err ->
            if (err != null) updateState { networkError = err }
            else {
                showNotification("Вождение отменено" + formatDayTimeShort(startMs) + ". Курсант: $cadetShortName")
                getOpenWindowsForInstructor(usr.id) { wins ->
                    getSessionsForInstructor(usr.id) { sess ->
                        getBalanceHistory(usr.id) { hist ->
                            updateState { recordingOpenWindows = wins; recordingSessions = sess; historySessions = sess; historyBalance = hist }
                            notifyNewBalanceOpsForCurrentUser(hist)
                        }
                    }
                }
            }
        }
    })
    document.getElementById("sd-instructor-cancel-reason-cancel")?.addEventListener("click", {
        document.getElementById("sd-cancel-reason-other-wrap")?.classList?.add("sd-hidden")
        (document.getElementById("sd-cancel-reason-other-text") as? HTMLTextAreaElement)?.value = ""
        cancelReasonModalSessionId = null
        syncInstructorActionModalsVisibility()
    })
}

/** Модалка «Опаздываю» — вне #sd-card, чтобы не моргала при опросе/рендере. */
private fun instructorRunningLateModalOverlayHtml(): String = """<div class="sd-modal-overlay sd-hidden" id="sd-running-late-modal"><div class="sd-modal"><h3 class="sd-modal-title">Опаздываю</h3><p>Выберите задержку:</p><div class="sd-running-late-options"><label class="sd-radio"><input type="radio" name="sd-late-mins" value="5" /> 5 мин.</label><label class="sd-radio"><input type="radio" name="sd-late-mins" value="10" /> 10 мин.</label><label class="sd-radio"><input type="radio" name="sd-late-mins" value="15" /> 15 мин.</label></div><p class="sd-modal-actions"><button type="button" id="sd-running-late-confirm" class="sd-btn sd-btn-primary">Подтвердить</button><button type="button" id="sd-running-late-cancel" class="sd-btn sd-btn-secondary">Отмена</button></p></div></div>"""

private fun appendInstructorRunningLateModalIfNeeded(root: org.w3c.dom.Element) {
    if (appState.screen != AppScreen.Instructor || appState.user?.role != "instructor") {
        document.getElementById("sd-running-late-modal")?.remove()
        return
    }
    if (document.getElementById("sd-running-late-modal") != null) return
    val wrap = document.createElement("div")
    wrap.innerHTML = instructorRunningLateModalOverlayHtml()
    val el = wrap.firstElementChild ?: return
    root.appendChild(el)
    wireInstructorRunningLateModalListeners()
}

/** Модалка оценки курсанту — вне #sd-card, чтобы радио не сбрасывались при каждом перерисовке графика. */
private fun instructorRateCadetModalOverlayHtml(): String = """<div class="sd-modal-overlay sd-hidden" id="sd-instructor-rate-cadet-modal"><div class="sd-modal"><h3 class="sd-modal-title">Поставьте оценку курсанту:</h3><p id="sd-instructor-rate-cadet-name" class="sd-rate-cadet-name"></p><div class="sd-rate-options-instructor"><label class="sd-radio"><input type="radio" name="sd-instructor-rate" value="3" /> 3</label><label class="sd-radio"><input type="radio" name="sd-instructor-rate" value="4" /> 4</label><label class="sd-radio"><input type="radio" name="sd-instructor-rate" value="5" /> 5</label></div><p class="sd-modal-actions"><button type="button" id="sd-instructor-rate-cadet-confirm" class="sd-btn sd-btn-primary">Подтвердить</button></p></div></div>"""

private fun removeDuplicateInstructorRateCadetModals() {
    try {
        val all = document.querySelectorAll("#sd-instructor-rate-cadet-modal")
        val len = (all.length as? Int) ?: 0
        if (len <= 1) return
        for (i in 1 until len) {
            val n = all.item(i) ?: continue
            n.parentNode?.removeChild(n)
        }
    } catch (_: Throwable) { }
}

private fun appendInstructorRateCadetModalIfNeeded(root: org.w3c.dom.Element) {
    if (appState.screen != AppScreen.Instructor || appState.user?.role != "instructor") {
        document.getElementById("sd-instructor-rate-cadet-modal")?.remove()
        return
    }
    removeDuplicateInstructorRateCadetModals()
    if (document.getElementById("sd-instructor-rate-cadet-modal") != null) return
    val wrap = document.createElement("div")
    wrap.innerHTML = instructorRateCadetModalOverlayHtml()
    val el = wrap.firstElementChild ?: return
    root.appendChild(el)
    wireInstructorRateCadetModalListeners()
    // attachListeners вызывается до append* — иначе sync не находит узел при первом входе инструктора.
    syncInstructorActionModalsVisibility()
}

private fun wireInstructorRateCadetModalListeners() {
    document.getElementById("sd-instructor-rate-cadet-confirm")?.addEventListener("click", {
        val usr = appState.user ?: return@addEventListener
        if (usr.role != "instructor") return@addEventListener
        val modal = document.getElementById("sd-instructor-rate-cadet-modal") ?: return@addEventListener
        val sessionId = modal.asDynamic().dataset["sessionId"] as? String ?: return@addEventListener
        val startMs = appState.recordingSessions.find { it.id == sessionId }?.startTimeMillis
        val checked = document.querySelector("input[name=sd-instructor-rate]:checked") as? HTMLInputElement
        val rating = checked?.value?.toIntOrNull() ?: return@addEventListener
        if (rating !in 3..5) return@addEventListener
        instructorRateCadetModalSessionId = null
        instructorRateCadetModalCadetName = ""
        syncInstructorActionModalsVisibility()
        setInstructorRating(sessionId, rating) { err ->
            if (err != null) updateState { networkError = err }
            else {
                showNotification("Оценка курсанту поставлена" + formatDayTimeShort(startMs))
                getOpenWindowsForInstructor(usr.id) { wins ->
                    getSessionsForInstructor(usr.id) { sess ->
                        getBalanceHistory(usr.id) { hist ->
                            updateState { recordingOpenWindows = wins; recordingSessions = sess; historySessions = sess; historyBalance = hist }
                            getCurrentUser { newUser, _ -> if (newUser != null) updateState { user = newUser } }
                            notifyNewBalanceOpsForCurrentUser(hist)
                        }
                    }
                }
            }
        }
    })
}

private fun wireInstructorRunningLateModalListeners() {
    document.getElementById("sd-running-late-confirm")?.addEventListener("click", {
        val usr = appState.user ?: return@addEventListener
        if (usr.role != "instructor") return@addEventListener
        val modal = document.getElementById("sd-running-late-modal") ?: return@addEventListener
        val sessionId = modal.asDynamic().dataset["sessionId"] as? String ?: return@addEventListener
        val checkedEl = document.querySelector("input[name=\"sd-late-mins\"]:checked")?.asDynamic()
        val delay = (checkedEl?.value as? String)?.toIntOrNull() ?: run { showToast("Выберите задержку"); return@addEventListener }
        val originalStart = appState.recordingSessions.find { it.id == sessionId }?.startTimeMillis ?: 0L
        val delayMs = delay * 60L * 1000L
        val untilMs = js("Date.now()").unsafeCast<Double>().toLong() + delayMs
        updateState { instructorRunningLateUntilMs = untilMs }
        setInstructorRunningLate(sessionId, delay) { err ->
            if (err != null) {
                updateState { networkError = err; instructorRunningLateUntilMs = 0L }
                return@setInstructorRunningLate
            }
            getSessionsForInstructor(usr.id) { sess ->
                val toShift = sess.filter { it.id != sessionId && it.status == "scheduled" && it.startTimeMillis != null && it.startTimeMillis!! > originalStart && it.startTimeMillis!! <= originalStart + delayMs }
                fun shiftNext(remaining: List<DrivingSession>) {
                    if (remaining.isEmpty()) {
                        getSessionsForInstructor(usr.id) { s -> updateState { recordingSessions = s } }
                        showNotification("Опаздываю: сдвиг на $delay мин." + formatDayTimeShort(originalStart))
                        runningLateModalSessionId = null
                        syncInstructorActionModalsVisibility()
                        return
                    }
                    val sess = remaining.first()
                    updateSessionStartTime(sess.id, (sess.startTimeMillis ?: 0L) + delayMs) {
                        shiftNext(remaining.drop(1))
                    }
                }
                shiftNext(toShift)
            }
        }
    })
    document.getElementById("sd-running-late-cancel")?.addEventListener("click", {
        runningLateModalSessionId = null
        syncInstructorActionModalsVisibility()
    })
}

/** Подтверждение отмены занятия на вкладке «Запись» (инструктор/курсант) — вне #sd-card. */
private fun recCancelSessionConfirmModalOverlayHtml(): String = """<div class="sd-modal-overlay sd-hidden" id="sd-rec-cancel-session-confirm-modal"><div class="sd-modal"><h3 class="sd-modal-title">Вы уверены?</h3><p class="sd-modal-actions"><button type="button" id="sd-rec-cancel-session-yes" class="sd-btn sd-btn-primary">Да</button><button type="button" id="sd-rec-cancel-session-no" class="sd-btn sd-btn-secondary">Нет</button></p></div></div>"""

/** Курсант: нельзя отменить за 6 ч до начала — только сообщение и OK. */
private fun cadetCancelSixHoursBlockedModalOverlayHtml(): String = """<div class="sd-modal-overlay sd-hidden" id="sd-cadet-cancel-six-hours-modal" aria-hidden="true"><div class="sd-modal"><h3 class="sd-modal-title">Внимание</h3><p class="sd-muted">Нельзя отменить за 6 часов до вождения. Сообщите своему инструктору или администратору.</p><p class="sd-modal-actions"><button type="button" id="sd-cadet-cancel-six-hours-ok" class="sd-btn sd-btn-primary">OK</button></p></div></div>"""

private fun cadetCancelBlockedWithinSixHoursOfStart(startTimeMillis: Long?): Boolean {
    if (startTimeMillis == null) return false
    val now = js("Date.now()").unsafeCast<Double>().toLong()
    if (startTimeMillis <= now) return true
    return (startTimeMillis - now) <= 6L * 60 * 60 * 1000
}

private fun appendRecCancelSessionConfirmModalIfNeeded(root: org.w3c.dom.Element) {
    if (appState.screen != AppScreen.Instructor && appState.screen != AppScreen.Cadet) {
        document.getElementById("sd-rec-cancel-session-confirm-modal")?.remove()
        document.getElementById("sd-cadet-cancel-six-hours-modal")?.remove()
        val w = window.asDynamic()
        w.__sdRecCancelModalWired = false
        return
    }
    if (document.getElementById("sd-rec-cancel-session-confirm-modal") == null) {
        val wrap = document.createElement("div")
        wrap.innerHTML = recCancelSessionConfirmModalOverlayHtml()
        val el = wrap.firstElementChild ?: return
        root.appendChild(el)
    }
    if (document.getElementById("sd-cadet-cancel-six-hours-modal") == null) {
        val wrap2 = document.createElement("div")
        wrap2.innerHTML = cadetCancelSixHoursBlockedModalOverlayHtml()
        val el2 = wrap2.firstElementChild ?: return
        root.appendChild(el2)
    }
    val w = window.asDynamic()
    if (w.__sdRecCancelModalWired != true) {
        w.__sdRecCancelModalWired = true
        wireRecCancelSessionConfirmModalListeners()
    }
}

private fun wireRecCancelSessionConfirmModalListeners() {
    document.getElementById("sd-rec-cancel-session-yes")?.addEventListener("click", {
        val usr = appState.user ?: return@addEventListener
        if (usr.role != "instructor" && usr.role != "cadet") return@addEventListener
        val modal = document.getElementById("sd-rec-cancel-session-confirm-modal") ?: return@addEventListener
        val sessionId = modal.asDynamic().dataset["sessionId"] as? String ?: return@addEventListener
        val session = appState.recordingSessions.find { it.id == sessionId }
        val startMs = session?.startTimeMillis
        val cadetShortNameForNotif = if (usr.role == "instructor") appState.instructorCadets.find { it.id == session?.cadetId }?.fullName?.let { formatShortName(it) } ?: "Курсант" else null
        modal.classList.add("sd-hidden")
        val onSuccess = {
            showNotification("Вождение отменено" + formatDayTimeShort(startMs) + (if (cadetShortNameForNotif != null) ". Курсант: $cadetShortNameForNotif" else ""))
            if (usr.role == "cadet") {
                val instId = usr.assignedInstructorId ?: ""
                getOpenWindowsForCadet(instId) { wins ->
                    getSessionsForCadet(usr.id) { sess ->
                        val safeSess = sess.filter { it.cadetId == usr.id }
                        updateState {
                            recordingOpenWindows = wins
                            recordingSessions = safeSess
                            // История курсанта должна показывать только его сессии; обновим её сразу после отмены.
                            historySessions = safeSess
                        }
                    }
                }
            } else {
                getOpenWindowsForInstructor(usr.id) { wins ->
                    getSessionsForInstructor(usr.id) { sess ->
                        getBalanceHistory(usr.id) { hist ->
                            updateState { recordingOpenWindows = wins; recordingSessions = sess; historySessions = sess; historyBalance = hist }
                            notifyNewBalanceOpsForCurrentUser(hist)
                        }
                    }
                }
            }
        }
        if (usr.role == "cadet") {
            cancelByCadet(sessionId) { err ->
                if (err != null) updateState { networkError = err }
                else onSuccess()
            }
        } else {
            cancelByInstructor(sessionId, "—") { err ->
                if (err != null) updateState { networkError = err }
                else onSuccess()
            }
        }
    })
    document.getElementById("sd-rec-cancel-session-no")?.addEventListener("click", {
        document.getElementById("sd-rec-cancel-session-confirm-modal")?.let { m -> m.classList.add("sd-hidden") }
    })
    document.getElementById("sd-cadet-cancel-six-hours-ok")?.addEventListener("click", {
        document.getElementById("sd-cadet-cancel-six-hours-modal")?.let { m ->
            m.classList.add("sd-hidden")
            m.setAttribute("aria-hidden", "true")
        }
    })
}

private fun wireAdminAddGroupModalControls() {
    val chk = document.getElementById("sd-admin-group-no-date") as? HTMLInputElement ?: return
    val from = document.getElementById("sd-admin-group-from") as? HTMLInputElement
    val to = document.getElementById("sd-admin-group-to") as? HTMLInputElement
    val wrap = document.querySelector(".sd-admin-group-dates")
    fun sync() {
        val noDate = chk.checked
        from?.asDynamic()?.disabled = noDate
        to?.asDynamic()?.disabled = noDate
        wrap?.classList?.toggle("sd-admin-group-dates-muted", noDate)
    }
    chk.addEventListener("change", { sync() })
    sync()
}

private fun renderAdminAddGroupModalHtml(): String {
    val editId = appState.adminEditingGroupId
    val g = editId?.let { id -> appState.cadetGroups.find { it.id == id } }
    val title = if (g != null) "Редактировать группу" else "Новая группа"
    val numVal = (g?.numberLabel ?: "").escapeHtml()
    val noDateChecked = g != null && (g.dateFromMillis == null || g.dateToMillis == null)
    val fromVal = g?.dateFromMillis?.let { formatMillisToIsoDate(it) } ?: ""
    val toVal = g?.dateToMillis?.let { formatMillisToIsoDate(it) } ?: ""
    val datesDisabledAttr = if (noDateChecked) " disabled" else ""
    val noDateCheckedAttr = if (noDateChecked) " checked" else ""
    val datesMutedClass = if (noDateChecked) " sd-admin-group-dates-muted" else ""
    return """<div class="sd-modal-overlay" id="sd-admin-add-group-overlay">
        <div class="sd-modal sd-admin-group-modal" id="sd-admin-add-group-dialog">
            <h3 class="sd-modal-title">$title</h3>
            <div class="sd-admin-group-fields">
                <label class="sd-auth-label" for="sd-admin-group-number">№ группы</label>
                <input type="text" id="sd-admin-group-number" class="sd-auth-input" maxlength="64" placeholder="Число или текст" autocomplete="off" value="$numVal" />
                <label class="sd-auth-label">Срок обучения</label>
                <div class="sd-admin-group-no-date-row">
                    <label class="sd-switch sd-switch-no-date">
                        <input type="checkbox" id="sd-admin-group-no-date"$noDateCheckedAttr />
                        <span class="sd-switch-slider" aria-hidden="true"></span>
                        <span class="sd-switch-label">Без срока обучения</span>
                    </label>
                </div>
                <div class="sd-admin-group-dates$datesMutedClass">
                    <span class="sd-muted">с</span>
                    <input type="date" id="sd-admin-group-from" class="sd-auth-input sd-admin-group-date-inp" value="$fromVal"$datesDisabledAttr />
                    <span class="sd-muted">по</span>
                    <input type="date" id="sd-admin-group-to" class="sd-auth-input sd-admin-group-date-inp" value="$toVal"$datesDisabledAttr />
                </div>
            </div>
            <p class="sd-modal-actions">
                <button type="button" id="sd-admin-add-group-cancel" class="sd-btn sd-btn-secondary">Отменить</button>
                <button type="button" id="sd-admin-add-group-save" class="sd-btn sd-btn-primary">Сохранить</button>
            </p>
        </div>
    </div>"""
}

private fun renderAdminPickGroupModalHtml(cadetId: String): String {
    val groups = appState.cadetGroups
    val groupBtns = groups.joinToString("") { g ->
        val label = cadetGroupDisplayTextEscaped(g)
        """<button type="button" class="sd-btn sd-btn-secondary sd-admin-pick-group-item" data-admin-pick-group="${g.id.escapeHtml()}">$label</button>"""
    }
    val emptyHint = if (groups.isEmpty()) """<p class="sd-muted sd-admin-pick-group-empty">Нет групп. Создайте через «Добавить группу».</p>""" else ""
    return """<div class="sd-modal-overlay" id="sd-admin-cadet-group-picker-overlay" data-cadet-id="${cadetId.escapeHtml()}">
        <div class="sd-modal sd-admin-group-modal" id="sd-admin-cadet-group-picker-dialog">
            <h3 class="sd-modal-title">Выберите группу</h3>
            $emptyHint
            <div class="sd-admin-pick-group-list">
                <button type="button" class="sd-btn sd-btn-secondary sd-admin-pick-group-item" data-admin-pick-group="">Без группы</button>
                $groupBtns
            </div>
            <p class="sd-modal-actions"><button type="button" id="sd-admin-cadet-group-picker-cancel" class="sd-btn sd-btn-secondary">Отмена</button></p>
        </div>
    </div>"""
}

private fun renderAdminTrainingVehicleModalHtml(instructorId: String): String {
    val iid = instructorId.escapeHtml()
    val v1 = TRAINING_VEHICLE_LADA.escapeHtml()
    val v2 = TRAINING_VEHICLE_RENAULT.escapeHtml()
    return """<div class="sd-modal-overlay" id="sd-admin-training-vehicle-overlay" data-instructor-id="$iid">
        <div class="sd-modal sd-admin-group-modal" id="sd-admin-training-vehicle-dialog">
            <h3 class="sd-modal-title">Учебное ТС</h3>
            <p class="sd-muted sd-admin-training-vehicle-hint">Выберите автомобиль для инструктора</p>
            <div class="sd-admin-pick-group-list sd-admin-pick-training-vehicle-list">
                <button type="button" class="sd-btn sd-btn-secondary sd-admin-pick-training-vehicle" data-training-vehicle="$v1">$v1</button>
                <button type="button" class="sd-btn sd-btn-secondary sd-admin-pick-training-vehicle" data-training-vehicle="$v2">$v2</button>
            </div>
            <p class="sd-modal-actions"><button type="button" id="sd-admin-training-vehicle-cancel" class="sd-btn sd-btn-secondary">Отмена</button></p>
        </div>
    </div>"""
}

/**
 * Модалки групп вешаем на [root], а не в #sd-card — иначе при каждом updateState поля ввода пересоздаются и «моргают».
 */
private fun appendAdminCadetGroupModalsIfNeeded(root: org.w3c.dom.Element) {
    val adminHome = appState.user?.role == "admin" && appState.selectedTabIndex == 0
    if (!adminHome) {
        removeAdminCadetGroupModalsFromRoot()
        return
    }

    if (appState.adminAddGroupModalOpen) {
        val ex = document.getElementById("sd-admin-add-group-overlay")
        val sig = appState.adminEditingGroupId ?: ""
        val needNew = ex == null || ex.getAttribute("data-modal-sig") != sig
        if (needNew) {
            ex?.let { el -> el.parentNode?.removeChild(el) }
            val wrap = document.createElement("div")
            wrap.innerHTML = renderAdminAddGroupModalHtml()
            val el = wrap.firstElementChild ?: return
            el.setAttribute("data-modal-sig", sig)
            root.appendChild(el)
            wireAdminAddGroupModalControls()
        }
    } else {
        document.getElementById("sd-admin-add-group-overlay")?.let { el -> el.parentNode?.removeChild(el) }
    }

    val pid = appState.adminCadetGroupPickerCadetId
    if (pid != null) {
        val groupsSig = appState.cadetGroups.joinToString("|") { "${it.id}:${it.numberLabel}:${it.dateFromMillis}:${it.dateToMillis}:${it.createdAtMillis}" }
        val ex = document.getElementById("sd-admin-cadet-group-picker-overlay")
        val needNew = ex == null
            || ex.getAttribute("data-cadet-id") != pid
            || ex.getAttribute("data-groups-sig") != groupsSig
        if (needNew) {
            ex?.let { el -> el.parentNode?.removeChild(el) }
            val wrap = document.createElement("div")
            wrap.innerHTML = renderAdminPickGroupModalHtml(pid)
            val el = wrap.firstElementChild ?: return
            el.setAttribute("data-groups-sig", groupsSig)
            root.appendChild(el)
        }
    } else {
        document.getElementById("sd-admin-cadet-group-picker-overlay")?.let { el -> el.parentNode?.removeChild(el) }
    }

    val instVid = appState.adminTrainingVehiclePickerInstructorId
    if (instVid != null) {
        val ex = document.getElementById("sd-admin-training-vehicle-overlay")
        val needNew = ex == null || ex.getAttribute("data-instructor-id") != instVid
        if (needNew) {
            ex?.let { el -> el.parentNode?.removeChild(el) }
            val wrap = document.createElement("div")
            wrap.innerHTML = renderAdminTrainingVehicleModalHtml(instVid)
            val el = wrap.firstElementChild ?: return
            root.appendChild(el)
        }
    } else {
        document.getElementById("sd-admin-training-vehicle-overlay")?.let { el -> el.parentNode?.removeChild(el) }
    }
}

private fun refreshAdminScheduleData() {
    fun runWithUsers(users: List<User>) {
        val instructors = users.filter { it.role == "instructor" }
        val schedIdx = getTabsForUserRole("admin").indexOf("Расписание")
        if (instructors.isEmpty()) {
            updateState {
                adminScheduleSessionsByInstructorId = emptyMap()
                adminScheduleLoading = false
                if (selectedTabIndex == schedIdx && user?.role == "admin") {
                    adminScheduleSeenSignature = ""
                }
            }
            return
        }
        updateState { adminScheduleLoading = true; networkError = null }
        val tid = window.setTimeout({ updateState { adminScheduleLoading = false } }, 12000)
        getSessionsForInstructorsMap(instructors.map { it.id }) { map ->
            window.clearTimeout(tid)
            val onSched = appState.selectedTabIndex == schedIdx && appState.user?.role == "admin"
            val sig = computeAdminScheduleSignature(map)
            updateState {
                adminScheduleSessionsByInstructorId = map
                adminScheduleLoading = false
                if (onSched) adminScheduleSeenSignature = sig
            }
        }
    }
    val users = appState.adminHomeUsers.ifEmpty { appState.balanceAdminUsers }
    if (users.isEmpty()) {
        updateState { adminScheduleLoading = true; networkError = null }
        getUsers { list ->
            updateState { adminHomeUsers = list; balanceAdminUsers = list }
            runWithUsers(list)
        }
        return
    }
    runWithUsers(users)
}

private fun renderAdminScheduleTabContent(): String {
    val loading = appState.adminScheduleLoading
    val users = appState.adminHomeUsers.ifEmpty { appState.balanceAdminUsers }
    val instructors = users.filter { it.role == "instructor" }.sortedBy { it.fullName.lowercase() }
    val map = appState.adminScheduleSessionsByInstructorId
    val userById = users.associateBy { it.id }

    fun cadetShortName(cadetId: String): String {
        val u = userById[cadetId]
        return u?.fullName?.takeIf { it.isNotBlank() }?.let { formatShortName(it) } ?: "—"
    }

    fun sessionStatusLabel(st: String): String = when (st) {
        "completed" -> "Завершено"
        "cancelledByInstructor" -> "Отменено (инструктор)"
        "cancelledByCadet" -> "Отменено (курсант)"
        else -> st
    }

    fun upcomingStatusLabel(st: String): String = when (st) {
        "scheduled" -> "Запланировано"
        "inProgress" -> "В процессе"
        else -> st
    }

    /** Заголовок дня и таблица №, время, курсант, статус — одинаковый формат для текущих записей и истории. */
    fun scheduleDayBlocksHtml(
        sessionsByDate: List<Pair<String, List<DrivingSession>>>,
        statusFor: (DrivingSession) -> String,
    ): String {
        if (sessionsByDate.isEmpty()) return ""
        return sessionsByDate.joinToString("") { (dateStr, daySessions) ->
            val sorted = daySessions.sortedBy { it.startTimeMillis ?: 0L }
            val firstMs = sorted.firstOrNull()?.startTimeMillis
            val drivingCount = sorted.size
            val dayTitle = if (firstMs != null && firstMs > 0) {
                val w = weekdayNameLongRu(firstMs)
                """${dateStr.escapeHtml()}, ${w.escapeHtml()} (${drivingCount})"""
            } else {
                """${dateStr.escapeHtml()} (${drivingCount})"""
            }
            val rows = sorted.mapIndexed { idx, s ->
                val ms = s.startTimeMillis ?: 0L
                val num = idx + 1
                val timeStr = formatTimeOnly(ms)
                val cad = cadetShortName(s.cadetId)
                val st = statusFor(s)
                """<tr><td>$num</td><td>${timeStr.escapeHtml()}</td><td>${cad.escapeHtml()}</td><td>${st.escapeHtml()}</td></tr>"""
            }.joinToString("")
            """<div class="sd-admin-schedule-day-block">
                <h5 class="sd-admin-schedule-day-head">$dayTitle</h5>
                <div class="sd-admin-schedule-table-wrap">
                    <table class="sd-admin-schedule-table">
                        <thead><tr><th>№</th><th>Время</th><th>Фамилия И.О. курсанта</th><th>Статус</th></tr></thead>
                        <tbody>$rows</tbody>
                    </table>
                </div>
            </div>"""
        }
    }

    if (loading && map.isEmpty()) {
        return """<h2>Расписание</h2><p class="sd-loading-text">Загрузка расписания…</p>"""
    }
    if (instructors.isEmpty()) {
        return """<h2>Расписание</h2><p class="sd-muted">Нет инструкторов. Откройте вкладку «Главная» и загрузите пользователей.</p>"""
    }

    val blocks = instructors.joinToString("") { inst ->
        val sessions = map[inst.id] ?: emptyList()
        val upcoming = sessions.filter { it.status == "scheduled" || it.status == "inProgress" }
            .sortedBy { it.startTimeMillis ?: 0L }
        val history = sessions.filter {
            it.status == "completed" || it.status == "cancelledByInstructor" || it.status == "cancelledByCadet"
        }.sortedByDescending { it.startTimeMillis ?: 0L }

        val byDateUp = upcoming.groupBy { formatDateOnly(it.startTimeMillis) }
        val upcomingDays: List<Pair<String, List<DrivingSession>>> = byDateUp.entries
            .sortedBy { e -> e.value.minOfOrNull { s -> s.startTimeMillis ?: 0L } ?: 0L }
            .map { it.key to it.value }

        val upcomingHtml = if (upcoming.isEmpty()) {
            """<p class="sd-muted">Нет запланированных занятий.</p>"""
        } else {
            scheduleDayBlocksHtml(upcomingDays) { s -> upcomingStatusLabel(s.status) }
        }

        val byMonth = history.groupBy { s ->
            val ms = s.startTimeMillis ?: 0L
            if (ms <= 0) "0000-00" else monthSortKeyFromMillis(ms)
        }
        val monthKeys = byMonth.keys.filter { it != "0000-00" }.sortedDescending() +
            listOf("0000-00").filter { byMonth.containsKey("0000-00") }

        val historyBlock = if (history.isEmpty()) {
            """<p class="sd-muted">История пуста.</p>"""
        } else {
            monthKeys.joinToString("") { mk ->
                val monthSessions = byMonth[mk] ?: emptyList()
                val labelMs = monthSessions.maxOfOrNull { it.startTimeMillis ?: 0L } ?: 0L
                val monthTitle = monthYearTitleRu(labelMs).escapeHtml()
                val byDateHist = monthSessions.groupBy { formatDateOnly(it.startTimeMillis) }
                val historyDays: List<Pair<String, List<DrivingSession>>> = byDateHist.entries
                    .sortedByDescending { e -> e.value.maxOfOrNull { s -> s.startTimeMillis ?: 0L } ?: 0L }
                    .map { it.key to it.value }
                """<div class="sd-admin-schedule-month-block"><h4 class="sd-admin-schedule-month-head">$monthTitle</h4>${scheduleDayBlocksHtml(historyDays) { s -> sessionStatusLabel(s.status) }}</div>"""
            }
        }

        val instIdEsc = inst.id.escapeHtml()
        val historySummaryRow = """<span class="sd-admin-schedule-summary-heading">
                <span class="sd-admin-schedule-summary-title">История вождений (${history.size})</span>
                <button type="button" class="sd-btn sd-btn-small sd-btn-danger sd-admin-history-clear-btn" data-admin-history-clear="$instIdEsc" title="Удалить все записи истории вождений">Очистить</button>
            </span>"""

        """<details class="sd-admin-schedule-block sd-admin-schedule-instructor-details" open>
            <summary class="sd-admin-schedule-instructor-summary">Инструктор: ${formatShortName(inst.fullName).escapeHtml()}</summary>
            <div class="sd-admin-schedule-instructor-body">
            <h4 class="sd-admin-schedule-h4">Текущие записи</h4>
            $upcomingHtml
            <details class="sd-admin-schedule-details">
                <summary class="sd-admin-schedule-summary sd-admin-schedule-history-summary">$historySummaryRow</summary>
                $historyBlock
            </details>
            </div>
        </details>"""
    }

    val refreshBtn = """<p class="sd-admin-schedule-actions"><button type="button" id="sd-admin-schedule-refresh" class="sd-btn sd-btn-secondary sd-btn-small">Обновить</button></p>"""
    return """<h2>Расписание</h2>
        $refreshBtn
        <div class="sd-admin-schedule-list">$blocks</div>"""
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
        val instAvatarHtml = avatarBlockHtml("sd-ucard-avatar sd-ucard-avatar-blue", u, appState.user?.id)
        val phoneHrefInst = if (u.phone.isNotBlank()) "tel:${u.phone.escapeHtml()}" else "#"
        val phoneDisabled = if (u.phone.isBlank()) " sd-ucard-icon-btn-disabled" else ""
        """<div class="sd-ucard sd-ucard-instructor">
            <div class="sd-ucard-accent-bar"></div>
            <div class="sd-ucard-top">
                $instAvatarHtml
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
                <div class="sd-ucard-row sd-ucard-row-stretch sd-admin-instructor-vehicle-row"><span class="sd-ucard-row-icon">$iconCarSvg</span><span class="sd-admin-vehicle-label">Учебное ТС:</span> <strong class="sd-admin-vehicle-value">${trainingVehicleLabelOrDash(u).escapeHtml()}</strong><button type="button" class="sd-ucard-tag-btn sd-admin-training-vehicle-btn" data-admin-training-vehicle-pick="${u.id.escapeHtml()}" title="Выбрать учебное ТС" aria-label="Выбрать учебное ТС">$iconSelectSvg</button></div>
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
    fun adminCadetCardHtml(u: User, showGroupLabelOnCard: Boolean = true): String {
        val driveCount = appState.adminCadetCompletedDriveCounts[u.id] ?: 0
        val group = u.cadetGroupId?.let { gid -> appState.cadetGroups.find { it.id == gid } }
        val groupText = cadetGroupDisplayTextEscaped(group)
        val instId = u.assignedInstructorId
        val instName = instId?.let { id -> instructors.find { it.id == id }?.let { formatShortName(it.fullName) } ?: "—" } ?: "—"
        val displayInstText = if (instId != null) instName.escapeHtml() else "Не назначен"
        val phoneHrefCadet = if (u.phone.isNotBlank()) "tel:${u.phone.escapeHtml()}" else "#"
        val phoneDisabledCadet = if (u.phone.isBlank()) " sd-ucard-icon-btn-disabled" else ""
        val cadetAvatarHtml = avatarBlockHtml("sd-ucard-avatar sd-ucard-avatar-teal", u, appState.user?.id)
        val unlinkOrAssign = if (instId != null)
            """<button type="button" class="sd-ucard-tag-btn sd-ucard-tag-btn-warn sd-admin-unlink-right" data-admin-unlink-instructor="${instId.escapeHtml()}" data-admin-unlink-cadet="${u.id.escapeHtml()}" title="Отвязать инструктора">$iconUnlinkSvg</button>"""
        else
            """<button type="button" class="sd-ucard-tag-btn sd-admin-assign-cadet-btn" data-admin-assign-cadet="${u.id.escapeHtml()}" title="Назначить инструктора">$iconUserPlusSvg</button>"""
        return """<div class="sd-ucard sd-ucard-cadet">
            <div class="sd-ucard-accent-bar"></div>
            <div class="sd-ucard-top">
                $cadetAvatarHtml
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
                <div class="sd-ucard-row"><span class="sd-ucard-row-icon">$iconCarSvg</span>Кол-во вождений: <strong>$driveCount</strong></div>
                ${if (showGroupLabelOnCard) """<div class="sd-ucard-row sd-ucard-row-stretch"><span class="sd-ucard-row-icon">$iconGroupSvg</span>Группа: <strong>$groupText</strong><button type="button" class="sd-ucard-tag-btn sd-admin-cadet-group-btn" data-admin-cadet-group-pick="${u.id.escapeHtml()}" title="Выбрать группу">$iconSelectSvg</button></div>""" else """<div class="sd-ucard-row sd-ucard-row-stretch"><span class="sd-ucard-row-icon">$iconGroupSvg</span><span class="sd-admin-cadet-group-inline-label">Сменить группу</span><button type="button" class="sd-ucard-tag-btn sd-admin-cadet-group-btn" data-admin-cadet-group-pick="${u.id.escapeHtml()}" title="Выбрать группу">$iconSelectSvg</button></div>"""}
            </div>
            <div class="sd-ucard-footer">
                <button type="button" class="sd-ucard-foot-btn ${if (u.isActive) "sd-ucard-foot-btn-deact" else "sd-ucard-foot-btn-act"}" data-admin-activate="${u.id.escapeHtml()}" data-admin-active="${u.isActive}" title="${if (u.isActive) "Деактивировать" else "Активировать"}">$iconPowerSvg</button>
                <button type="button" class="sd-ucard-foot-btn sd-ucard-foot-btn-danger" data-admin-delete="${u.id.escapeHtml()}" title="Удалить">$iconTrashSvg</button>
            </div>
        </div>"""
    }
    val groupIds = appState.cadetGroups.map { it.id }.toSet()
    val cadetGroupSectionsHtml = appState.cadetGroups.joinToString("") { g ->
        val inGroup = cadets.filter { it.cadetGroupId == g.id }.sortedBy { it.fullName }
        val title = cadetGroupDisplayTextEscaped(g)
        val cardsInner = if (inGroup.isEmpty()) {
            """<p class="sd-muted sd-admin-cadets-empty">В этой группе пока нет курсантов</p>"""
        } else {
            inGroup.joinToString("") { adminCadetCardHtml(it, showGroupLabelOnCard = false) }
        }
        """<div class="sd-admin-cadets-subsection" data-admin-cadet-group-section="${g.id.escapeHtml()}">
            <div class="sd-admin-cadets-subsection-head">
                <h4 class="sd-admin-cadets-subtitle">$title <span class="sd-admin-cadets-subcount">(${inGroup.size})</span></h4>
                <div class="sd-admin-cadets-subsection-actions">
                    <button type="button" class="sd-chat-create-group-btn sd-accent-pill-light sd-admin-group-edit" data-admin-group-edit="${g.id.escapeHtml()}">Редактировать</button>
                    <button type="button" class="sd-chat-create-group-btn sd-accent-pill-light sd-chat-create-group-btn--danger sd-admin-group-delete" data-admin-group-delete="${g.id.escapeHtml()}">Удалить</button>
                </div>
            </div>
            <div class="sd-admin-cards sd-admin-cards-cadet-group">$cardsInner</div>
        </div>"""
    }
    val unassignedCadets = cadets.filter { u ->
        val gid = u.cadetGroupId
        gid.isNullOrBlank() || !groupIds.contains(gid)
    }.sortedBy { it.fullName }
    val unassignedCardsInner = if (unassignedCadets.isEmpty()) {
        """<p class="sd-muted sd-admin-cadets-empty">Нет курсантов без группы</p>"""
    } else {
        unassignedCadets.joinToString("") { adminCadetCardHtml(it, showGroupLabelOnCard = true) }
    }
    val cadetsGroupedHtml = """<div class="sd-admin-cadets-grouped">$cadetGroupSectionsHtml<div class="sd-admin-cadets-subsection sd-admin-cadets-subsection-unassigned" data-admin-cadet-group-section="">
        <div class="sd-admin-cadets-subsection-head sd-admin-cadets-subsection-head-single">
            <h4 class="sd-admin-cadets-subtitle">Не в группе <span class="sd-admin-cadets-subcount">(${unassignedCadets.size})</span></h4>
        </div>
        <div class="sd-admin-cards sd-admin-cards-cadet-group">$unassignedCardsInner</div>
    </div></div>"""
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
    val cadetsToolbarHint = if (appState.cadetGroups.isEmpty()) {
        """<p class="sd-muted sd-admin-cadets-toolbar-hint">Пока нет групп. Создайте группу — под её названием появятся карточки курсантов.</p>"""
    } else ""
    return """<h2>Главная</h2>$topSlot$newbiesBlock<details class="sd-block sd-details-block" data-admin-section="instructors"$instOpen><summary class="sd-block-title">Инструкторы (${instructors.size})</summary><div class="sd-admin-cards">$instCards</div></details>$assignBlock<details class="sd-block sd-details-block" data-admin-section="cadets"$cadetOpen><summary class="sd-block-title">Курсанты (${cadets.size})</summary><div class="sd-admin-cadets-toolbar"><button type="button" id="sd-admin-add-group-btn" class="sd-chat-create-group-btn sd-accent-pill-light sd-admin-add-group-toolbar-btn" title="Добавить группу курсантов" aria-label="Добавить группу">$iconCreateGroupSvg Добавить группу</button>$cadetsToolbarHint</div>$cadetsGroupedHtml</details>$cadetsModalHtml"""
}

/** День недели: Пн=0 … Вс=6 (локальное время). */
private fun weekdayIndexMon0Local(ms: Long): Int {
    val d = js("new Date(ms)").unsafeCast<dynamic>()
    val day = (d.getDay() as Number).toInt()
    return if (day == 0) 6 else day - 1
}

private const val INSTRUCTOR_PROFILE_MAX_WEEK_OFFSET = 52

/** Понедельник 00:00 локального времени для недели, сдвинутой на [weekOffsetBack] назад от текущей. */
private fun weekStartMondayLocalMs(refMs: Long, weekOffsetBack: Int): Long {
    val d = js("new Date(refMs)").unsafeCast<dynamic>()
    d.setHours(0, 0, 0, 0)
    val day = (d.getDay() as Number).toInt()
    val daysFromMonday = if (day == 0) 6 else day - 1
    val oneDay = 24L * 60 * 60 * 1000
    val mondayStart = (d.getTime() as Number).toLong() - daysFromMonday * oneDay
    return mondayStart - weekOffsetBack * 7L * oneDay
}

/** Границы выбранной недели [startMs, endMs) в локальном времени. */
private fun weekRangeMsForOffset(weekOffsetBack: Int): Pair<Long, Long> {
    val nowMs = js("Date.now()").unsafeCast<Double>().toLong()
    val startMs = weekStartMondayLocalMs(nowMs, weekOffsetBack)
    val endMs = startMs + 7L * 24 * 60 * 60 * 1000
    return startMs to endMs
}

private fun formatWeekRangeLabelRu(startMs: Long): String {
    val d0 = js("new Date(startMs)").unsafeCast<dynamic>()
    val d6 = js("new Date(startMs + 6 * 24 * 60 * 60 * 1000)").unsafeCast<dynamic>()
    val opts = js("({ day: 'numeric', month: 'short' })")
    val s0 = js("d0.toLocaleDateString('ru-RU', opts)").unsafeCast<String>()
    val s6 = js("d6.toLocaleDateString('ru-RU', opts)").unsafeCast<String>()
    return "$s0 — $s6"
}

private fun formatFixed1(x: Double): String =
    js("(function(x){ return Number(x).toFixed(1); })")(x).unsafeCast<String>()

private fun resetInstructorProfileCalculatorCache() {
    instructorProfileCalcTokensOverride = null
    instructorProfileCalcRubInput = ""
    instructorProfileEarnedCalcMountEl?.let { el ->
        el.parentNode?.removeChild(el)
    }
    instructorProfileEarnedCalcMountEl = null
}

/** Снять узел калькулятора с экрана перед заменой innerHTML #sd-card (сохраняем DOM полей ввода). */
private fun detachInstructorProfileEarnedCalcMount() {
    val el = document.getElementById("sd-instr-earned-calc-mount") ?: instructorProfileEarnedCalcMountEl
    if (el != null) {
        instructorProfileEarnedCalcMountEl = el
        el.parentNode?.removeChild(el)
    }
}

private fun instructorProfileEarnedCalcHtml(defaultTokenBalance: Int): String {
    val tokensForCalc = instructorProfileCalcTokensDisplayed(defaultTokenBalance)
    val tokensAttr = tokensForCalc.escapeHtml()
    val rubAttr = instructorProfileCalcRubInput.escapeHtml()
    val rubTotalLabel = instructorProfileCalcRubTotalLabel(tokensForCalc, instructorProfileCalcRubInput).escapeHtml()
    return """<div id="sd-instr-earned-calc-mount" class="sd-instr-earned-calc">
            <div class="sd-instr-earned-calc-grid">
                <div class="sd-instr-earned-field">
                    <label class="sd-instr-earned-field-label" for="sd-instr-earned-tokens-input">Талоны</label>
                    <input type="text" inputmode="numeric" id="sd-instr-earned-tokens-input" class="sd-auth-input sd-instr-earned-input" value="$tokensAttr" maxlength="8" autocomplete="off" />
                </div>
                <div class="sd-instr-earned-field">
                    <label class="sd-instr-earned-field-label" for="sd-instr-rub-per-token">Руб.</label>
                    <input type="text" inputmode="decimal" id="sd-instr-rub-per-token" class="sd-auth-input sd-instr-earned-input" value="$rubAttr" maxlength="12" placeholder="за 1" autocomplete="off" />
                </div>
                <div class="sd-instr-earned-total-col">
                    <span class="sd-instr-earned-total-label">Итого</span>
                    <span class="sd-instr-earned-rub-out" id="sd-instr-earned-rub-total">$rubTotalLabel</span>
                </div>
            </div>
        </div>"""
}

/** Вернуть калькулятор в placeholder после рендера профиля. */
private fun reattachInstructorProfileEarnedCalcMount(root: Element?) {
    val placeholder = document.getElementById("sd-instr-earned-calc-placeholder")
    if (placeholder == null) {
        instructorProfileEarnedCalcMountEl?.let { el ->
            document.body?.appendChild(el)
            el.classList.add("sd-hidden")
        }
        return
    }
    val calcDefaultTokens = appState.user?.balance ?: 0
    var mount = instructorProfileEarnedCalcMountEl
    if (mount == null) {
        val wrap = document.createElement("div")
        wrap.innerHTML = instructorProfileEarnedCalcHtml(calcDefaultTokens)
        mount = wrap.firstElementChild as? Element
        instructorProfileEarnedCalcMountEl = mount
    } else {
        mount.classList.remove("sd-hidden")
    }
    if (mount != null && mount.parentNode != placeholder) {
        placeholder.appendChild(mount)
    }
}

private fun isInstructorEarnedCalcInputFocused(): Boolean {
    val a = document.activeElement ?: return false
    val id = a.unsafeCast<dynamic>().id as? String ?: return false
    return id == "sd-instr-earned-tokens-input" || id == "sd-instr-rub-per-token"
}

/** Один глобальный слушатель на document — не теряется при смене #root / #sd-card. */
private fun ensureInstructorProfileEarnedCalcDelegation() {
    val w = window.asDynamic()
    if (w.__sdEarnedCalcDelegation == true) return
    w.__sdEarnedCalcDelegation = true
    document.addEventListener("input", { ev ->
        val t = ev.target as? HTMLInputElement ?: return@addEventListener
        when (t.id) {
            "sd-instr-earned-tokens-input" -> {
                val digits = t.value.filter { it.isDigit() }.take(8)
                if (t.value != digits) t.value = digits
                instructorProfileCalcTokensOverride = digits
                scheduleInstructorEarnedRubDisplayUpdate()
            }
            "sd-instr-rub-per-token" -> {
                var s = t.value.replace(",", ".").filter { it.isDigit() || it == '.' }
                val firstDot = s.indexOf('.')
                if (firstDot >= 0) {
                    s = s.substring(0, firstDot + 1) + s.substring(firstDot + 1).filter { it.isDigit() }.take(2)
                }
                if (t.value != s) t.value = s
                instructorProfileCalcRubInput = s
                scheduleInstructorEarnedRubDisplayUpdate()
            }
        }
    }, true)
    // После ухода с поля — один раз перерисовать карточку (актуальные графики/баланс без мигания при вводе).
    document.addEventListener("focusout", { ev ->
        val t = ev.target as? org.w3c.dom.HTMLElement ?: return@addEventListener
        val id = t.id
        if (id != "sd-instr-earned-tokens-input" && id != "sd-instr-rub-per-token") return@addEventListener
        scheduleAppRender?.invoke()
    }, true)
    // Как у кнопок «Отменить» в <details>: не даём всплывать mousedown/pointerdown, чтобы не ловили лишние обработчики.
    fun stopPointerIfEarnedCalcMount(ev: dynamic) {
        val raw = ev?.target as? org.w3c.dom.Node ?: return
        val inside = js("(function(n){ return n && n.closest && n.closest('#sd-instr-earned-calc-mount'); })")(raw) != null
        if (inside) {
            (ev as? org.w3c.dom.events.Event)?.stopPropagation()
        }
    }
    document.addEventListener("mousedown", { ev: dynamic -> stopPointerIfEarnedCalcMount(ev) }, false)
    document.addEventListener("pointerdown", { ev: dynamic -> stopPointerIfEarnedCalcMount(ev) }, false)
}

private fun scheduleInstructorEarnedRubDisplayUpdate() {
    if (instructorEarnedRubRafId != 0) {
        window.cancelAnimationFrame(instructorEarnedRubRafId)
    }
    instructorEarnedRubRafId = window.requestAnimationFrame {
        instructorEarnedRubRafId = 0
        updateInstructorEarnedRubDisplay()
    }
}

private fun instructorProfileCalcTokensDisplayed(defaultTokenBalance: Int): String =
    when (instructorProfileCalcTokensOverride) {
        null -> defaultTokenBalance.coerceAtLeast(0).toString()
        else -> instructorProfileCalcTokensOverride!!
    }

private fun instructorProfileCalcRubTotalLabel(tokensStr: String, rubStr: String): String {
    val tok = tokensStr.filter { it.isDigit() }.toIntOrNull() ?: 0
    val rub = rubStr.replace(",", ".").toDoubleOrNull() ?: 0.0
    val total = tok * rub
    val n = js("(function(x){ return Number(x).toFixed(2); })")(total).unsafeCast<String>()
    return "$n руб."
}

private fun updateInstructorEarnedRubDisplay() {
    val tokStr = (document.getElementById("sd-instr-earned-tokens-input") as? HTMLInputElement)?.value ?: ""
    val tokens = tokStr.filter { it.isDigit() }.toIntOrNull() ?: 0
    val rubStr = (document.getElementById("sd-instr-rub-per-token") as? HTMLInputElement)?.value ?: ""
    val rub = rubStr.replace(",", ".").toDoubleOrNull() ?: 0.0
    val total = tokens * rub
    val n = js("(function(x){ return Number(x).toFixed(2); })")(total).unsafeCast<String>()
    document.getElementById("sd-instr-earned-rub-total")?.textContent = "$n руб."
}

/** Фон conic-gradient для круговой диаграммы оценок (1–5). */
private fun buildInstructorRatingPieBackground(counts: List<Int>): String {
    val total = counts.sum()
    if (total <= 0) return "conic-gradient(#bdbdbd 0deg 360deg)"
    val colors = listOf("#E53935", "#FF9800", "#FFEB3B", "#8BC34A", "#4CAF50")
    var angle = 0.0
    val parts = mutableListOf<String>()
    for (i in 0..4) {
        val c = counts[i]
        if (c > 0) {
            val sweep = 360.0 * c / total
            val a1 = angle + sweep
            parts.add("${colors[i]} ${angle}deg ${a1}deg")
            angle = a1
        }
    }
    return "conic-gradient(${parts.joinToString(", ")})"
}

/** Карточка профиля инструктора на главной / в экране «Профиль». */
private fun instructorHomeProfileCardHtml(user: User, showProfileShortcut: Boolean): String {
    val svgEmail = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>"""
    val svgPhoneP = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/></svg>"""
    val svgTicketP = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M22 10V6c0-1.1-.9-2-2-2H4c-1.1 0-1.99.9-1.99 2v4c1.1 0 1.99.9 1.99 2s-.89 2-2 2v4c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2v-4c-1.1 0-2-.9-2-2s.9-2 2-2zm-2-1.46c-1.19.69-2 1.99-2 3.46s.81 2.77 2 3.46V18H4v-2.54c1.19-.69 2-1.99 2-3.46 0-1.48-.8-2.77-1.99-3.46L4 6h16v2.54z"/></svg>"""
    val svgCarP = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/></svg>"""
    val instrProfileAvatarHtml = avatarBlockHtml("sd-profile-avatar", user, user.id)
    val iconPerson = """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20" aria-hidden="true"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>"""
    val profileBtn = if (showProfileShortcut) {
        """<button type="button" class="sd-instructor-profile-card-btn" id="sd-instructor-profile-open" title="Профиль" aria-label="Профиль">$iconPerson</button>"""
    } else ""
    val wrapOpen = if (showProfileShortcut) """<div class="sd-instructor-profile-card-wrap">""" else ""
    val wrapClose = if (showProfileShortcut) """</div>""" else ""
    val card = """
        <div class="sd-profile-card">
            <div class="sd-profile-accent-bar"></div>
            <div class="sd-profile-hero">
                $instrProfileAvatarHtml
                <div class="sd-profile-hero-info">
                    <p class="sd-profile-fullname">${(user.fullName.ifBlank { "—" }).escapeHtml()}</p>
                    <span class="sd-profile-role-badge">Инструктор</span>
                </div>
            </div>
            <div class="sd-profile-info-rows">
                <div class="sd-profile-info-row"><span class="sd-profile-info-icon">$svgEmail</span><span class="sd-profile-info-label">Email</span><span class="sd-profile-info-value">${(user.email.ifBlank { "—" }).escapeHtml()}</span></div>
                <div class="sd-profile-info-row"><span class="sd-profile-info-icon">$svgCarP</span><span class="sd-profile-info-label">Учебное ТС</span><span class="sd-profile-info-value">${trainingVehicleLabelOrDash(user).escapeHtml()}</span></div>
                <div class="sd-profile-info-row"><span class="sd-profile-info-icon">$svgPhoneP</span><span class="sd-profile-info-label">Телефон</span><span class="sd-profile-info-value">${(user.phone.ifBlank { "—" }).escapeHtml()}</span></div>
                <div class="sd-profile-info-row sd-profile-info-row-balance"><span class="sd-profile-info-icon">$svgTicketP</span><span class="sd-profile-info-label">Талоны</span><span class="sd-balance-badge">${user.balance}</span></div>
            </div>
        </div>"""
    return wrapOpen + profileBtn + card + wrapClose
}

/** Экран «Профиль» инструктора: статистика как в Android (рейтинг, заработок, графики). */
private fun renderInstructorProfileStatsContent(user: User, version: String): String {
    val sessions = appState.recordingSessions
    val hist = appState.historyBalance
    val nowMs = js("Date.now()").unsafeCast<Double>().toLong()
    val monthAgo = nowMs - 30L * 24 * 60 * 60 * 1000
    val totalEarned = hist.filter { it.type == "credit" }.sumOf { it.amount }
    val earnedLast30 = hist.filter { it.type == "credit" && (it.timestampMillis ?: 0L) >= monthAgo }.sumOf { it.amount }
    val earned30Html = if (earnedLast30 != 0) {
        val cls = if (earnedLast30 > 0) "sd-instr-earned-delta-pos" else "sd-instr-earned-delta-neg"
        val txt = if (earnedLast30 > 0) "+$earnedLast30 за 30 дн." else "$earnedLast30 за 30 дн."
        """<span class="$cls">${txt.escapeHtml()}</span>"""
    } else ""
    val completedCount = sessions.count { it.status == "completed" }
    val rated = sessions.filter { it.status == "completed" && it.cadetRating in 1..5 }
    val counts = (1..5).map { r -> rated.count { it.cadetRating == r } }
    val totalRated = counts.sum()
    val averageStr = if (totalRated > 0) {
        formatFixed1(rated.sumOf { it.cadetRating }.toDouble() / totalRated)
    } else "—"
    val pieBg = buildInstructorRatingPieBackground(counts)
    val ratingLegend = (5 downTo 1).joinToString("") { r ->
        val i = r - 1
        val cnt = counts[i]
        val pct = if (totalRated > 0) ((100f * cnt / totalRated).toInt()) else 0
        val colors = listOf("#E53935", "#FF9800", "#FFEB3B", "#8BC34A", "#4CAF50")
        """<div class="sd-instr-rating-legend-row"><span class="sd-instr-rating-sq" style="background:${colors[i]}"></span><span>$r ★: $pct%</span></div>"""
    }
    val starSvgLarge = """<svg class="sd-instr-rating-star-bg" viewBox="0 0 24 24" width="110" height="110" aria-hidden="true" xmlns="http://www.w3.org/2000/svg">
<defs>
  <linearGradient id="sdInstrStarBody" x1="15%" y1="5%" x2="85%" y2="95%">
    <stop offset="0%" stop-color="#FFF9C4"/>
    <stop offset="35%" stop-color="#FFEB3B"/>
    <stop offset="65%" stop-color="#F9A825"/>
    <stop offset="100%" stop-color="#E65100"/>
  </linearGradient>
  <linearGradient id="sdInstrStarShine" x1="10%" y1="0%" x2="55%" y2="55%">
    <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.95"/>
    <stop offset="40%" stop-color="#FFFFFF" stop-opacity="0.35"/>
    <stop offset="100%" stop-color="#FFFFFF" stop-opacity="0"/>
  </linearGradient>
  <radialGradient id="sdInstrStarGloss" cx="35%" cy="35%" r="55%">
    <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.75"/>
    <stop offset="45%" stop-color="#FFFDE7" stop-opacity="0.25"/>
    <stop offset="100%" stop-color="#F9A825" stop-opacity="0"/>
  </radialGradient>
  <filter id="sdInstrStarDepth" x="-25%" y="-25%" width="150%" height="150%">
    <feGaussianBlur in="SourceAlpha" stdDeviation="0.6" result="blur"/>
    <feOffset dx="0" dy="1.2" in="blur" result="off"/>
    <feComponentTransfer in="off" result="shadow"><feFuncA type="linear" slope="0.4"/></feComponentTransfer>
    <feMerge><feMergeNode in="shadow"/><feMergeNode in="SourceGraphic"/></feMerge>
  </filter>
</defs>
<path filter="url(#sdInstrStarDepth)" fill="url(#sdInstrStarBody)" stroke="#B8860B" stroke-width="0.35" d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
<path fill="url(#sdInstrStarShine)" d="M12 3.5l2.05 5.1L19.2 9.1l-3.7 3.6 0.9 4.2L12 15.2l-4.4 2.3-0.9-4.2-3.7-3.6 5.4-0.8L12 3.5z"/>
<ellipse cx="10.5" cy="9.5" rx="4.2" ry="3" fill="url(#sdInstrStarGloss)" transform="rotate(-18 10.5 9.5)"/>
</svg>"""
    val pieInner = if (totalRated > 0) {
        """<div class="sd-instr-rating-pie" style="background:$pieBg"></div><div class="sd-instr-rating-pie-center">$starSvgLarge<span class="sd-instr-rating-avg-overlay">$averageStr</span></div>"""
    } else """<p class="sd-muted sd-instr-no-ratings">Нет оценок</p>"""
    val weekOffsetBack = appState.instructorProfileWeekOffset.coerceIn(0, INSTRUCTOR_PROFILE_MAX_WEEK_OFFSET)
    val (weekStartMs, weekEndMs) = weekRangeMsForOffset(weekOffsetBack)
    val weekRangeLabel = formatWeekRangeLabelRu(weekStartMs)
    val completedAll = sessions.filter { it.status == "completed" }
    val completedForWeek = completedAll.filter { s ->
        val t = s.completedAtMillis ?: s.startTimeMillis ?: return@filter false
        t >= weekStartMs && t < weekEndMs
    }
    val dayCountsByWeekday = IntArray(7)
    completedForWeek.forEach { s ->
        val t = s.completedAtMillis ?: s.startTimeMillis ?: return@forEach
        dayCountsByWeekday[weekdayIndexMon0Local(t)]++
    }
    val dowShort = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    /** Макс. занятий в день по правилам — 8; шкала по вертикали 0…8 (подписи 8…1). */
    val weekFreqMaxY = 8
    val prevWeekDisabled = weekOffsetBack >= INSTRUCTOR_PROFILE_MAX_WEEK_OFFSET
    val nextWeekDisabled = weekOffsetBack <= 0
    val prevWeekAttr = if (prevWeekDisabled) """ disabled""" else ""
    val nextWeekAttr = if (nextWeekDisabled) """ disabled""" else ""
    val weekNavHtml = if (completedAll.isEmpty()) {
        ""
    } else {
        """
        <div class="sd-instr-week-nav" role="group" aria-label="Неделя для графика">
            <button type="button" class="sd-instr-week-btn" id="sd-instr-week-prev" aria-label="Предыдущая неделя"$prevWeekAttr>
                <svg class="sd-instr-week-chevron" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M15 18l-6-6 6-6"/></svg>
            </button>
            <span class="sd-instr-week-label">${weekRangeLabel.escapeHtml()}</span>
            <button type="button" class="sd-instr-week-btn" id="sd-instr-week-next" aria-label="Следующая неделя"$nextWeekAttr>
                <svg class="sd-instr-week-chevron" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M9 18l6-6-6-6"/></svg>
            </button>
        </div>"""
    }
    val weekFreqHtml = if (completedAll.isEmpty()) {
        """<p class="sd-muted">Нет данных</p>"""
    } else {
        val vbW = 560
        val vbH = 130
        val paddingTop = 12
        val paddingBottom = 30
        val leftPad = 22
        val rightPad = 8
        val plotH = vbH - paddingTop - paddingBottom
        val plotChartW = vbW - leftPad - rightPad

        fun yForCount(cnt: Int): Double {
            val c = cnt.coerceIn(0, weekFreqMaxY)
            return paddingTop + plotH * (1.0 - c.toDouble() / weekFreqMaxY.toDouble())
        }

        val polyPoints = (0 until 7).joinToString(" ") { i ->
            val cnt = dayCountsByWeekday[i].coerceIn(0, weekFreqMaxY)
            val x = leftPad + plotChartW * ((i + 0.5) / 7.0)
            val y = kotlin.math.round(yForCount(cnt)).toInt()
            "${x.toInt()},$y"
        }

        val circlesHtml = (0 until 7).joinToString("") { i ->
            val cnt = dayCountsByWeekday[i].coerceIn(0, weekFreqMaxY)
            val x = kotlin.math.round(leftPad + plotChartW * ((i + 0.5) / 7.0)).toInt()
            val y = kotlin.math.round(yForCount(cnt)).toInt()
            val fill = if (cnt == 0) "#ffffff" else "#1565C0"
            val stroke = if (cnt == 0) "#1565C0" else "#1565C0"
            val r = if (cnt == 0) 2 else 3
            """<circle cx="$x" cy="$y" r="$r" fill="$fill" stroke="$stroke" stroke-width="2">
                    <title>$cnt занятий</title>
               </circle>"""
        }

        val xLabelsHtml = (0 until 7).joinToString("") { i ->
            """<div class="sd-cadet-point-x-label sd-instr-dow-x-label-col" aria-hidden="true"><span class="sd-cadet-point-x-cnt">${
                dayCountsByWeekday[i]
            }</span><span class="sd-instr-dow-x-day">${dowShort[i]}</span></div>"""
        }

        val yAxisTextsHtml = (weekFreqMaxY downTo 1).joinToString("") { k ->
            val y = kotlin.math.round(yForCount(k)).toInt()
            """<text x="${leftPad - 6}" y="${y + 4}" text-anchor="end" font-size="11" fill="#607D8B" font-weight="600">$k</text>"""
        }

        val gridStroke = "rgba(21,101,192,0.20)"
        val gridLinesHtml = (1..weekFreqMaxY).joinToString("") { k ->
            val y = kotlin.math.round(yForCount(k)).toInt()
            """<line x1="$leftPad" y1="$y" x2="${leftPad + plotChartW}" y2="$y" stroke="$gridStroke" stroke-width="1"></line>"""
        } + run {
            val y0 = kotlin.math.round(yForCount(0)).toInt()
            """<line x1="$leftPad" y1="$y0" x2="${leftPad + plotChartW}" y2="$y0" stroke="$gridStroke" stroke-width="1"></line>"""
        }

        """<div class="sd-instr-dow-chart-grid">
            <div class="sd-cadet-point-svg-wrap">
                <svg class="sd-cadet-point-svg" viewBox="0 0 $vbW $vbH" preserveAspectRatio="none" role="img" aria-label="Частота вождений по дням">
                    $gridLinesHtml
                    $yAxisTextsHtml
                    <polyline points="$polyPoints" fill="none" stroke="#1565C0" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></polyline>
                    $circlesHtml
                </svg>
            </div>
            <div class="sd-cadet-point-x-labels sd-instr-dow-x-labels">$xLabelsHtml</div>
        </div>"""
    }
    val cancelled = sessions.filter { it.status == "cancelledByCadet" }
    val dayCountsCancel = IntArray(7)
    cancelled.forEach { s ->
        val t = s.cancelledAtMillis ?: s.startTimeMillis ?: return@forEach
        dayCountsCancel[weekdayIndexMon0Local(t)]++
    }
    val maxCancel = dayCountsCancel.maxOrNull()?.coerceAtLeast(1) ?: 1
    val cancelBarsHtml = if (cancelled.isEmpty()) {
        """<p class="sd-muted">Нет отменённых вождений</p>"""
    } else {
        (0 until 7).joinToString("") { i ->
            val cnt = dayCountsCancel[i]
            val h = if (cnt == 0) 0 else ((cnt.toFloat() / maxCancel * 80f).toInt().coerceAtLeast(4))
            val short = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")[i]
            """<div class="sd-instr-bar-col"><div class="sd-instr-bar sd-instr-bar-cancel" style="height:${h}px"></div><span class="sd-instr-bar-day">$short</span><span class="sd-instr-bar-num sd-instr-bar-num-cancel">$cnt</span></div>"""
        }
    }
    val profileCard = instructorHomeProfileCardHtml(user, showProfileShortcut = false)
    val svgBank = """<svg viewBox="0 0 24 24" fill="currentColor" width="40" height="40" class="sd-instr-stat-icon"><path d="M4 10v7h3v-7H4zm6 0v7h3v-7h-3zM2 22h19v-3H2v3zm2-12h15V7l-7.5-4L2 10zm15 0h3v7h-3v-7z"/></svg>"""
    val svgCar = """<svg viewBox="0 0 24 24" fill="currentColor" width="40" height="40" class="sd-instr-stat-icon"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/></svg>"""
    return """<div class="sd-instructor-profile-page">
        <p class="sd-instructor-profile-toolbar">
            <button type="button" class="sd-btn sd-btn-secondary sd-instructor-profile-back" id="sd-instructor-profile-back">← Назад</button>
        </p>
        <h2 class="sd-instructor-profile-title">Профиль</h2>
        $profileCard
        <div class="sd-instr-stat-card sd-instr-stat-card-earned" id="sd-instructor-earned-stat-card">
            <div class="sd-instr-stat-card-head">
                <span class="sd-instr-stat-icon-wrap">$svgBank</span>
                <div>
                    <p class="sd-instr-stat-label">Всего заработано талонов</p>
                    <p class="sd-instr-stat-value">$totalEarned</p>
                </div>
                $earned30Html
            </div>
            <div id="sd-instr-earned-calc-placeholder" class="sd-instr-earned-calc-placeholder"></div>
        </div>
        <div class="sd-instr-stat-card sd-instr-stat-card-simple">
            <span class="sd-instr-stat-icon-wrap">$svgCar</span>
            <div>
                <p class="sd-instr-stat-label">Всего завершённых вождений</p>
                <p class="sd-instr-stat-value">$completedCount</p>
            </div>
        </div>
        <div class="sd-instr-stat-card">
            <h3 class="sd-instr-stat-heading">Ваш рейтинг:</h3>
            <div class="sd-instr-rating-row">
                <div class="sd-instr-rating-pie-wrap">$pieInner</div>
                <div class="sd-instr-rating-legend">$ratingLegend</div>
            </div>
        </div>
        <div class="sd-instr-stat-card">
            <h3 class="sd-instr-stat-heading">Частота вождений в неделю:</h3>
            $weekNavHtml
            <p class="sd-muted sd-instr-chart-hint">Вертикаль — шкала 1–8 занятий в день, по горизонтали — дни недели (завершённые вождения за выбранную неделю).</p>
            <div class="sd-cadet-week-chart sd-cadet-week-chart-dow">$weekFreqHtml</div>
        </div>
        <div class="sd-instr-stat-card">
            <h3 class="sd-instr-stat-heading">График отменённых вождений:</h3>
            <p class="sd-muted sd-instr-chart-hint">Всего отменено: ${cancelled.size}</p>
            <div class="sd-instr-bars sd-instr-bars-cancel">$cancelBarsHtml</div>
        </div>
        <p class="sd-version">Версия: $version</p>
    </div>"""
}

private fun renderInstructorHomeContent(user: User, version: String): String {
    val loading = appState.recordingLoading
    val cadets = appState.instructorCadets.sortedBy { it.fullName }
    val sessions = appState.recordingSessions.filter { it.status == "scheduled" || it.status == "inProgress" || (it.status == "completed" && it.instructorRating == 0) }.take(20)
    val allSessions = appState.recordingSessions
    val loadingLine = if (loading) """<p class="sd-loading-text">Загрузка…</p>""" else ""
    fun instructorCadetCardHtml(c: User): String {
        val completedCount = allSessions.count { it.cadetId == c.id && it.status == "completed" }
        val phoneDisplay = (c.phone.ifBlank { "—" }).escapeHtml()
        val phoneHref = if (c.phone.isNotBlank()) "tel:${c.phone.escapeHtml()}" else "#"
        val svgChat = """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>"""
        val svgPhone = """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 1.18h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.77a16 16 0 0 0 6 6l.96-.96a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7a2 2 0 0 1 1.72 2.02z"/></svg>"""
        val svgPhoneRow = """<svg viewBox="0 0 24 24" fill="currentColor" width="13" height="13" style="opacity:0.6;flex-shrink:0"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 1.18h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.77a16 16 0 0 0 6 6l.96-.96a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7a2 2 0 0 1 1.72 2.02z"/></svg>"""
        val svgCar = """<svg viewBox="0 0 24 24" fill="currentColor" width="13" height="13" style="opacity:0.6;flex-shrink:0"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/></svg>"""
        val svgTicket = """<svg viewBox="0 0 24 24" fill="currentColor" width="13" height="13" style="opacity:0.6;flex-shrink:0"><path d="M22 10V6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v4c1.1 0 2 .9 2 2s-.9 2-2 2v4c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2v-4c-1.1 0-2-.9-2-2s.9-2 2-2zm-2-1.46c-1.19.69-2 1.99-2 3.46s.81 2.77 2 3.46V18H4v-2.54c1.19-.69 2-1.99 2-3.46 0-1.48-.8-2.77-2-3.46L4 6h16v2.54z"/></svg>"""
        val svgGroup = """<svg viewBox="0 0 24 24" fill="currentColor" width="13" height="13" style="opacity:0.6;flex-shrink:0"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>"""
        val svgBookDriving = """<svg class="sd-instructor-cadet-book-btn-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20" aria-hidden="true"><path d="M8 2v4"/><path d="M16 2v4"/><rect width="18" height="18" x="3" y="4" rx="2"/><path d="M3 10h18"/><path d="M12 14v4"/><path d="M10 16h4"/></svg>"""
        val cg = c.cadetGroupId?.let { gid -> appState.cadetGroups.find { it.id == gid } }
        val groupRowText = cadetGroupDisplayTextEscaped(cg)
        val phoneDisabled = if (c.phone.isBlank()) " sd-cadet-icon-btn-disabled" else ""
        val cadetAvatarHtml = avatarBlockHtml("sd-cadet-avatar", c, user.id)
        return """<div class="sd-cadet-card">
            <div class="sd-cadet-accent-bar"></div>
            <div class="sd-cadet-top">
                $cadetAvatarHtml
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
                <div class="sd-cadet-row">$svgGroup Группа: $groupRowText</div>
            </div>
            <div class="sd-cadet-card-footer sd-instructor-cadet-card-footer">
                <button type="button" class="sd-instructor-cadet-book-btn" data-instructor-book-cadet="${c.id.escapeHtml()}" title="Записать на вождение">
                    <span class="sd-instructor-cadet-book-btn-inner">
                        $svgBookDriving
                        <span class="sd-instructor-cadet-book-btn-text">Записать</span>
                    </span>
                </button>
            </div>
        </div>"""
    }
    val cadetsListHtml = if (cadets.isEmpty()) {
        """<p class="sd-muted">Нет назначенных курсантов</p>"""
    } else {
        val groupIds = appState.cadetGroups.map { it.id }.toSet()
        val instructorGroupSectionsHtml = appState.cadetGroups.joinToString("") { g ->
            val inGroup = cadets.filter { it.cadetGroupId == g.id }.sortedBy { it.fullName }
            val title = cadetGroupDisplayTextEscaped(g)
            val cardsInner = if (inGroup.isEmpty()) {
                """<p class="sd-muted sd-admin-cadets-empty">В этой группе пока нет курсантов</p>"""
            } else {
                inGroup.joinToString("") { instructorCadetCardHtml(it) }
            }
            """<div class="sd-admin-cadets-subsection" data-instructor-cadet-group-section="${g.id.escapeHtml()}">
                <div class="sd-admin-cadets-subsection-head sd-admin-cadets-subsection-head-single">
                    <h4 class="sd-admin-cadets-subtitle">$title <span class="sd-admin-cadets-subcount">(${inGroup.size})</span></h4>
                </div>
                <div class="sd-cadet-cards sd-admin-cards-cadet-group">$cardsInner</div>
            </div>"""
        }
        val unassignedInstructorCadets = cadets.filter { u ->
            val gid = u.cadetGroupId
            gid.isNullOrBlank() || !groupIds.contains(gid)
        }.sortedBy { it.fullName }
        val unassignedCardsInner = if (unassignedInstructorCadets.isEmpty()) {
            """<p class="sd-muted sd-admin-cadets-empty">Нет курсантов без группы</p>"""
        } else {
            unassignedInstructorCadets.joinToString("") { instructorCadetCardHtml(it) }
        }
        val unassignedSectionHtml = """<div class="sd-admin-cadets-subsection sd-admin-cadets-subsection-unassigned" data-instructor-cadet-group-section="">
            <div class="sd-admin-cadets-subsection-head sd-admin-cadets-subsection-head-single">
                <h4 class="sd-admin-cadets-subtitle">Не в группе <span class="sd-admin-cadets-subcount">(${unassignedInstructorCadets.size})</span></h4>
            </div>
            <div class="sd-cadet-cards sd-admin-cards-cadet-group">$unassignedCardsInner</div>
        </div>"""
        """<div class="sd-admin-cadets-grouped sd-instructor-cadets-grouped">$instructorGroupSectionsHtml$unassignedSectionHtml</div>"""
    }
    val sessionsByWeekday = sessions.groupBy { getWeekdayIndex(it.startTimeMillis) }
    val nowMs = js("Date.now()").unsafeCast<Double>().toLong()
    val lateDisabled = appState.instructorRunningLateUntilMs > 0L && nowMs < appState.instructorRunningLateUntilMs
    val scheduleSectionsHtml = if (sessions.isEmpty()) {
        """<p class="sd-muted">Нет записанных на вождение курсантов</p>"""
    } else {
        WEEKDAY_NAMES.mapIndexed { index, dayName ->
            val daySessions = (sessionsByWeekday[index] ?: emptyList()).sortedBy { it.startTimeMillis ?: 0L }
            val count = daySessions.size
            val cardsHtml = daySessions.joinToString("") { s ->
                val cadetName = cadets.find { it.id == s.cadetId }?.fullName?.takeIf { it.isNotBlank() }?.let { formatShortName(it) } ?: "Курсант (${s.cadetId.take(8)})"
                val cadetNameEsc = cadetName.escapeHtml()
                val dateStr = formatDateOnly(s.startTimeMillis)
                val timeStr = formatTimeOnly(s.startTimeMillis)
                val startMs = s.startTimeMillis ?: 0L
                val bookedByCadet = s.openWindowId.isNotBlank()
                val waitingCadetConfirm = s.status == "scheduled" && s.startRequestedByInstructor
                val statusText = when {
                    s.status == "completed" && s.instructorRating == 0 -> "Вождение завершено ($cadetNameEsc) — оценка не выставлена"
                    bookedByCadet && !s.instructorConfirmed -> "Курсант $cadetNameEsc забронировал — ожидает вашего подтверждения"
                    !s.instructorConfirmed -> "Ожидает подтверждения записи курсантом ($cadetNameEsc)"
                    s.status == "inProgress" -> "В процессе ($cadetNameEsc)"
                    waitingCadetConfirm -> "Ожидание подтверждения курсантом ($cadetNameEsc)"
                    else -> "Подтверждён ($cadetNameEsc)"
                }
                val avatarLetter = cadetName.trim().split(" ").filter { it.isNotBlank() }.mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
                val schCadet = cadets.find { it.id == s.cadetId }
                val schAvatarHtml = avatarBlockHtml("sd-sch-avatar", schCadet, user.id, avatarLetter.ifBlank { "?" })
                val showConfirm = bookedByCadet && !s.instructorConfirmed && s.status != "completed"
                val showStart = s.status == "scheduled" && s.instructorConfirmed
                val nowMs = js("Date.now()").unsafeCast<Double>().toLong()
                val startRequested = s.startRequestedByInstructor
                val startBtnDisabled = showStart && (startRequested || (nowMs < startMs - START_ALLOWED_MINUTES_BEFORE * 60 * 1000))
                val statusClass = when {
                    s.status == "completed" && s.instructorRating == 0 -> "sd-sch-status-waiting"
                    bookedByCadet && !s.instructorConfirmed -> "sd-sch-status-waiting"
                    !s.instructorConfirmed -> "sd-sch-status-pending"
                    s.status == "inProgress" -> "sd-sch-status-active"
                    waitingCadetConfirm -> "sd-sch-status-pending"
                    else -> "sd-sch-status-confirmed"
                }
                val confirmBtn = if (showConfirm) """<button type="button" class="sd-sch-btn sd-sch-confirm-btn sd-home-schedule-confirm" data-session-id="${s.id.escapeHtml()}">$iconSelectSvg Подтвердить</button>""" else ""
                val startBtnLabel = if (startRequested) "Ожидание" else "Начать"
                val startBtn = if (showStart) """<button type="button" class="sd-sch-btn sd-sch-start-btn sd-home-schedule-start${if (startBtnDisabled) " sd-sch-start-disabled" else ""}" data-session-id="${s.id.escapeHtml()}" data-start-ms="$startMs">$iconPlaySvg $startBtnLabel</button>""" else ""
                val lateBtn = if (s.status == "scheduled") """<button type="button" class="sd-sch-btn sd-sch-late-btn sd-home-schedule-late" data-session-id="${s.id.escapeHtml()}"${if (lateDisabled) " disabled title=\"Задержка активна\"" else ""}>$iconLateSvg Опаздываю</button>""" else ""
                val cadetNotConfirmedYet = !bookedByCadet && !s.instructorConfirmed
                val cancelCadetUnconfirmedAttr = if (cadetNotConfirmedYet) """ data-home-cancel-needs-cadet-confirm="1"""" else ""
                val cancelBtn = if (s.status != "completed") """<button type="button" class="sd-sch-btn sd-sch-cancel-btn sd-home-schedule-cancel" data-session-id="${s.id.escapeHtml()}"$cancelCadetUnconfirmedAttr>$iconTrashSvg Отменить</button>""" else ""
                val rateCadetBtn = if (s.status == "completed" && s.instructorRating == 0) """<button type="button" class="sd-sch-btn sd-sch-rate-btn sd-instructor-rate-cadet-btn" data-session-id="${s.id.escapeHtml()}" data-cadet-name="${(cadets.find { it.id == s.cadetId }?.fullName?.takeIf { it.isNotBlank() }?.let { formatShortName(it) } ?: "Курсант").escapeHtml()}">Поставить оценку</button>""" else ""
                val timerBlock = if (s.status == "inProgress") {
                    val endMs = (s.actualStartMs ?: s.startTimeMillis ?: 0L) + LESSON_DURATION_MS
                    val paused = !s.sessionIsActive
                    val remainingAtPause = if (paused && s.sessionPausedAt != null) (endMs - s.sessionPausedAt).coerceAtLeast(0L) else 0L
                    val pausedAttr = if (paused) """ data-paused="true" data-remaining-ms="$remainingAtPause" """ else ""
                    val pauseIconSrc = if (paused) "pause-icon-pressed.png" else "pause-icon.png"
                    val playIconSrc = if (paused) "play-icon.png" else "play-icon-pressed.png"
                    val iconCarThumbSvg = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20" class="sd-sch-timer-thumb-icon"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/></svg>"""
                    """<div class="sd-sch-timer-block" data-end-ms="$endMs" data-session-id="${s.id.escapeHtml()}"$pausedAttr>
                        <div class="sd-sch-timer-header">
                            <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                            <span class="sd-sch-timer-label">Осталось:</span>
                            <span class="sd-sch-timer-value">--:--</span>
                        </div>
                        <div class="sd-sch-timer-bar-wrap"><div class="sd-sch-timer-bar-fill"></div><span class="sd-sch-timer-bar-thumb" role="presentation">$iconCarThumbSvg</span></div>
                        <div class="sd-sch-timer-actions">
                            <button type="button" class="sd-sch-btn sd-sch-timer-btn sd-sch-pause" data-session-id="${s.id.escapeHtml()}" title="Пауза"><img src="$pauseIconSrc" alt="Пауза" class="sd-sch-timer-icon" /></button>
                            <button type="button" class="sd-sch-btn sd-sch-timer-btn sd-sch-play" data-session-id="${s.id.escapeHtml()}" title="Продолжить"><img src="$playIconSrc" alt="Продолжить" class="sd-sch-timer-icon" /></button>
                            <button type="button" class="sd-sch-btn sd-sch-timer-btn sd-sch-stop" data-session-id="${s.id.escapeHtml()}" data-end-ms="$endMs" title="Завершить"><img src="stop-icon.png" alt="Стоп" class="sd-sch-timer-icon sd-stop-icon-default" /><img src="stop-icon-pressed.png" alt="" class="sd-sch-timer-icon sd-stop-icon-pressed" aria-hidden="true" /></button>
                        </div>
                    </div>"""
                } else ""
                """<div class="sd-schedule-card" data-session-id="${s.id.escapeHtml()}" data-start-ms="$startMs">
                    $schAvatarHtml
                    <div class="sd-sch-body">
                        <div class="sd-sch-name">${cadetName.escapeHtml()}</div>
                        <div class="sd-sch-dt">
                            <span class="sd-sch-pill">$iconCalendarSvg $dateStr</span>
                            <span class="sd-sch-pill">$iconClockSvg $timeStr</span>
                        </div>
                        <span class="sd-sch-status-line"><span class="sd-sch-status-label">Статус:</span> <span class="sd-sch-status $statusClass">$statusText</span></span>
                        $timerBlock
                    </div>
                    <div class="sd-sch-actions">$confirmBtn$startBtn$lateBtn$cancelBtn$rateCadetBtn</div>
                </div>"""
            }
            """<details class="sd-schedule-day" ${if (count > 0) "open" else ""}><summary class="sd-schedule-day-title">$dayName ($count)</summary><div class="sd-schedule-list">$cardsHtml</div></details>"""
        }.joinToString("")
    }
    val startEarlyModalHtml = """<div class="sd-modal-overlay sd-hidden" id="sd-start-early-modal"><div class="sd-modal"><h3 class="sd-modal-title">Начать вождение раньше?</h3><p id="sd-start-early-text" class="sd-start-early-text">Вы уверены начать вождение раньше? До вождения еще: <span id="sd-start-early-mins">0</span> минут.</p><p class="sd-modal-actions"><button type="button" id="sd-start-early-yes" class="sd-btn sd-btn-primary">Да</button><button type="button" id="sd-start-early-no" class="sd-btn sd-btn-secondary">Нет</button></p></div></div>"""
    val completeEarlyModalHtml = """<div class="sd-modal-overlay sd-hidden" id="sd-complete-early-modal"><div class="sd-modal"><h3 class="sd-modal-title">Завершить вождение досрочно?</h3><p id="sd-complete-early-text"></p><p class="sd-modal-actions"><button type="button" id="sd-complete-early-yes" class="sd-btn sd-btn-primary">Да</button><button type="button" id="sd-complete-early-no" class="sd-btn sd-btn-secondary">Нет</button></p></div></div>"""
    val profileCard = instructorHomeProfileCardHtml(user, showProfileShortcut = true)
    val myCadetsOpen = if (appState.instructorMyCadetsSectionOpen) " open" else ""
    return """<h2>Главная</h2>
        $profileCard
        <details class="sd-block sd-details-block" data-instructor-my-cadets$myCadetsOpen><summary class="sd-block-title">Мои курсанты (${cadets.size})</summary><div class="sd-cadet-cards sd-instructor-my-cadets-cards">$cadetsListHtml</div></details>
        $loadingLine
        <div class="sd-block sd-block-schedule"><h3 class="sd-block-title">Мой график</h3><div class="sd-schedule-days">$scheduleSectionsHtml</div>$startEarlyModalHtml$completeEarlyModalHtml</div>
        <p class="sd-version">Версия: $version</p>"""
}

/** Карточка курсанта на главной (с кнопкой «Профиль») и на экране статистики. */
private fun cadetHomeProfileCardHtml(user: User, showProfileShortcut: Boolean): String {
    val svgEmail = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>"""
    val svgPhoneP = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/></svg>"""
    val svgTicketP = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M22 10V6c0-1.1-.9-2-2-2H4c-1.1 0-1.99.9-1.99 2v4c1.1 0 1.99.9 1.99 2s-.89 2-2 2v4c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2v-4c-1.1 0-2-.9-2-2s.9-2 2-2zm-2-1.46c-1.19.69-2 1.99-2 3.46s.81 2.77 2 3.46V18H4v-2.54c1.19-.69 2-1.99 2-3.46 0-1.48-.8-2.77-1.99-3.46L4 6h16v2.54z"/></svg>"""
    val svgDriving = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/></svg>"""
    val completedDrivingsCount = (appState.recordingSessions + appState.historySessions).distinctBy { it.id }.count { it.status == "completed" }
    val cadetRoleName = when {
        completedDrivingsCount < 5 -> "Новичок"
        completedDrivingsCount < 15 -> "Любитель"
        completedDrivingsCount < 25 -> "Профи"
        else -> "Эксперт"
    }
    val cadetProfileAvatarHtml = avatarBlockHtml("sd-profile-avatar", user, user.id)
    val iconPerson = """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20" aria-hidden="true"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>"""
    val profileBtn = if (showProfileShortcut) {
        """<button type="button" class="sd-instructor-profile-card-btn" id="sd-cadet-profile-open" title="Профиль" aria-label="Профиль">$iconPerson</button>"""
    } else ""
    val wrapOpen = if (showProfileShortcut) """<div class="sd-instructor-profile-card-wrap">""" else ""
    val wrapClose = if (showProfileShortcut) """</div>""" else ""
    val card = """
        <div class="sd-profile-card sd-profile-card-cadet">
            <div class="sd-profile-accent-bar"></div>
            <div class="sd-profile-hero">
                $cadetProfileAvatarHtml
                <div class="sd-profile-hero-info">
                    <p class="sd-profile-fullname">${(user.fullName.ifBlank { "—" }).escapeHtml()}</p>
                    <span class="sd-profile-role-badge">Курсант</span>
                </div>
            </div>
            <div class="sd-profile-info-rows">
                <div class="sd-profile-info-row"><span class="sd-profile-info-icon">$svgEmail</span><span class="sd-profile-info-label">Email</span><span class="sd-profile-info-value">${(user.email.ifBlank { "—" }).escapeHtml()}</span></div>
                <div class="sd-profile-info-row"><span class="sd-profile-info-icon">$svgPhoneP</span><span class="sd-profile-info-label">Телефон</span><span class="sd-profile-info-value">${(user.phone.ifBlank { "—" }).escapeHtml()}</span></div>
                <div class="sd-profile-info-row"><span class="sd-profile-info-icon">$svgDriving</span><span class="sd-profile-info-label">Вождений завершено</span><span class="sd-profile-info-value">${completedDrivingsCount} (${cadetRoleName})</span></div>
                <div class="sd-profile-info-row sd-profile-info-row-balance"><span class="sd-profile-info-icon">$svgTicketP</span><span class="sd-profile-info-label">Талоны</span><span class="sd-balance-badge">${user.balance}</span></div>
            </div>
        </div>"""
    return wrapOpen + profileBtn + card + wrapClose
}

private fun cadetDrivingSessionsMerged(user: User): List<DrivingSession> {
    val uid = user.id
    return (appState.recordingSessions + appState.historySessions).distinctBy { it.id }.filter { it.cadetId == uid }
}

private fun weekKeyMondayFromMillis(millis: Long): String =
    js("(function(ms){ var d=new Date(ms); d.setHours(0,0,0,0); var day=d.getDay(); var diff=(day===0?-6:1-day); d.setDate(d.getDate()+diff); var m=d.getMonth()+1; var dayn=d.getDate(); return d.getFullYear()+'-'+('0'+m).slice(-2)+'-'+('0'+dayn).slice(-2); })")(millis).unsafeCast<String>()

/** Статистика курсанта — как CadetProfileStatsView / CadetHomeTab в Android. */
private fun renderCadetProfileStatsContent(user: User, version: String): String {
    val sessions = cadetDrivingSessionsMerged(user)
    val completed = sessions.count { it.status == "completed" }
    val capped = completed.coerceAtMost(30)
    /* Угол от 12 часов по часовой: ровно (завершено/30)*360°, 24 занятия → 288° — внутри синего сектора 180–300° */
    val filledDeg = capped / 30.0 * 360.0
    val filledDegStr = "${filledDeg}deg"
    val profileCard = cadetHomeProfileCardHtml(user, showProfileShortcut = false)
    /** Кольцо 30 занятий: 0–5 жёлтый, 5–15 зелёный, 15–25 синий, 25–30 красный (доли круга 60°+120°+120°+60°). */
    val segLegend = """
        <div class="sd-cadet-seg-legend">
            <div class="sd-cadet-seg-row"><span class="sd-cadet-role-sq" style="background:#FFC107"></span><span>0–5 занятий (Новичок)</span></div>
            <div class="sd-cadet-seg-row"><span class="sd-cadet-role-sq" style="background:#2E7D32"></span><span>5–15 занятий (Любитель)</span></div>
            <div class="sd-cadet-seg-row"><span class="sd-cadet-role-sq" style="background:#1565C0"></span><span>15–25 занятий (Профи)</span></div>
            <div class="sd-cadet-seg-row"><span class="sd-cadet-role-sq" style="background:#C62828"></span><span>25–30 занятий (Эксперт)</span></div>
        </div>"""
    val ratedSessions = sessions
        .filter { it.status == "completed" && it.instructorRating in 3..5 }
        .sortedBy { it.completedAtMillis ?: it.startTimeMillis ?: 0L }
        .takeLast(10)
    val ratingLineHtml = if (ratedSessions.isEmpty()) {
        """<p class="sd-muted">Нет оценок по завершённым вождениям</p>"""
    } else {
        val vbW = 560
        val vbH = 190
        val leftPad = 36
        val rightPad = 12
        val topPad = 12
        val bottomPad = 54
        val plotW = vbW - leftPad - rightPad
        val plotH = vbH - topPad - bottomPad
        fun yForRating(r: Int): Int {
            val frac = ((r - 3).toDouble() / 2.0).coerceIn(0.0, 1.0)
            return (topPad + plotH * (1.0 - frac)).toInt()
        }
        val points = ratedSessions.mapIndexed { idx, s ->
            val x = if (ratedSessions.size <= 1) leftPad + plotW / 2.0 else leftPad + plotW * (idx.toDouble() / (ratedSessions.size - 1).toDouble())
            val y = yForRating(s.instructorRating)
            Triple(x.toInt(), y, s)
        }
        val polyPoints = points.joinToString(" ") { "${it.first},${it.second}" }
        val pointDots = points.joinToString("") { (x, y, s) ->
            val dt = formatDateOnly(s.completedAtMillis ?: s.startTimeMillis)
            """<circle cx="$x" cy="$y" r="4" fill="#1565C0" stroke="#ffffff" stroke-width="2"><title>$dt: ${s.instructorRating}</title></circle>"""
        }
        val yGrid = (3..5).joinToString("") { r ->
            val y = yForRating(r)
            """<line x1="$leftPad" y1="$y" x2="${leftPad + plotW}" y2="$y" stroke="rgba(21,101,192,0.20)" stroke-width="1"></line>
               <text x="${leftPad - 8}" y="${y + 4}" text-anchor="end" font-size="12" fill="#607D8B">$r</text>"""
        }
        val xLabels = points.mapIndexed { idx, (x, _, _) ->
            val leftPct = (x.toDouble() * 100.0 / vbW.toDouble())
            """<div class="sd-cadet-rating-date" style="left:${formatFixed1(leftPct)}%">${(idx + 1).toString().escapeHtml()}</div>"""
        }.joinToString("")
        """<div class="sd-cadet-rating-line-wrap">
            <svg class="sd-cadet-rating-line-svg" viewBox="0 0 $vbW $vbH" preserveAspectRatio="none" role="img" aria-label="Ваши оценки по датам">
                $yGrid
                <polyline points="$polyPoints" fill="none" stroke="#1565C0" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></polyline>
                $pointDots
            </svg>
            <div class="sd-cadet-rating-dates">$xLabels</div>
        </div>"""
    }
    val cancelled = sessions.filter { it.status == "cancelledByCadet" }
    val weekCountsCancel = mutableMapOf<String, Int>()
    cancelled.forEach { s ->
        val t = s.cancelledAtMillis ?: s.startTimeMillis ?: return@forEach
        val k = weekKeyMondayFromMillis(t)
        weekCountsCancel[k] = (weekCountsCancel[k] ?: 0) + 1
    }
    val sortedWeeksCancel = weekCountsCancel.keys.sorted().takeLast(2)
    val countsCancel = sortedWeeksCancel.map { weekCountsCancel[it] ?: 0 }
    val maxWeekCancel = countsCancel.maxOrNull()?.coerceAtLeast(1) ?: 1
    val weekCancelHtml = if (sortedWeeksCancel.isEmpty()) {
        """<p class="sd-muted">Нет отменённых вождений</p>"""
    } else {
        sortedWeeksCancel.indices.joinToString("") { i ->
            val cnt = countsCancel[i]
            val h = if (maxWeekCancel > 0) ((cnt.toFloat() / maxWeekCancel * 80f).toInt().coerceAtLeast(4)) else 4
            val lbl = sortedWeeksCancel[i].takeLast(5)
            """<div class="sd-cadet-week-col"><div class="sd-cadet-week-bar sd-cadet-week-bar-cancel" style="height:${h}px"></div><span class="sd-cadet-week-lbl">$lbl</span><span class="sd-cadet-week-cnt">$cnt</span></div>"""
        }
    }
    return """<div class="sd-instructor-profile-page sd-cadet-profile-page">
        <p class="sd-instructor-profile-toolbar">
            <button type="button" class="sd-btn sd-btn-secondary sd-instructor-profile-back" id="sd-cadet-profile-back">← Назад</button>
        </p>
        <h2 class="sd-instructor-profile-title">Профиль</h2>
        $profileCard
        <div class="sd-instr-stat-card">
            <h3 class="sd-instr-stat-heading">Прогресс вождений:</h3>
            <div class="sd-cadet-progress-row">
                <div class="sd-cadet-progress-ring-wrap">
                    <div class="sd-cadet-progress-ring"></div>
                    <div class="sd-cadet-progress-pointer" style="transform: rotate($filledDegStr)" aria-hidden="true"></div>
                    <span class="sd-cadet-progress-center-num">$completed</span>
                </div>
                $segLegend
            </div>
        </div>
        <div class="sd-instr-stat-card">
            <h3 class="sd-instr-stat-heading">Ваши оценки:</h3>
            <p class="sd-muted sd-instr-chart-hint">Вертикаль — оценка (3–5), горизонталь — порядковый номер вождения.</p>
            <div class="sd-cadet-rating-line-chart">$ratingLineHtml</div>
        </div>
        <div class="sd-instr-stat-card">
            <h3 class="sd-instr-stat-heading">Отменённые курсантом вождения:</h3>
            <p class="sd-muted sd-instr-chart-hint">Всего отменено: ${cancelled.size}</p>
            <div class="sd-cadet-week-chart">$weekCancelHtml</div>
        </div>
        <p class="sd-version">Версия: $version</p>
    </div>"""
}

private fun renderCadetHomeContent(user: User, version: String): String {
    val inst = appState.cadetInstructor
    val profileCard = cadetHomeProfileCardHtml(user, showProfileShortcut = true)
    val instructorBlockHtml = when {
        inst != null -> {
            val instAvatarHtml = avatarBlockHtml("sd-cadet-avatar", inst, user.id)
            val svgChat = """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>"""
            val svgPhone = """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 1.18h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.77a16 16 0 0 0 6 6l.96-.96a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7a2 2 0 0 1 1.72 2.02z"/></svg>"""
            val phoneHref = if (inst.phone.isNotBlank()) "tel:${inst.phone.escapeHtml()}" else "#"
            val phoneDisabled = if (inst.phone.isBlank()) " sd-cadet-icon-btn-disabled" else ""
            val svgCarRow = """<svg viewBox="0 0 24 24" fill="currentColor" width="13" height="13" style="opacity:0.6;flex-shrink:0"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/></svg>"""
            val vehicleText = trainingVehicleLabelOrDash(inst).escapeHtml()
            """<div class="sd-block sd-details-block"><h3 class="sd-block-title">Мой инструктор</h3><div class="sd-cadet-cards">
                <div class="sd-cadet-card sd-cadet-card-instructor">
                    <div class="sd-cadet-accent-bar"></div>
                    <div class="sd-cadet-top">
                        $instAvatarHtml
                        <div class="sd-cadet-head">
                            <p class="sd-cadet-name">${(inst.fullName.ifBlank { "—" }).escapeHtml()}</p>
                            <span class="sd-cadet-badge sd-instructor-badge">Инструктор</span>
                        </div>
                        <div class="sd-cadet-quick">
                            <button type="button" class="sd-cadet-icon-btn sd-cadet-chat-btn" data-contact-id="${inst.id.escapeHtml()}" title="Чат">$svgChat</button>
                            <a href="$phoneHref" class="sd-cadet-icon-btn$phoneDisabled" title="Позвонить">$svgPhone</a>
                        </div>
                    </div>
                    <div class="sd-cadet-rows sd-cadet-rows-instructor-vehicle">
                        <div class="sd-cadet-row">$svgCarRow Учебное ТС: <strong>$vehicleText</strong></div>
                    </div>
                </div>
            </div></div>"""
        }
        user.assignedInstructorId != null -> """<div class="sd-block sd-details-block"><h3 class="sd-block-title">Мой инструктор</h3><p class="sd-loading-text">Загрузка…</p></div>"""
        else -> """<div class="sd-block sd-details-block"><h3 class="sd-block-title">Мой инструктор</h3><p class="sd-muted">Инструктор не назначен</p></div>"""
    }
    val loading = appState.recordingLoading
    val sessions = appState.recordingSessions.filter { it.status == "scheduled" || it.status == "inProgress" }.take(20)
    val sessionsByWeekday = sessions.groupBy { getWeekdayIndex(it.startTimeMillis) }
    val loadingLine = if (loading) """<p class="sd-loading-text">Загрузка…</p>""" else ""
    val cadetDrivingSectionsHtml = if (sessions.isEmpty()) {
        """<p class="sd-muted">Нет запланированных занятий</p>"""
    } else {
        WEEKDAY_NAMES.mapIndexed { index, dayName ->
            val daySessions = (sessionsByWeekday[index] ?: emptyList()).sortedBy { it.startTimeMillis ?: 0L }
            val count = daySessions.size
            val cardsHtml = daySessions.joinToString("") { s ->
                val dateStr = formatDateOnly(s.startTimeMillis)
                val timeStr = formatTimeOnly(s.startTimeMillis)
                val needCadetConfirm = s.status == "scheduled" && s.startRequestedByInstructor
                val statusText = when {
                    s.status == "inProgress" -> "В процессе"
                    needCadetConfirm -> "Инструктор начал вождение — подтвердите"
                    else -> "Запланировано"
                }
                val statusCls = when {
                    s.status == "inProgress" -> "sd-record-status-active"
                    needCadetConfirm -> "sd-record-status-waiting"
                    else -> "sd-record-status-sched"
                }
                val confirmStartBtn = if (needCadetConfirm) """<button type="button" class="sd-sch-btn sd-sch-confirm-btn sd-sch-start-btn sd-cadet-confirm-start" data-session-id="${s.id.escapeHtml()}">$iconPlaySvg Начать</button>""" else ""
                """<div class="sd-record-card">
                    <div class="sd-record-card-dt">
                        <span class="sd-sch-pill">$iconCalendarSvg $dateStr</span>
                        <span class="sd-sch-pill">$iconClockSvg $timeStr</span>
                    </div>
                    <span class="sd-record-card-status $statusCls">Статус: $statusText</span>
                    <div class="sd-record-card-actions">$confirmStartBtn</div>
                </div>"""
            }
            """<details class="sd-schedule-day" ${if (count > 0) "open" else ""}><summary class="sd-schedule-day-title">$dayName ($count)</summary><div class="sd-schedule-list">$cardsHtml</div></details>"""
        }.joinToString("")
    }
    val cadetRateInstructorModalHtml = """<div class="sd-modal-overlay sd-hidden" id="sd-cadet-rate-instructor-modal"><div class="sd-modal"><h3 class="sd-modal-title">Вождение завершено, поставьте оценку инструктору</h3><div class="sd-rate-stars" id="sd-cadet-rate-stars" data-selected="0"><span class="sd-star" data-value="1" aria-label="1">★</span><span class="sd-star" data-value="2" aria-label="2">★</span><span class="sd-star" data-value="3" aria-label="3">★</span><span class="sd-star" data-value="4" aria-label="4">★</span><span class="sd-star" data-value="5" aria-label="5">★</span></div><p class="sd-modal-actions"><button type="button" id="sd-cadet-rate-instructor-confirm" class="sd-btn sd-btn-primary">Подтвердить</button></p></div></div>"""
    return """<h2>Главная</h2>
        $profileCard
        $instructorBlockHtml
        $loadingLine
        <div class="sd-block sd-block-schedule"><h3 class="sd-block-title">Моё вождение</h3><div class="sd-schedule-days">$cadetDrivingSectionsHtml</div></div>
        $cadetRateInstructorModalHtml
        <p class="sd-version">Версия: $version</p>"""
}

private fun patchInstructorRecordingDatetimeInputsMin(sdCard: org.w3c.dom.Element) {
    val min = getDatetimeLocalMin()
    (sdCard.querySelector("#sd-recording-book-dt") as? HTMLInputElement)?.min = min
    (sdCard.querySelector("#sd-recording-add-dt") as? HTMLInputElement)?.min = min
}

private val closestButtonEl = js("(function(el){ return el && el.closest ? el.closest('button') : null; })").unsafeCast<(Any?) -> Any?>()

private fun handleInstructorRecordingAddWindowClick() {
    val usr = appState.user ?: return
    if (usr.role != "instructor") return
    val input = document.getElementById("sd-recording-add-dt") as? HTMLInputElement
    val v = input?.value ?: ""
    if (v.isBlank()) { updateState { networkError = "Укажите дату и время" }; return }
    val dateFn = js("function(s){ return new Date(s).getTime(); }").unsafeCast<(String) -> Number>()
    val ms = dateFn(v).toLong()
    if (ms <= 0) return
    val nowMs = js("Date.now()").unsafeCast<Double>().toLong()
    if (ms < nowMs) { showToast("Нельзя выбрать прошедшую дату и время"); return }
    val occupied = findOccupiedMessage(ms, appState.recordingSessions, appState.recordingOpenWindows, appState.instructorCadets)
    if (occupied != null) { showToast(occupied); return }
    addOpenWindow(usr.id, ms) { _, err ->
        if (err != null) updateState { networkError = err }
        else {
            showNotification("Добавлено свободное окно для записи" + formatDayTimeShort(ms))
            getOpenWindowsForInstructor(usr.id) { wins ->
                getSessionsForInstructor(usr.id) { sess ->
                    updateState {
                        recordingOpenWindows = wins
                        recordingSessions = sess
                        networkError = null
                        instructorRecordingAddDatetimeLocal = ""
                    }
                }
            }
        }
    }
}

private fun handleInstructorRecordingBookClick() {
    val usr = appState.user ?: return
    if (usr.role != "instructor") return
    val cadetSelect = document.getElementById("sd-recording-book-cadet") as? HTMLSelectElement
    val cadetId = cadetSelect?.value?.takeIf { it.isNotBlank() } ?: run { updateState { networkError = "Выберите курсанта" }; return }
    val input = document.getElementById("sd-recording-book-dt") as? HTMLInputElement
    val v = input?.value ?: ""
    if (v.isBlank()) { updateState { networkError = "Укажите дату и время" }; return }
    val dateFn = js("function(s){ return new Date(s).getTime(); }").unsafeCast<(String) -> Number>()
    val ms = dateFn(v).toLong()
    if (ms <= 0) return
    val nowMs = js("Date.now()").unsafeCast<Double>().toLong()
    if (ms < nowMs) { showToast("Нельзя выбрать прошедшую дату и время"); return }
    val occupied = findOccupiedMessage(ms, appState.recordingSessions, appState.recordingOpenWindows, appState.instructorCadets)
    if (occupied != null) { showToast(occupied); return }
    val cadet = appState.instructorCadets.find { it.id == cadetId }
    if (cadet == null) { updateState { networkError = "Курсант не найден" }; return }
    if (cadet.balance <= 0) {
        val cadetShortName = formatShortName(cadet.fullName)
        val msg = "У курсанта $cadetShortName 0 талонов, запись невозможна"
        updateState { networkError = msg }
        showNotification(msg)
        return
    }
    val scheduledCount = appState.recordingSessions.count { it.cadetId == cadetId && (it.status == "scheduled" || it.status == "inProgress") }
    if (scheduledCount >= cadet.balance) {
        val cadetShortName = formatShortName(cadet.fullName)
        val msg = "По балансу курсанта $cadetShortName уже запланировано максимальное число вождений"
        updateState { networkError = msg }
        showNotification(msg)
        return
    }
    createSession(usr.id, cadetId, ms, null) { _, err ->
        if (err != null) updateState { networkError = err }
        else {
            val cadetShortName = formatShortName(cadet.fullName)
            showNotification("Вождение назначено" + formatDayTimeShort(ms) + ". Курсант: $cadetShortName")
            getSessionsForInstructor(usr.id) { sess ->
                updateState {
                    recordingSessions = sess
                    networkError = null
                    instructorRecordingBookCadetId = null
                    instructorRecordingBookDatetimeLocal = ""
                }
            }
        }
    }
}

private fun setupInstructorRecordingSubmitButtonsDelegationOnce() {
    if (window.asDynamic().__sdInstructorRecordingSubmitDelegation == true) return
    window.asDynamic().__sdInstructorRecordingSubmitDelegation = true
    document.body?.addEventListener("click", { e: dynamic ->
        val raw = e?.target ?: return@addEventListener
        val el = raw as? org.w3c.dom.Element ?: return@addEventListener
        val btn = closestButtonEl(el) as? org.w3c.dom.Element ?: return@addEventListener
        when (btn.id) {
            "sd-recording-book-btn" -> handleInstructorRecordingBookClick()
            "sd-recording-add-btn" -> handleInstructorRecordingAddWindowClick()
            else -> {}
        }
    })
}

/** Только списки/статистика/модалки — без форм записи; при частичном обновлении вкладки не трогаем select/datetime. */
private fun renderInstructorRecordingDynamicPanelHtml(user: User): String {
    val loading = appState.recordingLoading
    val windows = appState.recordingOpenWindows
    val sessions = appState.recordingSessions
    val cadets = appState.instructorCadets
    val loadingLine = if (loading) """<div class="sd-rec-loading">Загрузка… <button type="button" id="sd-stop-loading" class="sd-btn sd-btn-small sd-btn-secondary">Показать пусто</button></div>""" else ""
    val recCalendarIco = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>"""
    val recClockIco = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>"""
    val recWeekdayIco = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 7h18"/><path d="M7 3v4"/><path d="M17 3v4"/><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M8 11h8"/><path d="M8 15h5"/></svg>"""
    val recCheckIco = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>"""
    val scheduledSessions = sessions.filter { it.status == "scheduled" || it.status == "inProgress" }.take(50)
    val freeWindows = windows.filter { it.status == "free" }
    val assignedList = if (scheduledSessions.isEmpty()) {
        """<div class="sd-rec-empty"><div class="sd-rec-empty-ico">📋</div><div>Нет запланированных занятий</div></div>"""
    } else {
        scheduledSessions.joinToString("") { s ->
            val cadetName = cadets.find { it.id == s.cadetId }?.fullName?.takeIf { it.isNotBlank() }?.let { formatShortName(it) } ?: "Курсант"
            val cadetNameEsc = cadetName.escapeHtml()
            val avatarLetter = cadetName.firstOrNull()?.uppercaseChar()?.toString() ?: "К"
            val recCadet = cadets.find { it.id == s.cadetId }
            val recAvatarHtml = avatarBlockHtml("sd-rec-scard-avatar", recCadet, user.id, avatarLetter)
            val dateStr = formatDateOnly(s.startTimeMillis)
            val timeStr = formatTimeOnly(s.startTimeMillis)
            val weekdayShort = weekdayNameShortRu(s.startTimeMillis)
            val bookedByCadet = s.openWindowId.isNotBlank()
            val statusText = when {
                bookedByCadet && !s.instructorConfirmed -> "Ожидает вашего подтверждения ($cadetNameEsc)"
                !s.instructorConfirmed -> "Ожидает подтверждения курсантом ($cadetNameEsc)"
                else -> "Запись подтверждена ($cadetNameEsc)"
            }
            val statusClass = when {
                bookedByCadet && !s.instructorConfirmed -> "sd-rec-status-waiting"
                !s.instructorConfirmed -> "sd-rec-status-pending"
                else -> "sd-rec-status-confirmed"
            }
            val showConfirm = bookedByCadet && !s.instructorConfirmed
            val confirmBtn = if (showConfirm) """<button type="button" class="sd-rec-confirm-btn sd-recording-confirm-session" data-session-id="${s.id.escapeHtml()}">$recCheckIco Подтвердить</button>""" else ""
            """<div class="sd-rec-session-card">
                $recAvatarHtml
                <div class="sd-rec-scard-body">
                    <div class="sd-rec-scard-name">${cadetName.escapeHtml()}</div>
                    <div class="sd-rec-scard-dt">
                        <span class="sd-rec-dt-pill">$recWeekdayIco $weekdayShort • $recCalendarIco $dateStr</span>
                        <span class="sd-rec-dt-pill">$recClockIco $timeStr</span>
                    </div>
                    <span class="sd-rec-scard-status $statusClass">$statusText</span>
                </div>
                <div class="sd-rec-scard-actions">
                    $confirmBtn
                    <button type="button" class="sd-rec-cancel-btn sd-recording-cancel-session" data-session-id="${s.id.escapeHtml()}">Отменить</button>
                </div>
            </div>"""
        }
    }
            val freeWindowsList = if (freeWindows.isEmpty()) {
        """<div class="sd-rec-empty"><div class="sd-rec-empty-ico">🕐</div><div>Нет свободных окон</div></div>"""
    } else {
        freeWindows.joinToString("") { w ->
            val dateStr = formatDateOnly(w.dateTimeMillis)
            val timeStr = formatTimeOnly(w.dateTimeMillis)
            val weekdayShort = weekdayNameShortRu(w.dateTimeMillis)
            """<div class="sd-rec-window-card">
                <div class="sd-rec-window-avatar"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
                <div class="sd-rec-scard-body">
                    <span class="sd-rec-window-status">Свободно</span>
                    <div class="sd-rec-scard-dt">
                        <span class="sd-rec-dt-pill sd-rec-dt-pill-purple">$recWeekdayIco $weekdayShort • $recCalendarIco $dateStr</span>
                        <span class="sd-rec-dt-pill sd-rec-dt-pill-purple">$recClockIco $timeStr</span>
                    </div>
                </div>
                <div class="sd-rec-scard-actions">
                    <button type="button" class="sd-rec-chip-del" data-window-id="${w.id.escapeHtml()}"><svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg> Удалить</button>
                </div>
            </div>"""
        }
    }
    return """$loadingLine
                <div class="sd-rec-stats-bar">
                    <div class="sd-rec-stat-card sd-rec-stat-blue">
                        <span class="sd-rec-stat-val">${scheduledSessions.size}</span>
                        <span class="sd-rec-stat-label">Занятий запланировано</span>
                    </div>
                    <div class="sd-rec-stat-card sd-rec-stat-purple">
                        <span class="sd-rec-stat-val">${freeWindows.size}</span>
                        <span class="sd-rec-stat-label">Свободных окон</span>
                    </div>
                </div>
                <div class="sd-rec-section">
                    <div class="sd-rec-section-hdr">
                        <span class="sd-rec-section-ico sd-rec-ico-blue">$recCalendarIco</span>
                        <span class="sd-rec-section-ttl">Записанные на вождение</span>
                    </div>
                    <div class="sd-rec-cards">$assignedList</div>
                </div>
                <div class="sd-rec-section">
                    <div class="sd-rec-section-hdr">
                        <span class="sd-rec-section-ico sd-rec-ico-purple">$recClockIco</span>
                        <span class="sd-rec-section-ttl">Свободные окна</span>
                    </div>
                    <div class="sd-rec-windows">$freeWindowsList</div>
                </div>
            <div class="sd-modal-overlay sd-hidden" id="sd-rec-delete-window-confirm-modal"><div class="sd-modal"><h3 class="sd-modal-title">Вы уверены?</h3><p class="sd-modal-actions"><button type="button" id="sd-rec-delete-window-yes" class="sd-btn sd-btn-primary">Да</button><button type="button" id="sd-rec-delete-window-no" class="sd-btn sd-btn-secondary">Нет</button></p></div></div>"""
}

/** Формы «Записать курсанта» / «Добавить окно» — стабильный DOM, не пересоздаётся при опросе сессий. */
private fun renderInstructorRecordingFormsPanelHtml(): String {
    val cadets = appState.instructorCadets
    val bookCadetId = appState.instructorRecordingBookCadetId
    val bookDtLocal = appState.instructorRecordingBookDatetimeLocal
    val addDtLocal = appState.instructorRecordingAddDatetimeLocal
    val dtMinEsc = getDatetimeLocalMin().escapeHtml()
    val recCalendarIco = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>"""
    val recClockIco = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>"""
    val recUserPlusIco = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>"""
    val recPlusIco = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>"""
    val cadetOptions = cadets.sortedBy { it.fullName }.joinToString("") { c ->
        val optSel = if (bookCadetId == c.id) " selected" else ""
        """<option value="${c.id.escapeHtml()}"$optSel>${formatShortName(c.fullName).escapeHtml()}</option>"""
    }
    val bookCadetSelectClass = if (!bookCadetId.isNullOrBlank()) " sd-has-cadet" else ""
    val bookDtAttr = if (bookDtLocal.isNotBlank()) """ value="${bookDtLocal.escapeHtml()}"""" else ""
    val addDtAttr = if (addDtLocal.isNotBlank()) """ value="${addDtLocal.escapeHtml()}"""" else ""
    return """<div class="sd-rec-forms-row">
                    <div class="sd-rec-form-card">
                        <div class="sd-rec-form-hdr">
                            <span class="sd-rec-section-ico sd-rec-ico-blue">$recUserPlusIco</span>
                            <span class="sd-rec-section-ttl">Записать курсанта</span>
                        </div>
                        <div class="sd-rec-picker-wrap">
                            <span class="sd-rec-picker-ico sd-rec-picker-ico-user">$recUserPlusIco</span>
                            <select id="sd-recording-book-cadet" class="sd-rec-picker-select$bookCadetSelectClass"><option value="">Выберите курсанта…</option>$cadetOptions</select>
                            <span class="sd-rec-picker-chevron"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"/></svg></span>
                        </div>
                        <div class="sd-rec-picker-wrap" lang="ru">
                            <span class="sd-rec-picker-ico sd-rec-picker-ico-cal">$recCalendarIco</span>
                            <input type="datetime-local" id="sd-recording-book-dt" class="sd-rec-picker-input" min="$dtMinEsc" step="900"$bookDtAttr />
                        </div>
                        <button type="button" id="sd-recording-book-btn" class="sd-rec-submit sd-rec-submit-blue">
                            <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                            Записать
                        </button>
                    </div>
                    <div class="sd-rec-form-card">
                        <div class="sd-rec-form-hdr">
                            <span class="sd-rec-section-ico sd-rec-ico-purple">$recPlusIco</span>
                            <span class="sd-rec-section-ttl">Добавить окно</span>
                        </div>
                        <div class="sd-rec-picker-wrap" lang="ru">
                            <span class="sd-rec-picker-ico sd-rec-picker-ico-cal sd-rec-picker-ico-purple">$recClockIco</span>
                            <input type="datetime-local" id="sd-recording-add-dt" class="sd-rec-picker-input sd-rec-picker-input-purple" min="$dtMinEsc" step="900"$addDtAttr />
                        </div>
                        <button type="button" id="sd-recording-add-btn" class="sd-rec-submit sd-rec-submit-purple">
                            <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
                            Добавить
                        </button>
                    </div>
                </div>"""
}

private fun renderRecordingTabContent(user: User): String {
    val loading = appState.recordingLoading
    val windows = appState.recordingOpenWindows
    val sessions = appState.recordingSessions
    val loadingLine = if (loading) """<div class="sd-rec-loading">Загрузка… <button type="button" id="sd-stop-loading" class="sd-btn sd-btn-small sd-btn-secondary">Показать пусто</button></div>""" else ""

    val recCalendarIco = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>"""
    val recClockIco = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>"""
    val recCheckIco = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>"""
    val recCarIco = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 17H3a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v5a2 2 0 0 1-2 2h-2"/><circle cx="7" cy="17" r="2"/><circle cx="17" cy="17" r="2"/></svg>"""

    return when (user.role) {
        "instructor" -> {
            """<div class="sd-rec-wrap">
                <div id="sd-instructor-rec-dynamic">${renderInstructorRecordingDynamicPanelHtml(user)}</div>
                <div id="sd-instructor-rec-forms-stable">${renderInstructorRecordingFormsPanelHtml()}</div>
            </div>"""
        }
        "cadet" -> {
            val myScheduled = sessions.filter { it.status == "scheduled" }.take(10)
            val recWeekdayIco = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 7h18"/><path d="M7 3v4"/><path d="M17 3v4"/><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M8 11h8"/><path d="M8 15h5"/></svg>"""

            val slotsHtml = if (windows.isEmpty()) {
                """<div class="sd-rec-empty"><div class="sd-rec-empty-ico">🗓️</div><div>Нет свободных окон инструктора</div></div>"""
            } else {
                windows.joinToString("") { w ->
                    val dateStr = formatDateOnly(w.dateTimeMillis)
                    val timeStr = formatTimeOnly(w.dateTimeMillis)
                    val weekdayShort = weekdayNameShortRu(w.dateTimeMillis)
                    """<div class="sd-rec-slot-card">
                        <div class="sd-rec-slot-avatar" aria-hidden="true">$recClockIco</div>
                        <div class="sd-rec-slot-body">
                            <div class="sd-rec-slot-title">Свободное окно</div>
                            <div class="sd-rec-slot-dt">
                                <span class="sd-rec-dt-pill sd-rec-slot-pill">$recWeekdayIco $weekdayShort • $recCalendarIco $dateStr</span>
                                <span class="sd-rec-dt-pill sd-rec-slot-pill">$recClockIco $timeStr</span>
                            </div>
                        </div>
                        <button type="button" class="sd-rec-book-slot-btn" data-window-id="${w.id.escapeHtml()}">Забронировать</button>
                    </div>"""
                }
            }

            val recStatusCheckIco = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="20 6 9 17 4 12"/></svg>"""
            val recStatusClockIco = """<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>"""
            val myRecords = if (myScheduled.isEmpty()) {
                """<div class="sd-rec-empty"><div class="sd-rec-empty-ico">📝</div><div>Нет запланированных занятий</div></div>"""
            } else {
                myScheduled.joinToString("") { s ->
                    val bookedByCadet = s.openWindowId.isNotBlank()
                    val needConfirm = !bookedByCadet && !s.instructorConfirmed
                    val statusText = when {
                        s.instructorConfirmed -> "Запись подтверждена"
                        !bookedByCadet -> "Инструктор записал вас — подтвердите"
                        else -> "Запланировано"
                    }
                    val statusClass = if (s.instructorConfirmed) "sd-rec-status-confirmed" else "sd-rec-status-pending"
                    val statusIco = if (s.instructorConfirmed) recStatusCheckIco else recStatusClockIco
                    val confirmBtn = if (needConfirm) """<button type="button" class="sd-rec-confirm-btn sd-recording-confirm-session" data-session-id="${s.id.escapeHtml()}">$recCheckIco Подтвердить</button>""" else ""
                    val cancelBtn = """<button type="button" class="sd-rec-cancel-btn sd-recording-cancel-session" data-session-id="${s.id.escapeHtml()}">Отменить</button>"""
                    val dateStr = formatDateOnly(s.startTimeMillis)
                    val timeStr = formatTimeOnly(s.startTimeMillis)
                    val weekdayShort = weekdayNameShortRu(s.startTimeMillis)
                    """<div class="sd-rec-my-record">
                        <div class="sd-rec-my-record-avatar" aria-hidden="true">$recCarIco</div>
                        <div class="sd-rec-my-record-body">
                            <div class="sd-rec-my-record-title">Вождение</div>
                            <div class="sd-rec-my-dt">
                                <span class="sd-rec-dt-pill">$recWeekdayIco $weekdayShort • $recCalendarIco $dateStr</span>
                                <span class="sd-rec-dt-pill">$recClockIco $timeStr</span>
                            </div>
                            <span class="sd-rec-my-record-status $statusClass">$statusIco $statusText</span>
                        </div>
                        <div class="sd-rec-my-record-actions">$confirmBtn $cancelBtn</div>
                    </div>"""
                }
            }

            """$loadingLine<div class="sd-rec-wrap">
                <div class="sd-rec-stats-bar">
                    <div class="sd-rec-stat-card sd-rec-stat-blue">
                        <span class="sd-rec-stat-val">${myScheduled.size}</span>
                        <span class="sd-rec-stat-label">Моих занятий</span>
                    </div>
                    <div class="sd-rec-stat-card sd-rec-stat-green">
                        <span class="sd-rec-stat-val">${windows.size}</span>
                        <span class="sd-rec-stat-label">Свободных окон инструктора</span>
                    </div>
                </div>
                <div class="sd-rec-section">
                    <div class="sd-rec-section-hdr">
                        <span class="sd-rec-section-ico sd-rec-ico-green">$recCarIco</span>
                        <span class="sd-rec-section-ttl">Свободные окна инструктора</span>
                    </div>
                    <div class="sd-rec-slots">$slotsHtml</div>
                </div>
                <div class="sd-rec-section">
                    <div class="sd-rec-section-hdr">
                        <span class="sd-rec-section-ico sd-rec-ico-blue">$recCarIco</span>
                        <span class="sd-rec-section-ttl">Записан на вождение</span>
                    </div>
                    <div class="sd-rec-my-list">$myRecords</div>
                </div>
            </div>"""
        }
        else -> """<div class="sd-rec-wrap"><div class="sd-rec-section"><p>Доступно инструктору и курсанту.</p></div></div>"""
    }
}

private fun renderHistoryTabContent(user: User): String {
    val loadingLine = if (appState.historyLoading) """<p class="sd-loading-text">Загрузка…</p>""" else ""
    // Safety: не показываем "чужие" записи даже если в state попал общий список.
    val sessions = if (user.role == "cadet") appState.historySessions.filter { it.cadetId == user.id } else appState.historySessions
    val balanceAll = if (user.role == "cadet") appState.historyBalance.filter { it.userId == user.id } else appState.historyBalance
    val balance = balanceAll.take(50)

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
        val users = if (withUser) appState.balanceAdminUsers else emptyList()
        val historyUsers = appState.historyUsers
        val resolveUser = { id: String -> (users.ifEmpty { historyUsers }.find { it.id == id }?.fullName)?.let { formatShortName(it) } ?: "—" }
        val byDate = balance.sortedByDescending { it.timestampMillis ?: 0L }.groupBy { formatDateOnly(it.timestampMillis) }
        val days = byDate.entries.joinToString("") { (date, entries) ->
            val rows = entries.joinToString("") { b ->
                val typeStr = when (b.type) { "credit" -> "+ "; "debit" -> "− "; "set" -> "= "; else -> "" }
                val typeClass = when (b.type) { "credit" -> "sd-hist-credit"; "debit" -> "sd-hist-debit"; else -> "" }
                val whoHtml = if (withUser) """<span class="sd-history-who">${resolveUser(b.userId).escapeHtml()}</span>""" else ""
                val performedByHtml = if (b.performedBy.isNotBlank()) """<span class="sd-history-performed">Кем: ${resolveUser(b.performedBy).escapeHtml()}</span>""" else ""
                """<div class="sd-history-row"><span class="sd-history-time">${formatTimeOnly(b.timestampMillis)}</span>$whoHtml$performedByHtml<span class="sd-history-amount $typeClass">$typeStr${b.amount} тал.</span></div>"""
            }
            historyDayBlock(date, entries.size, rows)
        }
        return """<div class="sd-history-by-date">$days</div>"""
    }

    if (user.role == "admin") {
        return """<h2>История</h2>$loadingLine<div class="sd-block"><h3 class="sd-block-title">Зачисления и списания (${balance.size})</h3>${balanceGroupedHtml(true)}</div>"""
    }

    if (user.role == "instructor") {
        val credits = balance.filter { it.type == "credit" }
        val debits = balance.filter { it.type == "debit" }
        val completedSessions = sessions.filter { it.status == "completed" }.sortedByDescending { it.completedAtMillis ?: it.startTimeMillis ?: 0L }
        val cancelledSessions = sessions.filter { it.status in listOf("cancelledByInstructor", "cancelledByCadet") }.sortedByDescending { it.cancelledAtMillis ?: it.startTimeMillis ?: 0L }
        val historyUsers = appState.historyUsers
        val instructorCadets = appState.instructorCadets
        val instructorName = formatShortName(user.fullName.ifBlank { "—" })
        fun cadetName(cadetId: String) = (instructorCadets.find { it.id == cadetId }?.fullName)?.let { formatShortName(it) } ?: (historyUsers.find { it.id == cadetId }?.fullName)?.let { formatShortName(it) } ?: "—"
        fun performedByName(performedBy: String) = if (performedBy.isBlank()) "—" else (historyUsers.find { it.id == performedBy }?.fullName)?.let { formatShortName(it) } ?: "—"

        val creditsHtml = if (credits.isEmpty()) """<p class="sd-history-empty">Нет зачислений</p>""" else credits.joinToString("") { b ->
            val dt = (b.timestampMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            """<div class="sd-history-balance-card sd-history-credit-card">
                <div class="sd-history-card-icon sd-history-icon-credit">$SD_ICON_CREDIT_SVG</div>
                <div class="sd-history-card-body">
                    <div class="sd-history-balance-head"><span class="sd-history-balance-type">Зачисление</span><span class="sd-history-balance-badge">+${b.amount} талонов</span></div>
                    <div class="sd-history-balance-row">Кем: ${performedByName(b.performedBy).escapeHtml()}</div>
                    <div class="sd-history-balance-row">Дата и время: $dt</div>
                </div>
            </div>"""
        }
        val debitsHtml = if (debits.isEmpty()) """<p class="sd-history-empty">Нет списаний</p>""" else debits.joinToString("") { b ->
            val dt = (b.timestampMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            """<div class="sd-history-balance-card sd-history-debit-card">
                <div class="sd-history-card-icon sd-history-icon-debit">$SD_ICON_DEBIT_SVG</div>
                <div class="sd-history-card-body">
                    <div class="sd-history-balance-head"><span class="sd-history-balance-type">Списание</span><span class="sd-history-balance-badge sd-history-debit-badge">−${b.amount} талонов</span></div>
                    <div class="sd-history-balance-row">Кем: ${performedByName(b.performedBy).escapeHtml()}</div>
                    <div class="sd-history-balance-row">Дата и время: $dt</div>
                </div>
            </div>"""
        }

        val completedCardsHtml = if (completedSessions.isEmpty()) """<p class="sd-history-empty">Нет завершённых занятий</p>""" else completedSessions.joinToString("") { s ->
            val drivingDt = (s.startTimeMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            val completedDt = (s.completedAtMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            val ratingRow = if (s.instructorRating in 3..5) """<div class="sd-history-session-row"><strong>Оценка курсанту:</strong> ${s.instructorRating}</div>""" else ""
            """<div class="sd-history-session-card sd-history-completed-card">
                <div class="sd-history-card-icon sd-history-icon-check">$SD_ICON_CHECK_SVG</div>
                <div class="sd-history-card-body">
                    <div class="sd-history-session-row"><strong>Дата:</strong> $drivingDt</div>
                    <div class="sd-history-session-row"><strong>Курсант:</strong> ${cadetName(s.cadetId).escapeHtml()}</div>
                    <div class="sd-history-session-row"><strong>Инструктор:</strong> $instructorName</div>
                    $ratingRow
                    <div class="sd-history-session-row"><strong>Статус:</strong> <span class="sd-hist-done">Завершен $completedDt</span></div>
                </div>
            </div>"""
        }
        val cancelledCardsHtml = if (cancelledSessions.isEmpty()) """<p class="sd-history-empty">Нет отменённых занятий</p>""" else cancelledSessions.joinToString("") { s ->
            val cancelledDt = (s.cancelledAtMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            val plannedDt = (s.startTimeMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            val statusText = when (s.status) {
                "cancelledByInstructor" -> "отменен инструктором"
                "cancelledByCadet" -> "отменен курсантом"
                else -> "отменен"
            }
            val reason = s.cancelReason.takeIf { it.isNotBlank() } ?: "—"
            val cancelledByLabel = when (s.status) {
                "cancelledByInstructor" -> "Инструктор: ${instructorName}"
                "cancelledByCadet" -> "Курсант: ${cadetName(s.cadetId)}"
                else -> "—"
            }
            """<div class="sd-history-session-card sd-history-cancelled-card">
                <div class="sd-history-card-icon sd-history-icon-close">$SD_ICON_CLOSE_SVG</div>
                <div class="sd-history-card-body">
                    <div class="sd-history-session-row"><strong>Дата отмены:</strong> $cancelledDt</div>
                    <div class="sd-history-session-row"><strong>Запланировано на:</strong> $plannedDt</div>
                    <div class="sd-history-session-row"><strong>Курсант:</strong> ${cadetName(s.cadetId).escapeHtml()}</div>
                    <div class="sd-history-session-row"><strong>Инструктор:</strong> $instructorName</div>
                    <div class="sd-history-session-row"><strong>Статус:</strong> <span class="sd-hist-cancel">$statusText</span></div>
                    <div class="sd-history-session-row"><strong>Кем отменено:</strong> ${cancelledByLabel.escapeHtml()}</div>
                    <div class="sd-history-session-row"><strong>Причина отмены:</strong> ${reason.escapeHtml()}</div>
                </div>
            </div>"""
        }

        val balanceTotal = credits.size + debits.size
        val drivingTotal = completedSessions.size + cancelledSessions.size
        return """<h2>История</h2>$loadingLine
        <details class="sd-block sd-details-block sd-history-card" open><summary class="sd-block-title sd-history-summary"><span class="sd-history-section-icon">$SD_ICON_BALANCE_SVG</span>Баланс ($balanceTotal)</summary>
            <details class="sd-history-sub" open><summary class="sd-history-sub-title"><span class="sd-history-section-icon sd-history-icon-credit">$SD_ICON_CREDIT_SVG</span>Зачисления (${credits.size})</summary><div class="sd-history-sub-list">$creditsHtml</div></details>
            <details class="sd-history-sub"><summary class="sd-history-sub-title"><span class="sd-history-section-icon sd-history-icon-debit">$SD_ICON_DEBIT_SVG</span>Списания (${debits.size})</summary><div class="sd-history-sub-list">$debitsHtml</div></details>
        </details>
        <details class="sd-block sd-details-block sd-history-card" open><summary class="sd-block-title sd-history-summary"><span class="sd-history-section-icon">$SD_ICON_DRIVING_SVG</span>Вождение ($drivingTotal)</summary>
            <details class="sd-history-sub" open><summary class="sd-history-sub-title"><span class="sd-history-section-icon sd-history-icon-check">$SD_ICON_CHECK_SVG</span>Завершенное вождение (${completedSessions.size})</summary><div class="sd-history-session-list">$completedCardsHtml</div></details>
            <details class="sd-history-sub"><summary class="sd-history-sub-title"><span class="sd-history-section-icon sd-history-icon-close">$SD_ICON_CLOSE_SVG</span>Отменённые (${cancelledSessions.size})</summary><div class="sd-history-session-list">$cancelledCardsHtml</div></details>
        </details>"""
    }

    /* Курсант: те же карточки, что у инструктора — Баланс + Занятия (завершённые/отменённые) */
    if (user.role == "cadet") {
        val credits = balance.filter { it.type == "credit" }
        val debits = balance.filter { it.type == "debit" }
        val completedSessions = sessions.filter { it.status == "completed" }.sortedByDescending { it.completedAtMillis ?: it.startTimeMillis ?: 0L }
        val cancelledSessions = sessions.filter { it.status in listOf("cancelledByInstructor", "cancelledByCadet") }.sortedByDescending { it.cancelledAtMillis ?: it.startTimeMillis ?: 0L }
        val historyUsers = appState.historyUsers
        val instFallback = appState.cadetInstructor
        fun nameFromKnownUsers(id: String): String? {
            if (id.isBlank()) return null
            historyUsers.find { it.id == id }?.fullName?.takeIf { it.isNotBlank() }?.let { return formatShortName(it) }
            instFallback?.takeIf { it.id == id }?.fullName?.takeIf { it.isNotBlank() }?.let { return formatShortName(it) }
            return null
        }
        fun performedByName(performedBy: String) = if (performedBy.isBlank()) "—" else (nameFromKnownUsers(performedBy) ?: "—")
        fun instructorName(instructorId: String) = nameFromKnownUsers(instructorId) ?: "—"
        val cadetShortNameSelf = formatShortName(user.fullName.ifBlank { "—" })

        val creditsHtml = if (credits.isEmpty()) """<p class="sd-history-empty">Нет зачислений</p>""" else credits.joinToString("") { b ->
            val dt = (b.timestampMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            """<div class="sd-history-balance-card sd-history-credit-card">
                <div class="sd-history-card-icon sd-history-icon-credit">$SD_ICON_CREDIT_SVG</div>
                <div class="sd-history-card-body">
                    <div class="sd-history-balance-head"><span class="sd-history-balance-type">Зачисление</span><span class="sd-history-balance-badge">+${b.amount} талонов</span></div>
                    <div class="sd-history-balance-row">Кем: Администратор</div>
                    <div class="sd-history-balance-row">Дата и время: $dt</div>
                </div>
            </div>"""
        }
        val debitsHtml = if (debits.isEmpty()) """<p class="sd-history-empty">Нет списаний</p>""" else debits.joinToString("") { b ->
            val dt = (b.timestampMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            """<div class="sd-history-balance-card sd-history-debit-card">
                <div class="sd-history-card-icon sd-history-icon-debit">$SD_ICON_DEBIT_SVG</div>
                <div class="sd-history-card-body">
                    <div class="sd-history-balance-head"><span class="sd-history-balance-type">Списание</span><span class="sd-history-balance-badge sd-history-debit-badge">−${b.amount} талонов</span></div>
                    <div class="sd-history-balance-row">Кем: ${performedByName(b.performedBy).escapeHtml()}</div>
                    <div class="sd-history-balance-row">Дата и время: $dt</div>
                </div>
            </div>"""
        }
        val completedCardsHtml = if (completedSessions.isEmpty()) """<p class="sd-history-empty">Нет завершённых занятий</p>""" else completedSessions.joinToString("") { s ->
            val drivingDt = (s.startTimeMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            val completedDt = (s.completedAtMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            val ratingRow = if (s.instructorRating in 3..5) """<div class="sd-history-session-row"><strong>Оценка:</strong> ${s.instructorRating}</div>""" else ""
            """<div class="sd-history-session-card sd-history-completed-card">
                <div class="sd-history-card-icon sd-history-icon-check">$SD_ICON_CHECK_SVG</div>
                <div class="sd-history-card-body">
                    <div class="sd-history-session-row"><strong>Дата:</strong> $drivingDt</div>
                    <div class="sd-history-session-row"><strong>Инструктор:</strong> ${instructorName(s.instructorId).escapeHtml()}</div>
                    $ratingRow
                    <div class="sd-history-session-row"><strong>Статус:</strong> <span class="sd-hist-done">Завершен $completedDt</span></div>
                </div>
            </div>"""
        }
        val cancelledCardsHtml = if (cancelledSessions.isEmpty()) """<p class="sd-history-empty">Нет отменённых занятий</p>""" else cancelledSessions.joinToString("") { s ->
            val cancelledDt = (s.cancelledAtMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            val plannedDt = (s.startTimeMillis?.takeIf { it > 0 }?.let { formatMessageDateTime(it) }) ?: "—"
            val statusText = when (s.status) {
                "cancelledByInstructor" -> "отменен инструктором"
                "cancelledByCadet" -> "отменен курсантом"
                else -> "отменен"
            }
            val reason = s.cancelReason.takeIf { it.isNotBlank() } ?: "—"
            val cancelledByLabel = when (s.status) {
                "cancelledByInstructor" -> "Инструктор: ${instructorName(s.instructorId)}"
                "cancelledByCadet" -> "Курсант: $cadetShortNameSelf"
                else -> "—"
            }
            """<div class="sd-history-session-card sd-history-cancelled-card">
                <div class="sd-history-card-icon sd-history-icon-close">$SD_ICON_CLOSE_SVG</div>
                <div class="sd-history-card-body">
                    <div class="sd-history-session-row"><strong>Дата отмены:</strong> $cancelledDt</div>
                    <div class="sd-history-session-row"><strong>Запланировано на:</strong> $plannedDt</div>
                    <div class="sd-history-session-row"><strong>Инструктор:</strong> ${instructorName(s.instructorId).escapeHtml()}</div>
                    <div class="sd-history-session-row"><strong>Статус:</strong> <span class="sd-hist-cancel">$statusText</span></div>
                    <div class="sd-history-session-row"><strong>Кем отменено:</strong> ${cancelledByLabel.escapeHtml()}</div>
                    <div class="sd-history-session-row"><strong>Причина отмены:</strong> ${reason.escapeHtml()}</div>
                </div>
            </div>"""
        }
        val balanceTotal = credits.size + debits.size
        val drivingTotal = completedSessions.size + cancelledSessions.size
        return """<h2>История</h2>$loadingLine
        <details class="sd-block sd-details-block sd-history-card" open><summary class="sd-block-title sd-history-summary"><span class="sd-history-section-icon">$SD_ICON_BALANCE_SVG</span>Баланс ($balanceTotal)</summary>
            <details class="sd-history-sub" open><summary class="sd-history-sub-title"><span class="sd-history-section-icon sd-history-icon-credit">$SD_ICON_CREDIT_SVG</span>Зачисления (${credits.size})</summary><div class="sd-history-sub-list">$creditsHtml</div></details>
            <details class="sd-history-sub"><summary class="sd-history-sub-title"><span class="sd-history-section-icon sd-history-icon-debit">$SD_ICON_DEBIT_SVG</span>Списания (${debits.size})</summary><div class="sd-history-sub-list">$debitsHtml</div></details>
        </details>
        <details class="sd-block sd-details-block sd-history-card" open><summary class="sd-block-title sd-history-summary"><span class="sd-history-section-icon">$SD_ICON_DRIVING_SVG</span>Занятия ($drivingTotal)</summary>
            <details class="sd-history-sub" open><summary class="sd-history-sub-title"><span class="sd-history-section-icon sd-history-icon-check">$SD_ICON_CHECK_SVG</span>Завершенное вождение (${completedSessions.size})</summary><div class="sd-history-session-list">$completedCardsHtml</div></details>
            <details class="sd-history-sub"><summary class="sd-history-sub-title"><span class="sd-history-section-icon sd-history-icon-close">$SD_ICON_CLOSE_SVG</span>Отменённые (${cancelledSessions.size})</summary><div class="sd-history-session-list">$cancelledCardsHtml</div></details>
        </details>"""
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
        val savedResults = getPddTicketSavedResults(catId, ticketName)
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
                if (answeredHere) {
                    when (isCorrectHere) {
                        true -> append(" sd-pdd-question-nav-correct")
                        false -> append(" sd-pdd-question-nav-wrong")
                        else -> { }
                    }
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

private fun renderNotificationsTabContent(user: User): String {
    val fromStorage = loadNotificationsFromStorage(user.id)
    val list = dedupeNotificationsByTextKeepNewest(fromStorage + appState.notifications)
    val listHtml = if (list.isEmpty()) """<p class="sd-notif-empty">Нет уведомлений</p>""" else list.joinToString("") { n ->
        val dt = formatMessageDateTime(n.dateTimeMs).replaceFirst(" ", ", ")
        """<div class="sd-notif-item"><span class="sd-notif-icon" aria-hidden="true">$SD_ICON_NOTIFICATION_SVG</span><div class="sd-notif-body"><div class="sd-notif-dt">$dt</div><div class="sd-notif-text">${n.text.escapeHtml()}</div></div></div>"""
    }
    val countStr = if (list.isNotEmpty()) " (${list.size})" else ""
    return """<div class="sd-notif-screen">
        <div class="sd-notif-header">
            <h2 class="sd-notif-title">Уведомления$countStr</h2>
            <button type="button" id="sd-notif-clear" class="sd-btn sd-btn-icon sd-notif-clear-btn" title="Очистить" aria-label="Очистить"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="22" height="22"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg></button>
        </div>
        <div style="margin: 8px 0 12px 0; display: flex; flex-wrap: wrap; gap: 8px; align-items: center;">
            <button type="button" id="sd-notif-enable-push" class="sd-btn sd-btn-secondary">Включить push</button>
            <button type="button" id="sd-notif-test-push" class="sd-btn sd-btn-secondary">Тест push</button>
        </div>
        <div class="sd-notif-list">$listHtml</div>
        <div class="sd-modal-overlay sd-hidden" id="sd-notif-clear-confirm-modal"><div class="sd-modal"><h3 class="sd-modal-title">Вы уверены?</h3><p class="sd-notif-clear-text">Вся история уведомлений будет удалена.</p><p class="sd-modal-actions"><button type="button" id="sd-notif-clear-yes" class="sd-btn sd-btn-primary">Да</button><button type="button" id="sd-notif-clear-no" class="sd-btn sd-btn-secondary">Нет</button></p></div></div>
        </div>"""
}

/** Вешает обработчики «Очистить» и модалки подтверждения внутри выпадающего блока уведомлений. */
private fun attachNotifDropdownListeners(dropdownEl: org.w3c.dom.Element) {
    dropdownEl.querySelector("#sd-notif-enable-push")?.addEventListener("click", {
        enableWebPushFromUi()
    })
    dropdownEl.querySelector("#sd-notif-test-push")?.addEventListener("click", {
        testPushFromUi()
    })
    dropdownEl.querySelector("#sd-notif-clear")?.addEventListener("click", {
        dropdownEl.querySelector("#sd-notif-clear-confirm-modal")?.unsafeCast<org.w3c.dom.HTMLElement>()?.classList?.remove("sd-hidden")
    })
    dropdownEl.querySelector("#sd-notif-clear-yes")?.addEventListener("click", {
        updateState { notifications = emptyList() }
        appState.user?.id?.let { saveNotificationsToStorage(it, emptyList()) }
        dropdownEl.querySelector("#sd-notif-clear-confirm-modal")?.unsafeCast<org.w3c.dom.HTMLElement>()?.classList?.add("sd-hidden")
    })
    dropdownEl.querySelector("#sd-notif-clear-no")?.addEventListener("click", {
        dropdownEl.querySelector("#sd-notif-clear-confirm-modal")?.unsafeCast<org.w3c.dom.HTMLElement>()?.classList?.add("sd-hidden")
    })
}

/** Fallback: данные аватара из localStorage (для старых сессий). Основной источник — Firestore chatAvatarUrl (URL файла в Firebase Storage). */
private fun getChatAvatarDataUrl(userId: String): String? =
    (window.asDynamic().localStorage?.getItem(CHAT_AVATAR_STORAGE_KEY_PREFIX + userId) as? String)?.takeIf { it.startsWith("data:") }

private fun setChatAvatarDataUrl(userId: String, dataUrl: String?) {
    try {
        if (dataUrl != null) window.asDynamic().localStorage?.setItem(CHAT_AVATAR_STORAGE_KEY_PREFIX + userId, dataUrl)
        else window.asDynamic().localStorage?.removeItem(CHAT_AVATAR_STORAGE_KEY_PREFIX + userId)
    } catch (_: Throwable) { }
}

/** Как в Android: аватар из Firestore (chatAvatarUrl → Storage). Инициалы при отсутствии или ошибке загрузки. */
private fun avatarBlockHtml(wrapperClass: String, u: User?, currentUserId: String?, initialsFallback: String = "?"): String {
    val initials = u?.fullName?.split(" ")?.filter { it.isNotBlank() }?.take(2)?.joinToString("") { it.first().uppercase() }?.ifBlank { null } ?: initialsFallback
    val bg = if (u != null) avatarColorForId(u.id) else "hsl(0,0%,40%)"
    val url = when {
        u == null -> null
        else -> u.chatAvatarUrl?.takeIf { it.isNotBlank() } ?: getChatAvatarDataUrl(u.id)
    }
    val initialsEsc = initials.escapeHtml()
    val userIdEsc = u?.id?.escapeHtml() ?: ""
    return if (url != null)
        """<div class="$wrapperClass sd-avatar-wrap" data-initials="$initialsEsc" data-bg="${bg.escapeHtml()}"><img src="${url.escapeHtml()}" alt="" class="sd-avatar-img" decoding="async" data-user-id="$userIdEsc" /></div>"""
    else
        """<div class="$wrapperClass" style="background:$bg">$initialsEsc</div>"""
}

/** Человекочитаемый размер для статистики Storage. */
private fun formatStorageBytes(bytes: Long): String = when {
    bytes <= 0L -> "0 Б"
    bytes < 1024L -> "$bytes Б"
    bytes < 1048576L -> "${bytes / 1024L} КБ"
    else -> {
        val mb = bytes.toDouble() / 1048576.0
        val r = ((mb * 100.0).toInt().toDouble() / 100.0)
        "$r МБ"
    }
}

/** Мегабайты для шкалы заполненности бакета (два знака после запятой). */
private fun formatStorageMegabytesOnly(bytes: Long): String {
    val mb = bytes.toDouble() / 1048576.0
    val rounded = kotlin.math.round(mb * 100.0) / 100.0
    return if (rounded == rounded.toLong().toDouble()) "${rounded.toLong()}" else "$rounded"
}

/** Килобайты (целое число, 1 КБ = 1024 Б) для подписи в скобках. */
private fun formatStorageKilobytesOnly(bytes: Long): String =
    if (bytes <= 0L) "0" else "${bytes / 1024L}"

/** Максимум шкалы «заполненности» в настройках админа (5 ГБ). */
private val sdStorageBucketScaleMaxBytes: Long = 5L * 1024L * 1024L * 1024L

/** Доля заполнения шкалы 5 ГБ для заданного объёма (байты). */
private fun storageBucketFillPercentOf5Gb(bytes: Long): Double {
    val used = bytes.toDouble()
    val max = sdStorageBucketScaleMaxBytes.toDouble()
    val p = (used / max) * 100.0
    return if (p > 100.0) 100.0 else p
}

/** Подраздел «Инструктор» / «Курсант»: МБ (КБ) и шкала до 5 ГБ. */
private fun storageBucketSubSectionHtml(title: String, bytes: Long, variantClass: String): String {
    val p = storageBucketFillPercentOf5Gb(bytes)
    val pctStr = kotlin.math.round(p * 10.0) / 10.0
    val titleEsc = title.escapeHtml()
    val mbLine = "${formatStorageMegabytesOnly(bytes)} МБ (${formatStorageKilobytesOnly(bytes)} КБ)"
    return """<div class="sd-admin-storage-bucket-sub $variantClass">
          <div class="sd-admin-storage-bucket-sub-head">$titleEsc</div>
          <p class="sd-admin-storage-bucket-sub-mb">$mbLine</p>
          <div class="sd-admin-storage-bucket-bar-wrap" role="presentation">
            <div class="sd-admin-storage-bucket-bar sd-admin-storage-bucket-bar-sub">
              <div class="sd-admin-storage-bucket-bar-fill sd-admin-storage-bucket-bar-fill-sub" style="width:${pctStr}%"></div>
            </div>
          </div>
        </div>"""
}

/** Одна карточка контакта в блоке очистки Storage (настройки чата админа). */
private fun adminStorageContactRowHtml(
    c: User,
    statsMap: Map<String, ChatContactStorageStats>,
    loading: Boolean,
): String {
    val st = statsMap[c.id]
    val voiceLine = when {
        loading -> "…"
        st != null -> "${st.voiceFileCount} шт. · ${formatStorageMegabytesOnly(st.voiceTotalBytes)} МБ (${formatStorageKilobytesOnly(st.voiceTotalBytes)} КБ)"
        else -> "—"
    }
    val fileLine = when {
        loading -> "…"
        st != null ->
            if (st.chatFileCount <= 0 && st.chatFileTotalBytes <= 0L) {
                "нет файлов"
            } else {
                "${st.chatFileCount} шт. · ${formatStorageMegabytesOnly(st.chatFileTotalBytes)} МБ (${formatStorageKilobytesOnly(st.chatFileTotalBytes)} КБ)"
            }
        else -> "—"
    }
    val avatarLine = when {
        loading -> "…"
        st != null -> if (st.avatarBytes > 0L) "1 файл · ${formatStorageBytes(st.avatarBytes)}" else "нет файла"
        else -> "—"
    }
    val nameShort = formatShortName(c.fullName).escapeHtml()
    val voiceDisabled = loading || st == null || st.voiceFileCount <= 0
    val filesDisabled = loading || st == null || (st.chatFileCount <= 0 && st.chatFileTotalBytes <= 0L)
    val avatarDisabled = loading || st == null || st.avatarBytes <= 0L
    val voiceDis = if (voiceDisabled) " disabled" else ""
    val filesDis = if (filesDisabled) " disabled" else ""
    val avatarDis = if (avatarDisabled) " disabled" else ""
    return """<div class="sd-admin-storage-row" data-storage-user-id="${c.id.escapeHtml()}">
            <div class="sd-admin-storage-contact-name">${formatShortName(c.fullName).escapeHtml()}</div>
            <ul class="sd-admin-storage-lines">
              <li class="sd-admin-storage-line">
                <span class="sd-admin-storage-line-main"><span class="sd-admin-storage-label">Голосовое сообщение:</span> $voiceLine</span>
                <button type="button" class="sd-chat-create-group-btn sd-accent-pill-light sd-admin-storage-clear-btn"$voiceDis data-clear-kind="voice" data-contact-id="${c.id.escapeHtml()}" data-contact-name="$nameShort">Очистить</button>
              </li>
              <li class="sd-admin-storage-line">
                <span class="sd-admin-storage-line-main"><span class="sd-admin-storage-label">Файлы:</span> $fileLine</span>
                <button type="button" class="sd-chat-create-group-btn sd-accent-pill-light sd-admin-storage-clear-btn"$filesDis data-clear-kind="files" data-contact-id="${c.id.escapeHtml()}" data-contact-name="$nameShort">Очистить</button>
              </li>
              <li class="sd-admin-storage-line">
                <span class="sd-admin-storage-line-main"><span class="sd-admin-storage-label">Аватар:</span> $avatarLine</span>
                <button type="button" class="sd-chat-create-group-btn sd-accent-pill-light sd-admin-storage-clear-btn"$avatarDis data-clear-kind="avatar" data-contact-id="${c.id.escapeHtml()}" data-contact-name="$nameShort">Очистить</button>
              </li>
            </ul>
          </div>"""
}

/** Карточка группового чата в блоке очистки Storage (кнопки «Очистить» как у контактов). */
private fun adminStorageGroupRowHtml(
    g: ChatGroup,
    st: ChatGroupStorageStats?,
    loading: Boolean,
): String {
    val voiceLine = when {
        loading -> "…"
        st != null -> "${st.voiceFileCount} шт. · ${formatStorageMegabytesOnly(st.voiceTotalBytes)} МБ (${formatStorageKilobytesOnly(st.voiceTotalBytes)} КБ)"
        else -> "—"
    }
    val fileLine = when {
        loading -> "…"
        st != null ->
            if (st.chatFileCount <= 0 && st.chatFileTotalBytes <= 0L) {
                "нет файлов"
            } else {
                "${st.chatFileCount} шт. · ${formatStorageMegabytesOnly(st.chatFileTotalBytes)} МБ (${formatStorageKilobytesOnly(st.chatFileTotalBytes)} КБ)"
            }
        else -> "—"
    }
    val avatarLine = when {
        loading -> "…"
        st != null -> if (st.avatarBytes > 0L) "1 файл · ${formatStorageBytes(st.avatarBytes)}" else "нет файла"
        else -> "—"
    }
    val nameEsc = g.name.trim().ifBlank { "Группа" }.escapeHtml()
    val nameShort = g.name.trim().ifBlank { "Группа" }.escapeHtml()
    val voiceDisabled = loading || st == null || st.voiceFileCount <= 0
    val filesDisabled = loading || st == null || (st.chatFileCount <= 0 && st.chatFileTotalBytes <= 0L)
    val avatarDisabled = loading || st == null || st.avatarBytes <= 0L
    val voiceDis = if (voiceDisabled) " disabled" else ""
    val filesDis = if (filesDisabled) " disabled" else ""
    val avatarDis = if (avatarDisabled) " disabled" else ""
    return """<div class="sd-admin-storage-row sd-admin-storage-row-group" data-storage-group-id="${g.id.escapeHtml()}">
            <div class="sd-admin-storage-contact-name">$nameEsc</div>
            <ul class="sd-admin-storage-lines">
              <li class="sd-admin-storage-line">
                <span class="sd-admin-storage-line-main"><span class="sd-admin-storage-label">Голосовое сообщение:</span> $voiceLine</span>
                <button type="button" class="sd-chat-create-group-btn sd-accent-pill-light sd-admin-storage-clear-btn"$voiceDis data-clear-kind="group-voice" data-group-id="${g.id.escapeHtml()}" data-group-name="$nameShort">Очистить</button>
              </li>
              <li class="sd-admin-storage-line">
                <span class="sd-admin-storage-line-main"><span class="sd-admin-storage-label">Файлы:</span> $fileLine</span>
                <button type="button" class="sd-chat-create-group-btn sd-accent-pill-light sd-admin-storage-clear-btn"$filesDis data-clear-kind="group-files" data-group-id="${g.id.escapeHtml()}" data-group-name="$nameShort">Очистить</button>
              </li>
              <li class="sd-admin-storage-line">
                <span class="sd-admin-storage-line-main"><span class="sd-admin-storage-label">Аватар группы:</span> $avatarLine</span>
                <button type="button" class="sd-chat-create-group-btn sd-accent-pill-light sd-admin-storage-clear-btn"$avatarDis data-clear-kind="group-avatar" data-group-id="${g.id.escapeHtml()}" data-group-name="$nameShort">Очистить</button>
              </li>
            </ul>
          </div>"""
}

private fun adminStorageGroupSectionHtml(
    groups: List<ChatGroup>,
    groupStatsMap: Map<String, ChatGroupStorageStats>,
    loading: Boolean,
): String {
    val sec = groups.joinToString("") { g ->
        adminStorageGroupRowHtml(g, groupStatsMap[g.id], loading)
    }
    return """<div class="sd-admin-storage-role-section sd-admin-storage-role-section-groups">
    <h4 class="sd-admin-storage-role-section-title">Группа в чате</h4>
    ${if (sec.isBlank()) """<p class="sd-admin-storage-role-empty sd-settings-hint">Нет групповых чатов — создайте группу на вкладке «Чат».</p>""" else """<div class="sd-admin-storage-role-cards">$sec</div>"""}
  </div>"""
}

private fun adminStorageContactsByRoleSectionsHtml(
    contacts: List<User>,
    statsMap: Map<String, ChatContactStorageStats>,
    groups: List<ChatGroup>,
    groupStatsMap: Map<String, ChatGroupStorageStats>,
    loading: Boolean,
): String {
    fun sortKey(u: User) = formatShortName(u.fullName).lowercase()
    val instructors = contacts.filter { it.role == "instructor" }.sortedBy(::sortKey)
    val cadets = contacts.filter { it.role == "cadet" }.sortedBy(::sortKey)
    val other = contacts.filter { it.role != "instructor" && it.role != "cadet" }.sortedBy(::sortKey)
    fun rowsHtml(list: List<User>) = list.joinToString("") { adminStorageContactRowHtml(it, statsMap, loading) }
    val secInstructor = rowsHtml(instructors)
    val secCadet = rowsHtml(cadets)
    val secOther = rowsHtml(other)
    val blockInstructor = """<div class="sd-admin-storage-role-section sd-admin-storage-role-section-instructors">
    <h4 class="sd-admin-storage-role-section-title">Инструкторы</h4>
    ${if (secInstructor.isBlank()) """<p class="sd-admin-storage-role-empty sd-settings-hint">Нет контактов</p>""" else """<div class="sd-admin-storage-role-cards">$secInstructor</div>"""}
  </div>"""
    val blockCadet = """<div class="sd-admin-storage-role-section sd-admin-storage-role-section-cadets">
    <h4 class="sd-admin-storage-role-section-title">Курсанты</h4>
    ${if (secCadet.isBlank()) """<p class="sd-admin-storage-role-empty sd-settings-hint">Нет контактов</p>""" else """<div class="sd-admin-storage-role-cards">$secCadet</div>"""}
  </div>"""
    val blockGroup = adminStorageGroupSectionHtml(groups, groupStatsMap, loading)
    val blockOther = if (secOther.isBlank()) {
        ""
    } else {
        """<div class="sd-admin-storage-role-section sd-admin-storage-role-section-other">
    <h4 class="sd-admin-storage-role-section-title">Другие роли</h4>
    <div class="sd-admin-storage-role-cards">$secOther</div>
  </div>"""
    }
    return blockInstructor + blockCadet + blockGroup + blockOther
}

/** Блок «Очистить Firebase storage» в настройках чата (только админ). */
private fun renderAdminChatStorageBlock(): String {
    val bucketLoading = appState.chatStorageBucketLoading
    val bucketErr = appState.chatStorageBucketError
    val br = appState.chatStorageBucketBreakdown
    val bucketBytes = br?.totalBytes
    val loading = appState.chatStorageStatsLoading
    val bucketPct = when {
        bucketBytes == null || bucketLoading || loading -> 0.0
        else -> storageBucketFillPercentOf5Gb(bucketBytes)
    }
    val bucketPctStr = kotlin.math.round(bucketPct * 10.0) / 10.0
    val bucketMbBlock = when {
        bucketLoading -> """<p class="sd-admin-storage-bucket-mb-wrap"><span class="sd-admin-storage-bucket-mb sd-admin-storage-loading">Обновление: объём бакета и карточки контактов…</span></p>"""
        bucketErr != null -> """<p class="sd-admin-storage-bucket-mb-wrap sd-error">${bucketErr.escapeHtml()}</p>"""
        loading -> """<p class="sd-admin-storage-bucket-mb-wrap"><span class="sd-admin-storage-bucket-mb sd-admin-storage-loading">Обновление: объём бакета и карточки контактов и групп…</span></p>"""
        bucketBytes == null -> """<p class="sd-admin-storage-bucket-mb-wrap sd-settings-hint">Нажмите «Обновить», чтобы загрузить объём бакета (МБ и КБ) и обновить все карточки ниже.</p>"""
        else -> """<p class="sd-admin-storage-bucket-mb-wrap"><span class="sd-admin-storage-bucket-mb">${formatStorageMegabytesOnly(bucketBytes)} МБ (${formatStorageKilobytesOnly(bucketBytes)} КБ)</span></p>"""
    }
    val storageRoleSubsHtml = when {
        bucketErr != null -> ""
        bucketLoading -> """<div class="sd-admin-storage-role-subs-below-refresh"><p class="sd-settings-hint sd-admin-storage-role-placeholder">Обновление данных по инструкторам, курсантам, администраторам и группам…</p></div>"""
        loading -> """<div class="sd-admin-storage-role-subs-below-refresh"><p class="sd-settings-hint sd-admin-storage-role-placeholder">Загрузка заполненности Firebase Storage по инструкторам, курсантам, администраторам и группам…</p></div>"""
        br == null -> """<div class="sd-admin-storage-role-subs-below-refresh"><p class="sd-settings-hint sd-admin-storage-role-placeholder">Нажмите «Обновить данные» или «Обновить» выше — появятся подразделы <strong>Инструктор</strong>, <strong>Курсант</strong>, <strong>Администратор</strong> и <strong>Группа в чате</strong> (МБ и КБ, шкала до 5 ГБ).</p></div>"""
        else -> {
            val subInstructor = storageBucketSubSectionHtml("Инструктор", br.instructorBytes, "sd-admin-storage-bucket-sub-instructor")
            val subCadet = storageBucketSubSectionHtml("Курсант", br.cadetBytes, "sd-admin-storage-bucket-sub-cadet")
            val subAdmin = storageBucketSubSectionHtml("Администратор", br.adminBytes, "sd-admin-storage-bucket-sub-admin")
            val subGroup = storageBucketSubSectionHtml("Группа в чате", br.groupBytes, "sd-admin-storage-bucket-sub-group")
            val otherLine = if (br.otherBytes > 0L) {
                """<p class="sd-admin-storage-bucket-other">Прочее (нераспознанное): ${formatStorageMegabytesOnly(br.otherBytes)} МБ (${formatStorageKilobytesOnly(br.otherBytes)} КБ)</p>"""
            } else {
                ""
            }
            """<div class="sd-admin-storage-bucket-subs sd-admin-storage-role-subs-below-refresh">
            <p class="sd-admin-storage-bucket-subs-lead">По ролям и группам</p>
            $subInstructor
            $subCadet
            $subAdmin
            $subGroup
            $otherLine
            <p class="sd-admin-storage-bucket-role-hint">Личные чаты: голосовые и файлы — пополам между участниками; групповые чаты (комнаты group_…) и аватары групп в Storage — в подразделе «Группа в чате»; папка пользователя в Storage (users/…) — по роли в базе, отдельно показано «Администратор».</p>
          </div>"""
        }
    }
    val bucketUsageHtml = """<div class="sd-admin-storage-bucket-usage">
        <div class="sd-admin-storage-bucket-usage-header">
          <span class="sd-admin-storage-bucket-title">Заполненность Firebase Storage</span>
          <button type="button" id="sd-admin-storage-bucket-refresh" class="sd-chat-create-group-btn sd-accent-pill-light"${if (bucketLoading || loading) " disabled" else ""}>Обновить</button>
        </div>
        $bucketMbBlock
        <div class="sd-admin-storage-bucket-bar-wrap" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${bucketPctStr.toInt().coerceIn(0, 100)}" aria-label="Заполненность относительно 5 ГБ">
          <div class="sd-admin-storage-bucket-bar">
            <div class="sd-admin-storage-bucket-bar-fill" style="width:${bucketPctStr}%"></div>
          </div>
        </div>
        <p class="sd-admin-storage-bucket-scale">Шкала: 0 — 5120 МБ (5 ГБ)</p>
      </div>"""
    val err = appState.chatStorageStatsError
    val statsMap = appState.chatStorageStatsByUserId
    val groupStatsMap = appState.chatGroupStorageStatsByGroupId
    val contacts = appState.chatContacts.sortedBy { formatShortName(it.fullName).lowercase() }
    val groups = appState.chatGroups.sortedBy { it.name.lowercase() }
    val statusLine = when {
        loading -> """<p class="sd-admin-storage-status sd-admin-storage-loading">Загрузка…</p>"""
        err != null -> """<p class="sd-admin-storage-status sd-error">${err.escapeHtml()}</p>"""
        statsMap.isEmpty() && groupStatsMap.isEmpty() && (contacts.isNotEmpty() || groups.isNotEmpty()) ->
            """<p class="sd-admin-storage-status sd-settings-hint">Нажмите «Обновить данные», чтобы загрузить сведения из Storage.</p>"""
        contacts.isEmpty() && groups.isEmpty() -> """<p class="sd-admin-storage-status sd-settings-hint">Нет контактов и групп — откройте вкладку «Чат» и обновите список.</p>"""
        else -> ""
    }
    val contactsListHtml = adminStorageContactsByRoleSectionsHtml(
        contacts,
        statsMap,
        groups,
        groupStatsMap,
        loading,
    )
    val confirmModal = """<div class="sd-modal-overlay sd-hidden" id="sd-admin-storage-confirm-modal" aria-hidden="true">
        <div class="sd-modal sd-admin-storage-confirm-modal">
          <h3 class="sd-modal-title">Вы уверены?</h3>
          <p class="sd-admin-storage-confirm-text" id="sd-admin-storage-confirm-text"></p>
          <p class="sd-modal-actions">
            <button type="button" id="sd-admin-storage-confirm-yes" class="sd-btn sd-btn-primary">Да</button>
            <button type="button" id="sd-admin-storage-confirm-no" class="sd-btn sd-btn-secondary">Нет</button>
          </p>
        </div>
      </div>"""
    return """<div class="sd-settings-block sd-admin-storage-block">
       <h3 class="sd-settings-block-title">Очистить Firebase storage</h3>
       <p class="sd-settings-hint sd-admin-storage-hint">Объёмы в личных чатах с каждым контактом: голосовые (папка Storage и сообщения в чате), вложения файлов в чате и файл аватара пользователя. Ниже — групповые чаты: голосовые, файлы и аватар группы. «Обновить данные» — только пересчёт объёма. «Очистить» у контакта по голосовым удаляет все личные переписки этого пользователя со всеми учётными записями из базы; по аватару — только профиль. «Очистить» у группы — только эту группу.</p>
       $bucketUsageHtml
       <p class="sd-admin-storage-actions"><button type="button" id="sd-admin-storage-stats-refresh" class="sd-chat-create-group-btn sd-accent-pill-light"${if (loading || bucketLoading) " disabled" else ""}>Обновить данные</button></p>
       $storageRoleSubsHtml
       $statusLine
       <div class="sd-admin-storage-list" id="sd-admin-storage-list">$contactsListHtml</div>
       $confirmModal
       </div>"""
}

private fun showAdminStorageClearConfirm(
    kind: String,
    contactId: String?,
    contactDisplayName: String,
    groupId: String? = null,
    groupDisplayName: String? = null,
) {
    adminStoragePendingClearKind = kind
    adminStoragePendingContactId = contactId
    adminStoragePendingGroupId = groupId
    val modal = document.getElementById("sd-admin-storage-confirm-modal") ?: return
    val p = document.getElementById("sd-admin-storage-confirm-text")
    val text = when (kind) {
        "voice" -> "Будут удалены все голосовые сообщения пользователя $contactDisplayName во всех личных чатах (со всеми собеседниками), из базы и из Storage."
        "files" -> "Будут удалены все файлы вложений в вашем личном чате с $contactDisplayName (из базы и из Storage)."
        "avatar" -> "Будет удалён файл аватара пользователя $contactDisplayName из Storage; в профиле аватар сбросится."
        "group-voice" -> "Будут удалены все голосовые сообщения в групповом чате «${groupDisplayName ?: "?"}» из базы и из Storage."
        "group-files" -> "Будут удалены все файлы вложений в групповом чате «${groupDisplayName ?: "?"}» из базы и из Storage."
        "group-avatar" -> "Будет удалён аватар группы «${groupDisplayName ?: "?"}» из Storage; в карточке группы аватар сбросится."
        else -> ""
    }
    p?.textContent = text
    modal.classList.remove("sd-hidden")
    modal.setAttribute("aria-hidden", "false")
}

private fun hideAdminStorageClearConfirm() {
    adminStoragePendingClearKind = null
    adminStoragePendingContactId = null
    adminStoragePendingGroupId = null
    val modal = document.getElementById("sd-admin-storage-confirm-modal") ?: return
    modal.classList.add("sd-hidden")
    modal.setAttribute("aria-hidden", "true")
}

private fun runAdminStorageClearAfterConfirm() {
    val kind = adminStoragePendingClearKind ?: return
    val contactId = adminStoragePendingContactId
    val groupId = adminStoragePendingGroupId
    hideAdminStorageClearConfirm()
    when (kind) {
        "voice" -> {
            val cid = contactId ?: return
            adminClearContactVoice(cid) { err ->
                if (err != null) updateState { networkError = err }
                else {
                    showToast("Голосовые сообщения удалены")
                    refreshAdminChatStorageStats()
                    val uid = appState.user?.id
                    if (uid != null) {
                        getUsersForChat(appState.user!!) { list ->
                            updateState { chatContacts = list }
                        }
                    }
                }
            }
        }
        "files" -> {
            val cid = contactId ?: return
            adminClearContactFiles(cid) { err ->
                if (err != null) updateState { networkError = err }
                else {
                    showToast("Файлы в чате удалены")
                    refreshAdminChatStorageStats()
                    val uid = appState.user?.id
                    if (uid != null) {
                        getUsersForChat(appState.user!!) { list ->
                            updateState { chatContacts = list }
                        }
                    }
                }
            }
        }
        "avatar" -> {
            val cid = contactId ?: return
            adminClearContactAvatar(cid) { err ->
                if (err != null) updateState { networkError = err }
                else {
                    showToast("Аватар удалён")
                    refreshAdminChatStorageStats()
                    val uid = appState.user?.id
                    if (uid != null) {
                        getUsersForChat(appState.user!!) { list ->
                            updateState { chatContacts = list }
                        }
                    }
                }
            }
        }
        "group-voice" -> {
            val gid = groupId ?: return
            adminClearGroupVoice(gid) { err ->
                if (err != null) updateState { networkError = err }
                else {
                    showToast("Голосовые в группе удалены")
                    refreshAdminChatStorageStats()
                    val uid = appState.user?.id
                    val u = appState.user
                    if (uid != null && u != null) {
                        getChatGroupsForUser(uid) { list -> updateState { chatGroups = list } }
                    }
                }
            }
        }
        "group-files" -> {
            val gid = groupId ?: return
            adminClearGroupFiles(gid) { err ->
                if (err != null) updateState { networkError = err }
                else {
                    showToast("Файлы в группе удалены")
                    refreshAdminChatStorageStats()
                    val uid = appState.user?.id
                    if (uid != null) {
                        getChatGroupsForUser(uid) { list -> updateState { chatGroups = list } }
                    }
                }
            }
        }
        "group-avatar" -> {
            val gid = groupId ?: return
            adminClearGroupAvatar(gid) { err ->
                if (err != null) updateState { networkError = err }
                else {
                    showToast("Аватар группы удалён")
                    refreshAdminChatStorageStats()
                    val uid = appState.user?.id
                    if (uid != null) {
                        getChatGroupsForUser(uid) { list -> updateState { chatGroups = list } }
                    }
                }
            }
        }
        else -> Unit
    }
}

/**
 * Перед пересчётом Storage подтягиваем контакты и группы из Firestore.
 * Иначе [AppState.chatGroups] может быть пустым (настройки чата без захода на вкладку «Чат»)
 * — тогда статистика по группам и голосовым в них не запрашивалась.
 */
private fun loadChatListsForAdminStorageThen(
    user: User,
    then: (contactIds: List<String>, groupIds: List<String>) -> Unit,
) {
    val uid = user.id
    getChatGroupsForUser(uid) { groups ->
        getUsersForChat(user) { contacts ->
            updateState { chatGroups = groups; chatContacts = contacts }
            val contactIds = contacts.map { it.id }
            val groupIds = groups.map { it.id }.filter { it.isNotBlank() }
            then(contactIds, groupIds)
        }
    }
}

/** Кнопка «Обновить» в блоке заполненности бакета: объём бакета (МБ и КБ) + пересчёт карточек контактов и групп. */
private fun refreshAdminStorageFull() {
    if (appState.user?.id == null) return
    if (appState.user?.role != "admin") return
    if (appState.chatStorageBucketLoading || appState.chatStorageStatsLoading) return
    val user = appState.user ?: return
    loadChatListsForAdminStorageThen(user) { ids, groupIds ->
        refreshAdminStorageFullWithLists(ids, groupIds)
    }
}

private fun refreshAdminStorageFullWithLists(ids: List<String>, groupIds: List<String>) {
    updateState {
        chatStorageBucketLoading = true
        chatStorageBucketError = null
        if (ids.isNotEmpty() || groupIds.isNotEmpty()) {
            chatStorageStatsLoading = true
            chatStorageStatsError = null
        }
    }
    var pending = 1
    if (ids.isNotEmpty()) pending++
    if (groupIds.isNotEmpty()) pending++
    fun finishChunk() {
        pending--
        if (pending <= 0) {
            updateState {
                chatStorageBucketLoading = false
                chatStorageStatsLoading = false
            }
        }
    }
    getFirebaseStorageBucketUsage { err, breakdown ->
        updateState {
            if (err != null) {
                chatStorageBucketError = err
                chatStorageBucketBreakdown = null
            } else {
                chatStorageBucketError = null
                chatStorageBucketBreakdown = breakdown
            }
        }
        finishChunk()
    }
    if (ids.isEmpty() && groupIds.isEmpty()) {
        updateState {
            chatStorageStatsError = "Нет контактов и групп в чате."
            chatStorageStatsByUserId = emptyMap()
            chatGroupStorageStatsByGroupId = emptyMap()
        }
    } else {
        if (ids.isEmpty()) {
            updateState {
                chatStorageStatsByUserId = emptyMap()
                chatStorageStatsError = null
            }
        } else {
            getAdminChatStorageStats(ids) { err, list ->
                if (err != null) {
                    updateState {
                        chatStorageStatsError = err
                        chatStorageStatsByUserId = emptyMap()
                    }
                } else {
                    val map = list?.associateBy { it.userId } ?: emptyMap()
                    updateState {
                        chatStorageStatsError = null
                        chatStorageStatsByUserId = map
                    }
                }
                finishChunk()
            }
        }
        if (groupIds.isEmpty()) {
            updateState { chatGroupStorageStatsByGroupId = emptyMap() }
        } else {
            getAdminGroupChatStorageStats(groupIds) { err, list ->
                if (err != null) {
                    updateState { chatGroupStorageStatsByGroupId = emptyMap() }
                } else {
                    val map = list?.associateBy { it.groupId } ?: emptyMap()
                    updateState { chatGroupStorageStatsByGroupId = map }
                }
                finishChunk()
            }
        }
    }
}

private fun refreshAdminChatStorageStats() {
    if (appState.user?.id == null) return
    if (appState.user?.role != "admin") return
    if (appState.chatStorageStatsLoading || appState.chatStorageBucketLoading) return
    val user = appState.user ?: return
    loadChatListsForAdminStorageThen(user) { ids, groupIds ->
        refreshAdminChatStorageStatsWithLists(ids, groupIds)
    }
}

private fun refreshAdminChatStorageStatsWithLists(ids: List<String>, groupIds: List<String>) {
    if (ids.isEmpty() && groupIds.isEmpty()) {
        updateState {
            chatStorageStatsError = "Нет контактов и групп в чате."
            chatStorageStatsByUserId = emptyMap()
            chatGroupStorageStatsByGroupId = emptyMap()
        }
        getFirebaseStorageBucketUsage { err, breakdown ->
            updateState {
                if (err != null) {
                    chatStorageBucketError = err
                    chatStorageBucketBreakdown = null
                } else {
                    chatStorageBucketError = null
                    chatStorageBucketBreakdown = breakdown
                }
            }
        }
        return
    }
    updateState { chatStorageStatsLoading = true; chatStorageStatsError = null }
    var pending = 1
    if (ids.isNotEmpty()) pending++
    if (groupIds.isNotEmpty()) pending++
    fun finishChunk() {
        pending--
        if (pending <= 0) updateState { chatStorageStatsLoading = false }
    }
    getFirebaseStorageBucketUsage { err, breakdown ->
        updateState {
            if (err != null) {
                chatStorageBucketError = err
                chatStorageBucketBreakdown = null
            } else {
                chatStorageBucketError = null
                chatStorageBucketBreakdown = breakdown
            }
        }
        finishChunk()
    }
    if (ids.isEmpty()) {
        updateState {
            chatStorageStatsByUserId = emptyMap()
            chatStorageStatsError = null
        }
    } else {
        getAdminChatStorageStats(ids) { err, list ->
            if (err != null) {
                updateState {
                    chatStorageStatsError = err
                    chatStorageStatsByUserId = emptyMap()
                }
            } else {
                val map = list?.associateBy { it.userId } ?: emptyMap()
                updateState {
                    chatStorageStatsError = null
                    chatStorageStatsByUserId = map
                }
            }
            finishChunk()
        }
    }
    if (groupIds.isEmpty()) {
        updateState { chatGroupStorageStatsByGroupId = emptyMap() }
    } else {
        getAdminGroupChatStorageStats(groupIds) { err, list ->
            if (err != null) {
                updateState { chatGroupStorageStatsByGroupId = emptyMap() }
            } else {
                val map = list?.associateBy { it.groupId } ?: emptyMap()
                updateState { chatGroupStorageStatsByGroupId = map }
            }
            finishChunk()
        }
    }
}

private fun renderSettingsTabContent(user: User): String {
    val avatarDataUrl = user.chatAvatarUrl?.takeIf { it.isNotBlank() } ?: getChatAvatarDataUrl(user.id)
    val avatarSection = """<div class="sd-settings-block">
       <h3 class="sd-settings-block-title">Аватар в чате</h3>
       <div class="sd-settings-avatar-wrap">
         <div class="sd-settings-avatar-preview" id="sd-settings-avatar-preview">
           ${if (avatarDataUrl != null) """<img src="${avatarDataUrl.escapeHtml()}" alt="" id="sd-settings-avatar-img" class="sd-settings-avatar-img sd-avatar-img" data-user-id="${user.id.escapeHtml()}" />""" else """<span class="sd-settings-avatar-placeholder" id="sd-settings-avatar-placeholder">${user.fullName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }.escapeHtml()}</span>"""}
         </div>
         <input type="file" id="sd-settings-avatar-file" class="sd-settings-avatar-file" accept="image/*" />
         <label for="sd-settings-avatar-file" class="sd-btn sd-btn-secondary sd-settings-avatar-label">Выбрать фото</label>
         <button type="button" id="sd-settings-avatar-remove" class="sd-btn sd-btn-secondary sd-settings-avatar-remove">Удалить аватар</button>
       </div>
       <div class="sd-avatar-crop-editor sd-hidden" id="sd-avatar-crop-editor">
         <p class="sd-avatar-crop-hint">Перетащите фото и измените масштаб. В круге будет виден аватар.</p>
         <div class="sd-avatar-crop-stage">
           <div class="sd-avatar-crop-overlay" id="sd-avatar-crop-overlay" aria-hidden="true"></div>
           <div class="sd-avatar-crop-frame" id="sd-avatar-crop-frame">
             <img id="sd-avatar-crop-img" class="sd-avatar-crop-img" alt="" draggable="false" />
           </div>
         </div>
         <div class="sd-avatar-crop-controls">
           <div class="sd-avatar-crop-scale-row">
             <span class="sd-avatar-crop-scale-label">Масштаб</span>
             <input type="range" id="sd-avatar-crop-scale" class="sd-avatar-crop-scale" min="0.5" max="2" step="0.05" value="1" title="Масштаб" />
           </div>
           <div class="sd-avatar-crop-buttons">
             <button type="button" id="sd-avatar-crop-cancel" class="sd-btn sd-btn-secondary">Отмена</button>
             <button type="button" id="sd-avatar-crop-apply" class="sd-btn sd-btn-primary">Готово</button>
           </div>
         </div>
       </div>
       </div>"""
    val soundBlock = if (user.role in listOf("cadet", "instructor", "admin")) {
        val checked = getSoundNotificationsEnabled() == true
        val showAllowBtn = checked && !appState.soundAudioUnlocked
        val allowBtnHtml = if (showAllowBtn) """<p style="margin-top:8px"><button type="button" id="sd-allow-sound-settings-btn" class="sd-btn sd-btn-primary">🔊 Разрешить воспроизведение звука</button></p><p class="sd-settings-hint">Браузер требует одно нажатие для доступа к звуку.</p>""" else ""
        val currentSound = getNotificationSoundFilename()
        val soundOptionsHtml = NOTIFICATION_SOUND_OPTIONS.joinToString("") { name ->
            val sel = if (name == currentSound) " selected" else ""
            """<option value="${name.escapeHtml()}"$sel>${name.escapeHtml()}</option>"""
        }
        """<div class="sd-settings-block">
           <h3 class="sd-settings-block-title">Уведомления</h3>
           <label class="sd-settings-checkbox"><input type="checkbox" id="sd-settings-sound-notifications" ${if (checked) "checked" else ""} /> Включить звук уведомлений</label>
           <p style="margin-top:12px">Звук уведомлений:</p>
           <select id="sd-settings-notification-sound" class="sd-input sd-settings-sound-select">$soundOptionsHtml</select>
           <button type="button" id="sd-settings-sound-preview" class="sd-btn sd-btn-icon sd-btn-primary sd-settings-sound-preview-btn" title="Прослушать" aria-label="Прослушать" style="margin-top:8px">$iconPlaySvgWhite</button>
           $allowBtnHtml
           </div>"""
    } else ""
    val adminStorageBlock = if (user.role == "admin") renderAdminChatStorageBlock() else ""
    return """<div class="sd-settings-screen">
       <div class="sd-settings-topbar">
         <h2 class="sd-settings-title">Настройки</h2>
         <button type="button" id="sd-settings-back-to-chat" class="sd-chat-create-group-btn sd-accent-pill-light sd-settings-back-chat-btn" title="К чату" aria-label="Назад к чату">$SD_ICON_BACK_CHEVRON_SVG</button>
       </div>
       $avatarSection
       $soundBlock
       $adminStorageBlock
       </div>"""
}

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
        val roleClass = if (u.role == "instructor") "sd-bcard-instructor" else "sd-bcard-cadet"
        val avatarClass = if (u.role == "instructor") "sd-ucard-avatar-blue" else "sd-ucard-avatar-teal"
        val selectedClass = if (u.id == selectedId) " sd-bcard-selected" else ""
        val bcardAvatarHtml = avatarBlockHtml("sd-ucard-avatar $avatarClass", u, appState.user?.id)
        """<div class="sd-bcard $roleClass$selectedClass">
            $bcardAvatarHtml
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
        val selAvatarHtml = avatarBlockHtml("sd-ucard-avatar $avatarCls", selectedUser, appState.user?.id)
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
                $selAvatarHtml
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
            val userName = users.find { it.id == b.userId }?.fullName?.let { formatShortName(it) } ?: "—"
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

/** Счётчик для вкладки (бейдж, как в Android). */
private fun getTabBadgeCount(tabName: String, user: User): Int {
    return when (tabName) {
        "Главная" -> when (user.role) {
            "instructor" -> {
                val sessions = appState.recordingSessions.filter { it.status == "scheduled" || it.status == "inProgress" || (it.status == "completed" && it.instructorRating == 0) }
                sessions.count { s ->
                    val bookedByCadet = s.openWindowId.isNotBlank()
                    (bookedByCadet && !s.instructorConfirmed) || (s.status == "completed" && s.instructorRating == 0) || (s.status == "scheduled" && s.instructorConfirmed && s.startRequestedByInstructor)
                }
            }
            "cadet" -> {
                val sessions = appState.recordingSessions.filter { it.status == "scheduled" || it.status == "inProgress" }
                sessions.count { s ->
                    val needConfirm = !s.instructorConfirmed && s.openWindowId.isNotBlank()
                    needConfirm || (s.status == "completed" && s.instructorRating > 0 && s.cadetRating == 0)
                }
            }
            else -> 0
        }
        "Запись", "Запись на вождение" -> {
            val raw = when (user.role) {
                "instructor" -> appState.recordingSessions.count { it.status == "scheduled" || it.status == "inProgress" }
                "cadet" -> appState.recordingSessions.count { it.cadetId == user.id && (it.status == "scheduled" || it.status == "inProgress") } + appState.recordingOpenWindows.count { it.status == "free" }
                else -> 0
            }
            (raw - appState.recordingTabBadgeBaseline).coerceAtLeast(0)
        }
        "Чат" -> appState.chatUnreadCounts.values.sum()
        "Расписание" -> {
            if (user.role != "admin") return 0
            val cur = computeAdminScheduleSignature(appState.adminScheduleSessionsByInstructorId)
            if (cur.isEmpty()) return 0
            if (cur != appState.adminScheduleSeenSignature) return 1
            0
        }
        "История", "Баланс", "Билеты" -> 0
        else -> 0
    }
}

/** Возвращает (кнопки вкладок, контент вкладки) для текущего выбора. */
private fun getPanelTabButtonsAndContent(user: User, tabs: List<String>): Pair<String, String> {
    val appInfo = SharedFactory.getAppInfoRepository().getAppInfo()
    val canOpenChatSettingsFromChat = user.role == "instructor" || user.role == "cadet" || user.role == "admin"
    /** Подсветка нижней панели: при открытых настройках из чата активна вкладка «Чат». */
    val selectedForHighlight = when {
        appState.chatSettingsOpen && canOpenChatSettingsFromChat -> {
            val chatIdx = tabs.indexOf("Чат")
            if (chatIdx >= 0) chatIdx else appState.selectedTabIndex.coerceIn(0, tabs.size - 1)
        }
        else -> appState.selectedTabIndex.coerceIn(0, tabs.size - 1)
    }
    val tabIconMap = mapOf(
        "Главная"  to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>""",
        "Баланс"   to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>""",
        "Чат"      to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>""",
        "История"  to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>""",
        "Запись"   to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>""",
        "Расписание" to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/><path d="M8 14h.01"/><path d="M12 14h.01"/><path d="M16 14h.01"/><path d="M8 18h.01"/><path d="M12 18h.01"/><path d="M16 18h.01"/></svg>""",
        "Билеты"   to """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z"/><path d="M13 5v2"/><path d="M13 17v2"/><path d="M13 11v2"/></svg>"""
    )
    val tabButtons = tabs.mapIndexed { i, name ->
        val cls = if (i == selectedForHighlight) "sd-tab sd-active" else "sd-tab"
        val icon = tabIconMap[name] ?: """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg>"""
        val badgeCount = if (i == selectedForHighlight) 0 else getTabBadgeCount(name, user)
        val badgeHtml = if (badgeCount > 0) """<span class="sd-tab-badge">${if (badgeCount > 99) "99+" else badgeCount.toString()}</span>""" else ""
        """<button type="button" class="$cls" data-tab="$i"><span class="sd-tab-icon">$icon</span><span class="sd-tab-label">$name</span>$badgeHtml</button>"""
    }.joinToString("")
    val tabContent = when {
        appState.chatSettingsOpen && canOpenChatSettingsFromChat -> renderSettingsTabContent(user)
        else -> {
            val tabName = tabs[appState.selectedTabIndex.coerceIn(0, tabs.size - 1)]
            when (tabName) {
                "Главная" -> when (user.role) {
                    "admin" -> renderAdminHomeContent()
                    "instructor" -> if (appState.instructorHomeSubView == "profile") {
                        renderInstructorProfileStatsContent(user, appInfo.version)
                    } else {
                        renderInstructorHomeContent(user, appInfo.version)
                    }
                    "cadet" -> if (appState.cadetHomeSubView == "profile") {
                        renderCadetProfileStatsContent(user, appInfo.version)
                    } else {
                        renderCadetHomeContent(user, appInfo.version)
                    }
                    else -> """<h2>$tabName</h2><p>Баланс: ${user.balance} талонов.</p><p>Версия: ${appInfo.version}</p>"""
                }
                "Чат" -> renderChatTabContent(user)
                "Баланс" -> renderBalanceTabContent(user)
                "Расписание" -> if (user.role == "admin") renderAdminScheduleTabContent() else """<h2>Расписание</h2><p>Раздел только для администратора.</p>"""
                "Запись", "Запись на вождение" -> renderRecordingTabContent(user)
                "История" -> renderHistoryTabContent(user)
                "Билеты" -> renderTicketsTabContent()
                "ПДД" -> """<h2>$tabName</h2><p>Правила дорожного движения — полный функционал в приложении.</p><p><a href="https://play.google.com/store/apps/details?id=com.example.startdrive" target="_blank" rel="noopener">Приложение StartDrive</a> · <a href="https://pdd.ru/" target="_blank" rel="noopener">ПДД РФ (pdd.ru)</a></p>"""
                else -> """<h2>$tabName</h2><p>Раздел в разработке.</p>"""
            }
        }
    }
    return Pair(tabButtons, tabContent)
}

private fun getNotificationButtonWrapHtml(): String {
    val unreadCount = (appState.notifications.size - appState.notificationsReadCount).coerceAtLeast(0)
    val showBadge = !appState.notificationsViewOpen && unreadCount > 0
    val badgeHtml = if (showBadge) """<span class="sd-notif-badge">${if (unreadCount > 99) "99+" else unreadCount.toString()}</span>""" else ""
    return """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>$badgeHtml"""
}

/** Классы кнопки уведомлений в шапке (подсветка при непрочитанных). */
private fun notificationButtonHtmlClass(): String {
    val unreadCount = (appState.notifications.size - appState.notificationsReadCount).coerceAtLeast(0)
    val hasUnread = !appState.notificationsViewOpen && unreadCount > 0
    return if (hasUnread) "sd-btn sd-btn-notifications sd-btn-notifications--active" else "sd-btn sd-btn-notifications"
}

/** Модалка подтверждения выхода из аккаунта (кнопка в шапке панели). */
private fun signOutConfirmModalHtml(): String = """
        <div class="sd-modal-overlay sd-hidden" id="sd-signout-confirm-modal" aria-hidden="true">
            <div class="sd-modal sd-signout-confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="sd-signout-confirm-title">
                <h3 class="sd-modal-title" id="sd-signout-confirm-title">Вы уверены?</h3>
                <p class="sd-muted">Вы уверены, что хотите выйти из системы? Можно просто свернуть приложение))</p>
                <p class="sd-modal-actions">
                    <button type="button" class="sd-btn sd-btn-small sd-btn-primary" id="sd-signout-confirm-yes">Да</button>
                    <button type="button" class="sd-btn sd-btn-small sd-btn-secondary" id="sd-signout-confirm-no">Нет</button>
                </p>
            </div>
        </div>
    """

/** Модалка подтверждения очистки истории вождений (вкладка «Расписание» админа). */
private fun adminClearHistoryModalHtml(): String = """
        <div class="sd-modal-overlay sd-hidden" id="sd-admin-clear-history-modal" aria-hidden="true">
            <div class="sd-modal sd-admin-clear-history-dialog" role="dialog" aria-modal="true" aria-labelledby="sd-admin-clear-history-title">
                <h3 class="sd-modal-title" id="sd-admin-clear-history-title">Вы уверены?</h3>
                <p class="sd-muted">Все записи истории вождений этого инструктора будут удалены из базы. Это действие нельзя отменить.</p>
                <p class="sd-modal-actions">
                    <button type="button" class="sd-btn sd-btn-small sd-btn-primary" id="sd-admin-clear-history-yes">Да</button>
                    <button type="button" class="sd-btn sd-btn-small sd-btn-secondary" id="sd-admin-clear-history-no">Нет</button>
                </p>
            </div>
        </div>
    """

private fun renderPanel(user: User, roleTitle: String, tabs: List<String>): String {
    val (tabButtons, tabContent) = getPanelTabButtonsAndContent(user, tabs)
    val notifWrapHtml = getNotificationButtonWrapHtml()
    val notifBtnClass = notificationButtonHtmlClass()
    val adminClearHistoryModal = if (user.role == "admin") adminClearHistoryModalHtml() else ""
    val signOutModal = signOutConfirmModalHtml()
    return """
        <header class="sd-header sd-panel-header">
            <div class="sd-header-text">
                <h1>StartDrive · $roleTitle</h1>
                <p>${formatShortName(user.fullName)} · ${user.email}</p>
            </div>
            <div class="sd-header-actions">
                <button type="button" id="sd-btn-notifications" class="$notifBtnClass" title="Уведомления" aria-label="Уведомления">
                    <span class="sd-btn-notif-wrap">$notifWrapHtml</span>
                </button>
                <button type="button" id="sd-btn-signout" class="sd-btn sd-btn-signout" title="Выйти" aria-label="Выйти">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20" aria-hidden="true">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                        <polyline points="16 17 21 12 16 7"/>
                        <line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                </button>
            </div>
        </header>
        <main class="sd-content">
            <div class="sd-card" id="sd-card">
                $tabContent
            </div>
        </main>
        $adminClearHistoryModal
        $signOutModal
        <nav class="sd-tabs">$tabButtons</nav>
    """.trimIndent()
}

/** Принудительно обновляет контент #sd-card по текущему appState (для панели инструктора/курсанта). */
private fun refreshPanelCardContent(root: org.w3c.dom.Element) {
    val user = appState.user ?: return
    val tabs = when (appState.screen) {
        AppScreen.Admin -> listOf("Главная", "Баланс", "Расписание", "Чат", "История")
        AppScreen.Instructor -> listOf("Главная", "Запись", "Чат", "Билеты", "История")
        else -> listOf("Главная", "Запись", "Чат", "Билеты", "История")
    }
    val (_, tabContent) = getPanelTabButtonsAndContent(user, tabs)
    detachInstructorProfileEarnedCalcMount()
    (root.querySelector("#sd-card") as? org.w3c.dom.Element)?.innerHTML = tabContent
    reattachInstructorProfileEarnedCalcMount(root)
    attachListeners(root)
    appendInstructorCancelReasonModalIfNeeded(root)
    appendInstructorRunningLateModalIfNeeded(root)
    appendInstructorRateCadetModalIfNeeded(root)
    appendRecCancelSessionConfirmModalIfNeeded(root)
}

/** Обновляет только контент вкладки «Билеты» в #sd-card (экзамен/билеты). */
private fun refreshTicketsCardContent(root: org.w3c.dom.Element) {
    val content = renderTicketsTabContent()
    detachInstructorProfileEarnedCalcMount()
    (root.querySelector("#sd-card") as? org.w3c.dom.Element)?.innerHTML = content
    reattachInstructorProfileEarnedCalcMount(root)
    attachListeners(root)
    appendInstructorCancelReasonModalIfNeeded(root)
    appendInstructorRunningLateModalIfNeeded(root)
    appendInstructorRateCadetModalIfNeeded(root)
    appendRecCancelSessionConfirmModalIfNeeded(root)
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
                AppScreen.Instructor -> listOf("Главная", "Запись", "Чат", "Билеты", "История")
                else -> listOf("Главная", "Запись", "Чат", "Билеты", "История")
            }
            val ticketsTabIndex = tabsList.indexOf("Билеты").coerceAtLeast(0)
            updateState {
                selectedTabIndex = ticketsTabIndex
                chatSettingsOpen = false
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
    val networkBanner = appState.networkError?.takeIf { shouldShowTopNetworkBanner(it) }?.let { msg ->
        val friendly = friendlyNetworkError(msg)
        """<div class="sd-network-error" id="sd-network-error"><span>$friendly</span> <button type="button" id="sd-network-retry" class="sd-btn-inline sd-btn-inline-primary">Повторить</button> <button type="button" id="sd-dismiss-network-error" class="sd-btn-inline">Закрыть</button></div>"""
    } ?: ""
    val loadingOverlay = if (appState.loading) """<div class="sd-loading-overlay" id="sd-loading-overlay"><div class="sd-spinner"></div><p>Загрузка…</p></div>""" else ""
    val roleTitle = when (appState.screen) {
        AppScreen.Admin -> "Администратор"
        AppScreen.Instructor -> "Инструктор"
        AppScreen.Cadet -> "Курсант"
        else -> ""
    }
    val tabs = when (appState.screen) {
        AppScreen.Admin -> listOf("Главная", "Баланс", "Расписание", "Чат", "История")
        AppScreen.Instructor -> listOf("Главная", "Запись", "Чат", "Билеты", "История")
        else -> listOf("Главная", "Запись", "Чат", "Билеты", "История")
    }
    val (tabButtons, tabContentFromTabs) = getPanelTabButtonsAndContent(user, tabs)
    val tabContent = if (useExamContent) renderTicketsTabContent() else tabContentFromTabs
    val notifWrapHtml = getNotificationButtonWrapHtml()
    val notifBtnClass = notificationButtonHtmlClass()
    val adminClearHistoryModal = if (appState.screen == AppScreen.Admin) adminClearHistoryModalHtml() else ""
    val signOutModal = signOutConfirmModalHtml()
    val panelHtml = """
        <header class="sd-header sd-panel-header">
            <div class="sd-header-text">
                <h1>StartDrive · $roleTitle</h1>
                <p>${formatShortName(user.fullName)} · ${user.email}</p>
            </div>
            <div class="sd-header-actions">
                <button type="button" id="sd-btn-notifications" class="$notifBtnClass" title="Уведомления" aria-label="Уведомления">
                    <span class="sd-btn-notif-wrap">$notifWrapHtml</span>
                </button>
                <button type="button" id="sd-btn-signout" class="sd-btn sd-btn-signout" title="Выйти" aria-label="Выйти">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20" aria-hidden="true">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                        <polyline points="16 17 21 12 16 7"/>
                        <line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                </button>
            </div>
        </header>
        <main class="sd-content">
            <div class="sd-card" id="sd-card">
                $tabContent
            </div>
        </main>
        $adminClearHistoryModal
        $signOutModal
        <nav class="sd-tabs">$tabButtons</nav>
    """.trimIndent()
    detachInstructorProfileEarnedCalcMount()
    root.innerHTML = networkBanner + loadingOverlay + panelHtml
    if (useExamContent) {
        val card = root.querySelector("#sd-card") as? org.w3c.dom.Element
        if (card != null) {
            detachInstructorProfileEarnedCalcMount()
            card.innerHTML = renderTicketsTabContent()
            reattachInstructorProfileEarnedCalcMount(root)
        }
    }
    attachListeners(root)
    reattachInstructorProfileEarnedCalcMount(root)
    appendInstructorCancelReasonModalIfNeeded(root)
    appendInstructorRunningLateModalIfNeeded(root)
    appendInstructorRateCadetModalIfNeeded(root)
    appendRecCancelSessionConfirmModalIfNeeded(root)
    root.querySelector("#sd-card")?.unsafeCast<dynamic>()?.scrollIntoView(js("({ block: 'start', behavior: 'auto' })"))
}

private fun setupPanelClickDelegation(root: org.w3c.dom.Element) {
    root.addEventListener("click", { e: dynamic ->
        if (appState.screen != AppScreen.Admin && appState.screen != AppScreen.Instructor && appState.screen != AppScreen.Cadet) return@addEventListener
        val target = e?.target as? org.w3c.dom.Element ?: return@addEventListener
        try {
            if (js("(function(el){ return el && el.closest && el.closest('#sd-instr-earned-calc-mount'); })")(target) != null) {
                (e as? org.w3c.dom.events.Event)?.stopPropagation()
                return@addEventListener
            }
        } catch (_: Throwable) {
        }
        if (target.id == "sd-admin-schedule-refresh") {
            (e as? org.w3c.dom.events.Event)?.preventDefault()
            refreshAdminScheduleData()
            return@addEventListener
        }
        if (target.id == "sd-signout-confirm-no") {
            (e as? org.w3c.dom.events.Event)?.preventDefault()
            document.getElementById("sd-signout-confirm-modal")?.let { el ->
                el.classList.add("sd-hidden")
                el.setAttribute("aria-hidden", "true")
            }
            return@addEventListener
        }
        if (target.id == "sd-signout-confirm-yes") {
            (e as? org.w3c.dom.events.Event)?.preventDefault()
            document.getElementById("sd-signout-confirm-modal")?.let { el ->
                el.classList.add("sd-hidden")
                el.setAttribute("aria-hidden", "true")
            }
            signOutAndClearPresence()
            resetInstructorSessionSoundBaseline()
            return@addEventListener
        }
        if (target.id == "sd-signout-confirm-modal") {
            (e as? org.w3c.dom.events.Event)?.preventDefault()
            target.classList.add("sd-hidden")
            target.setAttribute("aria-hidden", "true")
            return@addEventListener
        }
        if (target.id == "sd-admin-clear-history-no") {
            (e as? org.w3c.dom.events.Event)?.preventDefault()
            document.getElementById("sd-admin-clear-history-modal")?.let { el ->
                el.classList.add("sd-hidden")
            }
            return@addEventListener
        }
        if (target.id == "sd-admin-clear-history-yes") {
            (e as? org.w3c.dom.events.Event)?.preventDefault()
            val modal = document.getElementById("sd-admin-clear-history-modal") ?: return@addEventListener
            val iid = modal.getAttribute("data-instructor-id") ?: return@addEventListener
            modal.classList.add("sd-hidden")
            modal.removeAttribute("data-instructor-id")
            val sessions = appState.adminScheduleSessionsByInstructorId[iid] ?: emptyList()
            val ids = sessions.filter {
                it.status == "completed" || it.status == "cancelledByInstructor" || it.status == "cancelledByCadet"
            }.map { it.id }
            if (ids.isEmpty()) {
                showToast("Нет записей для удаления")
                return@addEventListener
            }
            updateState { adminScheduleLoading = true; networkError = null }
            deleteInstructorHistorySessionsSequential(ids) { err ->
                updateState { adminScheduleLoading = false }
                if (err != null) {
                    updateState { networkError = err }
                    showToast(err)
                } else {
                    showToast("История вождений очищена")
                    refreshAdminScheduleData()
                }
            }
            return@addEventListener
        }
        val closestHelperEarly = js("(function(el, sel) { return el && el.closest ? el.closest(sel) : null; })").unsafeCast<(Any?, String) -> Any?>()
        val clearHistBtn = try {
            closestHelperEarly(target, "[data-admin-history-clear]") as? org.w3c.dom.Element
        } catch (_: Throwable) {
            null
        }
        if (clearHistBtn != null && appState.user?.role == "admin") {
            (e as? org.w3c.dom.events.Event)?.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            val iid = clearHistBtn.getAttribute("data-admin-history-clear") ?: return@addEventListener
            document.getElementById("sd-admin-clear-history-modal")?.let { modal ->
                modal.setAttribute("data-instructor-id", iid)
                modal.classList.remove("sd-hidden")
            }
            return@addEventListener
        }
        if (target.id == "sd-admin-clear-history-modal") {
            (e as? org.w3c.dom.events.Event)?.preventDefault()
            target.classList.add("sd-hidden")
            target.removeAttribute("data-instructor-id")
            return@addEventListener
        }
        if (target.id == "sd-allow-sound-settings-btn" || target.id == "sd-allow-sound-btn") {
            (e as? org.w3c.dom.events.Event)?.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            setSoundNotificationsEnabled(true)
            updateState { soundAudioUnlocked = true }
            unlockCadetNotificationAudio()
            if (target.id == "sd-allow-sound-btn") {
                root.querySelectorAll(".sd-allow-sound-bar").let { list ->
                    for (i in 0 until list.length) {
                        list.item(i)?.let { node -> node.parentNode?.removeChild(node) }
                    }
                }
            }
            showToast("Звук уведомлений разрешён")
            return@addEventListener
        }
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
            if (appState.user?.role == "instructor") {
                val delWindowBtn = closest(".sd-rec-chip-del[data-window-id]")
                if (delWindowBtn != null) {
                    (e as? org.w3c.dom.events.Event)?.preventDefault()
                    (e as? org.w3c.dom.events.Event)?.stopPropagation()
                    val wid = delWindowBtn.getAttribute("data-window-id") ?: return@addEventListener
                    recDeleteWindowModalWindowId = wid
                    syncInstructorActionModalsVisibility()
                    return@addEventListener
                }
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
        val instructorBookCadetBtn = closest("[data-instructor-book-cadet]")
        if (instructorBookCadetBtn != null) {
            val cadetId = instructorBookCadetBtn.getAttribute("data-instructor-book-cadet") ?: return@addEventListener
            if (appState.user?.role != "instructor") return@addEventListener
            val recordingBaseline = appState.recordingSessions.count { it.status == "scheduled" || it.status == "inProgress" }
            saveChatScrollForCurrentContact()
            abortChatVoiceMediaBeforeRoomSwitch()
            var instructorMyCadetsOpen = appState.instructorMyCadetsSectionOpen
            val cardEl = document.getElementById("sd-card") as? org.w3c.dom.Element
            if (cardEl != null) {
                val myCadetsDetails = cardEl.querySelector("details[data-instructor-my-cadets]")
                instructorMyCadetsOpen = (myCadetsDetails?.unsafeCast<dynamic>()?.open == true)
            }
            updateState {
                selectedChatContactId = null
                selectedChatGroupId = null
                chatMessages = emptyList()
                chatReplyToMessageId = null
                chatReplyToText = null
                selectedTabIndex = 1
                instructorRecordingBookCadetId = cadetId
                instructorRecordingScrollToBookForm = true
                recordingTabBadgeBaseline = recordingBaseline
                chatSettingsOpen = false
                notificationsViewOpen = false
                instructorMyCadetsSectionOpen = instructorMyCadetsOpen
                resetChatVoiceFieldsForNewRoom()
                chatPlayingVoiceId = null
                chatVoicePlaybackPaused = false
                chatPlayingVoiceCurrentMs = 0
            }
            unsubscribeChat()
            val usrBook = appState.user ?: return@addEventListener
            getUsers { list -> updateState { instructorCadets = list.filter { usrBook.assignedCadets.contains(it.id) } } }
            if (!appState.recordingLoading) {
                updateState { recordingLoading = true }
                val tid = window.setTimeout({ updateState { recordingLoading = false } }, 8000)
                getOpenWindowsForInstructor(usrBook.id) { wins ->
                    getSessionsForInstructor(usrBook.id) { sess ->
                        window.clearTimeout(tid)
                        updateState { recordingOpenWindows = wins; recordingSessions = sess; recordingLoading = false }
                        getUsers { list -> updateState { instructorCadets = list.filter { usrBook.assignedCadets.contains(it.id) } } }
                    }
                }
            }
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
        val addGroupOverlay = if (target.id == "sd-admin-add-group-overlay") target else null
        if (addGroupOverlay != null) {
            updateState { adminAddGroupModalOpen = false; adminEditingGroupId = null }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val addGroupBtn = closest("#sd-admin-add-group-btn")
        if (addGroupBtn != null) {
            updateState { adminAddGroupModalOpen = true; adminEditingGroupId = null }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val addGroupCancel = closest("#sd-admin-add-group-cancel")
        if (addGroupCancel != null) {
            updateState { adminAddGroupModalOpen = false; adminEditingGroupId = null }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val groupEditBtn = closest(".sd-admin-group-edit")
        if (groupEditBtn != null) {
            val gid = groupEditBtn.getAttribute("data-admin-group-edit") ?: return@addEventListener
            updateState { adminEditingGroupId = gid; adminAddGroupModalOpen = true }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val groupDelBtn = closest(".sd-admin-group-delete")
        if (groupDelBtn != null) {
            val gid = groupDelBtn.getAttribute("data-admin-group-delete") ?: return@addEventListener
            if (!window.confirm("Удалить эту группу? Курсанты с этой группой будут от неё отвязаны.")) return@addEventListener
            deleteCadetGroup(gid) { err ->
                if (err != null) updateState { networkError = err }
                else {
                    updateState { networkError = null }
                    refreshCadetGroups()
                    getUsers { list ->
                        updateState { adminHomeUsers = list }
                        refreshAdminHomeAuxiliaryData()
                    }
                }
            }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val addGroupSave = closest("#sd-admin-add-group-save")
        if (addGroupSave != null) {
            val numEl = document.getElementById("sd-admin-group-number") as? HTMLInputElement
            val fromEl = document.getElementById("sd-admin-group-from") as? HTMLInputElement
            val toEl = document.getElementById("sd-admin-group-to") as? HTMLInputElement
            val noDateEl = document.getElementById("sd-admin-group-no-date") as? HTMLInputElement
            val num = numEl?.value?.trim() ?: ""
            val fromS = fromEl?.value?.trim() ?: ""
            val toS = toEl?.value?.trim() ?: ""
            val noDate = noDateEl?.checked == true
            val editId = appState.adminEditingGroupId
            if (num.isBlank()) {
                updateState { networkError = "Укажите № группы" }
            } else if (!noDate) {
                val fromMs = parseIsoDateToMillis(fromS)
                val toMs = parseIsoDateToMillis(toS)
                when {
                    fromMs == null -> updateState { networkError = "Укажите дату «с» в календаре" }
                    toMs == null -> updateState { networkError = "Укажите дату «по» в календаре" }
                    toMs < fromMs -> updateState { networkError = "Дата «по» не может быть раньше даты «с»" }
                    else -> {
                        val done: (String?) -> Unit = { err ->
                            if (err != null) updateState { networkError = err }
                            else {
                                updateState { adminAddGroupModalOpen = false; adminEditingGroupId = null; networkError = null }
                                refreshCadetGroups()
                            }
                        }
                        if (editId != null) updateCadetGroup(editId, num, fromMs, toMs, done) else addCadetGroup(num, fromMs, toMs, done)
                    }
                }
            } else {
                val done: (String?) -> Unit = { err ->
                    if (err != null) updateState { networkError = err }
                    else {
                        updateState { adminAddGroupModalOpen = false; adminEditingGroupId = null; networkError = null }
                        refreshCadetGroups()
                    }
                }
                if (editId != null) updateCadetGroup(editId, num, null, null, done) else addCadetGroup(num, null, null, done)
            }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val cadetGroupPickBtn = closest(".sd-admin-cadet-group-btn")
        if (cadetGroupPickBtn != null) {
            val cid = cadetGroupPickBtn.getAttribute("data-admin-cadet-group-pick") ?: return@addEventListener
            updateState { adminCadetGroupPickerCadetId = cid }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val trainingVehiclePickBtn = closest(".sd-admin-training-vehicle-btn")
        if (trainingVehiclePickBtn != null) {
            val iid = trainingVehiclePickBtn.getAttribute("data-admin-training-vehicle-pick") ?: return@addEventListener
            updateState { adminTrainingVehiclePickerInstructorId = iid }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val pickGroupOverlay = if (target.id == "sd-admin-cadet-group-picker-overlay") target else null
        if (pickGroupOverlay != null) {
            updateState { adminCadetGroupPickerCadetId = null }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val pickGroupCancel = closest("#sd-admin-cadet-group-picker-cancel")
        if (pickGroupCancel != null) {
            updateState { adminCadetGroupPickerCadetId = null }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val pickGroupItem = closest(".sd-admin-pick-group-item")
        if (pickGroupItem != null) {
            val cadetId = appState.adminCadetGroupPickerCadetId ?: return@addEventListener
            val gidRaw = pickGroupItem.getAttribute("data-admin-pick-group")
            val groupId = gidRaw?.takeIf { it.isNotBlank() }
            setUserCadetGroup(cadetId, groupId) { err ->
                if (err != null) updateState { networkError = err }
                else {
                    updateState { adminCadetGroupPickerCadetId = null; networkError = null }
                    getUsers { list ->
                        updateState { adminHomeUsers = list }
                        refreshAdminHomeAuxiliaryData()
                    }
                }
            }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val trainingVehicleOverlay = if (target.id == "sd-admin-training-vehicle-overlay") target else null
        if (trainingVehicleOverlay != null) {
            updateState { adminTrainingVehiclePickerInstructorId = null }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val trainingVehicleCancel = closest("#sd-admin-training-vehicle-cancel")
        if (trainingVehicleCancel != null) {
            updateState { adminTrainingVehiclePickerInstructorId = null }
            e.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            return@addEventListener
        }
        val pickTrainingVehicleBtn = closest(".sd-admin-pick-training-vehicle")
        if (pickTrainingVehicleBtn != null) {
            val instructorId = appState.adminTrainingVehiclePickerInstructorId
                ?: (document.getElementById("sd-admin-training-vehicle-overlay")?.getAttribute("data-instructor-id"))
                ?: return@addEventListener
            val vehicle = pickTrainingVehicleBtn.getAttribute("data-training-vehicle") ?: return@addEventListener
            setInstructorTrainingVehicle(instructorId, vehicle) { err ->
                if (err != null) updateState { networkError = err }
                else {
                    updateState { adminTrainingVehiclePickerInstructorId = null; networkError = null }
                    getUsers { list ->
                        updateState { adminHomeUsers = list }
                        refreshAdminHomeAuxiliaryData()
                    }
                    val uid = appState.user?.id
                    if (uid == instructorId) {
                        getCurrentUser { u, _ -> if (u != null) updateState { user = u } }
                    }
                }
            }
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
            var instructorMyCadetsOpen = appState.instructorMyCadetsSectionOpen
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
            if (appState.user?.role == "instructor" && appState.selectedTabIndex == 0 && idx != 0) {
                val cardEl = document.getElementById("sd-card") as? org.w3c.dom.Element
                if (cardEl != null) {
                    val myCadetsDetails = cardEl.querySelector("details[data-instructor-my-cadets]")
                    instructorMyCadetsOpen = (myCadetsDetails?.unsafeCast<dynamic>()?.open == true)
                }
            }
            val chatTabIdx = getTabsForUserRole(appState.user?.role ?: "").indexOf("Чат").takeIf { it >= 0 } ?: 2
            if (idx != chatTabIdx) {
                saveChatScrollForCurrentContact()
                abortChatVoiceMediaBeforeRoomSwitch()
                updateState {
                    selectedChatContactId = null; selectedChatGroupId = null; chatMessages = emptyList(); chatReplyToMessageId = null; chatReplyToText = null
                    chatAdminCorrespondenceMode = false; chatAdminCorrespondenceSubjectId = null; chatAdminCorrespondencePeerId = null
                    resetChatVoiceFieldsForNewRoom()
                    chatPlayingVoiceId = null
                    chatVoicePlaybackPaused = false
                    chatPlayingVoiceCurrentMs = 0
                }
                unsubscribeChat()
            } else {
                unlockChatNotificationAudio()
            }
            val nOpen = newbiesOpen
            val iOpen = instOpen
            val cOpen = cadetsOpen
            val myCadetsOpenVal = instructorMyCadetsOpen
            val recordingBaseline = if (idx == 1 && (appState.user?.role == "instructor" || appState.user?.role == "cadet")) {
                when (appState.user?.role) {
                    "instructor" -> appState.recordingSessions.count { it.status == "scheduled" || it.status == "inProgress" }
                    "cadet" -> appState.recordingSessions.count { it.cadetId == appState.user?.id && (it.status == "scheduled" || it.status == "inProgress") } + appState.recordingOpenWindows.count { it.status == "free" }
                    else -> 0
                }
            } else null
            if (appState.user?.role == "instructor" && idx != 0) {
                resetInstructorProfileCalculatorCache()
            }
            // Genie exit: clone card as fixed overlay → animate out, switch content instantly underneath
            val sdCardEl = document.getElementById("sd-card")
            fun doTabSwitch() {
                updateState {
                    selectedTabIndex = idx
                    chatSettingsOpen = false
                    notificationsViewOpen = false
                    recordingLoading = false
                    historyLoading = false
                    balanceAdminLoading = false
                    chatContactsLoading = false
                    adminHomeLoading = false
                    adminScheduleLoading = false
                    if (appState.user?.role == "instructor" && idx != 1) {
                        instructorRecordingBookCadetId = null
                        instructorRecordingBookDatetimeLocal = ""
                        instructorRecordingAddDatetimeLocal = ""
                        instructorRecordingScrollToBookForm = false
                    }
                    if (recordingBaseline != null) recordingTabBadgeBaseline = recordingBaseline
                    if (appState.user?.role == "admin" && idx != 0) {
                        adminAddGroupModalOpen = false
                        adminEditingGroupId = null
                        adminCadetGroupPickerCadetId = null
                        adminTrainingVehiclePickerInstructorId = null
                        adminNewbiesSectionOpen = nOpen
                        adminInstructorsSectionOpen = iOpen
                        adminCadetsSectionOpen = cOpen
                    }
                    if (appState.user?.role == "instructor" && idx != 0) {
                        instructorMyCadetsSectionOpen = myCadetsOpenVal
                        instructorHomeSubView = "main"
                        instructorProfileWeekOffset = 0
                    }
                    if (appState.user?.role == "cadet" && idx != 0) {
                        cadetHomeSubView = "main"
                    }
                    if (appState.user?.role == "admin" && idx != chatTabIdx) {
                        clearGroupChatAvatarPending()
                        adminChatGroupModalOpen = false
                        adminChatGroupEditId = null
                        adminChatGroupDeleteConfirmId = null
                    }
                }
            }
            doTabSwitch()
            val user = appState.user ?: return@addEventListener
            val tabName = getTabsForUserRole(user.role).getOrNull(idx) ?: ""
            when (tabName) {
                "Главная" -> if (user.role == "admin" && !appState.adminHomeLoading) {
                    updateState { adminHomeLoading = true; networkError = null }
                    val tid = window.setTimeout({ updateState { adminHomeLoading = false } }, 8000)
                    getUsersWithError { list, err ->
                        window.clearTimeout(tid)
                        updateState {
                            adminHomeUsers = list; balanceAdminUsers = list; adminHomeLoading = false
                            if (err != null) networkError = err
                        }
                        refreshAdminHomeAuxiliaryData()
                    }
                }
                "Баланс" -> if (user.role == "admin" && !appState.balanceAdminLoading) {
                    updateState { balanceAdminLoading = true }
                    val tid = window.setTimeout({ updateState { balanceAdminLoading = false } }, 8000)
                    getUsers { list ->
                        loadBalanceHistoryForUsers(list.map { it.id }) { hist ->
                            window.clearTimeout(tid)
                            updateState { balanceAdminUsers = list; adminHomeUsers = list; balanceAdminHistory = hist; balanceAdminLoading = false }
                        }
                    }
                }
                "Расписание" -> if (user.role == "admin") {
                    updateState { adminScheduleSeenSignature = computeAdminScheduleSignature(appState.adminScheduleSessionsByInstructorId) }
                    if (!appState.adminScheduleLoading) {
                        refreshAdminScheduleData()
                    }
                }
                "Чат" -> {
                    getAppConfigChatShowOtherAvatars { showOther ->
                        updateState { chatShowOtherAvatars = showOther }
                    }
                    // Всегда обновляем контакты при открытии вкладки Чат, чтобы подтянуть актуальные аватары (инструктор/курсант)
                    updateState { chatContactsLoading = true }
                    val chatTid = window.setTimeout({ updateState { chatContactsLoading = false } }, 5000)
                    getUsersForChat(user) { list ->
                        window.clearTimeout(chatTid)
                        updateState { chatContacts = list; chatContactsLoading = false }
                        subscribeChatPresence(list.map { it.id })
                        getChatGroupsForUser(user.id) { groups ->
                            updateState { chatGroups = groups }
                            subscribeChatNotifications(user.id, list)
                        }
                    }
                }
                else -> { }
            }
            // При открытии Главная/Запись всегда обновляем список пользователей (курсанты у инструктора, инструктор у курсанта), чтобы отображались загруженные аватары
            if ((idx == 0 || idx == 1) && user.role == "instructor") {
                getUsers { list -> updateState { instructorCadets = list.filter { user.assignedCadets.contains(it.id) } } }
            }
            if ((idx == 0 || idx == 1) && user.role == "cadet") {
                user.assignedInstructorId?.let { id -> getUserById(id) { inst -> updateState { cadetInstructor = inst } } }
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
                    val prevWindowsCount = appState.recordingOpenWindows.size
                    getOpenWindowsForCadet(instId) { wins ->
                        getSessionsForCadet(user.id) { sess ->
                            window.clearTimeout(tid)
                            updateState { recordingOpenWindows = wins; recordingSessions = sess; recordingLoading = false }
                            if (user.role == "cadet" && wins.size > prevWindowsCount) {
                                val windowMs = wins.minByOrNull { (it.dateTimeMillis ?: Long.MAX_VALUE) }?.dateTimeMillis
                                showCadetNewWindowNotification(windowMs)
                            }
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
                else getUsers { list ->
                    updateState { adminHomeUsers = list }
                    refreshAdminHomeAuxiliaryData()
                }
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
                else getUsers { list ->
                    updateState { adminHomeUsers = list; adminAssignCadetId = null }
                    refreshAdminHomeAuxiliaryData()
                }
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
                else getUsers { list ->
                    updateState { adminHomeUsers = list }
                    refreshAdminHomeAuxiliaryData()
                }
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
                else getUsers { list ->
                    updateState { adminHomeUsers = list }
                    refreshAdminHomeAuxiliaryData()
                }
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
        val corrPeerPick = closest("[data-admin-corr-peer-id]")
        if (corrPeerPick != null && appState.user?.role == "admin") {
            val pid = corrPeerPick.getAttribute("data-admin-corr-peer-id") ?: return@addEventListener
            val subj = appState.chatAdminCorrespondenceSubjectId ?: return@addEventListener
            saveChatScrollForCurrentContact()
            abortChatVoiceMediaBeforeRoomSwitch()
            updateState {
                chatAdminCorrespondencePeerId = pid
                chatMessages = emptyList()
                chatReplyToMessageId = null
                chatReplyToText = null
                resetChatVoiceFieldsForNewRoom()
                chatPlayingVoiceId = null
                chatVoicePlaybackPaused = false
                chatPlayingVoiceCurrentMs = 0
            }
            unsubscribeChat()
            subscribeMessages(chatRoomId(subj, pid)) { list ->
                updateState { chatMessages = list }
                window.setTimeout({ scrollChatToBottom() }, 50)
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val corrSubjectPick = closest("[data-admin-corr-subject-id]")
        if (corrSubjectPick != null && appState.user?.role == "admin") {
            val sid = corrSubjectPick.getAttribute("data-admin-corr-subject-id") ?: return@addEventListener
            saveChatScrollForCurrentContact()
            abortChatVoiceMediaBeforeRoomSwitch()
            updateState {
                chatAdminCorrespondenceSubjectId = sid
                chatAdminCorrespondencePeerId = null
                chatMessages = emptyList()
                chatReplyToMessageId = null
                chatReplyToText = null
                resetChatVoiceFieldsForNewRoom()
                chatPlayingVoiceId = null
                chatVoicePlaybackPaused = false
                chatPlayingVoiceCurrentMs = 0
            }
            unsubscribeChat()
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val chatGroupRow = closest(".sd-chat-group-row")
        if (chatGroupRow != null) {
            val gid = chatGroupRow.getAttribute("data-chat-group-id") ?: return@addEventListener
            val uid = appState.user?.id ?: return@addEventListener
            saveChatScrollForCurrentContact()
            abortChatVoiceMediaBeforeRoomSwitch()
            updateState {
                selectedChatGroupId = gid
                selectedChatContactId = null
                chatMessages = emptyList()
                chatReplyToMessageId = null
                chatReplyToText = null
                chatAdminCorrespondenceMode = false
                chatAdminCorrespondenceSubjectId = null
                chatAdminCorrespondencePeerId = null
                resetChatVoiceFieldsForNewRoom()
                chatPlayingVoiceId = null
                chatVoicePlaybackPaused = false
                chatPlayingVoiceCurrentMs = 0
            }
            clearChatUnreadForGroup(uid, gid)
            unsubscribeChat()
            val roomId = groupChatRoomId(gid)
            subscribeMessages(roomId) { list ->
                val prevSize = appState.chatMessages.size
                updateState { chatMessages = list }
                val toMarkRead = list.filter { it.senderId != uid }.map { it.id }
                if (toMarkRead.isNotEmpty()) markMessagesAsRead(roomId, toMarkRead)
                if (prevSize > 0 && list.size > prevSize && list.lastOrNull()?.senderId != uid) playChatMessageSound()
                list.lastOrNull()?.timestamp?.let { ts -> setChatLastSeenMsForGroup(uid, gid, ts) }
                val gKey = chatUnreadKeyForGroup(gid)
                updateState { if (chatUnreadCounts.containsKey(gKey)) chatUnreadCounts = chatUnreadCounts - gKey }
                window.setTimeout({ scrollChatToBottom() }, 50)
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val chatContact = closest(".sd-chat-contact")
        if (chatContact != null) {
            val contactId = chatContact.getAttribute("data-contact-id") ?: return@addEventListener
            val uid = appState.user?.id ?: return@addEventListener
            saveChatScrollForCurrentContact()
            abortChatVoiceMediaBeforeRoomSwitch()
            updateState {
                selectedChatContactId = contactId
                selectedChatGroupId = null
                chatMessages = emptyList()
                chatReplyToMessageId = null
                chatReplyToText = null
                chatAdminCorrespondenceMode = false
                chatAdminCorrespondenceSubjectId = null
                chatAdminCorrespondencePeerId = null
                resetChatVoiceFieldsForNewRoom()
                chatPlayingVoiceId = null
                chatVoicePlaybackPaused = false
                chatPlayingVoiceCurrentMs = 0
            }
            clearChatUnread(uid, contactId)
            unsubscribeChat()
            subscribeMessages(chatRoomId(uid, contactId)) { list ->
                val prevSize = appState.chatMessages.size
                updateState { chatMessages = list }
                val toMarkRead = list.filter { it.senderId == contactId }.map { it.id }
                if (toMarkRead.isNotEmpty()) markMessagesAsRead(chatRoomId(uid, contactId), toMarkRead)
                if (prevSize > 0 && list.size > prevSize && list.lastOrNull()?.senderId != uid) playChatMessageSound()
                list.lastOrNull()?.timestamp?.let { ts -> setChatLastSeenMs(uid, contactId, ts) }
                updateState { if (chatUnreadCounts.containsKey(contactId)) chatUnreadCounts = chatUnreadCounts - contactId }
                window.setTimeout({ scrollChatToBottom() }, 50)
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val adminOpenChat = closest(".sd-admin-open-chat")
        if (adminOpenChat != null) {
            val contactId = adminOpenChat.getAttribute("data-contact-id") ?: return@addEventListener
            val uid = appState.user?.id ?: return@addEventListener
            saveChatScrollForCurrentContact()
            abortChatVoiceMediaBeforeRoomSwitch()
            updateState {
                selectedTabIndex = chatTabIndexForRole(appState.user?.role)
                chatSettingsOpen = false
                selectedChatContactId = contactId
                selectedChatGroupId = null
                chatMessages = emptyList()
                chatReplyToMessageId = null
                chatReplyToText = null
                chatAdminCorrespondenceMode = false
                chatAdminCorrespondenceSubjectId = null
                chatAdminCorrespondencePeerId = null
                resetChatVoiceFieldsForNewRoom()
                chatPlayingVoiceId = null
                chatVoicePlaybackPaused = false
                chatPlayingVoiceCurrentMs = 0
            }
            clearChatUnread(uid, contactId)
            unsubscribeChat()
            subscribeMessages(chatRoomId(uid, contactId)) { list ->
                val prevSize = appState.chatMessages.size
                updateState { chatMessages = list }
                val toMarkRead = list.filter { it.senderId == contactId }.map { it.id }
                if (toMarkRead.isNotEmpty()) markMessagesAsRead(chatRoomId(uid, contactId), toMarkRead)
                if (prevSize > 0 && list.size > prevSize && list.lastOrNull()?.senderId != uid) playChatMessageSound()
                list.lastOrNull()?.timestamp?.let { ts -> setChatLastSeenMs(uid, contactId, ts) }
                updateState { if (chatUnreadCounts.containsKey(contactId)) chatUnreadCounts = chatUnreadCounts - contactId }
                window.setTimeout({ scrollChatToBottom() }, 50)
            }
            e.preventDefault(); e.stopPropagation()
            return@addEventListener
        }
        val cadetChatBtn = closest(".sd-cadet-chat-btn")
        if (cadetChatBtn != null) {
            val contactId = cadetChatBtn.getAttribute("data-contact-id") ?: return@addEventListener
            val uid = appState.user?.id ?: return@addEventListener
            saveChatScrollForCurrentContact()
            abortChatVoiceMediaBeforeRoomSwitch()
            updateState {
                selectedTabIndex = chatTabIndexForRole(appState.user?.role)
                chatSettingsOpen = false
                selectedChatContactId = contactId
                selectedChatGroupId = null
                chatMessages = emptyList()
                chatReplyToMessageId = null
                chatReplyToText = null
                chatAdminCorrespondenceMode = false
                chatAdminCorrespondenceSubjectId = null
                chatAdminCorrespondencePeerId = null
                resetChatVoiceFieldsForNewRoom()
                chatPlayingVoiceId = null
                chatVoicePlaybackPaused = false
                chatPlayingVoiceCurrentMs = 0
            }
            clearChatUnread(uid, contactId)
            unsubscribeChat()
            subscribeMessages(chatRoomId(uid, contactId)) { list ->
                val prevSize = appState.chatMessages.size
                updateState { chatMessages = list }
                val toMarkRead = list.filter { it.senderId == contactId }.map { it.id }
                if (toMarkRead.isNotEmpty()) markMessagesAsRead(chatRoomId(uid, contactId), toMarkRead)
                if (prevSize > 0 && list.size > prevSize && list.lastOrNull()?.senderId != uid) playChatMessageSound()
                list.lastOrNull()?.timestamp?.let { ts -> setChatLastSeenMs(uid, contactId, ts) }
                updateState { if (chatUnreadCounts.containsKey(contactId)) chatUnreadCounts = chatUnreadCounts - contactId }
                window.setTimeout({ scrollChatToBottom() }, 50)
            }
            e.preventDefault(); e.stopPropagation()
        }
    }, true)
}

// ── Esc закрывает верхний видимый модальный оверлей (.sd-modal-overlay) ──
private fun installGlobalModalEscapeListenerOnce() {
    if (window.asDynamic().__sdModalEscInstalled == true) return
    window.asDynamic().__sdModalEscInstalled = true
    document.addEventListener("keydown", { ev: Event ->
        val key = (ev as? KeyboardEvent)?.key ?: return@addEventListener
        if (key != "Escape") return@addEventListener
        val overlays = document.querySelectorAll(".sd-modal-overlay:not(.sd-hidden)")
        val len = (overlays.length as? Int) ?: 0
        if (len == 0) return@addEventListener
        val last = overlays.item(len - 1) as? org.w3c.dom.HTMLElement ?: return@addEventListener
        val cancel = last.querySelector(".sd-modal-actions button.sd-btn-secondary") as? org.w3c.dom.HTMLElement
            ?: last.querySelector("button.sd-btn-secondary") as? org.w3c.dom.HTMLElement
        if (cancel != null) cancel.click()
        else {
            last.classList.add("sd-hidden")
            last.setAttribute("aria-hidden", "true")
        }
        ev.preventDefault()
        ev.stopPropagation()
    }, true)
}

private fun attachListeners(root: org.w3c.dom.Element) {
    installGlobalModalEscapeListenerOnce()
    syncStartEarlyModalVisibility()
    // Авто-открытие модалки оценки: задаём sessionId до sync, чтобы не скрывать окно и не дублировать setTimeout с reset радио.
    if (appState.user?.role == "instructor" && appState.instructorHomeSubView == "main") {
        val need = appState.recordingSessions.filter { it.status == "completed" && it.instructorRating == 0 }
        if (need.isNotEmpty() && instructorRateCadetModalSessionId.isNullOrBlank()) {
            val first = need.first()
            val cadetName = appState.instructorCadets.find { it.id == first.cadetId }?.fullName?.takeIf { it.isNotBlank() }?.let { formatShortName(it) } ?: "Курсант"
            instructorRateCadetModalSessionId = first.id
            instructorRateCadetModalCadetName = cadetName
            instructorRateModalRadioInitSessionId = null
        }
    }
    syncInstructorActionModalsVisibility()
    ensureInstructorProfileEarnedCalcDelegation()
    setupInstructorRecordingSubmitButtonsDelegationOnce()
    // Один раз: черновик формы «Запись» у инструктора (курсант + даты) — иначе при каждом attachListeners дублировались change
    if (window.asDynamic().__sdInstructorRecordingDraftListeners != true) {
        window.asDynamic().__sdInstructorRecordingDraftListeners = true
        document.body?.addEventListener("change", { e: dynamic ->
            val t = e?.target as? org.w3c.dom.Element ?: return@addEventListener
            when (t.id) {
                "sd-recording-book-cadet" -> {
                    val sel = t.unsafeCast<HTMLSelectElement>()
                    val v = sel.value
                    val newId = if (v.isBlank()) null else v
                    if (newId == appState.instructorRecordingBookCadetId) {
                        if (v.isNotBlank()) sel.classList.add("sd-has-cadet") else sel.classList.remove("sd-has-cadet")
                        return@addEventListener
                    }
                    // Откладываем updateState до следующего тика — иначе перерисовка #sd-card может сбросить открытый список/календарь.
                    window.setTimeout({
                        updateState { instructorRecordingBookCadetId = newId }
                        val el = document.getElementById("sd-recording-book-cadet") as? HTMLSelectElement
                        if (el != null) {
                            if (el.value.isNotBlank()) el.classList.add("sd-has-cadet") else el.classList.remove("sd-has-cadet")
                        }
                    }, 0)
                }
                "sd-recording-book-dt" -> {
                    val inp = t.unsafeCast<HTMLInputElement>()
                    val valStr = inp.value
                    if (valStr == appState.instructorRecordingBookDatetimeLocal) return@addEventListener
                    window.setTimeout({
                        updateState { instructorRecordingBookDatetimeLocal = valStr }
                    }, 0)
                }
                "sd-recording-add-dt" -> {
                    val inp = t.unsafeCast<HTMLInputElement>()
                    val valStr = inp.value
                    if (valStr == appState.instructorRecordingAddDatetimeLocal) return@addEventListener
                    window.setTimeout({
                        updateState { instructorRecordingAddDatetimeLocal = valStr }
                    }, 0)
                }
                else -> {}
            }
        })
    }
    // Один раз: при ошибке загрузки аватара показываем инициалы (как в Android)
    if (window.asDynamic().__sdAvatarErrorBound != true) {
        window.asDynamic().__sdAvatarErrorBound = true
        document.body?.addEventListener("error", { e: dynamic ->
            val target = e?.target
            if (target != null && (target as? org.w3c.dom.Element)?.classList?.contains("sd-avatar-img") == true) {
                val img = target.unsafeCast<org.w3c.dom.HTMLElement>()
                val userId = img.getAttribute("data-user-id")
                val fallback = if (userId != null && userId.isNotBlank()) getChatAvatarDataUrl(userId) else null
                if (fallback != null) {
                    img.setAttribute("src", fallback)
                    (e as? org.w3c.dom.events.Event)?.preventDefault()
                    (e as? org.w3c.dom.events.Event)?.stopPropagation()
                    return@addEventListener
                }
                val wrap = img.closest(".sd-avatar-wrap") as? org.w3c.dom.Element
                if (wrap != null) {
                    val initials = wrap.getAttribute("data-initials") ?: "?"
                    val bg = wrap.getAttribute("data-bg") ?: "hsl(0,0%,40%)"
                    wrap.setAttribute("style", "background:$bg")
                    wrap.innerHTML = initials
                    wrap.classList.remove("sd-avatar-wrap")
                }
            }
        }, true)
    }
    // Один раз: делегирование клика по кнопке воспроизведения голосовых сообщений (на document.body, чтобы работало при любой вкладке)
    if (window.asDynamic().__sdVoicePlayDelegation != true) {
        window.asDynamic().__sdVoicePlayDelegation = true
        document.body?.addEventListener("click", { e: dynamic ->
            val target = (e?.target as? org.w3c.dom.Element) ?: return@addEventListener
            val btn = (target.asDynamic().closest(".sd-voice-play-btn")) as? org.w3c.dom.Element ?: return@addEventListener
            val msgEl = (btn.asDynamic().closest(".sd-msg-voice")) as? org.w3c.dom.Element ?: return@addEventListener
            val msgId = msgEl.getAttribute("data-voice-id") ?: return@addEventListener
            val voiceUrl = msgEl.getAttribute("data-voice-url") ?: return@addEventListener
            val durationSec = (msgEl.getAttribute("data-voice-duration") ?: "0").toIntOrNull() ?: 0
            (e as? org.w3c.dom.events.Event)?.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            val audioEl = msgEl.querySelector(".sd-voice-audio")?.asDynamic()
            toggleVoicePlay(audioEl, msgId, voiceUrl, durationSec)
        }, true)
    }

    // Один раз: админ — очистка голоса/аватара по контакту (настройки чата → Firebase storage)
    if (window.asDynamic().__sdAdminStorageClearUi != true) {
        window.asDynamic().__sdAdminStorageClearUi = true
        document.body?.addEventListener("click", { e: dynamic ->
            val t = e?.target as? org.w3c.dom.Element ?: return@addEventListener
            if (t.closest("#sd-admin-storage-confirm-yes") != null) {
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                (e as? org.w3c.dom.events.Event)?.stopPropagation()
                runAdminStorageClearAfterConfirm()
                return@addEventListener
            }
            if (t.closest("#sd-admin-storage-confirm-no") != null) {
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                (e as? org.w3c.dom.events.Event)?.stopPropagation()
                hideAdminStorageClearConfirm()
                return@addEventListener
            }
            val clearBtn = t.closest("button.sd-admin-storage-clear-btn") as? org.w3c.dom.HTMLButtonElement ?: return@addEventListener
            if (appState.user?.role != "admin") return@addEventListener
            if (clearBtn.disabled) return@addEventListener
            if (clearBtn.closest(".sd-admin-storage-block") == null) return@addEventListener
            val kind = clearBtn.getAttribute("data-clear-kind") ?: return@addEventListener
            (e as? org.w3c.dom.events.Event)?.preventDefault()
            (e as? org.w3c.dom.events.Event)?.stopPropagation()
            if (kind.startsWith("group-")) {
                val gid = clearBtn.getAttribute("data-group-id") ?: return@addEventListener
                val gname = clearBtn.getAttribute("data-group-name") ?: "?"
                showAdminStorageClearConfirm(kind, null, "", gid, gname)
            } else {
                val cid = clearBtn.getAttribute("data-contact-id") ?: return@addEventListener
                val name = clearBtn.getAttribute("data-contact-name") ?: "?"
                showAdminStorageClearConfirm(kind, cid, name)
            }
        }, true)
    }

    installChatVoiceControlsOnce()

    // Один раз: long-press меню на исходящих сообщениях (текст и голосовые)
    if (window.asDynamic().__sdChatMsgMenuDelegation != true) {
        window.asDynamic().__sdChatMsgMenuDelegation = true

        document.body?.addEventListener("pointerdown", { e: dynamic ->
            val target = e?.target as? org.w3c.dom.Element ?: return@addEventListener
            val bubble = target.closest(".sd-msg-me .sd-msg-bubble-wrap") as? org.w3c.dom.Element ?: return@addEventListener
            val msgRoot = bubble.closest(".sd-msg-me") as? org.w3c.dom.Element ?: return@addEventListener
            val msgId = msgRoot.getAttribute("data-msg-id") ?: return@addEventListener
            val fromList = appState.chatMessages.find { it.id == msgId }?.text
            val msgText = fromList ?: (msgRoot.getAttribute("data-msg-text") ?: "")
            val uid = appState.user?.id ?: return@addEventListener
            val roomId = when {
                appState.selectedChatGroupId != null -> groupChatRoomId(appState.selectedChatGroupId!!)
                else -> {
                    val contactId = appState.selectedChatContactId ?: return@addEventListener
                    chatRoomId(uid, contactId)
                }
            }
            if (chatMsgLongPressTimerId != 0) {
                window.clearTimeout(chatMsgLongPressTimerId)
                chatMsgLongPressTimerId = 0
            }
            val getPoint = js("(function(ev){ return { x: (ev && typeof ev.clientX==='number') ? ev.clientX : 0, y: (ev && typeof ev.clientY==='number') ? ev.clientY : 0 }; })")
                .unsafeCast<(dynamic) -> dynamic>()
            val point = getPoint(e)
            val clientX = (point?.x as? Number)?.toDouble() ?: 0.0
            val clientY = (point?.y as? Number)?.toDouble() ?: 0.0
            chatMsgLongPressTimerId = window.setTimeout({
                showChatMessageMenu(msgRoot, clientX + 8.0, clientY + 8.0, uid, roomId, msgId, msgText)
            }, 450).unsafeCast<Int>()
        }, true)

        document.body?.addEventListener("pointerup", { _: dynamic ->
            if (chatMsgLongPressTimerId != 0) {
                window.clearTimeout(chatMsgLongPressTimerId)
                chatMsgLongPressTimerId = 0
            }
        }, true)

        document.body?.addEventListener("pointercancel", { _: dynamic ->
            if (chatMsgLongPressTimerId != 0) {
                window.clearTimeout(chatMsgLongPressTimerId)
                chatMsgLongPressTimerId = 0
            }
        }, true)

        document.body?.addEventListener("click", { e: dynamic ->
            val target = e?.target as? org.w3c.dom.Element ?: return@addEventListener
            if (target.closest("#sd-chat-msg-menu") == null) hideChatMessageMenu()
        }, true)
    }

    if (window.asDynamic().__sdChatFileInAppClick != true) {
        window.asDynamic().__sdChatFileInAppClick = true
        document.body?.addEventListener("click", { e: dynamic ->
            val target = e?.target as? org.w3c.dom.Element ?: return@addEventListener
            val a = target.closest("a.sd-chat-file-in-app") as? org.w3c.dom.HTMLAnchorElement ?: return@addEventListener
            val chatMsgs = document.getElementById("sd-chat-messages") ?: return@addEventListener
            if (!chatMsgs.contains(a)) return@addEventListener
            e.preventDefault()
            e.stopPropagation()
            val url = a.href
            val name = a.getAttribute("data-file-name")?.takeIf { it.isNotBlank() }
                ?: a.getAttribute("download")?.takeIf { it.isNotBlank() }
                ?: a.title?.takeIf { it.isNotBlank() }
                ?: "файл"
            val isImage = a.closest(".sd-msg-file-is-image") != null
            openChatFileViewer(url, name, isImage)
        }, true)
    }

    document.getElementById("sd-dismiss-network-error")?.addEventListener("click", {
        updateState { networkError = null }
    })
    document.getElementById("sd-chat-reply-cancel")?.addEventListener("click", {
        updateState { chatReplyToMessageId = null; chatReplyToText = null }
    })
    document.getElementById("sd-network-retry")?.addEventListener("click", {
        updateState { networkError = null }
        val uid = appState.user?.id
        if (uid != null) {
            getCurrentUser { newUser, err ->
                if (newUser != null) updateState { user = newUser }
                if (err != null) updateState { networkError = friendlyNetworkError(err) }
            }
        }
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
    document.getElementById("sd-chat-admin-correspondence")?.addEventListener("click", {
        if (appState.user?.role != "admin") return@addEventListener
        saveChatScrollForCurrentContact()
        abortChatVoiceMediaBeforeRoomSwitch()
        if (appState.chatAdminCorrespondenceMode) {
            updateState {
                chatAdminCorrespondenceMode = false
                chatAdminCorrespondenceSubjectId = null
                chatAdminCorrespondencePeerId = null
                chatMessages = emptyList()
                chatReplyToMessageId = null
                chatReplyToText = null
                resetChatVoiceFieldsForNewRoom()
                chatPlayingVoiceId = null
                chatVoicePlaybackPaused = false
                chatPlayingVoiceCurrentMs = 0
            }
            unsubscribeChat()
        } else {
            updateState {
                chatAdminCorrespondenceMode = true
                chatAdminCorrespondenceSubjectId = null
                chatAdminCorrespondencePeerId = null
                selectedChatContactId = null
                selectedChatGroupId = null
                chatMessages = emptyList()
                chatReplyToMessageId = null
                chatReplyToText = null
                resetChatVoiceFieldsForNewRoom()
                chatPlayingVoiceId = null
                chatVoicePlaybackPaused = false
                chatPlayingVoiceCurrentMs = 0
            }
            unsubscribeChat()
        }
    })
    document.getElementById("sd-chat-create-group")?.addEventListener("click", {
        if (appState.user?.role != "admin") return@addEventListener
        clearGroupChatAvatarPending()
        val hadCorrRoom = appState.chatAdminCorrespondenceMode && appState.chatAdminCorrespondenceSubjectId != null && appState.chatAdminCorrespondencePeerId != null
        updateState {
            adminChatGroupModalOpen = true
            adminChatGroupEditId = null
            adminChatGroupDraftName = ""
            adminChatGroupDraftMemberIds = emptyList()
            chatAdminCorrespondenceMode = false
            chatAdminCorrespondenceSubjectId = null
            chatAdminCorrespondencePeerId = null
            if (hadCorrRoom) chatMessages = emptyList()
        }
        if (hadCorrRoom) unsubscribeChat()
    })
    document.getElementById("sd-chat-group-edit")?.addEventListener("click", {
        val uid = appState.user?.id ?: return@addEventListener
        if (appState.user?.role != "admin") return@addEventListener
        val gid = appState.selectedChatGroupId ?: return@addEventListener
        val grp = appState.chatGroups.find { it.id == gid } ?: return@addEventListener
        clearGroupChatAvatarPending()
        updateState {
            adminChatGroupModalOpen = true
            adminChatGroupEditId = gid
            adminChatGroupDraftName = grp.name
            adminChatGroupDraftMemberIds = grp.memberIds.filter { it != uid }
        }
    })
    document.getElementById("sd-chat-group-delete")?.addEventListener("click", {
        if (appState.user?.role != "admin") return@addEventListener
        val gid = appState.selectedChatGroupId ?: return@addEventListener
        updateState { adminChatGroupDeleteConfirmId = gid }
    })
    document.getElementById("sd-chat-settings-btn")?.addEventListener("click", {
        when (appState.screen) {
            AppScreen.Instructor, AppScreen.Cadet, AppScreen.Admin -> Unit
            else -> return@addEventListener
        }
        val chatIdx = when (appState.screen) {
            AppScreen.Admin -> listOf("Главная", "Баланс", "Расписание", "Чат", "История").indexOf("Чат")
            else -> listOf("Главная", "Запись", "Чат", "Билеты", "История").indexOf("Чат")
        }
        updateState {
            chatSettingsOpen = true
            if (chatIdx >= 0) selectedTabIndex = chatIdx
        }
    })
    document.getElementById("sd-settings-back-to-chat")?.addEventListener("click", {
        updateState { chatSettingsOpen = false }
    })
    document.getElementById("sd-admin-storage-stats-refresh")?.addEventListener("click", {
        if (appState.chatStorageStatsLoading || appState.chatStorageBucketLoading) return@addEventListener
        refreshAdminChatStorageStats()
    })
    document.getElementById("sd-admin-storage-bucket-refresh")?.addEventListener("click", {
        if (appState.chatStorageBucketLoading || appState.chatStorageStatsLoading) return@addEventListener
        refreshAdminStorageFull()
    })
    document.getElementById("sd-chat-refresh")?.addEventListener("click", {
        val u = appState.user ?: return@addEventListener
        updateState { chatContacts = emptyList(); chatContactsLoading = true }
        getUsersForChat(u) { list ->
            updateState { chatContacts = list; chatContactsLoading = false }
            subscribeChatPresence(list.map { it.id })
            getChatGroupsForUser(u.id) { groups ->
                updateState { chatGroups = groups }
                subscribeChatNotifications(u.id, list)
            }
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
            refreshAdminHomeAuxiliaryData()
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
                resetInstructorSessionSoundBaseline()
            })
        }
        AppScreen.ProfileNotFound -> {
            document.getElementById("sd-btn-signout-profile-not-found")?.addEventListener("click", {
                signOutAndClearPresence()
                resetInstructorSessionSoundBaseline()
            })
        }
        AppScreen.Admin, AppScreen.Instructor, AppScreen.Cadet -> {
            if (cadetWindowsPollIntervalId != 0) {
                window.clearInterval(cadetWindowsPollIntervalId)
                cadetWindowsPollIntervalId = 0
            }
            if (instructorSessionsPollIntervalId != 0) {
                window.clearInterval(instructorSessionsPollIntervalId)
                instructorSessionsPollIntervalId = 0
            }
            if (adminSchedulePollIntervalId != 0) {
                window.clearInterval(adminSchedulePollIntervalId)
                adminSchedulePollIntervalId = 0
            }
            root.asDynamic().onclick = null
            root.asDynamic().ontouchstart = null
            if (appState.user?.role == "instructor" || appState.user?.role == "cadet") {
                try {
                    if (js("(function(){ return typeof Notification !== 'undefined' && Notification.permission === 'default'; })").unsafeCast<() -> Boolean>().invoke()) {
                        js("(function(){ try { Notification.requestPermission(); } catch(e) {} })").unsafeCast<() -> Unit>().invoke()
                    }
                } catch (_: Throwable) { }
                var once: (dynamic) -> Unit = {}
                once = {
                    unlockCadetNotificationAudio()
                    root.asDynamic().onclick = null
                    root.asDynamic().ontouchstart = null
                }
                root.asDynamic().onclick = once
                root.asDynamic().ontouchstart = once
            }
            val uid = appState.user?.id
            val chatTabIdx = chatTabIndexForRole(appState.user?.role)
            if (appState.selectedTabIndex != chatTabIdx) {
                unsubscribeChat()
            }
            window.setTimeout({
                if (appState.selectedTabIndex == chatTabIdx && uid != null) {
                    if (appState.chatContacts.isEmpty() && !appState.chatContactsLoading) {
                        updateState { chatContactsLoading = true }
                        val chatTid = window.setTimeout({ updateState { chatContactsLoading = false } }, 5000)
                        getUsersForChat(appState.user!!) { list ->
                            window.clearTimeout(chatTid)
                            updateState { chatContacts = list; chatContactsLoading = false }
                            subscribeChatPresence(list.map { it.id })
                            getChatGroupsForUser(uid) { groups ->
                                updateState { chatGroups = groups }
                                subscribeChatNotifications(uid, list)
                            }
                        }
                    }
                }
                val usr = appState.user ?: return@setTimeout
                when {
                (usr.role == "instructor" && (appState.selectedTabIndex == 0 || appState.selectedTabIndex == 1)) -> {
                    refreshCadetGroups()
                    if (!appState.recordingLoading && appState.recordingSessions.isEmpty() && appState.recordingOpenWindows.isEmpty()) {
                        updateState { recordingLoading = true }
                        val tid = window.setTimeout({ updateState { recordingLoading = false } }, 8000)
                        getOpenWindowsForInstructor(usr.id) { wins ->
                            getSessionsForInstructor(usr.id) { sess ->
                                window.clearTimeout(tid)
                                maybePlayInstructorSessionSounds(appState.recordingSessions, sess)
                                updateState { recordingOpenWindows = wins; recordingSessions = sess; recordingLoading = false }
                                getUsers { list -> updateState { instructorCadets = list.filter { usr.assignedCadets.contains(it.id) } } }
                            }
                        }
                    }
                    if (instructorSessionsPollIntervalId == 0) {
                        instructorSessionsPollIntervalId = window.setInterval({
                            val u = appState.user ?: return@setInterval
                            if (u.role != "instructor") return@setInterval
                            if (appState.selectedTabIndex != 0 && appState.selectedTabIndex != 1) return@setInterval
                            // Не пропускать опрос на подэкране «Профиль»: иначе recordingSessions не обновляется,
                            // и при возврате на главную таймер inProgress не появляется до следующего цикла/действия.
                            if (isModalOpenById("sd-start-early-modal")) return@setInterval
                            getOpenWindowsForInstructor(u.id) { wins ->
                                getSessionsForInstructor(u.id) { sess ->
                                    if (isModalOpenById("sd-start-early-modal")) return@getSessionsForInstructor
                                    val newSig = recordingPollSignature(wins, sess)
                                    val oldSig = recordingPollSignature(appState.recordingOpenWindows, appState.recordingSessions)
                                    if (newSig == oldSig) return@getSessionsForInstructor
                                    maybePlayInstructorSessionSounds(appState.recordingSessions, sess)
                                    updateState { recordingOpenWindows = wins; recordingSessions = sess }
                                }
                            }
                        }, 5000).unsafeCast<Int>()
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
                        val prevWindowsCount = appState.recordingOpenWindows.size
                        getOpenWindowsForCadet(instId) { wins ->
                            getSessionsForCadet(usr.id) { sess ->
                                window.clearTimeout(tid)
                                updateState { recordingOpenWindows = wins; recordingSessions = sess; recordingLoading = false }
                                notifyCadetNewInstructorScheduledSessions(usr.id, sess)
                                if (usr.role == "cadet" && wins.size > prevWindowsCount) {
                                    val windowMs = wins.minByOrNull { (it.dateTimeMillis ?: Long.MAX_VALUE) }?.dateTimeMillis
                                    showCadetNewWindowNotification(windowMs)
                                }
                                usr.assignedInstructorId?.let { id -> getUserById(id) { inst -> updateState { cadetInstructor = inst } } }
                            }
                        }
                    }
                    if (cadetWindowsPollIntervalId == 0 && usr.role == "cadet") {
                        cadetWindowsPollIntervalId = window.setInterval({
                            val u = appState.user ?: return@setInterval
                            if (u.role != "cadet") return@setInterval
                            if (appState.selectedTabIndex != 0 && appState.selectedTabIndex != 1) return@setInterval
                            if (appState.cadetHomeSubView == "profile" && appState.selectedTabIndex == 0) return@setInterval
                            val instId = u.assignedInstructorId ?: return@setInterval
                            val prevCount = appState.recordingOpenWindows.size
                            getOpenWindowsForCadet(instId) { wins ->
                                getSessionsForCadet(u.id) { sess ->
                                    val newSig = recordingPollSignature(wins, sess)
                                    val oldSig = recordingPollSignature(appState.recordingOpenWindows, appState.recordingSessions)
                                    if (newSig != oldSig) {
                                        updateState { recordingOpenWindows = wins; recordingSessions = sess }
                                    }
                                    notifyCadetNewInstructorScheduledSessions(u.id, sess)
                                    if (wins.size > prevCount) {
                                        val windowMs = wins.minByOrNull { (it.dateTimeMillis ?: Long.MAX_VALUE) }?.dateTimeMillis
                                        showCadetNewWindowNotification(windowMs)
                                    } else if (wins.size != prevCount) {
                                        updateState { recordingOpenWindows = wins }
                                    }
                                }
                            }
                        }, 5000).unsafeCast<Int>()
                    }
                }
                usr.role == "instructor" && appState.selectedTabIndex == 4 -> {
                    if (!appState.historyLoading && appState.historySessions.isEmpty()) {
                        updateState { historyLoading = true }
                        val tid = window.setTimeout({ updateState { historyLoading = false } }, 8000)
                        getUsers { list ->
                            getSessionsForInstructor(usr.id) { sess ->
                                getBalanceHistory(usr.id) { hist ->
                                    window.clearTimeout(tid)
                                    updateState { historyUsers = list; historySessions = sess; historyBalance = hist; historyLoading = false }
                                    notifyNewBalanceOpsForCurrentUser(hist, list)
                                }
                            }
                        }
                    }
                }
                usr.role == "cadet" && appState.selectedTabIndex == 4 -> {
                    // Подгружаем список пользователей и при уже заполненных сессиях/балансе, если имён нет
                    // (например история обновилась с главной без getUsers — «Кем» и «Инструктор» были «—»).
                    if (!appState.historyLoading && (appState.historySessions.isEmpty() || appState.historyUsers.isEmpty())) {
                        updateState { historyLoading = true }
                        val tid = window.setTimeout({ updateState { historyLoading = false } }, 8000)
                        getUsers { list ->
                            getSessionsForCadet(usr.id) { sess ->
                                getBalanceHistory(usr.id) { hist ->
                                    window.clearTimeout(tid)
                                    updateState { historyUsers = list; historySessions = sess; historyBalance = hist; historyLoading = false }
                                    // Обновляем объект текущего пользователя, чтобы сразу подтянуть новый баланс
                                    val self = list.find { it.id == usr.id }
                                    if (self != null) {
                                        updateState { user = self }
                                    }
                                    notifyNewBalanceOpsForCurrentUser(hist, list)
                                }
                            }
                        }
                    }
                }
                usr.role == "admin" && appState.selectedTabIndex == 4 -> {
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
                            refreshAdminHomeAuxiliaryData()
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
                if (usr.role == "admin" && adminSchedulePollIntervalId == 0) {
                    adminSchedulePollIntervalId = window.setInterval({
                        val u = appState.user ?: return@setInterval
                        if (u.role != "admin") return@setInterval
                        val users = appState.adminHomeUsers.ifEmpty { appState.balanceAdminUsers }
                        val instructors = users.filter { it.role == "instructor" }
                        if (instructors.isEmpty()) return@setInterval
                        getSessionsForInstructorsMap(instructors.map { it.id }) { map ->
                            val newSig = computeAdminScheduleSignature(map)
                            val oldSig = computeAdminScheduleSignature(appState.adminScheduleSessionsByInstructorId)
                            if (newSig == oldSig) return@getSessionsForInstructorsMap
                            val schedIdx = getTabsForUserRole("admin").indexOf("Расписание")
                            val onScheduleTab = appState.selectedTabIndex == schedIdx
                            updateState {
                                adminScheduleSessionsByInstructorId = map
                                if (onScheduleTab) adminScheduleSeenSignature = newSig
                            }
                        }
                    }, 45000).unsafeCast<Int>()
                }
            }, 0)
            val u = appState.user
            u?.let { usr ->
                document.getElementById("sd-btn-signout")?.addEventListener("click", {
                    document.getElementById("sd-signout-confirm-modal")?.let { modal ->
                        modal.classList.remove("sd-hidden")
                        modal.setAttribute("aria-hidden", "false")
                    }
                })
                if (appState.screen == AppScreen.Instructor || appState.screen == AppScreen.Cadet || appState.screen == AppScreen.Admin) {
                    document.getElementById("sd-btn-notifications")?.addEventListener("click", {
                        val uid = appState.user?.id
                        val loaded = if (uid != null) loadNotificationsFromStorage(uid) else emptyList()
                        fun openWithMerged(extra: List<AppNotification>) {
                            val merged = dedupeNotificationsByTextKeepNewest(loaded + appState.notifications + extra)
                            updateState { notifications = merged; notificationsViewOpen = true; notificationsReadCount = merged.size }
                            uid?.let { saveNotificationsToStorage(it, merged) }
                        }
                        if (appState.user?.role == "admin") {
                            fetchAdminEventsForNotifications { pairs ->
                                val extra = pairs.map {
                                    AppNotification(dateTimeMs = it.first, text = it.second, sourceId = it.third)
                                }
                                openWithMerged(extra)
                            }
                        } else {
                            openWithMerged(emptyList())
                        }
                    })
                }
                document.getElementById("sd-notif-back")?.addEventListener("click", {
                    updateState { notificationsViewOpen = false }
                })
                if (appState.notificationsViewOpen) {
                    (root.querySelector("#sd-card") as? org.w3c.dom.Element)?.let { card ->
                        attachNotifDropdownListeners(card)
                    }
                }
            if (usr.role == "instructor") {
                document.getElementById("sd-instructor-profile-open")?.addEventListener("click", {
                    val uid = usr.id
                    resetInstructorProfileCalculatorCache()
                    if (appState.historyBalance.isNotEmpty()) {
                        updateState { instructorHomeSubView = "profile"; instructorProfileWeekOffset = 0 }
                    } else {
                        getBalanceHistory(uid) { hist ->
                            updateState { historyBalance = hist; instructorHomeSubView = "profile"; instructorProfileWeekOffset = 0 }
                        }
                    }
                })
                document.getElementById("sd-instructor-profile-back")?.addEventListener("click", {
                    resetInstructorProfileCalculatorCache()
                    updateState { instructorHomeSubView = "main"; instructorProfileWeekOffset = 0 }
                    // Сразу подтянуть сессии (курсант мог нажать «Начать», пока открыт профиль).
                    getOpenWindowsForInstructor(usr.id) { wins ->
                        getSessionsForInstructor(usr.id) { sess ->
                            maybePlayInstructorSessionSounds(appState.recordingSessions, sess)
                            updateState { recordingOpenWindows = wins; recordingSessions = sess }
                        }
                    }
                })
                document.getElementById("sd-instr-week-prev")?.addEventListener("click", {
                    if (appState.instructorProfileWeekOffset < INSTRUCTOR_PROFILE_MAX_WEEK_OFFSET) {
                        updateState { instructorProfileWeekOffset = instructorProfileWeekOffset + 1 }
                    }
                })
                document.getElementById("sd-instr-week-next")?.addEventListener("click", {
                    if (appState.instructorProfileWeekOffset > 0) {
                        updateState { instructorProfileWeekOffset = instructorProfileWeekOffset - 1 }
                    }
                })
                val homeConfirmNodes = root.querySelectorAll(".sd-home-schedule-confirm")
                for (k in 0 until homeConfirmNodes.length) {
                    val btn = homeConfirmNodes.item(k) as? org.w3c.dom.Element ?: continue
                    val sessionId = btn.getAttribute("data-session-id") ?: continue
                    btn.addEventListener("click", {
                        val startMs = appState.recordingSessions.find { it.id == sessionId }?.startTimeMillis
                        confirmBookingByInstructor(sessionId) { err ->
                            if (err != null) updateState { networkError = err }
                            else {
                                showNotification("Запись подтверждена" + formatDayTimeShort(startMs))
                                getSessionsForInstructor(usr.id) { sess -> updateState { recordingSessions = sess } }
                            }
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
                            showToast("Вождение можно начать за 15 минут до назначенного времени, пожалуйста ожидайте...")
                            return@addEventListener
                        }
                        if (now < startMs) {
                            val minutesLeft = ((startMs - now) / 60000L).toInt().coerceAtLeast(1)
                            startEarlyModalSessionId = sessionId
                            startEarlyModalMinutesLeft = minutesLeft
                            syncStartEarlyModalVisibility()
                            return@addEventListener
                        }
                        requestStartByInstructor(sessionId) { err ->
                            if (err != null) updateState { networkError = err }
                            else getSessionsForInstructor(usr.id) { sess -> updateState { recordingSessions = sess } }
                        }
                    })
                }
                document.getElementById("sd-start-early-yes")?.addEventListener("click", {
                    val modal = document.getElementById("sd-start-early-modal") ?: return@addEventListener
                    val sessionId = modal.asDynamic().dataset["sessionId"] as? String ?: return@addEventListener
                    startEarlyModalSessionId = null
                    startEarlyModalMinutesLeft = 0
                    syncStartEarlyModalVisibility()
                    requestStartByInstructor(sessionId) { err ->
                        if (err != null) updateState { networkError = err }
                        else getSessionsForInstructor(usr.id) { sess -> updateState { recordingSessions = sess } }
                    }
                })
                document.getElementById("sd-start-early-no")?.addEventListener("click", {
                    startEarlyModalSessionId = null
                    startEarlyModalMinutesLeft = 0
                    syncStartEarlyModalVisibility()
                })
                val homeLateNodes = root.querySelectorAll(".sd-home-schedule-late")
                for (k in 0 until homeLateNodes.length) {
                    val btn = homeLateNodes.item(k) as? org.w3c.dom.Element ?: continue
                    val sessionId = btn.getAttribute("data-session-id") ?: continue
                    btn.addEventListener("click", {
                        runningLateModalSessionId = sessionId
                        (document.querySelector("input[name=\"sd-late-mins\"][value=\"5\"]")?.asDynamic())?.checked = true
                        syncInstructorActionModalsVisibility()
                    })
                }
                val homeCancelNodes = root.querySelectorAll(".sd-home-schedule-cancel")
                for (k in 0 until homeCancelNodes.length) {
                    val btn = homeCancelNodes.item(k) as? org.w3c.dom.Element ?: continue
                    val sessionId = btn.getAttribute("data-session-id") ?: continue
                    btn.addEventListener("click", {
                        if (btn.getAttribute("data-home-cancel-needs-cadet-confirm") == "1") {
                            homeCancelUnconfirmedModalSessionId = sessionId
                            syncInstructorActionModalsVisibility()
                            return@addEventListener
                        }
                        document.getElementById("sd-instructor-cancel-reason-modal")?.let { modal ->
                            val session = appState.recordingSessions.find { it.id == sessionId }
                            val cadetConfirmed = session?.instructorConfirmed == true
                            val now = js("Date.now()").unsafeCast<Double>().toLong()
                            val timeReached = session?.startTimeMillis == null || now >= (session.startTimeMillis ?: 0L)
                            val allowCadetNoShow = cadetConfirmed && timeReached
                            cancelReasonModalSessionId = sessionId
                            document.getElementById("sd-cancel-reason-other-wrap")?.classList?.add("sd-hidden")
                            (document.getElementById("sd-cancel-reason-other-text") as? HTMLTextAreaElement)?.value = ""
                            document.querySelectorAll("input[name=sd-cancel-reason]").let { radios ->
                                for (i in 0 until radios.length) {
                                    val radio = radios.item(i) as? HTMLInputElement ?: continue
                                    val isCadetNoShow = (radio.value == "Курсант не явился")
                                    radio.unsafeCast<dynamic>().disabled = !allowCadetNoShow && isCadetNoShow
                                    radio.checked = (allowCadetNoShow && isCadetNoShow) || (!allowCadetNoShow && radio.value == "ТС на ремонте")
                                }
                            }
                            syncInstructorActionModalsVisibility()
                        }
                    })
                }
                document.getElementById("sd-rec-delete-window-yes")?.addEventListener("click", {
                    val modal = document.getElementById("sd-rec-delete-window-confirm-modal") ?: return@addEventListener
                    val wid = modal.asDynamic().dataset["windowId"] as? String ?: return@addEventListener
                    val windowMs = appState.recordingOpenWindows.find { it.id == wid }?.dateTimeMillis
                    recDeleteWindowModalWindowId = null
                    syncInstructorActionModalsVisibility()
                    deleteOpenWindow(wid) { err ->
                        if (err != null) updateState { networkError = err }
                        else {
                            showNotification("Окно удалено" + formatDayTimeShort(windowMs))
                            getOpenWindowsForInstructor(usr.id) { wins ->
                                updateState { recordingOpenWindows = wins }
                            }
                        }
                    }
                })
                document.getElementById("sd-rec-delete-window-no")?.addEventListener("click", {
                    recDeleteWindowModalWindowId = null
                    syncInstructorActionModalsVisibility()
                })
                val timerPauseNodes = root.querySelectorAll(".sd-sch-timer-actions .sd-sch-pause")
                for (kp in 0 until timerPauseNodes.length) {
                    val btn = timerPauseNodes.item(kp) as? org.w3c.dom.Element ?: continue
                    val sessionId = btn.getAttribute("data-session-id") ?: continue
                    btn.addEventListener("click", {
                        pauseDrivingSession(sessionId) { err ->
                            if (err != null) updateState { networkError = err }
                            else getSessionsForInstructor(usr.id) { sess -> updateState { recordingSessions = sess } }
                        }
                    })
                }
                val timerPlayNodes = root.querySelectorAll(".sd-sch-timer-actions .sd-sch-play")
                for (kp in 0 until timerPlayNodes.length) {
                    val btn = timerPlayNodes.item(kp) as? org.w3c.dom.Element ?: continue
                    val sessionId = btn.getAttribute("data-session-id") ?: continue
                    btn.addEventListener("click", {
                        resumeDrivingSession(sessionId) { err ->
                            if (err != null) updateState { networkError = err }
                            else getSessionsForInstructor(usr.id) { sess -> updateState { recordingSessions = sess } }
                        }
                    })
                }
                val timerStopNodes = root.querySelectorAll(".sd-sch-timer-actions .sd-sch-stop")
                for (ks in 0 until timerStopNodes.length) {
                    val btn = timerStopNodes.item(ks) as? org.w3c.dom.Element ?: continue
                    val sessionId = btn.getAttribute("data-session-id") ?: continue
                    val endMsStr = btn.getAttribute("data-end-ms")
                    btn.addEventListener("click", {
                        val endMs = endMsStr?.toLongOrNull() ?: 0L
                        val now = js("Date.now()").unsafeCast<Double>().toLong()
                        val remainingMs = (endMs - now).coerceAtLeast(0L)
                        val remainingMin = (remainingMs / 60000L).toInt()
                        completeEarlyModalSessionId = sessionId
                        completeEarlyModalText = "Еще осталось: $remainingMin мин.!"
                        syncInstructorActionModalsVisibility()
                    })
                }
                document.getElementById("sd-complete-early-yes")?.addEventListener("click", {
                    val modal = document.getElementById("sd-complete-early-modal") ?: return@addEventListener
                    val sessionId = modal.asDynamic().dataset["sessionId"] as? String ?: return@addEventListener
                    val startMs = appState.recordingSessions.find { it.id == sessionId }?.startTimeMillis
                    completeEarlyModalSessionId = null
                    completeEarlyModalText = ""
                    syncInstructorActionModalsVisibility()
                    completeDrivingSession(sessionId) { err ->
                        if (err != null) updateState { networkError = err }
                        else {
                            showNotification("Вождение завершено досрочно" + formatDayTimeShort(startMs))
                            getOpenWindowsForInstructor(usr.id) { wins ->
                                getSessionsForInstructor(usr.id) { sess ->
                                    getBalanceHistory(usr.id) { hist ->
                                        updateState { recordingOpenWindows = wins; recordingSessions = sess; historySessions = sess; historyBalance = hist }
                                        getCurrentUser { newUser, _ -> if (newUser != null) updateState { user = newUser } }
                                        notifyNewBalanceOpsForCurrentUser(hist)
                                    }
                                }
                            }
                        }
                    }
                })
                document.getElementById("sd-complete-early-no")?.addEventListener("click", {
                    completeEarlyModalSessionId = null
                    completeEarlyModalText = ""
                    syncInstructorActionModalsVisibility()
                })
                val rateCadetBtns = root.querySelectorAll(".sd-instructor-rate-cadet-btn")
                for (ri in 0 until rateCadetBtns.length) {
                    val btn = rateCadetBtns.item(ri) as? org.w3c.dom.Element ?: continue
                    btn.addEventListener("click", {
                        val sessionId = btn.getAttribute("data-session-id") ?: return@addEventListener
                        val cadetName = btn.getAttribute("data-cadet-name") ?: "Курсант"
                        instructorRateModalRadioInitSessionId = null
                        instructorRateCadetModalSessionId = sessionId
                        instructorRateCadetModalCadetName = cadetName
                        syncInstructorActionModalsVisibility()
                    })
                }
                startDrivingTimers()
            }
            document.getElementById("sd-chat-back")?.addEventListener("click", {
                abortChatVoiceMediaBeforeRoomSwitch()
                chatVoiceResetDiscardOnStop()
                if (chatVoiceRecordTickInterval != 0) {
                    window.clearInterval(chatVoiceRecordTickInterval)
                    chatVoiceRecordTickInterval = 0
                }
                if (voicePlayInterval != 0) { window.clearInterval(voicePlayInterval); voicePlayInterval = 0 }
                try {
                    document.getElementById("sd-chat-voice-review-audio")?.asDynamic()?.pause?.invoke()
                } catch (_: Throwable) { }

                saveChatScrollForCurrentContact()
                if (appState.chatAdminCorrespondenceMode) {
                    val subj = appState.chatAdminCorrespondenceSubjectId
                    val peer = appState.chatAdminCorrespondencePeerId
                    if (peer != null && subj != null) {
                        updateState {
                            chatAdminCorrespondencePeerId = null
                            chatMessages = emptyList()
                            chatReplyToMessageId = null
                            chatReplyToText = null
                            resetChatVoiceFieldsForNewRoom()
                            chatPlayingVoiceId = null
                            chatVoicePlaybackPaused = false
                            chatPlayingVoiceCurrentMs = 0
                        }
                        unsubscribeChat()
                        return@addEventListener
                    }
                    if (subj != null) {
                        updateState {
                            chatAdminCorrespondenceSubjectId = null
                            chatMessages = emptyList()
                            chatReplyToMessageId = null
                            chatReplyToText = null
                            resetChatVoiceFieldsForNewRoom()
                            chatPlayingVoiceId = null
                            chatVoicePlaybackPaused = false
                            chatPlayingVoiceCurrentMs = 0
                        }
                        unsubscribeChat()
                        return@addEventListener
                    }
                    updateState {
                        chatAdminCorrespondenceMode = false
                        chatMessages = emptyList()
                        chatReplyToMessageId = null
                        chatReplyToText = null
                        resetChatVoiceFieldsForNewRoom()
                        chatPlayingVoiceId = null
                        chatVoicePlaybackPaused = false
                        chatPlayingVoiceCurrentMs = 0
                    }
                    unsubscribeChat()
                    return@addEventListener
                }
                updateState {
                    chatSettingsOpen = false
                    selectedChatContactId = null
                    selectedChatGroupId = null
                    chatMessages = emptyList()
                    chatReplyToMessageId = null
                    chatReplyToText = null
                    resetChatVoiceFieldsForNewRoom()
                    chatPlayingVoiceId = null
                    chatVoicePlaybackPaused = false
                    chatPlayingVoiceCurrentMs = 0
                }
                unsubscribeChat()
            })
            val chatInput = document.getElementById("sd-chat-input") as? HTMLInputElement
            document.getElementById("sd-chat-send")?.addEventListener("click", {
                sendChatMessage(chatInput, uid)
            })
            document.getElementById("sd-chat-file-btn")?.addEventListener("click", {
                (document.getElementById("sd-chat-file-input") as? HTMLInputElement)?.click()
            })
            document.getElementById("sd-chat-file-input")?.addEventListener("change", {
                val fi = document.getElementById("sd-chat-file-input") as? HTMLInputElement
                handleChatAdminFileSelected(uid, fi)
            })
            chatInput?.addEventListener("keypress", { e: dynamic ->
                if (e?.key == "Enter") sendChatMessage(chatInput, uid)
            })
            if (document.getElementById("sd-chat-voice-recording") != null || document.getElementById("sd-chat-voice-recording-review") != null) {
                if (chatVoiceRecordTickInterval != 0) window.clearInterval(chatVoiceRecordTickInterval)
                chatVoiceRecordTickInterval = window.setInterval({
                    val start = appState.chatVoiceRecordStartMs
                    if (start > 0) updateState { chatVoiceRecordElapsedSec = ((js("Date.now()").unsafeCast<Double>() - start) / 1000.0).toInt().coerceAtLeast(0) }
                }, 1000).unsafeCast<Int>()
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
            if (playingId != null && voicePlayInterval == 0 && !appState.chatVoicePlaybackPaused) {
                val durAttr = root.querySelector(".sd-msg-voice[data-voice-id=\"$playingId\"]")?.getAttribute("data-voice-duration") ?: "0"
                val durationSec = durAttr.toIntOrNull() ?: 0
                startVoicePlaybackTicker(durationSec)
            }
            (document.getElementById("sd-recording-book-cadet") as? HTMLSelectElement)?.let { cadetSelectEl ->
                if (cadetSelectEl.value.isNotBlank()) cadetSelectEl.classList.add("sd-has-cadet")
                else cadetSelectEl.classList.remove("sd-has-cadet")
            }
            if (appState.instructorRecordingScrollToBookForm && usr.role == "instructor") {
                window.setTimeout({
                    document.getElementById("sd-recording-book-cadet")?.closest(".sd-rec-form-card")?.scrollIntoView(js("({behavior:'smooth',block:'center'})"))
                    updateState { instructorRecordingScrollToBookForm = false }
                }, 120)
            }
            val confirmSessionNodes = root.querySelectorAll(".sd-recording-confirm-session")
            for (k in 0 until confirmSessionNodes.length) {
                val btn = confirmSessionNodes.item(k) as? org.w3c.dom.Element ?: continue
                val sessionId = btn.getAttribute("data-session-id") ?: continue
                btn.addEventListener("click", {
                    confirmBookingByInstructor(sessionId) { err ->
                        if (err != null) updateState { networkError = err }
                        else {
                            if (usr.role == "instructor") getSessionsForInstructor(usr.id) { sess -> updateState { recordingSessions = sess } }
                            else getSessionsForCadet(usr.id) { sess -> updateState { recordingSessions = sess } }
                        }
                    }
                })
            }
            val cancelSessionNodes = root.querySelectorAll(".sd-recording-cancel-session")
            for (k in 0 until cancelSessionNodes.length) {
                val btn = cancelSessionNodes.item(k) as? org.w3c.dom.Element ?: continue
                val sessionId = btn.getAttribute("data-session-id") ?: continue
                btn.addEventListener("click", {
                    if (usr.role == "cadet") {
                        val sess = appState.recordingSessions.find { it.id == sessionId }
                        if (cadetCancelBlockedWithinSixHoursOfStart(sess?.startTimeMillis)) {
                            document.getElementById("sd-cadet-cancel-six-hours-modal")?.let { m ->
                                m.classList.remove("sd-hidden")
                                m.setAttribute("aria-hidden", "false")
                            }
                            return@addEventListener
                        }
                    }
                    document.getElementById("sd-rec-cancel-session-confirm-modal")?.let { modal ->
                        modal.asDynamic().dataset["sessionId"] = sessionId
                        modal.classList.remove("sd-hidden")
                    }
                })
            }
            val bookNodes = root.querySelectorAll(".sd-rec-book-slot-btn[data-window-id]")
            for (k in 0 until bookNodes.length) {
                val btn = bookNodes.item(k) as? org.w3c.dom.Element ?: continue
                val wid = btn.getAttribute("data-window-id") ?: continue
                btn.addEventListener("click", {
                    val userBalance = usr.balance
                    val bookedCount = appState.recordingSessions.count { it.cadetId == usr.id && (it.status == "scheduled" || it.status == "inProgress") }
                    if (userBalance <= 0) {
                        val msg = "У вас 0 талонов, запись невозможна"
                        updateState { networkError = msg }
                        showNotification(msg)
                        return@addEventListener
                    }
                    if (bookedCount >= userBalance) {
                        val msg = "По вашему балансу уже запланировано максимальное число вождений"
                        updateState { networkError = msg }
                        showNotification(msg)
                        return@addEventListener
                    }
                    val windowMs = appState.recordingOpenWindows.find { it.id == wid }?.dateTimeMillis
                    bookWindow(wid, usr.id) { err ->
                        if (err != null) updateState { networkError = err }
                        else {
                            showNotification("Вы записаны на вождение" + formatDayTimeShort(windowMs))
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
            val cadetConfirmStartNodes = root.querySelectorAll(".sd-cadet-confirm-start")
            for (k in 0 until cadetConfirmStartNodes.length) {
                val btn = cadetConfirmStartNodes.item(k) as? org.w3c.dom.Element ?: continue
                val sessionId = btn.getAttribute("data-session-id") ?: continue
                btn.addEventListener("click", {
                    playCadetDrivingStartGreetingSound()
                    val startMs = appState.recordingSessions.find { it.id == sessionId }?.startTimeMillis
                    startSession(sessionId) { err ->
                        if (err != null) updateState { networkError = err }
                        else {
                            showNotification("Вождение начато" + formatDayTimeShort(startMs))
                            getSessionsForCadet(usr.id) { sess -> updateState { recordingSessions = sess } }
                        }
                    }
                })
            }
            if (usr.role == "cadet") {
                document.getElementById("sd-cadet-profile-open")?.addEventListener("click", {
                    val uid = usr.id
                    if (appState.historySessions.isNotEmpty()) {
                        updateState { cadetHomeSubView = "profile" }
                    } else {
                        updateState { historyLoading = true }
                        val tid = window.setTimeout({ updateState { historyLoading = false } }, 8000)
                        getUsers { list ->
                            getSessionsForCadet(uid) { sess ->
                                getBalanceHistory(uid) { hist ->
                                    window.clearTimeout(tid)
                                    updateState {
                                        historyUsers = list
                                        historySessions = sess
                                        historyBalance = hist
                                        historyLoading = false
                                        cadetHomeSubView = "profile"
                                    }
                                    val self = list.find { it.id == uid }
                                    if (self != null) updateState { user = self }
                                    notifyNewBalanceOpsForCurrentUser(hist, list)
                                }
                            }
                        }
                    }
                })
                document.getElementById("sd-cadet-profile-back")?.addEventListener("click", {
                    updateState { cadetHomeSubView = "main" }
                })
                val needCadetRating = appState.recordingSessions.filter { it.status == "completed" && it.instructorRating > 0 && it.cadetRating == 0 }
                if (needCadetRating.isNotEmpty()) {
                    val first = needCadetRating.first()
                    document.getElementById("sd-cadet-rate-instructor-modal")?.let { modal ->
                        modal.asDynamic().dataset["sessionId"] = first.id
                        modal.classList.remove("sd-hidden")
                    }
                    document.getElementById("sd-cadet-rate-stars")?.let { container ->
                        container.asDynamic().dataset["selected"] = "0"
                        val stars = container.querySelectorAll(".sd-star")
                        for (j in 0 until stars.length) {
                            (stars.item(j) as? org.w3c.dom.Element)?.classList?.remove("sd-star-filled")
                        }
                    }
                }
                val starsContainer = document.getElementById("sd-cadet-rate-stars")
                starsContainer?.let { container ->
                    val stars = container.querySelectorAll(".sd-star")
                    for (i in 0 until stars.length) {
                        val star = stars.item(i) as? org.w3c.dom.Element ?: continue
                        val value = star.getAttribute("data-value")?.toIntOrNull() ?: continue
                        star.addEventListener("click", {
                            container.asDynamic().dataset["selected"] = value
                            for (j in 0 until stars.length) {
                                val s = stars.item(j) as? org.w3c.dom.Element
                                val v = s?.getAttribute("data-value")?.toIntOrNull() ?: 0
                                s?.classList?.toggle("sd-star-filled", v <= value)
                            }
                        })
                    }
                }
                document.getElementById("sd-cadet-rate-instructor-confirm")?.addEventListener("click", {
                    val modal = document.getElementById("sd-cadet-rate-instructor-modal") ?: return@addEventListener
                    val sessionId = modal.asDynamic().dataset["sessionId"] as? String ?: return@addEventListener
                    val startMs = appState.recordingSessions.find { it.id == sessionId }?.startTimeMillis
                    val sel = (document.getElementById("sd-cadet-rate-stars")?.asDynamic()?.dataset?.selected as? String)?.toIntOrNull() ?: 0
                    if (sel !in 1..5) return@addEventListener
                    modal.classList.add("sd-hidden")
                    setCadetRating(sessionId, sel) { err ->
                        if (err != null) updateState { networkError = err }
                        else {
                            showNotification("Оценка инструктору поставлена" + formatDayTimeShort(startMs))
                            getUsers { list ->
                                getSessionsForCadet(usr.id) { sess ->
                                    getBalanceHistory(usr.id) { hist ->
                                        updateState { historyUsers = list; recordingSessions = sess; historySessions = sess; historyBalance = hist }
                                    }
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
                            val targetUser = list.find { it.id == targetId }
                            updateState {
                                balanceAdminUsers = list
                                balanceAdminHistory = hist
                                if (user?.id == targetId && targetUser != null) {
                                    user = targetUser
                                }
                            }
                            val targetNameFull = targetUser?.fullName?.takeIf { it.isNotBlank() } ?: "Пользователь"
                            val targetNameShort = formatShortName(targetNameFull)
                            val fromShort = formatShortName(usr.fullName.ifBlank { "Администратор" })
                            val nowTs = js("Date.now()").unsafeCast<Double>().toLong()
                            val dt = formatMessageDateTime(nowTs)
                            when (type) {
                                "credit" -> showNotification("Зачислено $amount ${ticketWord(amount)}. От: $fromShort. Кому: $targetNameShort. Дата и время: $dt")
                                "debit" -> showNotification("Списано $amount ${ticketWord(amount)}. От: $fromShort. У кого: $targetNameShort. Дата и время: $dt")
                                "set" -> showNotification("Баланс установлен. От: $fromShort. Пользователь: $targetNameShort. Дата и время: $dt")
                            }
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
            document.getElementById("sd-settings-sound-notifications")?.addEventListener("change", {
                val cb = document.getElementById("sd-settings-sound-notifications") as? HTMLInputElement ?: return@addEventListener
                setSoundNotificationsEnabled(cb.checked)
            })
            document.getElementById("sd-settings-notification-sound")?.addEventListener("change", {
                val sel = document.getElementById("sd-settings-notification-sound") as? HTMLSelectElement ?: return@addEventListener
                val name = sel.value
                try { window.asDynamic().localStorage?.setItem(NOTIFICATION_SOUND_STORAGE_KEY, name) } catch (_: Throwable) { }
            })
            document.getElementById("sd-settings-sound-preview")?.addEventListener("click", {
                val sel = document.getElementById("sd-settings-notification-sound") as? HTMLSelectElement
                val name = sel?.value ?: getNotificationSoundFilename()
                val audio = getOrCreateCadetNotificationAudio()
                audio.src = getResourceBaseUrl() + "sounds/" + name + ".mp3"
                audio.currentTime = 0.0
                audio.play()?.catch { _: dynamic -> Unit }
            })
            val cropState = doubleArrayOf(0.0, 0.0, 1.0) /* offsetX, offsetY, scale */
            val cropDataUrlHolder = mutableListOf<String?>(null)
            val updateCropImgTransform = {
                document.getElementById("sd-avatar-crop-img")?.unsafeCast<org.w3c.dom.HTMLElement>()?.style?.setProperty(
                    "transform", "translate(-50%,-50%) translate(${cropState[0]}px,${cropState[1]}px) scale(${cropState[2]})"
                )
            }
            document.getElementById("sd-settings-avatar-file")?.addEventListener("change", { ev: dynamic ->
                val target = ev?.target?.unsafeCast<dynamic>()
                val getFirstFile = js("(function(f){ return f && f[0]; })").unsafeCast<(dynamic) -> dynamic>()
                val file = getFirstFile(target?.files)
                if (file != null) {
                val reader = js("new FileReader()").unsafeCast<dynamic>()
                reader.onload = {
                    val dataUrl = reader.result as? String
                    if (dataUrl != null) {
                    cropDataUrlHolder[0] = dataUrl
                    val editor = document.getElementById("sd-avatar-crop-editor")?.unsafeCast<org.w3c.dom.HTMLElement>()
                    val cropImg = document.getElementById("sd-avatar-crop-img")?.unsafeCast<org.w3c.dom.HTMLImageElement>()
                    val frame = document.getElementById("sd-avatar-crop-frame")
                    val scaleInput = document.getElementById("sd-avatar-crop-scale") as? HTMLInputElement
                    if (editor != null && cropImg != null && frame != null) {
                        editor.classList.remove("sd-hidden")
                        cropImg.src = dataUrl
                        cropImg.onload = {
                            val nw = cropImg.naturalWidth.toDouble().coerceAtLeast(1.0)
                            val nh = cropImg.naturalHeight.toDouble().coerceAtLeast(1.0)
                            // В круге сразу видно всё фото целиком (contain); масштаб не больше 1 — без размытия
                            val containScale = (200.0 / nw).coerceAtMost(200.0 / nh).coerceAtMost(1.0)
                            cropImg.style.setProperty("width", cropImg.naturalWidth.toString() + "px")
                            cropImg.style.setProperty("height", cropImg.naturalHeight.toString() + "px")
                            cropImg.style.setProperty("object-fit", "none")
                            cropState[0] = 0.0
                            cropState[1] = 0.0
                            cropState[2] = containScale
                            scaleInput?.value = containScale.toString()
                            scaleInput?.setAttribute("value", containScale.toString())
                            updateCropImgTransform()
                        }
                    }
                    }
                }
                reader.readAsDataURL(file)
                }
            })
            fun syncCropScaleSlider() {
                (document.getElementById("sd-avatar-crop-scale") as? HTMLInputElement)?.let { it.value = cropState[2].toString(); it.setAttribute("value", cropState[2].toString()) }
            }
            document.getElementById("sd-avatar-crop-scale")?.addEventListener("input", {
                val input = document.getElementById("sd-avatar-crop-scale") as? HTMLInputElement ?: return@addEventListener
                val v = input.value.toDoubleOrNull() ?: 1.0
                cropState[2] = v.coerceIn(0.5, 2.0)
                updateCropImgTransform()
            })
            var cropDragStartX = 0.0
            var cropDragStartY = 0.0
            var cropDragOffsetStartX = 0.0
            var cropDragOffsetStartY = 0.0
            var cropDragging = false
            var cropPinching = false
            var cropPinchStartDist = 0.0
            var cropPinchStartScale = 0.0
            fun getClientXY(e: dynamic): Pair<Double, Double> {
                val tx = (e?.touches?.get(0) ?: e)?.clientX as? Number
                val ty = (e?.touches?.get(0) ?: e)?.clientY as? Number
                return Pair((tx?.toDouble() ?: (e?.clientX as? Number)?.toDouble() ?: 0.0), (ty?.toDouble() ?: (e?.clientY as? Number)?.toDouble() ?: 0.0))
            }
            fun getTouchDistance(e: dynamic): Double {
                val t = e?.touches
                if (t == null || (t.asDynamic().length as Int) < 2) return 0.0
                val x1 = (t.asDynamic()[0].clientX as Number).toDouble()
                val y1 = (t.asDynamic()[0].clientY as Number).toDouble()
                val x2 = (t.asDynamic()[1].clientX as Number).toDouble()
                val y2 = (t.asDynamic()[1].clientY as Number).toDouble()
                return kotlin.math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
            }
            document.getElementById("sd-avatar-crop-frame")?.addEventListener("mousedown", { e: dynamic ->
                cropDragging = true
                cropPinching = false
                val (x, y) = getClientXY(e)
                cropDragStartX = x
                cropDragStartY = y
                cropDragOffsetStartX = cropState[0]
                cropDragOffsetStartY = cropState[1]
                (e as? org.w3c.dom.events.Event)?.preventDefault()
            })
            document.getElementById("sd-avatar-crop-frame")?.addEventListener("touchstart", { e: dynamic ->
                val touchCount = (e?.touches?.asDynamic()?.length as? Int) ?: 0
                if (touchCount >= 2) {
                    cropPinching = true
                    cropDragging = false
                    cropPinchStartDist = getTouchDistance(e).coerceAtLeast(1.0)
                    cropPinchStartScale = cropState[2]
                } else {
                    cropDragging = true
                    cropPinching = false
                    val (x, y) = getClientXY(e)
                    cropDragStartX = x
                    cropDragStartY = y
                    cropDragOffsetStartX = cropState[0]
                    cropDragOffsetStartY = cropState[1]
                }
                (e as? org.w3c.dom.events.Event)?.preventDefault()
            })
            root.addEventListener("mousemove", { e: dynamic ->
                if (!cropDragging) return@addEventListener
                val (x, y) = getClientXY(e)
                cropState[0] = cropDragOffsetStartX + (x - cropDragStartX)
                cropState[1] = cropDragOffsetStartY + (y - cropDragStartY)
                updateCropImgTransform()
            })
            root.addEventListener("touchmove", { e: dynamic ->
                val touchCount = (e?.touches?.asDynamic()?.length as? Int) ?: 0
                if (cropPinching && touchCount >= 2) {
                    val d = getTouchDistance(e).coerceAtLeast(1.0)
                    val newScale = (cropPinchStartScale * d / cropPinchStartDist).coerceIn(0.5, 2.0)
                    cropState[2] = newScale
                    syncCropScaleSlider()
                    updateCropImgTransform()
                    (e as? org.w3c.dom.events.Event)?.preventDefault()
                } else if (cropDragging && touchCount >= 1) {
                    val (x, y) = getClientXY(e)
                    cropState[0] = cropDragOffsetStartX + (x - cropDragStartX)
                    cropState[1] = cropDragOffsetStartY + (y - cropDragStartY)
                    updateCropImgTransform()
                    (e as? org.w3c.dom.events.Event)?.preventDefault()
                }
            })
            root.addEventListener("mouseup", { _: dynamic -> cropDragging = false })
            root.addEventListener("touchend", { e: dynamic ->
                cropDragging = false
                val touchCount = (e?.touches?.asDynamic()?.length as? Int) ?: 0
                if (touchCount < 2) cropPinching = false
            })
            document.getElementById("sd-avatar-crop-apply")?.addEventListener("click", {
                fun uploadAndRefreshAvatar(usr: User, img: org.w3c.dom.HTMLElement, resultDataUrl: String) {
                    uploadChatAvatar(usr.id, resultDataUrl) { err ->
                        if (err != null) {
                            updateState { networkError = err }
                        } else {
                            // Обновляем state только когда пришёл chatAvatarUrl, иначе перерисовка заменит превью на пустое → моргание и белый круг
                            fun tryRefresh(attempt: Int) {
                                val maxAttempts = 8
                                val delayMs = 400
                                getCurrentUser { newUser, _ ->
                                    val url = newUser?.chatAvatarUrl?.takeIf { it.isNotBlank() }
                                    if (url != null) {
                                        img.setAttribute("src", url)
                                        // localStorage не очищаем — при ошибке загрузки URL (CORS и т.д.) fallback в onerror подставит data URL
                                        updateState { user = newUser }
                                        getUsersForChat(usr) { list ->
                                            updateState { chatContacts = list }
                                        }
                                    } else if (attempt < maxAttempts) {
                                        window.setTimeout({ tryRefresh(attempt + 1) }, delayMs)
                                    } else if (newUser != null) {
                                        updateState { user = newUser }
                                        getUsersForChat(usr) { list ->
                                            updateState { chatContacts = list }
                                        }
                                    }
                                }
                            }
                            window.setTimeout({ tryRefresh(0) }, 600)
                        }
                    }
                }
                val dataUrl = cropDataUrlHolder[0] ?: return@addEventListener
                val cropImg = document.getElementById("sd-avatar-crop-img") as? org.w3c.dom.HTMLImageElement ?: return@addEventListener
                if (cropImg.naturalWidth == 0) return@addEventListener
                val scale = cropState[2]
                val ox = cropState[0]
                val oy = cropState[1]
                val nw = cropImg.naturalWidth.toDouble()
                val nh = cropImg.naturalHeight.toDouble()
                val canvas = document.createElement("canvas").unsafeCast<org.w3c.dom.HTMLCanvasElement>()
                val size = 200
                canvas.width = size
                canvas.height = size
                val ctx = canvas.getContext("2d")?.unsafeCast<dynamic>() ?: return@addEventListener
                ctx.save()
                // Фон как у превью, чтобы без прозрачности не было белого в круге
                ctx.fillStyle = "rgba(0,0,0,0.06)"
                ctx.fillRect(0.0, 0.0, size.toDouble(), size.toDouble())
                ctx.beginPath()
                ctx.arc(size / 2.0, size / 2.0, size / 2.0, 0.0, 6.283185307179586)
                ctx.closePath()
                ctx.clip()
                ctx.translate(size / 2.0 + ox, size / 2.0 + oy)
                ctx.scale(scale, scale)
                ctx.drawImage(cropImg, -nw / 2.0, -nh / 2.0, nw, nh)
                ctx.restore()
                val resultDataUrl = try { canvas.toDataURL("image/png") } catch (_: Throwable) { "" }
                val finalDataUrl = if (resultDataUrl.isNotEmpty()) resultDataUrl else dataUrl
                document.getElementById("sd-avatar-crop-editor")?.unsafeCast<org.w3c.dom.HTMLElement>()?.classList?.add("sd-hidden")
                cropDataUrlHolder[0] = null
                setChatAvatarDataUrl(usr.id, finalDataUrl)
                val preview = document.getElementById("sd-settings-avatar-preview") ?: return@addEventListener
                preview.querySelector(".sd-settings-avatar-placeholder")?.remove()
                var img = preview.querySelector(".sd-settings-avatar-img") as? org.w3c.dom.HTMLElement
                if (img == null) {
                    img = document.createElement("img").unsafeCast<org.w3c.dom.HTMLElement>()
                    img.className = "sd-settings-avatar-img sd-avatar-img"
                    img.setAttribute("alt", "")
                    img.id = "sd-settings-avatar-img"
                    img.setAttribute("data-user-id", usr.id)
                    preview.appendChild(img)
                }
                img.setAttribute("src", finalDataUrl)
                updateState { }
                uploadAndRefreshAvatar(usr, img, finalDataUrl)
            })
            document.getElementById("sd-avatar-crop-cancel")?.addEventListener("click", {
                document.getElementById("sd-avatar-crop-editor")?.unsafeCast<org.w3c.dom.HTMLElement>()?.classList?.add("sd-hidden")
                cropDataUrlHolder[0] = null
                document.getElementById("sd-settings-avatar-file")?.let { it.unsafeCast<dynamic>().value = "" }
            })
            document.getElementById("sd-settings-avatar-remove")?.addEventListener("click", {
                setChatAvatarDataUrl(usr.id, null)
                removeChatAvatar(usr.id) { err ->
                    if (err != null) updateState { networkError = err }
                    else getCurrentUser { newUser, _ -> if (newUser != null) updateState { user = newUser } }
                }
                val preview = document.getElementById("sd-settings-avatar-preview") ?: return@addEventListener
                preview.querySelector(".sd-settings-avatar-img")?.remove()
                val placeholder = document.createElement("span").unsafeCast<org.w3c.dom.HTMLElement>()
                placeholder.className = "sd-settings-avatar-placeholder"
                placeholder.id = "sd-settings-avatar-placeholder"
                placeholder.textContent = usr.fullName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }
                preview.appendChild(placeholder)
                updateState { }
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

private fun hideChatMessageMenu() {
    document.getElementById("sd-chat-msg-menu")?.remove()
}

private fun showChatMessageMenu(anchorEl: org.w3c.dom.Element, x: Double, y: Double, uid: String, roomId: String, messageId: String, messageText: String) {
    hideChatMessageMenu()
    val menu = document.createElement("div").unsafeCast<org.w3c.dom.HTMLElement>()
    menu.id = "sd-chat-msg-menu"
    menu.className = "sd-chat-msg-menu"
    menu.innerHTML = """
        <button type="button" class="sd-chat-msg-menu-item" data-action="reply">Ответить</button>
        <button type="button" class="sd-chat-msg-menu-item" data-action="copy">Копировать</button>
        <button type="button" class="sd-chat-msg-menu-item sd-chat-msg-menu-item-danger" data-action="delete">Удалить</button>
    """.trimIndent()
    document.body?.appendChild(menu)

    val viewportW = window.innerWidth.toDouble()
    val viewportH = window.innerHeight.toDouble()
    val rect = menu.getBoundingClientRect()
    val left = x.coerceIn(8.0, (viewportW - rect.width - 8.0).coerceAtLeast(8.0))
    val top = y.coerceIn(8.0, (viewportH - rect.height - 8.0).coerceAtLeast(8.0))
    menu.style.left = "${left}px"
    menu.style.top = "${top}px"

    menu.addEventListener("click", { ev: dynamic ->
        val target = ev?.target as? org.w3c.dom.Element ?: return@addEventListener
        val btn = target.closest(".sd-chat-msg-menu-item") as? org.w3c.dom.Element ?: return@addEventListener
        when (btn.getAttribute("data-action")) {
            "reply" -> {
                updateState { chatReplyToMessageId = messageId; chatReplyToText = messageText }
                val bubble = anchorEl.querySelector(".sd-msg-bubble-wrap")
                bubble?.unsafeCast<org.w3c.dom.HTMLElement>()?.scrollIntoView()
            }
            "copy" -> {
                val text = messageText
                val nav = js("navigator").unsafeCast<dynamic>()
                val clip = nav.clipboard
                if (clip != null && clip.writeText != null) {
                    clip.writeText(text).catch { _: dynamic -> Unit }
                } else {
                    val ta = document.createElement("textarea").unsafeCast<org.w3c.dom.HTMLTextAreaElement>()
                    ta.value = text
                    document.body?.appendChild(ta)
                    ta.select()
                    try { document.execCommand("copy") } catch (_: Throwable) { }
                    ta.remove()
                }
            }
            "delete" -> {
                val msg = appState.chatMessages.find { it.id == messageId }
                if (msg != null && msg.isVoice && msg.senderId == uid) {
                    deleteVoiceMessageFully(roomId, messageId, msg.voiceUrl).catch { _: dynamic -> Unit }
                } else {
                    deleteMessageForUser(roomId, messageId, uid).catch { _: dynamic -> Unit }
                }
            }
        }
        hideChatMessageMenu()
        (ev as? org.w3c.dom.events.Event)?.preventDefault()
        (ev as? org.w3c.dom.events.Event)?.stopPropagation()
    }, true)
}

private fun handleChatAdminFileSelected(uid: String?, fileInput: HTMLInputElement?) {
    if (uid == null) return
    val file = fileInput?.files?.item(0) ?: return
    val size = (file.asDynamic().size as? Number)?.toDouble()?.toLong() ?: 0L
    if (size > CHAT_ADMIN_FILE_MAX_BYTES) {
        updateState { networkError = "Файл больше 8 МБ." }
        fileInput.value = ""
        return
    }
    val roomId = when {
        appState.selectedChatGroupId != null -> groupChatRoomId(appState.selectedChatGroupId!!)
        else -> {
            val contactId = appState.selectedChatContactId ?: return
            chatRoomId(uid, contactId)
        }
    }
    val chatInput = document.getElementById("sd-chat-input") as? HTMLInputElement
    val caption = (chatInput?.value ?: "").trim()
    val fileName = (file.asDynamic().name as? String) ?: "file"
    val mime = (file.asDynamic().type as? String)?.takeIf { it.isNotBlank() } ?: "application/octet-stream"
    val reader = js("new FileReader()").unsafeCast<dynamic>()
    reader.onload = { _: dynamic ->
        val dataUrl = reader.result as? String
        if (dataUrl == null) {
            updateState { networkError = "Не удалось прочитать файл." }
            fileInput.value = ""
        } else {
            val base64 = dataUrl.substringAfter("base64,")
            showToast("Отправка файла…")
            uploadChatAdminFile(roomId, fileName, mime, base64) { err, url ->
                fileInput.value = ""
                if (err != null) {
                    updateState { networkError = err }
                } else if (url == null) {
                    updateState { networkError = "Пустой ответ сервера" }
                } else {
                    sendFileMessage(
                        roomId,
                        uid,
                        caption,
                        url,
                        fileName,
                        mime,
                        appState.chatReplyToMessageId,
                        appState.chatReplyToText,
                    ).then {
                        chatInput?.value = ""
                        updateState { chatReplyToMessageId = null; chatReplyToText = null }
                    }.catch { _ ->
                        updateState { networkError = "Не удалось отправить сообщение с файлом." }
                    }
                }
            }
        }
    }
    reader.onerror = { _: dynamic ->
        updateState { networkError = "Ошибка чтения файла." }
        fileInput.value = ""
    }
    reader.readAsDataURL(file.asDynamic())
}

private fun sendChatMessage(chatInput: HTMLInputElement?, uid: String?) {
    val text = (chatInput?.value ?: "").trim()
    if (text.isBlank() || uid == null) return
    val roomId = when {
        appState.selectedChatGroupId != null -> groupChatRoomId(appState.selectedChatGroupId!!)
        else -> {
            val contactId = appState.selectedChatContactId ?: return
            chatRoomId(uid, contactId)
        }
    }
    val replyId = appState.chatReplyToMessageId
    val replyText = appState.chatReplyToText
    sendMessage(roomId, uid, text, replyId, replyText)
        .then {
            chatInput?.value = ""
            updateState { chatReplyToMessageId = null; chatReplyToText = null }
        }
        .catch { _ ->
            updateState { networkError = "Не удалось отправить сообщение." }
        }
}

/** Запись/превью голоса — [ChatVoiceWeb]; здесь только остановка проигрывания сообщений в ленте. */
private fun abortChatVoiceMediaBeforeRoomSwitch() {
    abortChatVoiceRecordingAndPreview()
    if (voicePlayInterval != 0) {
        window.clearInterval(voicePlayInterval)
        voicePlayInterval = 0
    }
    try {
        document.getElementById("sd-global-voice-audio")?.asDynamic()?.pause?.invoke()
    } catch (_: Throwable) { }
}

private fun getOrCreateGlobalVoiceAudio(): dynamic {
    var el = document.getElementById("sd-global-voice-audio")
    if (el == null) {
        el = document.createElement("audio")
        el.id = "sd-global-voice-audio"
        el.setAttribute("class", "sd-voice-audio")
        el.setAttribute("preload", "auto")
        el.asDynamic().crossOrigin = "anonymous"
        el.asDynamic().muted = false
        document.body?.appendChild(el)
    }
    return el.asDynamic()
}

/** Обновление позиции по таймеру, пока глобальный audio играет (не вызывать в паузе). */
private fun startVoicePlaybackTicker(durationSec: Int) {
    if (voicePlayInterval != 0) {
        window.clearInterval(voicePlayInterval)
        voicePlayInterval = 0
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
            appState.chatVoicePlaybackPaused = false
            appState.chatPlayingVoiceCurrentMs = durationSec * 1000
            patchVoicePlayerDOM()
        }
    }, 200).unsafeCast<Int>()
}

private fun toggleVoicePlay(audioEl: dynamic, msgId: String, voiceUrl: String, durationSec: Int) {
    val globalAudio = getOrCreateGlobalVoiceAudio()
    val activeId = appState.chatPlayingVoiceId
    val paused = appState.chatVoicePlaybackPaused

    if (activeId == msgId) {
        if (!paused) {
            globalAudio.pause()
            appState.chatPlayingVoiceCurrentMs = ((globalAudio.currentTime as Double) * 1000).toInt()
            appState.chatVoicePlaybackPaused = true
            if (voicePlayInterval != 0) {
                window.clearInterval(voicePlayInterval)
                voicePlayInterval = 0
            }
            patchVoicePlayerDOM()
            return
        } else {
            appState.chatVoicePlaybackPaused = false
            val playPromise = globalAudio.play()
            if (playPromise != null) {
                js("(function(p){ if(p&&typeof p.catch==='function') p.catch(function(){ }); })").unsafeCast<(dynamic) -> Unit>().invoke(playPromise)
            }
            startVoicePlaybackTicker(durationSec)
            patchVoicePlayerDOM()
            return
        }
    }

    if (activeId != null) {
        globalAudio.pause()
        if (voicePlayInterval != 0) {
            window.clearInterval(voicePlayInterval)
            voicePlayInterval = 0
        }
    }
    if (voiceUrl.isBlank() || (!voiceUrl.startsWith("http") && !voiceUrl.startsWith("data:"))) return
    appState.chatPlayingVoiceId = msgId
    appState.chatVoicePlaybackPaused = false
    appState.chatPlayingVoiceCurrentMs = 0
    patchVoicePlayerDOM()
    globalAudio.src = voiceUrl
    globalAudio.currentTime = 0.0
    val playPromise = globalAudio.play()
    if (playPromise != null) {
        js("(function(p){ if(p&&typeof p.catch==='function') p.catch(function(){ }); })").unsafeCast<(dynamic) -> Unit>().invoke(playPromise)
    }
    globalAudio.onended = {
        if (voicePlayInterval != 0) {
            window.clearInterval(voicePlayInterval)
            voicePlayInterval = 0
        }
        appState.chatPlayingVoiceId = null
        appState.chatVoicePlaybackPaused = false
        appState.chatPlayingVoiceCurrentMs = durationSec * 1000
        patchVoicePlayerDOM()
    }
    startVoicePlaybackTicker(durationSec)
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
        if (voicePlayInterval != 0) {
            window.clearInterval(voicePlayInterval)
            voicePlayInterval = 0
        }
        globalAudio.pause()
        globalAudio.src = voiceUrl
        globalAudio.currentTime = r * durationSec
        appState.chatPlayingVoiceId = msgId
        appState.chatVoicePlaybackPaused = true
        appState.chatPlayingVoiceCurrentMs = currentMs
        patchVoicePlayerDOM()
    }
}

private fun attachVoiceProgressDrag(wrap: org.w3c.dom.Element, msgId: String, voiceUrl: String, durationSec: Int) {
    window.asDynamic().__sdSeekVoice = { a: String, b: String, c: Int, d: Double -> seekToVoicePosition(a, b, c, d) }
    val setupDrag = js("(function(wrap, msgId, voiceUrl, durationSec){ var getRatio=function(cx){ var r=wrap.getBoundingClientRect(); if(r.width<=0)return 0; var x=cx-r.left; return Math.max(0,Math.min(1,x/r.width)); }; var onDown=function(e){ e.preventDefault(); var cx=e.clientX!=null?e.clientX:e.touches[0].clientX; if(window.__sdSeekVoice) window.__sdSeekVoice(msgId,voiceUrl,durationSec,getRatio(cx)); var onMove=function(ev){ ev.preventDefault(); var x=ev.clientX!=null?ev.clientX:(ev.touches&&ev.touches[0]?ev.touches[0].clientX:0); if(window.__sdSeekVoice) window.__sdSeekVoice(msgId,voiceUrl,durationSec,getRatio(x)); }; var onUp=function(){ document.removeEventListener('mousemove',onMove); document.removeEventListener('mouseup',onUp); document.removeEventListener('touchmove',onMove); document.removeEventListener('touchend',onUp); }; document.addEventListener('mousemove',onMove); document.addEventListener('mouseup',onUp); document.addEventListener('touchmove',onMove); document.addEventListener('touchend',onUp); }; wrap.addEventListener('mousedown',onDown); wrap.addEventListener('touchstart',onDown,{passive:false}); })")
    setupDrag.unsafeCast<(dynamic, dynamic, dynamic, dynamic) -> Unit>().invoke(wrap, msgId, voiceUrl, durationSec)
}
