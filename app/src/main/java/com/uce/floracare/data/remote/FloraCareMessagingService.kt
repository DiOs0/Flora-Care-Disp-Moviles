package com.uce.floracare.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.uce.floracare.application.activities.MainActivity

class FloraCareMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        val prefs =
            getSharedPreferences(
                "flora_care_prefs",
                Context.MODE_PRIVATE
            )


        val enabled =
            prefs.getBoolean(
                "notifications_enabled",
                true
            )


        if(!enabled) return



        val title =
            remoteMessage.data["title"]
                ?: "FloraCare"



        val body =
            remoteMessage.data["body"]
                ?: "Revisa tus plantas"



        sendNotification(
            title,
            body
        )

    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun sendNotification(title: String, messageBody: String) {
        val channelId = "remote_watering_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas Remotas de FloraCare",
                NotificationManager.IMPORTANCE_HIGH // UX Premium: Canales de alta prioridad
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "mi_jardin")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            // Estilo expansible para albergar descripciones largas sin truncamiento
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))

        notificationManager.notify(
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            notificationBuilder.build()
        )    }
}