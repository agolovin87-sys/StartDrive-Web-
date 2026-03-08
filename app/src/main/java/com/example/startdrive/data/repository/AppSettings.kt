package com.example.startdrive.data.repository

import android.content.Context
import android.content.SharedPreferences
import java.io.File

/** Настройки приложения: звуковые и текстовые уведомления, тема, аватар в чате. */
object AppSettings {
    private const val PREFS_NAME = "startdrive_settings"

    private const val KEY_SOUND_NOTIFICATIONS = "sound_notifications"
    private const val KEY_TEXT_NOTIFICATIONS = "text_notifications"
    private const val KEY_THEME = "theme"
    private const val CHAT_AVATAR_FILENAME = "chat_avatar.png"

    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSoundNotificationsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SOUND_NOTIFICATIONS, true)

    fun setSoundNotificationsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SOUND_NOTIFICATIONS, enabled).apply()
    }

    fun getTextNotificationsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_TEXT_NOTIFICATIONS, true)

    fun setTextNotificationsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_TEXT_NOTIFICATIONS, enabled).apply()
    }

    fun getTheme(context: Context): String =
        prefs(context).getString(KEY_THEME, THEME_LIGHT) ?: THEME_LIGHT

    fun setTheme(context: Context, theme: String) {
        prefs(context).edit().putString(KEY_THEME, theme).apply()
    }

    /** true если выбран тёмная тема или «как в системе» при тёмной системе. */
    fun isDarkTheme(context: Context, isSystemInDarkTheme: Boolean): Boolean =
        when (getTheme(context)) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            else -> isSystemInDarkTheme
        }

    /** Путь к файлу аватара в чате или null, если не задан. */
    fun getChatAvatarPath(context: Context): String? {
        val file = File(context.applicationContext.filesDir, CHAT_AVATAR_FILENAME)
        return file.takeIf { it.exists() }?.absolutePath
    }

    /** Файл для сохранения аватара чата (один и тот же для всех). */
    fun chatAvatarFile(context: Context): File =
        File(context.applicationContext.filesDir, CHAT_AVATAR_FILENAME)

    /** Удалить сохранённый аватар чата. */
    fun clearChatAvatar(context: Context) {
        chatAvatarFile(context).delete()
    }
}
