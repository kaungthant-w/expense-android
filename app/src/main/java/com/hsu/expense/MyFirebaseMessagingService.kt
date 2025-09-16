package com.hsu.expense

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            handleNotificationMessage(it)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val festivalMessage = data["message"]
        val animation = data["animation"] ?: "fireworks"
        val festivalName = data["festival"]

        if (!festivalMessage.isNullOrEmpty()) {
            showFestivalNotification(festivalMessage, animation, festivalName)
        }
    }

    private fun handleNotificationMessage(notification: RemoteMessage.Notification) {
        val title = notification.title ?: "Festival Alert"
        val body = notification.body ?: ""

        showFestivalNotification(body, "fireworks", title)
    }

    private fun showFestivalNotification(message: String, animation: String, festivalName: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("festival_message", message)
            putExtra("festival_animation", animation)
            putExtra("festival_name", festivalName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "festival_channel")
            .setSmallIcon(R.drawable.ic_festival)
            .setContentTitle("🎉 ပွဲတော်နေ့")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        // Send token to your server if needed
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        // Implement if you need to send the token to your server
        Log.d("FCM", "Token: $token")
    }
}