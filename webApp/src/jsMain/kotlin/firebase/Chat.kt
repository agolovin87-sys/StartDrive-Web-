package firebase

import com.example.startdrive.shared.FirebasePaths

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    /** Время редактирования (Server timestamp). Если null — сообщение не редактировали. */
    val editedAt: Long? = null,
    val status: String = "sent",
    val voiceUrl: String? = null,
    val voiceDurationSec: Int? = null,
    /** Вложение (только админ): URL в Storage + имя файла. */
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileMime: String? = null,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val deletedForUserIds: Set<String> = emptySet(),
    /** Ключ — эмодзи, значение — uid пользователей, поставивших эту реакцию. */
    val reactions: Map<String, Set<String>> = emptyMap(),
) {
    val isVoice: Boolean get() = !voiceUrl.isNullOrBlank() || (voiceDurationSec != null && voiceDurationSec!! > 0)
    val isFile: Boolean get() = !fileUrl.isNullOrBlank()
}

fun chatRoomId(id1: String, id2: String): String {
    val sorted = listOf(id1, id2).sorted()
    return "${sorted[0]}_${sorted[1]}"
}

/** Комната Realtime DB для группового чата (Firestore id документа chat_groups). */
fun groupChatRoomId(groupId: String): String = "group_$groupId"

private fun parseMessage(key: String, m: dynamic): ChatMessage {
    val ts = m?.timestamp
    val timestamp = when (ts) {
        is Number -> ts.toLong()
        else -> (ts?.unsafeCast<Double>())?.toLong() ?: 0L
    }
    val editedTs = m?.editedAt
    val editedAt = when (editedTs) {
        is Number -> editedTs.toLong()
        else -> (editedTs?.unsafeCast<Double>())?.toLong()
    }
    val dur = m?.voiceDurationSec
    val durationSec = when (dur) {
        is Number -> dur.toInt()
        else -> null
    }
    val deletedForIds = mutableSetOf<String>()
    val deletedForObj = m?.deletedFor
    if (deletedForObj != null && deletedForObj != undefined) {
        val keys = js("Object.keys(deletedForObj)").unsafeCast<Array<String>>()
        keys.forEach { uid ->
            val v = deletedForObj[uid]
            if (v == true) deletedForIds.add(uid)
        }
    }
    val reactionsMap = mutableMapOf<String, MutableSet<String>>()
    val reactObj = m?.reactions
    if (reactObj != null && reactObj != undefined) {
        val emojiKeys = js("Object.keys(reactObj)").unsafeCast<Array<String>>()
        emojiKeys.forEach { em ->
            val usersObj = reactObj[em]
            if (usersObj != null && usersObj != undefined) {
                val ukeys = js("Object.keys(usersObj)").unsafeCast<Array<String>>()
                val set = mutableSetOf<String>()
                ukeys.forEach { u ->
                    if (usersObj[u] == true) set.add(u)
                }
                if (set.isNotEmpty()) reactionsMap[em] = set
            }
        }
    }
    return ChatMessage(
        id = key,
        senderId = (m?.senderId as? String) ?: "",
        text = (m?.text as? String) ?: "",
        timestamp = timestamp,
        editedAt = editedAt,
        status = (m?.status as? String) ?: "sent",
        voiceUrl = m?.voiceUrl as? String,
        voiceDurationSec = durationSec,
        fileUrl = (m?.fileUrl as? String)?.takeIf { it.isNotBlank() },
        fileName = (m?.fileName as? String)?.takeIf { it.isNotBlank() },
        fileMime = (m?.fileMime as? String)?.takeIf { it.isNotBlank() },
        replyToMessageId = m?.replyToMessageId as? String,
        replyToText = m?.replyToText as? String,
        deletedForUserIds = deletedForIds,
        reactions = reactionsMap.mapValues { it.value.toSet() },
    )
}

private var currentUnsubscribe: (() -> Unit)? = null
private var currentTypingUnsubscribe: (() -> Unit)? = null

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
    currentTypingUnsubscribe?.invoke()
    currentTypingUnsubscribe = null
}

/** Поставить/снять индикатор «печатает» для пользователя [uid] в комнате [roomId]. */
fun setTyping(roomId: String, uid: String, isTyping: Boolean): kotlin.js.Promise<Unit> {
    val db = getDatabase() ?: return kotlin.js.Promise.reject(js("Error('Database not initialized')"))
    val ref = db.ref("${FirebasePaths.CHATS}/$roomId/typing/$uid")
    return if (isTyping) {
        ref.set(getDatabaseServerTimestamp()).then { js("undefined") }
    } else {
        ref.remove().then { js("undefined") }
    }
}

/** Подписка на индикатор «печатает» (map uid -> timestamp). */
fun subscribeTyping(roomId: String, callback: (Map<String, Long>) -> Unit) {
    currentTypingUnsubscribe?.invoke()
    val db = getDatabase()
    if (db == null) {
        callback(emptyMap())
        return
    }
    val ref = db.ref("${FirebasePaths.CHATS}/$roomId/typing")
    val listener: (dynamic) -> Unit = { snap: dynamic ->
        val out = mutableMapOf<String, Long>()
        val v = snap?.`val`()
        if (v != null && v != undefined) {
            val obj = v.unsafeCast<dynamic>()
            val keys = js("Object.keys(obj)").unsafeCast<Array<String>>()
            keys.forEach { k ->
                val tsAny = obj[k]
                val ts = when (tsAny) {
                    is Number -> tsAny.toLong()
                    else -> (tsAny?.unsafeCast<Double>())?.toLong() ?: 0L
                }
                if (ts > 0L) out[k] = ts
            }
        }
        callback(out)
    }
    ref.on("value", listener)
    currentTypingUnsubscribe = {
        try { ref.off("value", listener) } catch (_: Throwable) { }
        currentTypingUnsubscribe = null
    }
}

fun sendMessage(
    roomId: String,
    senderId: String,
    text: String,
    replyToMessageId: String? = null,
    replyToText: String? = null,
): kotlin.js.Promise<Unit> {
    val db = getDatabase() ?: return kotlin.js.Promise.reject(js("Error('Database not initialized')"))
    val ref = db.ref("${FirebasePaths.CHATS}/$roomId/${FirebasePaths.MESSAGES}").push()
    val serverTimestamp = getDatabaseServerTimestamp()
    // Plain JS object: надёжнее для Firebase set(), чем дозапись в json() (как в Android ChatRepository).
    val data = js("{}").unsafeCast<dynamic>()
    data.senderId = senderId
    data.text = text
    data.timestamp = serverTimestamp
    data.status = "sent"
    if (!replyToMessageId.isNullOrBlank()) {
        data.replyToMessageId = replyToMessageId
        val trimmed = replyToText?.trim()?.take(200)
        data.replyToText = if (trimmed.isNullOrBlank()) "Сообщение" else trimmed
    }
    return ref.set(data).then { js("undefined") }
}

/** Сообщение с файлом (URL после Callable uploadChatAdminFile). */
fun sendFileMessage(
    roomId: String,
    senderId: String,
    text: String,
    fileUrl: String,
    fileName: String,
    fileMime: String?,
    replyToMessageId: String? = null,
    replyToText: String? = null,
): kotlin.js.Promise<Unit> {
    val db = getDatabase() ?: return kotlin.js.Promise.reject(js("Error('Database not initialized')"))
    val ref = db.ref("${FirebasePaths.CHATS}/$roomId/${FirebasePaths.MESSAGES}").push()
    val serverTimestamp = getDatabaseServerTimestamp()
    val data = js("{}").unsafeCast<dynamic>()
    data.senderId = senderId
    data.text = text
    data.fileUrl = fileUrl
    data.fileName = fileName
    if (!fileMime.isNullOrBlank()) data.fileMime = fileMime
    data.timestamp = serverTimestamp
    data.status = "sent"
    if (!replyToMessageId.isNullOrBlank()) {
        data.replyToMessageId = replyToMessageId
        val trimmed = replyToText?.trim()?.take(200)
        data.replyToText = if (trimmed.isNullOrBlank()) "Сообщение" else trimmed
    }
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

/** Удалить сообщение только у текущего пользователя (скрыть в его клиенте). */
/** Поставить или снять реакцию [emoji] пользователем [userId] на сообщение. */
fun setMessageReactionUser(
    roomId: String,
    messageId: String,
    emoji: String,
    userId: String,
    add: Boolean,
): kotlin.js.Promise<Unit> {
    val db = getDatabase() ?: return kotlin.js.Promise.reject(js("Error('Database not initialized')"))
    val ref = db.ref("${FirebasePaths.CHATS}/$roomId/${FirebasePaths.MESSAGES}/$messageId/reactions/$emoji/$userId")
    return if (add) {
        ref.set(true).then { js("undefined") }
    } else {
        ref.remove().then { js("undefined") }
    }
}

fun deleteMessageForUser(roomId: String, messageId: String, userId: String): kotlin.js.Promise<Unit> {
    val db = getDatabase() ?: return kotlin.js.Promise.reject(js("Error('Database not initialized')"))
    val ref = db.ref("${FirebasePaths.CHATS}/$roomId/${FirebasePaths.MESSAGES}/$messageId/deletedFor/$userId")
    return ref.set(true).then { js("undefined") }
}

/** Удалить сообщение для всех участников чата (полное удаление узла в RTDB). */
fun deleteMessageForEveryone(roomId: String, messageId: String): kotlin.js.Promise<Unit> {
    val db = getDatabase() ?: return kotlin.js.Promise.reject(js("Error('Database not initialized')"))
    val ref = db.ref("${FirebasePaths.CHATS}/$roomId/${FirebasePaths.MESSAGES}/$messageId")
    return ref.remove().then { js("undefined") }
}

/** Редактировать текст сообщения (только для текста). */
fun editMessage(roomId: String, messageId: String, newText: String): kotlin.js.Promise<Unit> {
    val db = getDatabase() ?: return kotlin.js.Promise.reject(js("Error('Database not initialized')"))
    val ref = db.ref("${FirebasePaths.CHATS}/$roomId/${FirebasePaths.MESSAGES}/$messageId")
    val serverTimestamp = getDatabaseServerTimestamp()
    val updateObj = js("{}").unsafeCast<dynamic>()
    updateObj.text = newText
    updateObj.editedAt = serverTimestamp
    return ref.update(updateObj).then { js("undefined") }
}

/**
 * Полностью удалить голосовое сообщение: файл в Storage (если есть URL), затем узел в Realtime DB.
 * Вызывать только для своего сообщения (проверка на клиенте).
 */
fun deleteVoiceMessageFully(roomId: String, messageId: String, voiceUrl: String?): kotlin.js.Promise<Unit> {
    val db = getDatabase() ?: return kotlin.js.Promise.reject(js("Error('Database not initialized')"))
    val storage = getStorage()
    val msgRef = db.ref("${FirebasePaths.CHATS}/$roomId/${FirebasePaths.MESSAGES}/$messageId")
    fun removeDb(): kotlin.js.Promise<dynamic> = msgRef.remove().then { js("undefined") }
    if (storage != null && !voiceUrl.isNullOrBlank()) {
        return try {
            val storageRef = storage.refFromURL(voiceUrl)
            storageRef.delete()
                .catch { _: dynamic -> js("undefined") }
                .then { removeDb() }
                .then { js("undefined") }
        } catch (_: Throwable) {
            removeDb()
        }
    }
    return removeDb()
}
