package firebase

import com.example.startdrive.shared.FirebasePaths

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val status: String = "sent",
)

fun chatRoomId(id1: String, id2: String): String {
    val sorted = listOf(id1, id2).sorted()
    return "${sorted[0]}_${sorted[1]}"
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
                val ts = m?.timestamp as? Number
                list.add(
                    ChatMessage(
                        id = key,
                        senderId = (m?.senderId as? String) ?: "",
                        text = (m?.text as? String) ?: "",
                        timestamp = ts?.toLong() ?: 0L,
                        status = (m?.status as? String) ?: "sent",
                    )
                )
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
