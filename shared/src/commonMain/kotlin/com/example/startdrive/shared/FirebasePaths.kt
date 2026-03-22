package com.example.startdrive.shared

object FirebasePaths {
    const val USERS = "users"
    /** Учебные группы курсантов (номер, срок обучения). */
    const val CADET_GROUPS = "cadet_groups"
    /** Групповые чаты (название, участники; сообщения в RTDB chats/group_{id}/messages). */
    const val CHAT_GROUPS = "chat_groups"
    /** Лента событий для админов (пишут только Cloud Functions; веб подписывается для уведомлений без FCM). */
    const val ADMIN_EVENTS = "admin_events"
    const val DRIVING_SESSIONS = "driving_sessions"
    const val INSTRUCTOR_OPEN_WINDOWS = "instructor_open_windows"
    const val BALANCE_HISTORY = "balance_history"
    const val CHATS = "chats"
    const val MESSAGES = "messages"
    const val PRESENCE = "presence"
}
