package com.example.startdrive.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Сохраняет «базовое» состояние уведомлений при выходе из кабинета,
 * чтобы после повторного входа не показывать уже полученные уведомления.
 */
object InAppNotificationBaseline {
    private const val PREFS_NAME = "startdrive_inapp_baseline"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveInstructor(
        context: Context,
        userId: String,
        totalUnread: Int,
        balanceCount: Int,
        totalHistoryCount: Int,
        sessionStates: Map<String, Triple<String, Boolean, Boolean>>,
    ) {
        val sessionStr = sessionStates.entries.joinToString(",") { (id, t) ->
            "$id|${t.first}|${t.second}|${t.third}"
        }
        prefs(context).edit()
            .putInt("${userId}_unread", totalUnread)
            .putInt("${userId}_balance", balanceCount)
            .putInt("${userId}_history", totalHistoryCount)
            .putString("${userId}_sessions", sessionStr)
            .apply()
    }

    fun loadInstructor(context: Context, userId: String): InstructorBaseline? {
        val p = prefs(context)
        val unread = p.getInt("${userId}_unread", -1)
        if (unread < 0) return null
        val balance = p.getInt("${userId}_balance", -1)
        val history = p.getInt("${userId}_history", -1)
        val sessionStr = p.getString("${userId}_sessions", null) ?: ""
        val sessions = mutableMapOf<String, Triple<String, Boolean, Boolean>>()
        if (sessionStr.isNotEmpty()) {
            sessionStr.split(",").forEach { part ->
                val tokens = part.split("|")
                when {
                    tokens.size >= 4 -> sessions[tokens[0]] = Triple(tokens[1], tokens[2] == "true", tokens[3] == "true")
                    tokens.size >= 3 -> sessions[tokens[0]] = Triple(tokens[1], false, tokens[2] == "true")
                    else -> { }
                }
            }
        }
        return InstructorBaseline(unread, balance, history, sessions)
    }

    data class InstructorBaseline(
        val unread: Int,
        val balanceCount: Int,
        val historyTotalCount: Int,
        val sessionStates: Map<String, Triple<String, Boolean, Boolean>>,
    )

    fun saveCadet(
        context: Context,
        userId: String,
        totalUnread: Int,
        balanceCount: Int,
        openWindowsCount: Int,
        totalHistoryCount: Int,
        sessionStates: Map<String, Pair<String, Boolean>>,
    ) {
        val sessionStr = sessionStates.entries.joinToString(",") { (id, p) ->
            "$id|${p.first}|${p.second}"
        }
        prefs(context).edit()
            .putInt("${userId}_unread", totalUnread)
            .putInt("${userId}_balance", balanceCount)
            .putInt("${userId}_open_windows", openWindowsCount)
            .putInt("${userId}_history", totalHistoryCount)
            .putString("${userId}_sessions", sessionStr)
            .apply()
    }

    fun loadCadet(context: Context, userId: String): CadetBaseline? {
        val p = prefs(context)
        val unread = p.getInt("${userId}_unread", -1)
        if (unread < 0) return null
        val balance = p.getInt("${userId}_balance", -1)
        val openWindows = p.getInt("${userId}_open_windows", -1)
        val history = p.getInt("${userId}_history", -1)
        val sessionStr = p.getString("${userId}_sessions", null) ?: ""
        val sessions = mutableMapOf<String, Pair<String, Boolean>>()
        if (sessionStr.isNotEmpty()) {
            sessionStr.split(",").forEach { part ->
                val tokens = part.split("|")
                if (tokens.size >= 3) {
                    sessions[tokens[0]] = tokens[1] to (tokens[2] == "true")
                }
            }
        }
        return CadetBaseline(unread, balance, openWindows, history, sessions)
    }

    data class CadetBaseline(
        val unread: Int,
        val balanceCount: Int,
        val openWindowsCount: Int,
        val historyTotalCount: Int,
        val sessionStates: Map<String, Pair<String, Boolean>>,
    )

    fun saveAdmin(
        context: Context,
        userId: String,
        totalUnread: Int,
        totalHistoryCount: Int,
    ) {
        prefs(context).edit()
            .putInt("${userId}_unread", totalUnread)
            .putInt("${userId}_history", totalHistoryCount)
            .apply()
    }

    fun loadAdmin(context: Context, userId: String): AdminBaseline? {
        val p = prefs(context)
        val unread = p.getInt("${userId}_unread", -1)
        if (unread < 0) return null
        val history = p.getInt("${userId}_history", -1)
        return AdminBaseline(unread, history)
    }

    data class AdminBaseline(
        val unread: Int,
        val historyTotalCount: Int,
    )
}
