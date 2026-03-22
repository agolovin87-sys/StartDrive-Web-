package firebase

import com.example.startdrive.shared.FirebasePaths
import kotlin.js.json

data class CadetGroup(
    val id: String = "",
    val numberLabel: String = "",
    val dateFromMillis: Long? = null,
    val dateToMillis: Long? = null,
    val createdAtMillis: Long = 0L,
)

private fun parseTs(ts: dynamic): Long? {
    if (ts == null || ts == undefined) return null
    val t = ts.unsafeCast<dynamic>()
    val toMillis = t?.toMillis?.unsafeCast<dynamic>()
    return (if (toMillis != null) (toMillis.call(t) as? Number)?.toLong() else null)
        ?: (t?.seconds as? Number)?.toLong()?.let { it * 1000 }
        ?: (ts as? Number)?.toLong()
}

private fun cadetGroupFromDoc(doc: dynamic): CadetGroup {
    val d = (doc.unsafeCast<dynamic>()).data()
    return CadetGroup(
        id = doc.id,
        numberLabel = (d?.numberLabel as? String) ?: "",
        dateFromMillis = parseTs(d?.dateFrom),
        dateToMillis = parseTs(d?.dateTo),
        createdAtMillis = parseTs(d?.createdAt) ?: 0L,
    )
}

fun getCadetGroups(callback: (List<CadetGroup>) -> Unit) {
    getFirestore().collection(FirebasePaths.CADET_GROUPS)
        .orderBy("createdAt", "desc")
        .get()
        .then { snap: dynamic ->
            try {
                val docs = snap?.docs ?: js("[]")
                val len = (docs.length as? Int) ?: 0
                callback((0 until len).map { i -> cadetGroupFromDoc(docs[i]) })
            } catch (e: Throwable) {
                callback(emptyList())
            }
        }
        .catch { _ -> callback(emptyList()) }
}

/** Создать группу. Если [dateFromMillis] и [dateToMillis] null — группа без срока. */
fun addCadetGroup(numberLabel: String, dateFromMillis: Long?, dateToMillis: Long?, callback: (String?) -> Unit) {
    val ref = getFirestore().collection(FirebasePaths.CADET_GROUPS).doc()
    val payload = js("{}").unsafeCast<dynamic>()
    payload.numberLabel = numberLabel.trim()
    payload.createdAt = getFirestoreTimestampNow()
    if (dateFromMillis != null && dateToMillis != null) {
        payload.dateFrom = getFirestoreTimestampFromMillis(dateFromMillis)
        payload.dateTo = getFirestoreTimestampFromMillis(dateToMillis)
    }
    ref.set(payload)
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка сохранения") }
}

fun updateCadetGroup(groupId: String, numberLabel: String, dateFromMillis: Long?, dateToMillis: Long?, callback: (String?) -> Unit) {
    val payload = js("{}").unsafeCast<dynamic>()
    payload.numberLabel = numberLabel.trim()
    if (dateFromMillis != null && dateToMillis != null) {
        payload.dateFrom = getFirestoreTimestampFromMillis(dateFromMillis)
        payload.dateTo = getFirestoreTimestampFromMillis(dateToMillis)
    } else {
        payload.dateFrom = js("firebase.firestore.FieldValue.delete()")
        payload.dateTo = js("firebase.firestore.FieldValue.delete()")
    }
    getFirestore().collection(FirebasePaths.CADET_GROUPS).doc(groupId)
        .update(payload)
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка обновления") }
}

/** Удалить группу и снять её с курсантов. */
fun deleteCadetGroup(groupId: String, callback: (String?) -> Unit) {
    val firestore = getFirestore()
    firestore.collection(FirebasePaths.CADET_GROUPS).doc(groupId).delete()
        .then {
            firestore.collection(FirebasePaths.USERS).where("cadetGroupId", "==", groupId).get()
                .then { snap: dynamic ->
                    try {
                        val docs = snap?.docs ?: js("[]")
                        val len = (docs.length as? Int) ?: 0
                        if (len == 0) {
                            callback(null)
                            return@then
                        }
                        var pending = len
                        var firstErr: String? = null
                        (0 until len).forEach { i ->
                            val doc = docs[i]
                            val uid = doc.id as String
                            setUserCadetGroup(uid, null) { err ->
                                if (err != null && firstErr == null) firstErr = err
                                pending--
                                if (pending == 0) callback(firstErr)
                            }
                        }
                    } catch (e: Throwable) {
                        callback((e.asDynamic().message as? String) ?: "Ошибка")
                    }
                }
                .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка запроса") }
        }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка удаления") }
}
