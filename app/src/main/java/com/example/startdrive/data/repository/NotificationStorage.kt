package com.example.startdrive.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Сохранение и загрузка списка уведомлений пользователя.
 * При нажатии «Очистить» вызывать [clear].
 */
object NotificationStorage {
    private const val PREFS_NAME = "startdrive_notifications"
    private const val KEY_PREFIX = "list_"

    private val ITEM_SEP = "\u001E"
    private val FIELD_SEP = "\u001F"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(context: Context, userId: String, items: List<StoredNotification>) {
        val encoded = items.joinToString(ITEM_SEP) { n ->
            "${n.id}$FIELD_SEP${n.timestamp}$FIELD_SEP${n.message}"
        }
        prefs(context).edit().putString(KEY_PREFIX + userId, encoded).apply()
    }

    fun load(context: Context, userId: String): List<StoredNotification> {
        val raw = prefs(context).getString(KEY_PREFIX + userId, null) ?: return emptyList()
        if (raw.isEmpty()) return emptyList()
        return raw.split(ITEM_SEP).mapNotNull { part ->
            val fields = part.split(FIELD_SEP, limit = 3)
            if (fields.size >= 3) {
                StoredNotification(
                    id = fields[0].toLongOrNull() ?: return@mapNotNull null,
                    timestamp = fields[1].toLongOrNull() ?: return@mapNotNull null,
                    message = fields[2],
                )
            } else null
        }
    }

    fun clear(context: Context, userId: String) {
        prefs(context).edit().remove(KEY_PREFIX + userId).apply()
    }

    data class StoredNotification(val id: Long, val timestamp: Long, val message: String)
}
