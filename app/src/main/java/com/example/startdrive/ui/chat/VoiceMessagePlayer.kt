package com.example.startdrive.ui.chat

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.IOException
import kotlinx.coroutines.delay

@Composable
fun VoiceMessagePlayer(
    voiceUrl: String,
    durationSec: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableStateOf(0) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    val totalMs = durationSec * 1000

    DisposableEffect(voiceUrl) {
        onDispose {
            player?.release()
            player = null
            isPlaying = false
            currentPositionMs = 0
        }
    }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (true) {
            delay(200)
            val p = player ?: break
            if (!p.isPlaying) break
            currentPositionMs = p.currentPosition.coerceIn(0, totalMs)
        }
    }

    fun formatDuration(ms: Int): String {
        val sec = (ms / 1000).coerceAtLeast(0)
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    Row(
        modifier = modifier
            .widthIn(min = 200.dp, max = 280.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(
            onClick = {
                if (isPlaying) {
                    player?.pause()
                    player?.release()
                    player = null
                    isPlaying = false
                } else {
                    try {
                        val p = MediaPlayer().apply {
                            setDataSource(voiceUrl)
                            prepareAsync()
                            setOnPreparedListener {
                                start()
                                isPlaying = true
                            }
                            setOnCompletionListener {
                                isPlaying = false
                                currentPositionMs = totalMs
                                release()
                                player = null
                            }
                            setOnErrorListener { _, _, _ ->
                                isPlaying = false
                                release()
                                player = null
                                true
                            }
                        }
                        player = p
                    } catch (_: IOException) { }
                }
            },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            if (isPlaying && totalMs > 0) {
                LinearProgressIndicator(
                    progress = { (currentPositionMs.toFloat() / totalMs).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isPlaying && totalMs > 0) {
                    Text(
                        text = formatDuration(currentPositionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF212121).copy(alpha = 0.8f),
                    )
                }
                Text(
                    text = formatDuration(totalMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF212121),
                )
            }
        }
    }
}
