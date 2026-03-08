package com.example.startdrive.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.startdrive.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class StartDriveFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token will be sent to Firestore from MainActivity when user is logged in
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title: String
        val body: String
        val dataChannelId: String?
        if (message.notification != null) {
            title = message.notification!!.title ?: "StartDrive"
            body = message.notification!!.body ?: ""
            dataChannelId = message.data["channelId"]
        } else if (message.data.isNotEmpty()) {
            title = message.data["title"] ?: "StartDrive"
            body = message.data["body"] ?: ""
            dataChannelId = message.data["channelId"]
        } else return
        showNotification(title, body, dataChannelId ?: "startdrive_general")
    }

    private fun showNotification(title: String, body: String, channelId: String) {
        ensureChannel(channelId)
        val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationId = (System.currentTimeMillis() and 0x7FFFFFFF).toInt()
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }

    private fun ensureChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val name = when (channelId) {
            "startdrive_chat" -> "Сообщения чата"
            "startdrive_driving" -> "Вождение"
            "startdrive_balance" -> "Баланс"
            else -> "Уведомления"
        }
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val channel = NotificationChannel(
            channelId,
            name,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            setShowBadge(true)
            enableVibration(true)
            setSound(soundUri, android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
