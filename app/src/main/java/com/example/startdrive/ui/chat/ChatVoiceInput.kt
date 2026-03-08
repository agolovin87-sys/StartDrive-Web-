package com.example.startdrive.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
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
    var recordStartMs by remember { mutableStateOf(0L) }
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
                recordStartMs = System.currentTimeMillis()
                recordElapsedSec = 0
                isRecording = true
            }
        } else {
            android.widget.Toast.makeText(context, "Нужен доступ к микрофону для голосовых сообщений", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isRecording) {
        if (!isRecording) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(1000)
            if (!isRecording) break
            recordElapsedSec = ((System.currentTimeMillis() - recordStartMs) / 1000).toInt().coerceAtLeast(0)
        }
    }

    if (isRecording) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Запись… %d:%02d".format(recordElapsedSec / 60, recordElapsedSec % 60),
                style = MaterialTheme.typography.bodyLarge,
            )
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
                    voiceFile?.let { file ->
                        val durationSec = ((System.currentTimeMillis() - recordStartMs) / 1000).toInt().coerceAtLeast(1)
                        onSendVoice(file, durationSec)
                    }
                    voiceFile = null
                },
                modifier = Modifier.size(44.dp),
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Остановить", tint = Color.Red, modifier = Modifier.size(28.dp))
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
                        recordStartMs = System.currentTimeMillis()
                        recordElapsedSec = 0
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
        val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
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
