package com.example.startdrive.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun ChatVoiceInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendText: () -> Unit,
    onSendVoice: (File, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordElapsedSec by remember { mutableStateOf(0) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var voiceFile by remember { mutableStateOf<File?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startRecording(context) { mr, file ->
                mediaRecorder = mr
                voiceFile = file
                recordElapsedSec = 0
                isPaused = false
                isRecording = true
            }
        } else {
            android.widget.Toast.makeText(context, "Нужен доступ к микрофону для голосовых сообщений", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isRecording, isPaused) {
        if (!isRecording || isPaused) return@LaunchedEffect
        while (true) {
            delay(1000)
            if (!isRecording || isPaused) break
            recordElapsedSec += 1
        }
    }

    if (isRecording) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(
                onClick = {
                    mediaRecorder?.apply {
                        try {
                            stop()
                            release()
                        } catch (_: Exception) { }
                    }
                    mediaRecorder = null
                    isRecording = false
                    isPaused = false
                    voiceFile?.delete()
                    voiceFile = null
                },
                modifier = Modifier.size(44.dp),
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить запись", tint = Color(0xFF212121), modifier = Modifier.size(26.dp))
            }
            Text(
                "Запись… %d:%02d".format(recordElapsedSec / 60, recordElapsedSec % 60),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF212121),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                IconButton(
                    onClick = {
                        val mr = mediaRecorder ?: return@IconButton
                        try {
                            if (isPaused) {
                                mr.resume()
                                isPaused = false
                            } else {
                                mr.pause()
                                isPaused = true
                            }
                        } catch (_: Exception) { }
                    },
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Продолжить" else "Пауза",
                        tint = Color(0xFF212121),
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            IconButton(
                onClick = {
                    mediaRecorder?.apply {
                        try {
                            stop()
                            release()
                        } catch (_: Exception) { }
                    }
                    mediaRecorder = null
                    isRecording = false
                    isPaused = false
                    val sec = recordElapsedSec.coerceAtLeast(1)
                    voiceFile?.let { file -> onSendVoice(file, sec) }
                    voiceFile = null
                },
                modifier = Modifier.size(44.dp),
            ) {
                Icon(Icons.Default.Send, contentDescription = "Отправить", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            }
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = messageText,
            onValueChange = onMessageTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Сообщение") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
        )
        IconButton(
            onClick = {
                if (messageText.isNotBlank()) {
                    onSendText()
                }
            },
            modifier = Modifier.size(44.dp),
        ) {
            Icon(Icons.Default.Send, contentDescription = "Отправить", modifier = Modifier.size(28.dp))
        }
        IconButton(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    startRecording(context) { mr, file ->
                        mediaRecorder = mr
                        voiceFile = file
                        recordElapsedSec = 0
                        isPaused = false
                        isRecording = true
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier.size(44.dp),
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Голосовое сообщение", modifier = Modifier.size(28.dp))
        }
    }
}

private fun startRecording(context: android.content.Context, onReady: (MediaRecorder, File) -> Unit) {
    val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
    try {
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            setAudioChannels(1)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(128000)
            prepare()
            start()
        }
        onReady(recorder, file)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Ошибка записи: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}
