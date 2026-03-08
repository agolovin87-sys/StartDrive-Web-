package com.example.startdrive.data.repository

import android.content.Context
import android.content.SharedPreferences

/** Сообщения, скрытые «только у меня» (удалить для себя) по комнате. */
object HiddenChatMessages {
    private const val PREFS_NAME = "hidden_chat_messages"
    private const val PREFIX = "room_"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun key(userId: String, chatRoomId: String) = "${PREFIX}${userId}_$chatRoomId"

    fun add(context: Context, userId: String, chatRoomId: String, messageId: String) {
        val set = getSet(context, userId, chatRoomId).toMutableSet()
        set.add(messageId)
        prefs(context).edit().putStringSet(key(userId, chatRoomId), set).apply()
    }

    fun getSet(context: Context, userId: String, chatRoomId: String): Set<String> =
        prefs(context).getStringSet(key(userId, chatRoomId), null) ?: emptySet()

    /** Удаляет все сохранённые «скрытые» сообщения для пользователя [userId]. */
    fun clearAllForUser(context: Context, userId: String) {
        val all = prefs(context).all
        val editor = prefs(context).edit()
        for ((k, _) in all) {
            if (k is String && k.startsWith("${PREFIX}${userId}_")) {
                editor.remove(k)
            }
        }
        editor.apply()
    }
}
