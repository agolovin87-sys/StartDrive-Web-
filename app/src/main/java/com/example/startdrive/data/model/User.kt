package com.example.startdrive.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class User(
    @DocumentId val id: String = "",
    @get:PropertyName("fullName") @set:PropertyName("fullName") var fullName: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("phone") @set:PropertyName("phone") var phone: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = "", // admin, instructor, cadet
    @get:PropertyName("balance") @set:PropertyName("balance") var balance: Int = 0,
    @get:PropertyName("fcmToken") @set:PropertyName("fcmToken") var fcmToken: String? = null,
    @get:PropertyName("assignedInstructorId") @set:PropertyName("assignedInstructorId") var assignedInstructorId: String? = null,
    @get:PropertyName("assignedCadets") @set:PropertyName("assignedCadets") var assignedCadets: List<String> = emptyList(),
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = false,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Timestamp? = null,
    @get:PropertyName("chatAvatarUrl") @set:PropertyName("chatAvatarUrl") var chatAvatarUrl: String? = null,
    @get:PropertyName("cadetGroupId") @set:PropertyName("cadetGroupId") var cadetGroupId: String? = null,
) {
    fun initials(): String = fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
    fun shortName(): String = fullName.split(" ").getOrNull(0)?.plus(" ")?.plus(
        fullName.split(" ").getOrNull(1)?.firstOrNull()?.plus(".") ?: ""
    ) ?: fullName
}
