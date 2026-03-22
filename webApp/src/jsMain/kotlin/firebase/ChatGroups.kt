package firebase

import com.example.startdrive.shared.FirebasePaths

data class ChatGroup(
    val id: String = "",
    val name: String = "",
    val memberIds: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAtMillis: Long = 0L,
    /** URL аватара группы (Firebase Storage). */
    val chatAvatarUrl: String? = null,
)

private fun parseTs(ts: dynamic): Long? {
    if (ts == null || ts == undefined) return null
    val t = ts.unsafeCast<dynamic>()
    val toMillis = t?.toMillis?.unsafeCast<dynamic>()
    return (if (toMillis != null) (toMillis.call(t) as? Number)?.toLong() else null)
        ?: (t?.seconds as? Number)?.toLong()?.let { it * 1000 }
        ?: (ts as? Number)?.toLong()
}

private fun chatGroupFromDoc(doc: dynamic): ChatGroup {
    val d = (doc.unsafeCast<dynamic>()).data()
    val membersDyn = d?.memberIds
    val memberIds = mutableListOf<String>()
    if (membersDyn != null && membersDyn != undefined) {
        val len = js("Array.isArray(membersDyn) ? membersDyn.length : 0").unsafeCast<Int>()
        for (i in 0 until len) {
            val id = js("membersDyn[i]").unsafeCast<dynamic>() as? String
            if (id != null && id.isNotBlank()) memberIds.add(id)
        }
    }
    return ChatGroup(
        id = doc.id,
        name = (d?.name as? String) ?: "",
        memberIds = memberIds,
        createdBy = (d?.createdBy as? String) ?: "",
        createdAtMillis = parseTs(d?.createdAt) ?: 0L,
        chatAvatarUrl = (d?.chatAvatarUrl as? String)?.takeIf { it.isNotBlank() },
    )
}

/** Группы чата, в которых состоит пользователь. */
fun getChatGroupsForUser(userId: String, callback: (List<ChatGroup>) -> Unit) {
    val fs = getFirestore()
    fs.collection(FirebasePaths.CHAT_GROUPS)
        .where("memberIds", "array-contains", userId)
        .get()
        .then { snap: dynamic ->
            try {
                val docs = snap?.docs ?: js("[]")
                val len = (docs.length as? Int) ?: 0
                val list = (0 until len).map { i -> chatGroupFromDoc(docs[i]) }
                callback(list.sortedBy { it.name.lowercase() })
            } catch (_: Throwable) {
                callback(emptyList())
            }
        }
        .catch { _ -> callback(emptyList()) }
}

fun addChatGroup(
    name: String,
    memberIds: List<String>,
    createdBy: String,
    callback: (error: String?, newGroupId: String?) -> Unit,
) {
    val fs = getFirestore()
    val ref = fs.collection(FirebasePaths.CHAT_GROUPS).doc()
    val mut = memberIds.distinct().filter { it.isNotBlank() }.toMutableList()
    if (createdBy.isNotBlank() && createdBy !in mut) mut.add(createdBy)
    val payload = js("{}").unsafeCast<dynamic>()
    payload.name = name.trim()
    // Явный JS-массив строк — в Firestore должен сохраниться как list (иначе rules «list» не проходят).
    payload.memberIds = mut.toTypedArray()
    payload.createdBy = createdBy
    payload.createdAt = getFirestoreTimestampNow()
    payload.chatAvatarUrl = ""
    val newId = ref.id
    ref.set(payload)
        .then { callback(null, newId) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка создания группы", null) }
}

fun updateChatGroup(
    groupId: String,
    name: String,
    memberIds: List<String>,
    adminUid: String,
    callback: (String?) -> Unit,
) {
    val mut = memberIds.distinct().filter { it.isNotBlank() }.toMutableList()
    if (adminUid.isNotBlank() && adminUid !in mut) mut.add(adminUid)
    val payload = js("{}").unsafeCast<dynamic>()
    payload.name = name.trim()
    payload.memberIds = mut.toTypedArray()
    getFirestore().collection(FirebasePaths.CHAT_GROUPS).doc(groupId)
        .update(payload)
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка сохранения") }
}

fun deleteChatGroup(groupId: String, callback: (String?) -> Unit) {
    try {
        val storage = getStorage()
        if (storage != null) {
            storage.ref("chats/group_avatars/$groupId/avatar.png").delete().catch { _: dynamic -> js("undefined") }
        }
    } catch (_: Throwable) { }
    getFirestore().collection(FirebasePaths.CHAT_GROUPS).doc(groupId)
        .delete()
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка удаления") }
}
