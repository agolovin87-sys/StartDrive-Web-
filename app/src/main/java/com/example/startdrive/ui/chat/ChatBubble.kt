package com.example.startdrive.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.startdrive.data.model.ChatMessage
import com.example.startdrive.ui.theme.MessageBubbleIn
import com.example.startdrive.ui.theme.MessageBubbleOut
import com.example.startdrive.ui.chat.VoiceMessagePlayer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val textColorDark = Color(0xFF212121)

/**
 * Формат времени в чате. Можно изменить под себя:
 * dd — день (01–31), MM — месяц (01–12), yy или yyyy — год (24 или 2024)
 * HH — часы 00–23 (24ч), hh — 01–12 (12ч), mm — минуты, a — AM/PM (для 12ч)
 */
private const val CHAT_TIME_FORMAT = "dd.MM.yy HH:mm"

/**
 * Часовой пояс для времени в чате (Екатеринбург). Чтобы у отправителя и получателя было одно и то же время —
 * укажите одну зону. Если null — каждый видит время по своему устройству.
 */
private val CHAT_TIME_ZONE: TimeZone? = TimeZone.getTimeZone("Asia/Yekaterinburg")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    isOutgoing: Boolean,
    onLongPress: (() -> Unit)? = null,
) {
    val bubbleColor = if (isOutgoing) MessageBubbleOut else MessageBubbleIn
    val align = if (isOutgoing) Alignment.End else Alignment.Start
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isOutgoing) 16.dp else 4.dp,
        bottomEnd = if (isOutgoing) 4.dp else 16.dp,
    )
    val timeFormat = SimpleDateFormat(CHAT_TIME_FORMAT, Locale.getDefault()).apply {
        CHAT_TIME_ZONE?.let { timeZone = it }
    }
    val timeStr = timeFormat.format(Date(message.timestamp))
    // Галочки: одна — отправлено, две — доставлено/прочитано; синие — прочитано
    val (checkIcon, checkTint) = when (message.status) {
        "read" -> Icons.Default.DoneAll to Color(0xFF4CAF50)
        "delivered" -> Icons.Default.DoneAll to textColorDark.copy(alpha = 0.6f)
        else -> Icons.Default.Done to textColorDark.copy(alpha = 0.6f)
    }
    val checkSize = 14.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .then(
                if (onLongPress != null) Modifier.combinedClickable(onLongClick = onLongPress) { }
                else Modifier
            ),
        horizontalAlignment = align,
    ) {
        Column(
            modifier = Modifier
                .wrapContentWidth(align = align)
                .widthIn(min = if (message.isVoice) 240.dp else 0.dp, max = 280.dp)
                .padding(8.dp)
                .background(bubbleColor, shape)
                .padding(12.dp),
            horizontalAlignment = align,
        ) {
            if (message.replyToText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .background(
                            textColorDark.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(3.dp, 24.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = message.replyToText!!.take(80).let { if (it.length == 80) "$it…" else it },
                        style = MaterialTheme.typography.labelSmall,
                        color = textColorDark.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp),
                        maxLines = 2,
                    )
                }
            }
            if (!message.voiceUrl.isNullOrBlank() && (message.voiceDurationSec ?: 0) > 0) {
                VoiceMessagePlayer(
                    voiceUrl = message.voiceUrl,
                    durationSec = message.voiceDurationSec!!,
                )
            }
            if (message.text.isNotEmpty()) {
                Text(
                    text = message.text,
                    color = textColorDark,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColorDark.copy(alpha = 0.7f),
                )
                if (isOutgoing) {
                    Icon(
                        imageVector = checkIcon,
                        contentDescription = when (message.status) {
                            "read" -> "Прочитано"
                            "delivered" -> "Доставлено"
                            else -> "Отправлено"
                        },
                        tint = checkTint,
                        modifier = Modifier.size(checkSize),
                    )
                }
            }
        }
    }
}
