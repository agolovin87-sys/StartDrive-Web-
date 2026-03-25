/**
 * Голосовые в чате: не в стиле WhatsApp (без «полоски» записи и без удержания).
 * Схема: кнопка «микрофон» → запись с явными кнопками → превью → отправить.
 */
import firebase.chatRoomId
import firebase.groupChatRoomId
import firebase.sendVoiceMessage
import firebase.subscribeMessages
import kotlin.js.unsafeCast
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import kotlin.js.js

private val CHAT_VOICE_ICON_TRASH = """<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>"""

private val CHAT_VOICE_ICON_PLAY = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>"""
private val CHAT_VOICE_ICON_PAUSE = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>"""
/** Квадрат «стоп» — явно не «пауза как в мессенджерах». */
private val CHAT_VOICE_ICON_STOP_SQUARE = """<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="6" width="12" height="12" rx="1.5"/></svg>"""
/** Галочка «готово к превью» — не иконка «отправить», чтобы не путать с WhatsApp. */
private val CHAT_VOICE_ICON_DONE = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>"""

private fun String.chatVoiceEscapeHtml(): String = this

private var voiceRecorderChunks: dynamic = null
private var voiceRecorderStream: dynamic = null
private var voiceRecorder: dynamic = null
private var voiceRecorderMimeType: String = "audio/webm"
private var voiceMicEpoch: Int = 0
private var voiceDiscardOnStop: Boolean = false
private var voiceReviewBlob: dynamic = null
private var voiceReviewObjectUrl: String? = null
private var voiceReviewAudioBoundForUrl: String? = null

/** Таймер обновления «Запись… M:SS» (только во время записи). */
internal var chatVoiceRecordTickInterval: Int = 0

private var chatVoiceHostElement: Element? = null

internal fun chatVoiceIsWebAndroidLike(): Boolean {
    val ua = js("navigator.userAgent").unsafeCast<String>()
    return ua.contains("Android", ignoreCase = true) ||
        (ua.contains("Linux", ignoreCase = true) && ua.contains("Mobile", ignoreCase = true)) ||
        ua.contains("SamsungBrowser", ignoreCase = true)
}

internal fun buildChatVoiceInputRowInnerHtml(): String? {
    val recording = appState.chatVoiceRecording
    val reviewReady = appState.chatVoiceReviewReady
    if (!recording && !reviewReady) return null
    val elapsed = appState.chatVoiceRecordElapsedSec
    val recordTimeStr = "${elapsed / 60}:${(elapsed % 60).toString().padStart(2, '0')}"
    fun formatVoiceDuration(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "$m:${s.toString().padStart(2, '0')}"
    }
    val iconSendSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>"""
    val iconMicSvg = """<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>"""
    val iconPlaySvg = CHAT_VOICE_ICON_PLAY
    if (recording) {
        return """<div class="sd-chat-input-row sd-chat-voice-recording sd-chat-voice-recording-simple" id="sd-chat-voice-recording-review">
                    <div class="sd-chat-voice-simple-recording" aria-live="polite">
                        <span class="sd-chat-voice-simple-recording-label">Запись</span>
                        <span class="sd-chat-voice-simple-recording-time">$recordTimeStr</span>
                    </div>
                    <div class="sd-chat-voice-review-actions sd-chat-voice-review-actions-recording">
                        <button type="button" id="sd-chat-voice-review-delete" class="sd-chat-voice-review-icon-btn" title="Удалить">$CHAT_VOICE_ICON_TRASH</button>
                        <button type="button" id="sd-chat-voice-review-pause" class="sd-chat-voice-review-icon-btn" title="Стоп записи" aria-label="Стоп записи">$CHAT_VOICE_ICON_STOP_SQUARE</button>
                        <button type="button" id="sd-chat-voice-review-send" class="sd-chat-voice-review-icon-btn sd-chat-voice-review-send-btn" title="Готово — прослушать перед отправкой" aria-label="Готово">$CHAT_VOICE_ICON_DONE</button>
                    </div>
                </div>"""
    }
    val dur = appState.chatVoiceReviewDurationSec
    val totalStr = formatVoiceDuration(dur).chatVoiceEscapeHtml()
    val localUrl = appState.chatVoiceReviewLocalUrl?.takeIf { it.isNotBlank() }?.chatVoiceEscapeHtml() ?: ""
    return """<div class="sd-chat-input-row sd-chat-voice-record-review-ready" id="sd-chat-voice-record-review-ready">
                    <div class="sd-chat-voice-review-player sd-chat-voice-review-player--compact">
                        <button type="button" id="sd-chat-voice-review-play-btn" class="sd-chat-voice-review-play-btn" title="Воспроизвести">$iconPlaySvg</button>
                        <div class="sd-chat-voice-review-times">
                            <span id="sd-chat-voice-review-current" class="sd-chat-voice-review-current">0:00</span>
                            <span class="sd-chat-voice-review-sep">/</span>
                            <span class="sd-chat-voice-review-total">$totalStr</span>
                        </div>
                        <audio id="sd-chat-voice-review-audio" class="sd-chat-voice-review-audio" preload="metadata" src="$localUrl"></audio>
                    </div>
                    <div class="sd-chat-voice-review-actions">
                        <button type="button" id="sd-chat-voice-review-delete" class="sd-chat-voice-review-icon-btn" title="Удалить">$CHAT_VOICE_ICON_TRASH</button>
                        <button type="button" id="sd-chat-voice-review-mic" class="sd-chat-voice-review-icon-btn" title="Записать снова">$iconMicSvg</button>
                        <button type="button" id="sd-chat-voice-review-send" class="sd-chat-voice-review-icon-btn sd-chat-voice-review-send-btn" title="Отправить в чат">$iconSendSvg</button>
                    </div>
                </div>"""
}

internal fun chatVoicePlaceholderRowHtml(): String {
    val preview = appState.chatVoiceReviewReady && !appState.chatVoiceRecording
    val extra = if (preview) " sd-chat-voice-placeholder--preview" else ""
    return """<div id="sd-chat-voice-placeholder" class="sd-chat-voice-placeholder$extra" aria-hidden="true"></div>"""
}

private fun ensureChatVoiceHostElement(): org.w3c.dom.Element {
    var el = chatVoiceHostElement
    if (el != null && el.parentNode == null) {
        chatVoiceHostElement = null
        el = null
    }
    if (el == null) {
        el = document.getElementById("sd-chat-voice-host")
    }
    if (el == null) {
        el = document.createElement("div")
        el.id = "sd-chat-voice-host"
        el.setAttribute("class", "sd-chat-voice-host sd-hidden")
    }
    chatVoiceHostElement = el
    document.body?.appendChild(el!!)
    return el!!
}

internal fun syncChatVoiceHostOverlay() {
    val host = ensureChatVoiceHostElement()
    if (!chatVoiceIsWebAndroidLike()) {
        host.innerHTML = ""
        host.classList.add("sd-hidden")
        return
    }
    if (!appState.chatVoiceRecording && !appState.chatVoiceReviewReady) {
        host.innerHTML = ""
        host.classList.add("sd-hidden")
        return
    }
    val uid = appState.user?.id
    val inChat = uid != null &&
        (appState.selectedChatContactId != null || appState.selectedChatGroupId != null) &&
        !appState.chatSettingsOpen
    if (!inChat) {
        host.innerHTML = ""
        host.classList.add("sd-hidden")
        return
    }
    val html = buildChatVoiceInputRowInnerHtml() ?: ""
    host.innerHTML = html
    if (html.isNotBlank()) {
        host.classList.remove("sd-hidden")
    } else {
        host.classList.add("sd-hidden")
    }
}

internal fun startVoiceRecording(uid: String?) {
    if (uid == null) return
    if (appState.selectedChatGroupId == null && appState.selectedChatContactId == null) return
    if (voiceRecorder != null || voiceRecorderStream != null) {
        abortChatVoiceRecordingAndPreview()
    }
    val roomId = when {
        appState.selectedChatGroupId != null -> groupChatRoomId(appState.selectedChatGroupId!!)
        else -> chatRoomId(uid, appState.selectedChatContactId!!)
    }

    voiceMicEpoch++
    val myEpoch = voiceMicEpoch

    if (appState.chatVoiceReviewReady) discardVoiceReview()
    voiceDiscardOnStop = false
    voiceReviewBlob = null

    voiceReviewObjectUrl?.let { old ->
        js("(function(u){ try { URL.revokeObjectURL(u); } catch(e) {} })").unsafeCast<(String) -> Unit>()(old)
    }
    voiceReviewObjectUrl = null

    val nav = js("navigator").unsafeCast<dynamic>()
    val mediaDevices = nav.mediaDevices
    if (mediaDevices == null || mediaDevices.getUserMedia == null) {
        updateState {
            networkError = "Нужен доступ к микрофону для голосовых сообщений."
            chatVoiceRecording = false
            chatVoiceRecordElapsedSec = 0
            chatVoiceReviewReady = false
            chatVoiceReviewLocalUrl = null
            chatVoiceReviewDurationSec = 0
        }
        return
    }

    voiceRecorderChunks = js("[]")
    val constraints = js("({ audio: true })")
    mediaDevices.getUserMedia(constraints).then { stream: dynamic ->
        if (myEpoch != voiceMicEpoch) {
            js("(function(s){ if(s&&s.getTracks) s.getTracks().forEach(function(t){ t.stop(); }); })").unsafeCast<(dynamic) -> Unit>().invoke(stream)
            return@then
        }
        voiceRecorderStream = stream
        val Recorder = js("window.MediaRecorder")
        if (Recorder == undefined) {
            if (myEpoch != voiceMicEpoch) {
                js("(function(s){ if(s&&s.getTracks) s.getTracks().forEach(function(t){ t.stop(); }); })").unsafeCast<(dynamic) -> Unit>().invoke(stream)
                return@then
            }
            updateState {
                networkError = "MediaRecorder не поддерживается в этом браузере."
                chatVoiceRecording = false
                chatVoiceRecordElapsedSec = 0
                chatVoiceReviewReady = false
                chatVoiceReviewLocalUrl = null
                chatVoiceReviewDurationSec = 0
            }
            js("(function(s){ if(s&&s.getTracks) s.getTracks().forEach(function(t){ t.stop(); }); })").unsafeCast<(dynamic) -> Unit>().invoke(stream)
            return@then
        }
        val mime = js("(function(){ if(typeof MediaRecorder!=='undefined'&&MediaRecorder.isTypeSupported&&MediaRecorder.isTypeSupported('audio/webm;codecs=opus')) return 'audio/webm;codecs=opus'; if(typeof MediaRecorder!=='undefined'&&MediaRecorder.isTypeSupported&&MediaRecorder.isTypeSupported('audio/webm')) return 'audio/webm'; return 'audio/webm'; })()").unsafeCast<String>()
        voiceRecorderMimeType = mime
        val opts = js("(function(m){ return { mimeType: m }; })")(mime)
        val recorder = js("new MediaRecorder(stream, opts)").unsafeCast<dynamic>()
        recorder.ondataavailable = { e: dynamic ->
            val data = e?.data
            val buf = voiceRecorderChunks
            if (data != null && buf != null) {
                js("(function(a,d){ if(a&&d) a.push(d); })")(buf, data)
            }
        }

        recorder.onstop = onStop@{ _: dynamic ->
            val chunks = voiceRecorderChunks
            val mimeType = voiceRecorderMimeType
            val stopStream = voiceRecorderStream

            js("(function(s){ if(s&&s.getTracks) s.getTracks().forEach(function(t){ t.stop(); }); })").unsafeCast<(dynamic) -> Unit>().invoke(stopStream)

            voiceRecorderStream = null
            voiceRecorder = null
            voiceRecorderChunks = null
            if (chatVoiceRecordTickInterval != 0) {
                window.clearInterval(chatVoiceRecordTickInterval)
                chatVoiceRecordTickInterval = 0
            }

            val uidNow = appState.user?.id
            val roomNow = if (uidNow != null) {
                when {
                    appState.selectedChatGroupId != null -> groupChatRoomId(appState.selectedChatGroupId!!)
                    appState.selectedChatContactId != null -> chatRoomId(uidNow, appState.selectedChatContactId!!)
                    else -> null
                }
            } else null
            if (roomNow != null && roomNow != roomId) {
                voiceDiscardOnStop = false
                updateState {
                    chatVoiceRecording = false
                    chatVoiceRecordElapsedSec = 0
                    chatVoiceReviewReady = false
                    chatVoiceReviewLocalUrl = null
                    chatVoiceReviewDurationSec = 0
                }
                return@onStop
            }

            val startMs = appState.chatVoiceRecordStartMs
            val durationSec = ((js("Date.now()").unsafeCast<Double>() - startMs) / 1000.0).toInt().coerceAtLeast(1)

            updateState {
                chatVoiceRecording = false
                chatVoiceRecordElapsedSec = 0
            }

            val chunkLen: Int = when {
                chunks == null -> 0
                else -> js("(function(c){ try { return (c && c.length) ? (c.length|0) : 0; } catch(e){ return 0; } })")
                    .unsafeCast<(dynamic) -> Int>().invoke(chunks)
            }
            fun clearVoicePreviewOnly() {
                updateState {
                    chatVoiceReviewReady = false
                    chatVoiceReviewLocalUrl = null
                    chatVoiceReviewDurationSec = 0
                }
            }
            // В некоторых браузерах при частых stop/delete `chunks` может быть null/пусто.
            // Если это не «Удалить», не скрываем UI: показываем превью-кнопки, чтобы пользователь мог нажать «микрофон» и записать заново.
            if (chunks == null || chunkLen == 0) {
                if (voiceDiscardOnStop) {
                    voiceDiscardOnStop = false
                    clearVoicePreviewOnly()
                    return@onStop
                }
                updateState {
                    chatVoiceRecording = false
                    chatVoiceReviewReady = true
                    chatVoiceReviewLocalUrl = null
                    chatVoiceReviewDurationSec = durationSec
                }
                return@onStop
            }
            if (voiceDiscardOnStop) {
                voiceDiscardOnStop = false
                clearVoicePreviewOnly()
                return@onStop
            }

            val blob = js("(function(c,t){ return new Blob(c, { type: t || 'audio/webm' }); })").unsafeCast<(dynamic, String) -> dynamic>().invoke(chunks, mimeType)

            voiceReviewBlob = blob
            val objUrl = js("(function(b){ return URL.createObjectURL(b); })").unsafeCast<(dynamic) -> String>()(blob)
            voiceReviewObjectUrl = objUrl
            updateState {
                chatVoiceReviewReady = true
                chatVoiceReviewLocalUrl = objUrl
                chatVoiceReviewDurationSec = durationSec
            }
        }

        if (myEpoch != voiceMicEpoch) {
            js("(function(s){ if(s&&s.getTracks) s.getTracks().forEach(function(t){ t.stop(); }); })").unsafeCast<(dynamic) -> Unit>().invoke(stream)
            voiceRecorderStream = null
            return@then
        }
        updateState {
            chatVoiceRecording = true
            chatVoiceReviewReady = false
            chatVoiceReviewLocalUrl = null
            chatVoiceReviewDurationSec = 0
            chatVoiceRecordStartMs = js("Date.now()").unsafeCast<Double>()
            chatVoiceRecordElapsedSec = 0
        }
        recorder.start(1000)
        voiceRecorder = recorder
    }.catch { _: dynamic ->
        if (myEpoch != voiceMicEpoch) return@catch
        updateState { networkError = "Нужен доступ к микрофону для голосовых сообщений." }
        updateState {
            chatVoiceRecording = false
            chatVoiceReviewReady = false
            chatVoiceReviewLocalUrl = null
            chatVoiceReviewDurationSec = 0
        }
    }
}

internal fun stopVoiceRecording() {
    val rec = voiceRecorder
    if (rec == null) return
    try {
        rec.stop()
    } catch (_: Throwable) { }
}

/**
 * Останавливает запись/превью и освобождает Blob URL (без остановки проигрывания уже отправленных сообщений в ленте).
 * Полную очистку плеера сообщений делает [abortChatVoiceMediaBeforeRoomSwitch] в Main.
 */
internal fun abortChatVoiceRecordingAndPreview() {
    voiceMicEpoch++
    val rec = voiceRecorder
    if (rec != null) {
        voiceDiscardOnStop = true
        try {
            rec.unsafeCast<dynamic>().stop()
        } catch (_: Throwable) { }
    } else {
        val stream = voiceRecorderStream
        if (stream != null) {
            js("(function(s){ if(s&&s.getTracks) s.getTracks().forEach(function(t){ t.stop(); }); })").unsafeCast<(dynamic) -> Unit>().invoke(stream)
        }
        voiceRecorderStream = null
    }
    if (chatVoiceRecordTickInterval != 0) {
        window.clearInterval(chatVoiceRecordTickInterval)
        chatVoiceRecordTickInterval = 0
    }
    try {
        document.getElementById("sd-chat-voice-review-audio")?.asDynamic()?.pause?.invoke()
    } catch (_: Throwable) { }
    voiceReviewBlob = null
    voiceReviewObjectUrl?.let { old ->
        js("(function(u){ try { URL.revokeObjectURL(u); } catch(e) {} })").unsafeCast<(String) -> Unit>()(old)
    }
    voiceReviewObjectUrl = null
    voiceReviewAudioBoundForUrl = null
}

internal fun AppState.resetChatVoiceFieldsForNewRoom() {
    chatVoiceRecording = false
    chatVoiceReviewReady = false
    chatVoiceReviewLocalUrl = null
    chatVoiceReviewDurationSec = 0
    chatVoiceRecordElapsedSec = 0
    chatVoiceRecordStartMs = 0.0
    val err = networkError
    if (err != null && (err.contains("микрофон", ignoreCase = true) || err.contains("MediaRecorder", ignoreCase = true))) {
        networkError = null
    }
}

private fun discardVoiceReview() {
    val audio = document.getElementById("sd-chat-voice-review-audio")?.asDynamic()
    try {
        audio?.pause?.invoke()
    } catch (_: Throwable) { }

    voiceReviewBlob = null
    voiceReviewObjectUrl?.let { old ->
        js("(function(u){ try { URL.revokeObjectURL(u); } catch(e) {} })").unsafeCast<(String) -> Unit>()(old)
    }
    voiceReviewObjectUrl = null
    voiceReviewAudioBoundForUrl = null
    updateState {
        chatVoiceRecording = false
        chatVoiceRecordElapsedSec = 0
        chatVoiceReviewReady = false
        chatVoiceReviewLocalUrl = null
        chatVoiceReviewDurationSec = 0
    }
}

private fun formatVoiceDurationSec(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

private fun sendVoiceReviewMessage() {
    if (!appState.chatVoiceReviewReady) return
    val uid = appState.user?.id ?: return
    if (voiceReviewBlob == null) return
    val roomId = when {
        appState.selectedChatGroupId != null -> groupChatRoomId(appState.selectedChatGroupId!!)
        else -> {
            val contactId = appState.selectedChatContactId ?: return
            chatRoomId(uid, contactId)
        }
    }
    val durationSec = appState.chatVoiceReviewDurationSec

    val blob = voiceReviewBlob
    val objUrlToRevoke = voiceReviewObjectUrl

    updateState {
        chatVoiceRecording = false
        chatVoiceRecordElapsedSec = 0
        chatVoiceReviewReady = false
        chatVoiceReviewLocalUrl = null
        chatVoiceReviewDurationSec = 0
    }

    fun cleanup() {
        voiceReviewBlob = null
        if (objUrlToRevoke != null) {
            js("(function(u){ try { URL.revokeObjectURL(u); } catch(e) {} })").unsafeCast<(String) -> Unit>()(objUrlToRevoke)
        }
        voiceReviewObjectUrl = null
        voiceReviewAudioBoundForUrl = null
    }

    sendVoiceMessage(roomId, uid, blob, durationSec)
        .then {
            subscribeMessages(roomId) { list -> updateState { chatMessages = list } }
            window.setTimeout({ scrollChatToBottom() }, 50)
            cleanup()
        }
        .catch { e: dynamic ->
            updateState { networkError = "Не удалось отправить голосовое: ${(e?.message as? String) ?: "ошибка"}" }
            cleanup()
        }
}

private fun ensureVoiceReviewAudioBound() {
    val localUrl = appState.chatVoiceReviewLocalUrl ?: return
    if (localUrl.isBlank()) return
    if (voiceReviewAudioBoundForUrl == localUrl) return
    voiceReviewAudioBoundForUrl = localUrl

    val audio = document.getElementById("sd-chat-voice-review-audio")?.asDynamic() ?: return
    val currentEl = document.getElementById("sd-chat-voice-review-current")
    val durationSec = appState.chatVoiceReviewDurationSec
    val playBtn = document.getElementById("sd-chat-voice-review-play-btn") as? org.w3c.dom.HTMLButtonElement

    fun patchFromAudio() {
        val curMs = ((audio.currentTime as Double) * 1000).toInt()
        val curSec = (curMs / 1000).coerceAtLeast(0)
        currentEl?.textContent = formatVoiceDurationSec(curSec)
        if (playBtn != null) {
            playBtn.innerHTML = if (audio.paused == false) CHAT_VOICE_ICON_PAUSE else CHAT_VOICE_ICON_PLAY
            playBtn.setAttribute("title", if (audio.paused == false) "Пауза" else "Воспроизвести")
            playBtn.setAttribute("aria-label", if (audio.paused == false) "Пауза" else "Воспроизвести")
        }
    }

    audio.ontimeupdate = { _: dynamic -> patchFromAudio() }
    audio.onended = { _: dynamic ->
        currentEl?.textContent = formatVoiceDurationSec(durationSec)
        if (playBtn != null) {
            playBtn.innerHTML = CHAT_VOICE_ICON_PLAY
            playBtn.setAttribute("title", "Воспроизвести")
            playBtn.setAttribute("aria-label", "Воспроизвести")
        }
    }
    js("(function(a){ try { a.currentTime = 0; } catch(e){} })").unsafeCast<(dynamic) -> Unit>().invoke(audio)
}

private fun toggleVoiceReviewPlay() {
    if (!appState.chatVoiceReviewReady) return
    ensureVoiceReviewAudioBound()
    val audio = document.getElementById("sd-chat-voice-review-audio")?.asDynamic() ?: return
    val playBtn = document.getElementById("sd-chat-voice-review-play-btn") as? org.w3c.dom.HTMLButtonElement
    if (audio.paused == false) {
        audio.pause()
        if (playBtn != null) playBtn.innerHTML = CHAT_VOICE_ICON_PLAY
        return
    }
    audio.play()?.catch { _: dynamic -> Unit }
    if (playBtn != null) playBtn.innerHTML = CHAT_VOICE_ICON_PAUSE
}

/** «none» — не наша кнопка; «skip» — наша зона, без stopPropagation; «consume» — обработали. */
private fun handleChatVoiceUiFromTarget(target: org.w3c.dom.Element): String {
    val voiceCtrlSelector =
        "#sd-chat-voice-review-delete, #sd-chat-voice-review-pause, #sd-chat-voice-review-mic, #sd-chat-voice-review-send, #sd-chat-voice-review-play-btn"
    if (target.closest("#sd-chat-voice-mic") != null) {
        if (appState.chatVoiceRecording || appState.chatVoiceReviewReady) return "skip"
        val uid = appState.user?.id ?: return "skip"
        startVoiceRecording(uid)
        return "consume"
    }
    val ctrl = target.asDynamic().closest(voiceCtrlSelector) as? org.w3c.dom.Element ?: return "none"
    val uid = appState.user?.id ?: return "skip"

    when (ctrl.id) {
        "sd-chat-voice-review-delete" -> {
            if (appState.chatVoiceRecording) {
                voiceDiscardOnStop = true
                stopVoiceRecording()
            } else if (appState.chatVoiceReviewReady) {
                discardVoiceReview()
            }
        }
        "sd-chat-voice-review-pause" -> {
            stopVoiceRecording()
        }
        "sd-chat-voice-review-mic" -> {
            discardVoiceReview()
            startVoiceRecording(uid)
        }
        "sd-chat-voice-review-send" -> {
            if (appState.chatVoiceRecording) {
                stopVoiceRecording()
            } else {
                sendVoiceReviewMessage()
            }
        }
        "sd-chat-voice-review-play-btn" -> {
            toggleVoiceReviewPlay()
        }
        else -> return "none"
    }
    return "consume"
}

/** Один раз: только click (tap), без pointerup — модель «одно нажатие». */
internal fun chatVoiceResetDiscardOnStop() {
    voiceDiscardOnStop = false
}

internal fun installChatVoiceControlsOnce() {
    if (window.asDynamic().__sdChatVoiceControlsDelegation != true) {
        window.asDynamic().__sdChatVoiceControlsDelegation = true

        val voiceCtrlSelector =
            "#sd-chat-voice-review-delete, #sd-chat-voice-review-pause, #sd-chat-voice-review-mic, #sd-chat-voice-review-send, #sd-chat-voice-review-play-btn"

        fun isChatVoiceTarget(t: org.w3c.dom.Element): Boolean =
            t.closest("#sd-chat-voice-mic") != null || t.closest(voiceCtrlSelector) != null

        // Важно: не подавляем touch/pointer события для голосовых кнопок,
        // иначе некоторые Android-браузеры начинают показывать системный оверлей
        // «Отпустите чтобы отправить» на любом касании.

        document.body?.addEventListener("click", click@{ e: dynamic ->
            val target = (e as org.w3c.dom.events.Event).target as? org.w3c.dom.Element ?: return@click
            val r = handleChatVoiceUiFromTarget(target)
            if (r == "none") return@click
            if (r == "consume") {
                (e as? org.w3c.dom.events.Event)?.preventDefault()
                (e as? org.w3c.dom.events.Event)?.stopPropagation()
            }
        }, true)
    }
}
