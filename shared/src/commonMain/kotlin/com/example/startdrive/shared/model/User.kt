package com.example.startdrive.shared.model

import kotlinx.serialization.Serializable

/**
 * Модель пользователя для shared (Android и Web).
 * В app используется маппинг из Firestore; в webApp — из Firebase JS.
 */
@Serializable
data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "", // admin, instructor, cadet
    val balance: Int = 0,
    val fcmToken: String? = null,
    val assignedInstructorId: String? = null,
    val assignedCadets: List<String> = emptyList(),
    val isActive: Boolean = false,
    val createdAtMillis: Long? = null,
    val chatAvatarUrl: String? = null,
    /** Id документа в [FirebasePaths.CADET_GROUPS] (только для role=cadet). */
    val cadetGroupId: String? = null,
) {
    fun initials(): String =
        fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")

    fun shortName(): String {
        val parts = fullName.split(" ")
        val first = parts.getOrNull(0) ?: return fullName
        val second = parts.getOrNull(1)?.firstOrNull()?.plus(".") ?: ""
        return "$first $second".trim()
    }
}
