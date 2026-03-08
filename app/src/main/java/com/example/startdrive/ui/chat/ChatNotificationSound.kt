package com.example.startdrive.ui.chat

import android.media.RingtoneManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.startdrive.data.model.ChatMessage

/**
 * Воспроизводит звук уведомления при появлении нового сообщения от другого пользователя.
 */
@androidx.compose.runtime.Composable
fun ChatNotificationSound(
    messages: List<ChatMessage>,
    currentUserId: String,
    chatRoomId: String,
) {
    val context = LocalContext.current
    var prevCount by remember(chatRoomId) { mutableStateOf(-1) }
    LaunchedEffect(messages.size, messages.map { it.timestamp }.maxOrNull()) {
        if (prevCount >= 0 && messages.size > prevCount) {
            val sorted = messages.sortedBy { it.timestamp }
            val newOnes = sorted.takeLast(messages.size - prevCount)
            if (newOnes.any { it.senderId != currentUserId }) {
                try {
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    RingtoneManager.getRingtone(context, uri)?.play()
                } catch (_: Exception) { }
            }
        }
        prevCount = messages.size
    }
}
