package com.example.startdrive.data.repository

import com.example.startdrive.data.FirebasePaths
import com.example.startdrive.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUserId: String? get() = auth.currentUser?.uid
    val isLoggedIn: Boolean get() = auth.currentUser != null

    fun authStateFlow(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { a ->
            trySend(a.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        role: String,
    ): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw IllegalStateException("No user id")
        val isActive = role == "admin"
        val userMap = mapOf(
            "fullName" to fullName,
            "email" to email,
            "phone" to phone,
            "role" to role,
            "balance" to 0,
            "isActive" to isActive,
            "createdAt" to Timestamp.now(),
        )
        firestore.collection(FirebasePaths.USERS).document(uid).set(userMap).await()
    }

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection(FirebasePaths.USERS).document(uid).get().await()
            if (!doc.exists()) return null
            val data = doc.data ?: return null
            User(
                id = doc.id,
                fullName = (data["fullName"] as? String).orEmpty(),
                email = (data["email"] as? String).orEmpty(),
                phone = (data["phone"] as? String).orEmpty(),
                role = (data["role"] as? String).orEmpty(),
                balance = (data["balance"] as? Number)?.toInt() ?: 0,
                fcmToken = data["fcmToken"] as? String,
                assignedInstructorId = data["assignedInstructorId"] as? String,
                assignedCadets = (data["assignedCadets"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                isActive = data["isActive"] as? Boolean ?: false,
                createdAt = data["createdAt"] as? Timestamp,
                trainingVehicle = data["trainingVehicle"] as? String,
            )
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() = auth.signOut()
}
