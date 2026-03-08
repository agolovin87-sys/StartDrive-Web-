package com.example.startdrive.ui.driving

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val SESSION_DURATION_MS = 90 * 60 * 1000L // 1.5 часа

@Composable
fun DrivingTimer(
    initialRemainingMs: Long,
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
    onTick: (Long) -> Unit,
) {
    var remaining by remember { mutableLongStateOf(initialRemainingMs.coerceAtLeast(0L)) }
    var paused by remember { mutableStateOf(isPaused) }

    LaunchedEffect(remaining, paused) {
        if (remaining <= 0) {
            onFinish()
            return@LaunchedEffect
        }
        if (paused) return@LaunchedEffect
        delay(1000)
        remaining = (remaining - 1000).coerceAtLeast(0)
        onTick(remaining)
    }

    val hours = remaining / 3600000
    val minutes = (remaining % 3600000) / 60000
    val seconds = (remaining % 60000) / 1000
    val timeStr = "%02d:%02d:%02d".format(hours, minutes, seconds)

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = timeStr,
            style = MaterialTheme.typography.displayMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            Button(
                onClick = {
                    paused = !paused
                    if (paused) onPause() else onResume()
                },
            ) {
                Text(if (paused) "Продолжить" else "Пауза")
            }
            Button(onClick = onFinish) {
                Text("Завершить досрочно")
            }
        }
    }
}
