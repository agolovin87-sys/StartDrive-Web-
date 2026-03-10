package com.example.startdrive.shared.model

import kotlinx.serialization.Serializable

/**
 * Общая информация о приложении (версия, платформа).
 * Используется в Android и Web.
 */
@Serializable
data class AppInfo(
    val appName: String,
    val version: String,
    val platform: String
)
