package com.example.startdrive.ui.settings

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.startdrive.data.repository.AppSettings
import com.example.startdrive.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun AvatarCropScreen(
    imageUri: Uri,
    onDone: (String) -> Unit,
    onCancel: () -> Unit,
    currentUserId: String? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(imageUri) {
        bitmap = withContext(Dispatchers.IO) {
            try {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(imageUri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                var sampleSize = 1
                val max = 2048
                while (opts.outWidth / sampleSize > max || opts.outHeight / sampleSize > max) {
                    sampleSize = sampleSize shl 1
                }
                opts.inJustDecodeBounds = false
                opts.inSampleSize = sampleSize
                context.contentResolver.openInputStream(imageUri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                }
            } catch (e: Exception) {
                loadError = e.message
                null
            }
        }
    }

    if (loadError != null) {
        Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
            Text("Не удалось загрузить изображение", color = MaterialTheme.colorScheme.error)
            Button(onClick = onCancel, modifier = Modifier.align(Alignment.BottomCenter)) {
                Text("Закрыть")
            }
        }
        return
    }

    val bmp = bitmap
    if (bmp == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Загрузка…", color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var cropBoxSize by remember { mutableStateOf(IntSize.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)
        offsetX += panChange.x
        offsetY += panChange.y
    }

    val scope = rememberCoroutineScope()
    val userRepo = remember { UserRepository() }
    val imgW = bmp.width.toFloat()
    val imgH = bmp.height.toFloat()
    val minScale = remember(cropBoxSize, imgW, imgH) {
        if (cropBoxSize.width == 0 || cropBoxSize.height == 0) 1f
        else {
            val r = minOf(cropBoxSize.width, cropBoxSize.height) / 2f
            (2 * r / minOf(imgW, imgH)).coerceAtLeast(0.1f)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val cropSize = minOf(maxWidth, maxHeight)

        LaunchedEffect(cropBoxSize) {
            if (minScale > 0f && scale < minScale) scale = minScale
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { cropBoxSize = it }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(cropSize)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY,
                    )
                    .transformable(state = transformState),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val left = cx - imgW / 2f
                    val top = cy - imgH / 2f
                    drawImage(
                        image = bmp.asImageBitmap(),
                        topLeft = Offset(left, top),
                    )
                }
            }

            Canvas(Modifier.fillMaxSize()) {
                val r = minOf(size.width, size.height) / 2f
                val cx = size.width / 2f
                val cy = size.height / 2f
                val rectPath = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                }
                val circlePath = Path().apply {
                    addOval(Rect(cx - r, cy - r, cx + r, cy + r))
                }
                val framePath = Path.combine(PathOperation.Difference, rectPath, circlePath)
                drawPath(framePath, Color.Black.copy(alpha = 0.6f))
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Отмена", modifier = Modifier.padding(start = 8.dp))
                }
                Button(
                    onClick = {
                        val dest = AppSettings.chatAvatarFile(context)
                        val outSize = 256
                        val r = minOf(cropBoxSize.width, cropBoxSize.height) / 2f
                        if (r <= 0) return@Button
                        val icx = imgW / 2f - offsetX / scale
                        val icy = imgH / 2f - offsetY / scale
                        val imgR = r / scale
                        val path = dest.absolutePath
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val out = Bitmap.createBitmap(outSize, outSize, Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(out)
                                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                                val matrix = android.graphics.Matrix().apply {
                                    setScale(128f / imgR, 128f / imgR)
                                    postTranslate(-icx * (128f / imgR) + 128f, -icy * (128f / imgR) + 128f)
                                }
                                canvas.save()
                                canvas.drawBitmap(bmp, matrix, paint)
                                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
                                canvas.drawCircle(128f, 128f, 128f, paint)
                                canvas.restore()
                                FileOutputStream(dest).use { out.compress(Bitmap.CompressFormat.PNG, 90, it) }
                                out.recycle()
                            }
                            if (!currentUserId.isNullOrBlank()) {
                                try {
                                    val url = userRepo.uploadChatAvatar(currentUserId, File(path))
                                    userRepo.updateChatAvatarUrl(currentUserId, url)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Не удалось загрузить аватар: ${e.message ?: "ошибка"}",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            onDone(path)
                        }
                    },
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Готово", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
