package com.example.startdrive.data

object FirebasePaths {
    /**
     * URL Realtime Database. Если база создана в регионе europe-west1 — укажите этот URL.
     * Если база в us-central1 (США) — оставьте null (будет использован стандартный).
     * Текущий проект: startdrive-573fa.
     */
    val REALTIME_DATABASE_URL: String? = "https://startdrive-573fa-default-rtdb.europe-west1.firebasedatabase.app"

    const val USERS = "users"
    const val DRIVING_SESSIONS = "driving_sessions"
    const val INSTRUCTOR_OPEN_WINDOWS = "instructor_open_windows"
    const val BALANCE_HISTORY = "balance_history"
    const val CHATS = "chats"
    const val MESSAGES = "messages"
    const val PRESENCE = "presence"

    fun balanceHistory(userId: String) = "users/$userId/balance_history"
    fun chatRoom(id1: String, id2: String): String {
        val sorted = listOf(id1, id2).sorted()
        return "chats/${sorted[0]}_${sorted[1]}"
    }
    fun chatMessages(chatRoomId: String) = "chats/$chatRoomId/messages"
    fun presence(userId: String) = "presence/$userId"
}
