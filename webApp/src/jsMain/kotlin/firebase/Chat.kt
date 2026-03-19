package firebase

import com.example.startdrive.shared.FirebasePaths

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val status: String = "sent",
    val voiceUrl: String? = null,
    val voiceDurationSec: Int? = null,
) {
    val isVoice: Boolean get() = !voiceUrl.isNullOrBlank() || (voiceDurationSec != null && voiceDurationSec!! > 0)
}

fun chatRoomId(id1: String, id2: String): String {
    val sorted = listOf(id1, id2).sorted()
    return "${sorted[0]}_${sorted[1]}"
}

private fun parseMessage(key: String, m: dynamic): ChatMessage {
    val ts = m?.timestamp
    val timestamp = when (ts) {
        is Number -> ts.toLong()
        else -> (ts?.unsafeCast<Double>())?.toLong() ?: 0L
    }
    val dur = m?.voiceDurationSec
    val durationSec = when (dur) {
        is Number -> dur.toInt()
        else -> null
    }
    return ChatMessage(
        id = key,
        senderId = (m?.senderId as? String) ?: "",
        text = (m?.text as? String) ?: "",
        timestamp = timestamp,
        status = (m?.status as? String) ?: "sent",
        voiceUrl = m?.voiceUrl as? String,
        voiceDurationSec = durationSec,
    )
}

private var currentUnsubscribe: (() -> Unit)? = null

fun subscribeMessages(roomId: String, callback: (List<ChatMessage>) -> Unit) {
    currentUnsubscribe?.invoke()
    val db = getDatabase()
    if (db == null) {
        callback(emptyList())
        return
    }
    val ref = db.ref("${FirebasePaths.CHATS}/$roomId/${FirebasePaths.MESSAGES}").orderByChild("timestamp")
    val listener: (dynamic) -> Unit = { snap: dynamic ->
        val list = mutableListOf<ChatMessage>()
        val val_ = snap?.`val`()
        if (val_ != null && val_ != undefined) {
            val obj = val_.unsafeCast<dynamic>()
            val keys = js("Object.keys(obj)").unsafeCast<Array<String>>()
            keys.forEach { key ->
                val m = obj[key]
                if (m != null) list.add(parseMessage(key, m))
            }
        }
        list.sortBy { it.timestamp }
        callback(list)
    }
    ref.on("value", listener)
    currentUnsubscribe = {
        ref.off("value", listener)
        currentUnsubscribe = null
    }
}

fun unsubscribeChat() {
    currentUnsubscribe?.invoke()
    currentUnsubscribe = null
}

fun sendMessage(roomId: String, senderId: String, text: String): kotlin.js.Promise<Unit> {
    val db = getDatabase() ?: return kotlin.js.Promise.reject(js("Error('Database not initialized')"))
    val ref = db.ref("${FirebasePaths.CHATS}/$roomId/${FirebasePaths.MESSAGES}").push()
    val serverTimestamp = getDatabaseServerTimestamp()
    val data = kotlin.js.json(
        "senderId" to senderId,
        "text" to text,
        "timestamp" to serverTimestamp,
        "status" to "sent"
    )
    return ref.set(data).then { js("undefined") }
}

/** Пометить сообщения как прочитанные (одним мульти-путь update). Вызывать при открытии чата: messageIds — id сообщений от собеседника. */
fun markMessagesAsRead(roomId: String, messageIds: List<String>): kotlin.js.Promise<Unit> {
    val db = getDatabase() ?: return kotlin.js.Promise.reject(js("Error('Database not initialized')"))
    if (messageIds.isEmpty()) return kotlin.js.Promise.resolve(js("undefined"))
    val baseRef = db.ref("${FirebasePaths.CHATS}/$roomId/${FirebasePaths.MESSAGES}")
    val updateObj = js("{}").unsafeCast<dynamic>()
    messageIds.forEach { id -> updateObj["$id/status"] = "read" }
    return baseRef.update(updateObj).then { js("undefined") }
}

/**
 * Загрузка голосового в Storage и запись сообщения в Realtime Database (как в Android).
 * audioBlob — Blob из MediaRecorder (например audio/webm или audio/mp4).
 */
fun sendVoiceMessage(roomId: String, senderId: String, audioBlob: dynamic, durationSec: Int): kotlin.js.Promise<Unit> {
    val db = getDatabase() ?: return kotlin.js.Promise.reject(js("Error('Database not initialized')"))
    val storage = getStorage() ?: return kotlin.js.Promise.reject(js("Error('Storage not initialized')"))
    val messagesRef = db.ref("${FirebasePaths.CHATS}/$roomId/${FirebasePaths.MESSAGES}")
    val ref = messagesRef.push()
    val messageId = ref.key ?: return kotlin.js.Promise.reject(js("Error('No message id')"))
    val contentType = (audioBlob?.type as? String)?.takeIf { it.isNotBlank() } ?: "audio/webm"
    val ext = if (contentType.contains("webm")) "webm" else "m4a"
    val storagePath = "chats/voice/$roomId/${messageId}.$ext"
    val storageRef = storage.ref(storagePath)
    val serverTimestamp = getDatabaseServerTimestamp()
    return storageRef.put(audioBlob, kotlin.js.json("contentType" to contentType)).then { _: dynamic ->
        storageRef.getDownloadURL()
    }.then { url: dynamic ->
        val voiceUrl = url as String
        val data = kotlin.js.json(
            "senderId" to senderId,
            "text" to "",
            "voiceUrl" to voiceUrl,
            "voiceDurationSec" to durationSec,
            "timestamp" to serverTimestamp,
            "status" to "sent"
        )
        ref.set(data)
    }.then { js("undefined") }
}
