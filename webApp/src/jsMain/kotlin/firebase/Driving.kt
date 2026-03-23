package firebase

import com.example.startdrive.shared.FirebasePaths

data class DrivingSession(
    val id: String = "",
    val instructorId: String = "",
    val cadetId: String = "",
    val startTimeMillis: Long? = null,
    val actualStartMs: Long? = null,
    val status: String = "",
    val instructorRating: Int = 0,
    val cadetRating: Int = 0,
    val openWindowId: String = "",
    val instructorConfirmed: Boolean = false,
    val startRequestedByInstructor: Boolean = false,
    val sessionIsActive: Boolean = true,
    val sessionPausedAt: Long? = null,
    val completedAtMillis: Long? = null,
    val cancelledAtMillis: Long? = null,
    val cancelReason: String = "",
    /** Курсант подтвердил начало вождения (поле session.cadetConfirmed в Firestore). */
    val cadetConfirmed: Boolean = false,
)

data class InstructorOpenWindow(
    val id: String = "",
    val instructorId: String = "",
    val cadetId: String? = null,
    val dateTimeMillis: Long? = null,
    val status: String = "",
)

data class BalanceHistoryEntry(
    val id: String = "",
    val userId: String = "",
    val amount: Int = 0,
    val type: String = "",
    val performedBy: String = "",
    val timestampMillis: Long? = null,
)

private fun parseTimestamp(ts: dynamic): Long? {
    if (ts == null || ts == undefined) return null
    val t = ts.unsafeCast<dynamic>()
    val toMillis = t?.toMillis?.unsafeCast<dynamic>()
    return (if (toMillis != null) (toMillis.call(t) as? Number)?.toLong() else null)
        ?: (t?.seconds as? Number)?.toLong()?.let { it * 1000 }
        ?: (ts as? Number)?.toLong()
}

/** startTime внутри session может быть числом (Date.now) или Timestamp из Firestore. */
private fun sessionNestedStartMs(sessionObj: dynamic): Long? {
    if (sessionObj == null || sessionObj == undefined) return null
    val raw = sessionObj.startTime
    if (raw == null || raw == undefined) return null
    val n = raw as? Number
    if (n != null) return n.toLong()
    return parseTimestamp(raw)
}

private fun drivingSessionFromDoc(doc: dynamic): DrivingSession {
    val d = (doc.unsafeCast<dynamic>()).data()
    val startTime = d?.startTime
    val sessionObj = d?.session
    val sessionStartMs = sessionNestedStartMs(sessionObj)
    val sessActive = sessionObj?.isActive
    val sessPausedAt = sessionObj?.pausedAt
    val cadetConf = sessionObj?.cadetConfirmed as? Boolean ?: false
    return DrivingSession(
        id = doc.id,
        instructorId = (d?.instructorId as? String) ?: "",
        cadetId = (d?.cadetId as? String) ?: "",
        startTimeMillis = parseTimestamp(startTime),
        actualStartMs = sessionStartMs,
        status = (d?.status as? String) ?: "",
        instructorRating = (d?.instructorRating as? Number)?.toInt() ?: 0,
        cadetRating = (d?.cadetRating as? Number)?.toInt() ?: 0,
        openWindowId = (d?.openWindowId as? String) ?: "",
        instructorConfirmed = d?.instructorConfirmed as? Boolean ?: false,
        startRequestedByInstructor = d?.startRequestedByInstructor as? Boolean ?: false,
        sessionIsActive = if (sessActive == null) true else (sessActive as? Boolean) ?: true,
        sessionPausedAt = (sessPausedAt as? Number)?.toLong(),
        completedAtMillis = parseTimestamp(d?.completedAt),
        cancelledAtMillis = parseTimestamp(d?.cancelledAt),
        cancelReason = (d?.cancelReason as? String) ?: "",
        cadetConfirmed = cadetConf,
    )
}

/** Обновляет документ окна инструктора; если документа нет (удалён), промис всё равно резолвится. */
private fun updateInstructorWindowIfExists(windowId: String, data: dynamic): dynamic {
    val ref = getFirestore().collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS).doc(windowId)
    return ref.update(data).catch { e: dynamic ->
        val msg = (e?.message as? String) ?: ""
        if (msg.contains("No document to update") || msg.contains("NOT_FOUND")) js("Promise.resolve()").unsafeCast<dynamic>() else js("Promise.reject")(e).unsafeCast<dynamic>()
    }
}

fun getSessionsForInstructor(instructorId: String, callback: (List<DrivingSession>) -> Unit) {
    getFirestore().collection(FirebasePaths.DRIVING_SESSIONS)
        .where("instructorId", "==", instructorId)
        .orderBy("startTime", "asc")
        .get()
        .then { snap: dynamic ->
            try {
                val docs = snap?.docs ?: js("[]")
                val len = (docs.length as? Int) ?: 0
                callback((0 until len).map { i -> drivingSessionFromDoc(docs[i]) })
            } catch (e: Throwable) {
                callback(emptyList())
            }
        }
        .catch { _ -> callback(emptyList()) }
}

fun getSessionsForCadet(cadetId: String, callback: (List<DrivingSession>) -> Unit) {
    getFirestore().collection(FirebasePaths.DRIVING_SESSIONS)
        .where("cadetId", "==", cadetId)
        .orderBy("startTime", "desc")
        .get()
        .then { snap: dynamic ->
            try {
                val docs = snap?.docs ?: js("[]")
                val len = (docs.length as? Int) ?: 0
                callback((0 until len).map { i -> drivingSessionFromDoc(docs[i]) })
            } catch (e: Throwable) {
                callback(emptyList())
            }
        }
        .catch { _ -> callback(emptyList()) }
}

/** Все завершённые вождения (для агрегата по курсантам на главной админки). */
/** Сессии по списку инструкторов (для вкладки «Расписание» админа). */
fun getSessionsForInstructorsMap(instructorIds: List<String>, callback: (Map<String, List<DrivingSession>>) -> Unit) {
    if (instructorIds.isEmpty()) {
        callback(emptyMap())
        return
    }
    val result = mutableMapOf<String, List<DrivingSession>>()
    var pending = instructorIds.size
    instructorIds.forEach { id ->
        getSessionsForInstructor(id) { sess ->
            result[id] = sess
            pending--
            if (pending == 0) {
                callback(result)
            }
        }
    }
}

fun getCompletedDrivingSessionsForAdmin(callback: (List<DrivingSession>) -> Unit) {
    getFirestore().collection(FirebasePaths.DRIVING_SESSIONS)
        .where("status", "==", "completed")
        .get()
        .then { snap: dynamic ->
            try {
                val docs = snap?.docs ?: js("[]")
                val len = (docs.length as? Int) ?: 0
                callback((0 until len).map { i -> drivingSessionFromDoc(docs[i]) })
            } catch (e: Throwable) {
                callback(emptyList())
            }
        }
        .catch { _ -> callback(emptyList()) }
}

fun getOpenWindowsForInstructor(instructorId: String, callback: (List<InstructorOpenWindow>) -> Unit) {
    getFirestore().collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS)
        .where("instructorId", "==", instructorId)
        .orderBy("dateTime", "asc")
        .get()
        .then { snap: dynamic ->
            try {
                val docs = snap?.docs ?: js("[]")
                val len = (docs.length as? Int) ?: 0
                callback((0 until len).map { i ->
                    val doc = docs[i]
                    val d = (doc.unsafeCast<dynamic>()).data()
                    InstructorOpenWindow(
                        id = doc.id,
                        instructorId = (d?.instructorId as? String) ?: "",
                        cadetId = d?.cadetId as? String,
                        dateTimeMillis = parseTimestamp(d?.dateTime),
                        status = (d?.status as? String) ?: "",
                    )
                })
            } catch (e: Throwable) {
                callback(emptyList())
            }
        }
        .catch { _ -> callback(emptyList()) }
}

fun getOpenWindowsForCadet(instructorId: String, callback: (List<InstructorOpenWindow>) -> Unit) {
    getFirestore().collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS)
        .where("instructorId", "==", instructorId)
        .where("status", "==", "free")
        .orderBy("dateTime", "asc")
        .get()
        .then { snap: dynamic ->
            try {
                val docs = snap?.docs ?: js("[]")
                val len = (docs.length as? Int) ?: 0
                callback((0 until len).map { i ->
                    val doc = docs[i]
                    val d = (doc.unsafeCast<dynamic>()).data()
                    InstructorOpenWindow(
                        id = doc.id,
                        instructorId = (d?.instructorId as? String) ?: "",
                        cadetId = d?.cadetId as? String,
                        dateTimeMillis = parseTimestamp(d?.dateTime),
                        status = (d?.status as? String) ?: "",
                    )
                })
            } catch (e: Throwable) {
                callback(emptyList())
            }
        }
        .catch { _ -> callback(emptyList()) }
}

fun addOpenWindow(instructorId: String, dateTimeMillis: Long, callback: (String?, String?) -> Unit) {
    val ts = getFirestoreTimestampFromMillis(dateTimeMillis)
    val ref = getFirestore().collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS).doc()
    ref.set(kotlin.js.json(
        "instructorId" to instructorId,
        "dateTime" to ts,
        "status" to "free"
    ))
        .then {
            callback(ref.id, null)
        }
        .catch { e: Throwable ->
            callback(null, (e.asDynamic().message as? String) ?: "Ошибка")
        }
}

fun bookWindow(windowId: String, cadetId: String, callback: (String?) -> Unit) {
    val firestore = getFirestore()
    val windowRef = firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS).doc(windowId)
    windowRef.update(kotlin.js.json("cadetId" to cadetId, "status" to "booked"))
        .then {
            windowRef.get()
        }
        .then { windowDoc: dynamic ->
            val d = windowDoc?.data?.call(windowDoc)
            val instructorId = d?.instructorId as? String
            val dateTime = d?.dateTime
            if (instructorId == null || dateTime == null) {
                callback("Нет данных окна")
                return@then js("Promise.resolve()")
            }
            val sessionsRef = firestore.collection(FirebasePaths.DRIVING_SESSIONS).doc()
            sessionsRef.set(kotlin.js.json(
                "instructorId" to instructorId,
                "cadetId" to cadetId,
                "startTime" to dateTime,
                "status" to "scheduled",
                "instructorRating" to 0,
                "cadetRating" to 0,
                "instructorConfirmed" to false,
                "openWindowId" to windowId
            ))
        }
        .then { callback(null) }
        .catch { e: Throwable ->
            callback((e.asDynamic().message as? String) ?: "Ошибка бронирования")
        }
}

fun deleteOpenWindow(windowId: String, callback: (String?) -> Unit) {
    getFirestore().collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS).doc(windowId).delete()
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

/** Удалить документ сессии вождения (админ — очистка истории в расписании). */
fun deleteDrivingSession(sessionId: String, callback: (String?) -> Unit) {
    getFirestore().collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId).delete()
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка удаления") }
}

/** Получить сессию по id (для отмены и проверок). */
fun getSession(sessionId: String, callback: (DrivingSession?) -> Unit) {
    getFirestore().collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId).get()
        .then { doc: dynamic ->
            if (doc?.exists != true) {
                callback(null)
                return@then
            }
            val d: dynamic = (doc.unsafeCast<dynamic>()).data()
            if (d == null) {
                callback(null)
                return@then
            }
            val dyn = d.unsafeCast<dynamic>()
            val sessionObj = dyn["session"]
            val sessionStartMs = sessionNestedStartMs(sessionObj)
            val sessActive = sessionObj?.isActive
            val sessPausedAt = sessionObj?.pausedAt
            val cadetConf = sessionObj?.cadetConfirmed as? Boolean ?: false
            callback(DrivingSession(
                id = doc.id,
                instructorId = (dyn["instructorId"] as? String) ?: "",
                cadetId = (dyn["cadetId"] as? String) ?: "",
                startTimeMillis = parseTimestamp(dyn["startTime"]),
                actualStartMs = sessionStartMs,
                status = (dyn["status"] as? String) ?: "",
                instructorRating = (dyn["instructorRating"] as? Number)?.toInt() ?: 0,
                cadetRating = (dyn["cadetRating"] as? Number)?.toInt() ?: 0,
                openWindowId = (dyn["openWindowId"] as? String) ?: "",
                instructorConfirmed = dyn["instructorConfirmed"] as? Boolean ?: false,
                startRequestedByInstructor = dyn["startRequestedByInstructor"] as? Boolean ?: false,
                sessionIsActive = if (sessActive == null) true else (sessActive as? Boolean) ?: true,
                sessionPausedAt = (sessPausedAt as? Number)?.toLong(),
                completedAtMillis = parseTimestamp(dyn["completedAt"]),
                cancelledAtMillis = parseTimestamp(dyn["cancelledAt"]),
                cancelReason = (dyn["cancelReason"] as? String) ?: "",
                cadetConfirmed = cadetConf,
            ))
        }
        .catch { _ -> callback(null) }
}

/** Создать сессию вождения (инструктор записывает курсанта на дату/время, без окна). */
fun createSession(instructorId: String, cadetId: String, dateTimeMillis: Long, openWindowId: String? = null, callback: (String?, String?) -> Unit) {
    val ts = getFirestoreTimestampFromMillis(dateTimeMillis)
    val ref = getFirestore().collection(FirebasePaths.DRIVING_SESSIONS).doc()
    ref.set(kotlin.js.json(
        "instructorId" to instructorId,
        "cadetId" to cadetId,
        "startTime" to ts,
        "status" to "scheduled",
        "instructorRating" to 0,
        "cadetRating" to 0,
        "instructorConfirmed" to false,
        "openWindowId" to (openWindowId ?: "")
    ))
        .then { callback(ref.id, null) }
        .catch { e: Throwable -> callback(null, (e.asDynamic().message as? String) ?: "Ошибка") }
}

/** Подтвердить запись на вождение (инструктор подтвердил бронь курсанта). */
fun confirmBookingByInstructor(sessionId: String, callback: (String?) -> Unit) {
    getFirestore().collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId)
        .update("instructorConfirmed", true)
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

/** Запрос инструктора на начало вождения — курсант увидит кнопку «Подтвердить». */
fun requestStartByInstructor(sessionId: String, callback: (String?) -> Unit) {
    getFirestore().collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId)
        .update(kotlin.js.json("startRequestedByInstructor" to true))
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

/** Начать вождение (перевести в inProgress, создать объект session). */
fun startSession(sessionId: String, callback: (String?) -> Unit) {
    // Firestore не принимает Kotlin Long — только JS number (Double от Date.now()).
    val nowMs = kotlin.js.Date.now()
    val sessionObj = kotlin.js.json(
        "startTime" to nowMs,
        "pausedTime" to 0,
        "isActive" to true,
        "cadetConfirmed" to false
    )
    getFirestore().collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId)
        .update(kotlin.js.json(
            "status" to "inProgress",
            "session" to sessionObj
        ))
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

/** Поставить вождение на паузу (session.isActive = false). */
fun pauseDrivingSession(sessionId: String, callback: (String?) -> Unit) {
    getFirestore().collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId)
        .update(kotlin.js.json(
            "session.isActive" to false,
            "session.pausedAt" to js("Date.now()")
        ))
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

/** Продолжить вождение (session.isActive = true). */
fun resumeDrivingSession(sessionId: String, callback: (String?) -> Unit) {
    getFirestore().collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId)
        .update(kotlin.js.json("session.isActive" to true))
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

/** Завершить вождение (статус completed). Списывает 1 талон с курсанта и зачисляет инструктору — как Android [DrivingRepository.completeSession]. */
fun completeDrivingSession(sessionId: String, callback: (String?) -> Unit) {
    getSession(sessionId) { session ->
        if (session == null) {
            callback("Сессия не найдена")
            return@getSession
        }
        if (session.status == "completed") {
            callback(null)
            return@getSession
        }
        val cadetId = session.cadetId
        val instructorId = session.instructorId
        val firestore = getFirestore()
        val windowId = session.openWindowId
        val freeWindowPromise = if (windowId.isNotBlank()) {
            updateInstructorWindowIfExists(windowId, kotlin.js.json("status" to "free", "cadetId" to null))
        } else js("Promise.resolve()")
        freeWindowPromise.then {
            firestore.collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId)
                .update(kotlin.js.json(
                    "status" to "completed",
                    "completedAt" to getFirestoreTimestampNow()
                ))
        }.then {
            if (cadetId.isBlank() || instructorId.isBlank()) {
                callback(null)
                return@then js("undefined")
            }
            updateBalance(cadetId, "debit", 1, instructorId) { err1 ->
                if (err1 != null) {
                    callback(err1)
                    return@updateBalance
                }
                updateBalance(instructorId, "credit", 1, cadetId) { err2 ->
                    callback(err2)
                }
            }
            js("undefined")
        }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
    }
}

/** Поставить оценку курсанту (3, 4 или 5). Талоны переводятся при завершении сессии ([completeDrivingSession]). */
fun setInstructorRating(sessionId: String, rating: Int, callback: (String?) -> Unit) {
    if (rating !in 3..5) {
        callback("Оценка должна быть 3, 4 или 5")
        return
    }
    getSession(sessionId) { session ->
        if (session == null) {
            callback("Сессия не найдена")
            return@getSession
        }
        if (session.status != "completed") {
            callback("Сессия не завершена")
            return@getSession
        }
        getFirestore().collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId)
            .update(kotlin.js.json("instructorRating" to rating))
            .then { callback(null) }
            .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
    }
}

/** Поставить оценку инструктору (1–5 звёзд). */
fun setCadetRating(sessionId: String, rating: Int, callback: (String?) -> Unit) {
    if (rating !in 1..5) {
        callback("Оценка должна быть от 1 до 5")
        return
    }
    getSession(sessionId) { session ->
        if (session == null) {
            callback("Сессия не найдена")
            return@getSession
        }
        if (session.status != "completed" || session.instructorRating == 0) {
            callback("Сначала инструктор должен поставить оценку")
            return@getSession
        }
        getFirestore().collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId)
            .update(kotlin.js.json("cadetRating" to rating))
            .then { callback(null) }
            .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
    }
}

/** Инструктор опаздывает: сдвиг времени на N минут. */
fun setInstructorRunningLate(sessionId: String, delayMinutes: Int, callback: (String?) -> Unit) {
    getSession(sessionId) { session ->
        if (session == null) {
            callback("Сессия не найдена")
            return@getSession
        }
        val startMs = session.startTimeMillis ?: run { callback("Нет времени начала"); return@getSession }
        val newMs = startMs + delayMinutes * 60L * 1000L
        val newTs = getFirestoreTimestampFromMillis(newMs)
        val firestore = getFirestore()
        firestore.collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId)
            .update(kotlin.js.json(
                "startTime" to newTs,
                "delayNotificationMinutes" to delayMinutes
            ))
            .then {
                if (session.openWindowId.isNotBlank()) {
                    updateInstructorWindowIfExists(session.openWindowId, kotlin.js.json("dateTime" to newTs))
                } else js("Promise.resolve()")
            }
            .then { callback(null) }
            .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
    }
}

/** Сдвинуть время начала сессии (и окна при наличии) — для каскадного сдвига при «Опаздываю». */
fun updateSessionStartTime(sessionId: String, newStartMs: Long, callback: (String?) -> Unit) {
    getSession(sessionId) { session ->
        if (session == null) {
            callback("Сессия не найдена")
            return@getSession
        }
        val newTs = getFirestoreTimestampFromMillis(newStartMs)
        val firestore = getFirestore()
        firestore.collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId)
            .update(kotlin.js.json("startTime" to newTs))
            .then {
                if (session.openWindowId.isNotBlank()) {
                    updateInstructorWindowIfExists(session.openWindowId, kotlin.js.json("dateTime" to newTs))
                } else js("Promise.resolve()")
            }
            .then { callback(null) }
            .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
    }
}

/** Отменить вождение инструктором: освободить окно (если было), поставить статус отмены и причину. */
fun cancelByInstructor(sessionId: String, reason: String, callback: (String?) -> Unit) {
    getSession(sessionId) { session ->
        if (session == null) {
            callback("Сессия не найдена")
            return@getSession
        }
        val firestore = getFirestore()
        val windowId = session.openWindowId
        val freeWindowPromise = if (windowId.isNotBlank()) {
            updateInstructorWindowIfExists(windowId, kotlin.js.json("status" to "free", "cadetId" to null))
        } else {
            js("Promise.resolve()")
        }
        val reasonText = reason.takeIf { it.isNotBlank() } ?: "—"
        freeWindowPromise.then {
            firestore.collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId).update(kotlin.js.json(
                "status" to "cancelledByInstructor",
                "cancelledAt" to getFirestoreTimestampNow(),
                "cancelReason" to reasonText
            ))
        }.then {
            // Только при причине «Курсант не явился» — списать 1 талон с курсанта и начислить инструктору. При «ТС на ремонте» и др. — просто отмена без списания.
            val shouldTransfer = reasonText.trim() == "Курсант не явился"
            val cadetId = session.cadetId
            val instructorId = session.instructorId
            if (!shouldTransfer || cadetId.isBlank() || instructorId.isBlank()) {
                callback(null)
                return@then js("undefined")
            }
            firestore.collection(FirebasePaths.USERS).doc(cadetId).get().then { doc: dynamic ->
                val d = (doc.unsafeCast<dynamic>()).data()
                val bal = (d?.balance as? Number)?.toInt() ?: 0
                if (bal < 1) {
                    // Сессию отменяем в любом случае; если талонов нет — перевод пропускаем.
                    callback(null)
                    return@then js("undefined")
                }
                updateBalance(cadetId, "debit", 1, instructorId) { err1 ->
                    if (err1 != null) {
                        callback(err1)
                        return@updateBalance
                    }
                    updateBalance(instructorId, "credit", 1, cadetId) { err2 ->
                        callback(err2)
                    }
                }
                js("undefined")
            }.catch { _: dynamic ->
                callback(null)
                js("undefined")
            }
        }.catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
    }
}

/** Отменить вождение курсантом: освободить окно (если было), поставить статус cancelledByCadet. */
fun cancelByCadet(sessionId: String, callback: (String?) -> Unit) {
    getSession(sessionId) { session ->
        if (session == null) {
            callback("Сессия не найдена")
            return@getSession
        }
        val firestore = getFirestore()
        val windowId = session.openWindowId
        val freeWindowPromise = if (windowId.isNotBlank()) {
            updateInstructorWindowIfExists(windowId, kotlin.js.json("status" to "free", "cadetId" to null))
        } else {
            js("Promise.resolve()")
        }
        freeWindowPromise.then {
            firestore.collection(FirebasePaths.DRIVING_SESSIONS).doc(sessionId).update(kotlin.js.json(
                "status" to "cancelledByCadet",
                "cancelledAt" to getFirestoreTimestampNow(),
                "cancelReason" to "отменено курсантом"
            ))
        }.then { callback(null) }
         .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
    }
}

fun getBalanceHistory(userId: String, callback: (List<BalanceHistoryEntry>) -> Unit) {
    getFirestore().collection(FirebasePaths.USERS).doc(userId)
        .collection(FirebasePaths.BALANCE_HISTORY)
        .orderBy("timestamp", "desc")
        .get()
        .then { snap: dynamic ->
            try {
                val docs = snap?.docs ?: js("[]")
                val len = (docs.length as? Int) ?: 0
                callback((0 until len).map { i ->
                    val doc = docs[i]
                    val d = (doc.unsafeCast<dynamic>()).data()
                    BalanceHistoryEntry(
                        id = doc.id,
                        userId = (d?.userId as? String) ?: userId,
                        amount = (d?.amount as? Number)?.toInt() ?: 0,
                        type = (d?.type as? String) ?: "",
                        performedBy = (d?.performedBy as? String) ?: "",
                        timestampMillis = parseTimestamp(d?.timestamp),
                    )
                })
            } catch (e: Throwable) {
                callback(emptyList())
            }
        }
        .catch { _ -> callback(emptyList()) }
}

/** Загружает историю операций по списку пользователей и объединяет (без collectionGroup). */
fun loadBalanceHistoryForUsers(userIds: List<String>, callback: (List<BalanceHistoryEntry>) -> Unit) {
    if (userIds.isEmpty()) {
        callback(emptyList())
        return
    }
    val results = mutableListOf<BalanceHistoryEntry>()
    var pending = userIds.size
    userIds.forEach { userId ->
        getBalanceHistory(userId) { list ->
            results.addAll(list)
            pending--
            if (pending == 0) {
                callback(results.sortedByDescending { it.timestampMillis ?: 0L }.take(50))
            }
        }
    }
}

fun updateBalance(userId: String, type: String, amount: Int, performedBy: String, callback: (String?) -> Unit) {
    val firestore = getFirestore()
    val userRef = firestore.collection(FirebasePaths.USERS).doc(userId)
    val historyRef = userRef.collection(FirebasePaths.BALANCE_HISTORY).doc()
    userRef.get()
        .then { snap: dynamic ->
            val d = snap?.data?.call(snap)
            val current = (d?.balance as? Number)?.toInt() ?: 0
            val newBalance = when (type) {
                "credit" -> current + amount
                "debit" -> (current - amount).coerceAtLeast(0)
                "set" -> amount
                else -> current
            }
            userRef.update(kotlin.js.json("balance" to newBalance))
            historyRef.set(kotlin.js.json(
                "userId" to userId,
                "amount" to amount,
                "type" to type,
                "performedBy" to performedBy,
                "timestamp" to getFirestoreTimestampNow()
            ))
        }
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}
