package com.example.startdrive.data.repository

import com.example.startdrive.data.FirebasePaths
import com.example.startdrive.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUser(userId: String): User? {
        val doc = firestore.collection(FirebasePaths.USERS).document(userId).get().await()
        return doc.toObject(User::class.java)?.copy(id = doc.id)
    }

    fun usersByRole(role: String): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection(FirebasePaths.USERS)
            .whereEqualTo("role", role)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun allNonAdminUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection(FirebasePaths.USERS)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                }?.filter { it.role != "admin" } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun setActive(userId: String, active: Boolean) {
        firestore.collection(FirebasePaths.USERS).document(userId).update("isActive", active).await()
    }

    suspend fun assignCadetToInstructor(instructorId: String, cadetId: String) {
        val instructorRef = firestore.collection(FirebasePaths.USERS).document(instructorId)
        firestore.runTransaction { tx ->
            val instructor = tx.get(instructorRef).toObject(User::class.java)
            val list = (instructor?.assignedCadets?.toMutableList() ?: mutableListOf()).apply {
                if (!contains(cadetId)) add(cadetId)
            }
            tx.update(instructorRef, "assignedCadets", list)
            tx.update(firestore.collection(FirebasePaths.USERS).document(cadetId), "assignedInstructorId", instructorId)
        }.await()
    }

    suspend fun removeCadetFromInstructor(instructorId: String, cadetId: String) {
        val instructorRef = firestore.collection(FirebasePaths.USERS).document(instructorId)
        val cadetRef = firestore.collection(FirebasePaths.USERS).document(cadetId)
        firestore.runTransaction { tx ->
            val instructor = tx.get(instructorRef).toObject(User::class.java)
            val list = instructor?.assignedCadets?.toMutableList() ?: mutableListOf()
            list.remove(cadetId)
            tx.update(instructorRef, "assignedCadets", list)
            tx.update(cadetRef, "assignedInstructorId", null)
        }.await()
    }

    suspend fun updateBalance(userId: String, type: String, amount: Int, performedBy: String) {
        val userRef = firestore.collection(FirebasePaths.USERS).document(userId)
        val historyRef = userRef.collection(FirebasePaths.BALANCE_HISTORY).document()
        firestore.runTransaction { tx ->
            val snap = tx.get(userRef)
            val current = (snap.getLong("balance") ?: 0).toInt()
            val newBalance = when (type) {
                "credit" -> current + amount
                "debit" -> (current - amount).coerceAtLeast(0)
                "set" -> amount
                else -> current
            }
            tx.update(userRef, "balance", newBalance)
            tx.set(historyRef, mapOf(
                "userId" to userId,
                "amount" to amount,
                "type" to type,
                "performedBy" to performedBy,
                "timestamp" to com.google.firebase.Timestamp.now(),
            ))
        }.await()
    }

    suspend fun updateFcmToken(userId: String, token: String?) {
        firestore.collection(FirebasePaths.USERS).document(userId).update("fcmToken", token ?: "").await()
    }

    private val storage = FirebaseStorage.getInstance().reference

    /** Загружает файл аватара в Storage и возвращает URL. Путь: users/{userId}/chat_avatar.png */
    suspend fun uploadChatAvatar(userId: String, file: File): String {
        val ref = storage.child("users").child(userId).child("chat_avatar.png")
        val bytes = file.readBytes()
        ref.putBytes(bytes).await()
        // После загрузки объект может появиться в Storage с задержкой — повторяем getDownloadUrl с паузой
        var lastEx: Exception? = null
        repeat(4) { attempt ->
            try {
                return ref.getDownloadUrl().await().toString()
            } catch (e: Exception) {
                lastEx = e
                if (attempt < 3) delay(1000L * (attempt + 1))
            }
        }
        throw lastEx ?: Exception("Не удалось получить URL")
    }

    /** Записывает URL аватара в документ пользователя (merge — поле создаётся, если его ещё нет). */
    suspend fun updateChatAvatarUrl(userId: String, url: String?) {
        firestore.collection(FirebasePaths.USERS).document(userId)
            .set(mapOf("chatAvatarUrl" to (url ?: "")), SetOptions.merge()).await()
    }

    /** Удаляет файл аватара из Storage (users/{userId}/chat_avatar.png). */
    suspend fun deleteChatAvatarFromStorage(userId: String) {
        try {
            storage.child("users").child(userId).child("chat_avatar.png").delete().await()
        } catch (_: Exception) { /* файла может не быть */ }
    }

    suspend fun deleteUser(userId: String) {
        firestore.collection(FirebasePaths.USERS).document(userId).delete().await()
    }
}
