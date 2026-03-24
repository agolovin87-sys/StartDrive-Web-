package com.example.startdrive.data.repository

import com.example.startdrive.data.FirebasePaths
import com.example.startdrive.data.model.DrivingSession
import com.example.startdrive.data.model.InstructorOpenWindow
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Курсант не может отменить в пределах 6 ч до начала вождения. */
private fun isCadetCancelBlockedWithinSixHours(startTimeMillis: Long?): Boolean {
    if (startTimeMillis == null) return false
    val now = System.currentTimeMillis()
    if (startTimeMillis <= now) return true
    return (startTimeMillis - now) <= 6L * 60 * 60 * 1000
}

class DrivingRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun sessionsForInstructor(instructorId: String): Flow<List<DrivingSession>> = callbackFlow {
        val listener = firestore.collection(FirebasePaths.DRIVING_SESSIONS)
            .whereEqualTo("instructorId", instructorId)
            .orderBy("startTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(DrivingSession::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun sessionsForCadet(cadetId: String): Flow<List<DrivingSession>> = callbackFlow {
        val listener = firestore.collection(FirebasePaths.DRIVING_SESSIONS)
            .whereEqualTo("cadetId", cadetId)
            .orderBy("startTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(DrivingSession::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createSession(instructorId: String, cadetId: String, dateTime: Timestamp, openWindowId: String? = null): String {
        val ref = firestore.collection(FirebasePaths.DRIVING_SESSIONS).document()
        val data = mutableMapOf<String, Any>(
            "instructorId" to instructorId,
            "cadetId" to cadetId,
            "startTime" to dateTime,
            "status" to "scheduled",
            "instructorRating" to 0,
            "cadetRating" to 0,
            "instructorConfirmed" to false,
        )
        if (!openWindowId.isNullOrBlank()) data["openWindowId"] = openWindowId
        ref.set(data).await()
        return ref.id
    }

    suspend fun startSession(sessionId: String) {
        val ref = firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId)
        ref.update(
            "status", "inProgress",
            "session", mapOf(
                "startTime" to System.currentTimeMillis(),
                "pausedTime" to 0L,
                "isActive" to true,
                "cadetConfirmed" to false,
            )
        ).await()
    }

    suspend fun confirmSessionByCadet(sessionId: String) {
        val ref = firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId)
        firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            val existing = snap.get("session") as? Map<*, *>
            val session = mutableMapOf<String, Any>()
            existing?.forEach { (k, v) -> if (k is String) session[k] = v as Any }
            session["cadetConfirmed"] = true
            session["startTime"] = System.currentTimeMillis()
            session["isActive"] = true
            tx.update(ref, "session", session)
        }.await()
    }

    suspend fun updateSessionTimer(sessionId: String, pausedTime: Long, isActive: Boolean) {
        val ref = firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId)
        firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            val existing = snap.get("session") as? Map<*, *>
            val session = mutableMapOf<String, Any>()
            existing?.forEach { (k, v) -> if (k is String) session[k] = v as Any }
            session["pausedTime"] = pausedTime
            session["isActive"] = isActive
            tx.update(ref, "session", session)
        }.await()
    }

    suspend fun completeSession(sessionId: String, instructorRating: Int, cadetRating: Int, performedByInstructorId: String, completedTimerRemaining: String? = null) {
        val snap = firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId).get().await()
        val cadetId = snap.getString("cadetId") ?: throw IllegalStateException("No cadetId")
        val instructorId = performedByInstructorId
        firestore.runTransaction { tx ->
            val ref = firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId)
            val cadetRef = firestore.collection(FirebasePaths.USERS).document(cadetId)
            val instructorRef = firestore.collection(FirebasePaths.USERS).document(instructorId)
            // Все чтения — до любых записей (требование Firestore)
            val cadetCurrent = (tx.get(cadetRef).getLong("balance") ?: 0).toInt()
            val instructorCurrent = (tx.get(instructorRef).getLong("balance") ?: 0).toInt()
            // Записи
            tx.update(ref, "status", "completed")
            tx.update(ref, "completedAt", Timestamp.now())
            tx.update(ref, "instructorRating", instructorRating)
            tx.update(ref, "cadetRating", cadetRating)
            tx.update(ref, "session", null)
            if (completedTimerRemaining != null) {
                tx.update(ref, "completedTimerRemaining", completedTimerRemaining)
            }
            tx.update(cadetRef, "balance", (cadetCurrent - 1).coerceAtLeast(0))
            val cadetHistoryRef = cadetRef.collection(FirebasePaths.BALANCE_HISTORY).document()
            tx.set(cadetHistoryRef, mapOf(
                "userId" to cadetId,
                "amount" to 1,
                "type" to "debit",
                "performedBy" to instructorId,
                "timestamp" to Timestamp.now(),
            ))
            tx.update(instructorRef, "balance", instructorCurrent + 1)
            val instructorHistoryRef = instructorRef.collection(FirebasePaths.BALANCE_HISTORY).document()
            tx.set(instructorHistoryRef, mapOf(
                "userId" to instructorId,
                "amount" to 1,
                "type" to "credit",
                "performedBy" to cadetId,
                "timestamp" to Timestamp.now(),
            ))
        }.await()
    }

    suspend fun confirmBookingByInstructor(sessionId: String) {
        firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId)
            .update("instructorConfirmed", true).await()
    }

    /** Подтверждение записи курсантом — после назначения вождения инструктором. */
    suspend fun confirmBookingByCadet(sessionId: String) {
        firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId)
            .update("instructorConfirmed", true).await()
    }

    /** Оценка инструктора курсантом после завершения вождения (1–5). */
    suspend fun setCadetRating(sessionId: String, rating: Int) {
        firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId)
            .update("cadetRating", rating.coerceIn(1, 5)).await()
    }

    /** Запрос инструктора на начало вождения — курсант увидит кнопку «Подтвердить». */
    suspend fun requestStartByInstructor(sessionId: String) {
        firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId)
            .update("startRequestedByInstructor", true).await()
    }

    suspend fun cancelByInstructor(sessionId: String, reason: String? = null) {
        val session = getSession(sessionId) ?: return
        // Сначала освободить окно (нужны openWindowId/cadetId из сессии), затем обновить сессию
        val windowId = session.openWindowId
        if (!windowId.isBlank()) {
            firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS).document(windowId)
                .update("status", "free", "cadetId", null).await()
        } else {
            val windows = firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS)
                .whereEqualTo("instructorId", session.instructorId)
                .whereEqualTo("status", "booked")
                .get().await()
            val startTime = session.startTime?.toDate()?.time ?: 0L
            for (doc in windows.documents) {
                val w = doc.toObject(InstructorOpenWindow::class.java) ?: continue
                if (w.cadetId == session.cadetId && w.dateTime?.toDate()?.time == startTime) {
                    doc.reference.update("status", "free", "cadetId", null).await()
                    break
                }
            }
        }
        val updates = mutableMapOf<String, Any>(
            "status" to "cancelledByInstructor",
            "cancelledAt" to Timestamp.now(),
        )
        if (!reason.isNullOrBlank()) updates["cancelReason"] = reason
        firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId).update(updates).await()

        // При причине «Курсант не явился» — списать 1 талон у курсанта и зачислить инструктору
        if (reason == "Курсант не явился") {
            val cadetId = session.cadetId
            val instructorId = session.instructorId
            if (cadetId.isNotBlank() && instructorId.isNotBlank()) {
                firestore.runTransaction { tx ->
                    val cadetRef = firestore.collection(FirebasePaths.USERS).document(cadetId)
                    val instructorRef = firestore.collection(FirebasePaths.USERS).document(instructorId)
                    val cadetCurrent = (tx.get(cadetRef).getLong("balance") ?: 0).toInt()
                    val instructorCurrent = (tx.get(instructorRef).getLong("balance") ?: 0).toInt()
                    tx.update(cadetRef, "balance", (cadetCurrent - 1).coerceAtLeast(0))
                    val cadetHistoryRef = cadetRef.collection(FirebasePaths.BALANCE_HISTORY).document()
                    tx.set(cadetHistoryRef, mapOf(
                        "userId" to cadetId,
                        "amount" to 1,
                        "type" to "debit",
                        "performedBy" to instructorId,
                        "timestamp" to Timestamp.now(),
                    ))
                    tx.update(instructorRef, "balance", instructorCurrent + 1)
                    val instructorHistoryRef = instructorRef.collection(FirebasePaths.BALANCE_HISTORY).document()
                    tx.set(instructorHistoryRef, mapOf(
                        "userId" to instructorId,
                        "amount" to 1,
                        "type" to "credit",
                        "performedBy" to cadetId,
                        "timestamp" to Timestamp.now(),
                    ))
                }.await()
            }
        }
    }

    suspend fun cancelByCadet(sessionId: String) {
        val session = getSession(sessionId) ?: return
        val startMs = session.startTime?.toDate()?.time
        if (isCadetCancelBlockedWithinSixHours(startMs)) {
            throw IllegalStateException(
                "Нельзя отменить за 6 часов до вождения. Сообщите своему инструктору или администратору.",
            )
        }
        firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId).update(
            "status", "cancelledByCadet",
            "cancelledAt", Timestamp.now(),
        ).await()
        // Освободить окно инструктора по ID из сессии (надёжно) или поиском по полям
        val windowId = session.openWindowId
        if (!windowId.isBlank()) {
            firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS).document(windowId)
                .update("status", "free", "cadetId", null).await()
            return
        }
        // Для старых сессий без openWindowId — ищем окно по instructorId и status
        val windows = firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS)
            .whereEqualTo("instructorId", session.instructorId)
            .whereEqualTo("status", "booked")
            .get().await()
        val startTime = session.startTime?.toDate()?.time ?: 0L
        for (doc in windows.documents) {
            val w = doc.toObject(InstructorOpenWindow::class.java) ?: continue
            if (w.cadetId == session.cadetId && w.dateTime?.toDate()?.time == startTime) {
                doc.reference.update("status", "free", "cadetId", null).await()
                break
            }
        }
    }

    /** Инструктор опаздывает: сдвиг времени на N минут и уведомление для курсанта. */
    suspend fun setInstructorRunningLate(sessionId: String, delayMinutes: Int) {
        val session = getSession(sessionId) ?: return
        val oldStart = session.startTime ?: return
        val oldMillis = oldStart.toDate().time
        val newMillis = oldMillis + delayMinutes * 60_000L
        val newStartTime = Timestamp(Date(newMillis))
        val updates = mutableMapOf<String, Any>(
            "startTime" to newStartTime,
            "delayNotificationMinutes" to delayMinutes,
        )
        firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId).update(updates).await()
        if (session.openWindowId.isNotBlank()) {
            firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS).document(session.openWindowId)
                .update("dateTime", newStartTime).await()
        }
    }

    suspend fun clearDelayNotification(sessionId: String) {
        firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId)
            .update("delayNotificationMinutes", null).await()
    }

    suspend fun getSession(sessionId: String): DrivingSession? {
        val doc = firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId).get().await()
        return doc.toObject(DrivingSession::class.java)?.copy(id = doc.id)
    }

    fun openWindowsForInstructor(instructorId: String): Flow<List<InstructorOpenWindow>> = callbackFlow {
        val listener = firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS)
            .whereEqualTo("instructorId", instructorId)
            .orderBy("dateTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(InstructorOpenWindow::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun openWindowsForCadet(instructorId: String): Flow<List<InstructorOpenWindow>> = callbackFlow {
        val listener = firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS)
            .whereEqualTo("instructorId", instructorId)
            .whereEqualTo("status", "free")
            .orderBy("dateTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(InstructorOpenWindow::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addOpenWindow(instructorId: String, dateTime: Timestamp): String {
        val ref = firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS).document()
        ref.set(mapOf(
            "instructorId" to instructorId,
            "dateTime" to dateTime,
            "status" to "free",
        )).await()
        return ref.id
    }

    suspend fun bookWindow(windowId: String, cadetId: String) {
        firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS).document(windowId).update(
            "cadetId", cadetId,
            "status", "booked",
        ).await()
        val window = firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS).document(windowId).get().await()
        val instructorId = window.getString("instructorId") ?: return
        val dateTime = window.getTimestamp("dateTime") ?: return
        createSession(instructorId, cadetId, dateTime, openWindowId = windowId)
    }

    suspend fun deleteSession(sessionId: String) {
        firestore.collection(FirebasePaths.DRIVING_SESSIONS).document(sessionId).delete().await()
    }

    suspend fun deleteOpenWindow(windowId: String) {
        firestore.collection(FirebasePaths.INSTRUCTOR_OPEN_WINDOWS).document(windowId).delete().await()
    }

    fun balanceHistory(userId: String): Flow<List<com.example.startdrive.data.model.BalanceHistory>> = callbackFlow {
        val listener = firestore.collection(FirebasePaths.USERS).document(userId)
            .collection(FirebasePaths.BALANCE_HISTORY)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(com.example.startdrive.data.model.BalanceHistory::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /** Все операции по балансу (для админа). */
    fun allBalanceHistory(): Flow<List<com.example.startdrive.data.model.BalanceHistory>> = callbackFlow {
        val listener = firestore.collectionGroup(FirebasePaths.BALANCE_HISTORY)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(com.example.startdrive.data.model.BalanceHistory::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun drivingHistory(userId: String, isInstructor: Boolean): Flow<List<DrivingSession>> = callbackFlow {
        val field = if (isInstructor) "instructorId" else "cadetId"
        val listener = firestore.collection(FirebasePaths.DRIVING_SESSIONS)
            .whereEqualTo(field, userId)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(DrivingSession::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }
}
