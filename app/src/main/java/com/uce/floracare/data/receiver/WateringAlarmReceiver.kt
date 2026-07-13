package com.uce.floracare.data.receiver

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.uce.floracare.R
import com.uce.floracare.application.activities.MainActivity
import com.uce.floracare.data.notifications.NotificationHelper
import com.uce.floracare.data.scheduler.AndroidWateringScheduler

class WateringAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        Log.d(
            TAG,
            "Alarma de riego recibida"
        )

        val prefs =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val notificationsEnabled =
            prefs.getBoolean(
                KEY_NOTIFICATIONS_ENABLED,
                true
            )

        if (!notificationsEnabled) {

            Log.d(
                TAG,
                "Recordatorios de riego desactivados"
            )

            return
        }

        val plantId =
            intent.getStringExtra(
                AndroidWateringScheduler.EXTRA_PLANT_ID
            )

        if (plantId.isNullOrBlank()) {

            Log.e(
                TAG,
                "La alarma no contiene el identificador de la planta"
            )

            return
        }

        val plantName =
            intent.getStringExtra(
                AndroidWateringScheduler.EXTRA_PLANT_NAME
            )
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Tu planta"

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Log.e(
                TAG,
                "El permiso POST_NOTIFICATIONS no está concedido"
            )

            return
        }

        NotificationHelper.createChannel(
            context
        )

        val mainIntent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                putExtra(
                    EXTRA_NAVIGATE_TO,
                    DESTINATION_MY_GARDEN
                )
            }

        val contentPendingIntent =
            PendingIntent.getActivity(
                context,
                plantId.hashCode(),
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                context,
                NotificationHelper.CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_launcher_foreground
                )
                .setContentTitle(
                    "Hora de regar"
                )
                .setContentText(
                    "$plantName necesita agua"
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "Tu planta $plantName necesita agua. " +
                                    "Ingresa a FloraCare para registrar el riego."
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setCategory(
                    NotificationCompat.CATEGORY_REMINDER
                )
                .setContentIntent(
                    contentPendingIntent
                )
                .setAutoCancel(true)
                .build()

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        notificationManager.notify(
            plantId.hashCode(),
            notification
        )

        Log.d(
            TAG,
            "Notificación enviada para $plantName"
        )
    }

    companion object {

        private const val TAG =
            "WateringReceiver"

        private const val PREFS_NAME =
            "flora_care_prefs"

        private const val KEY_NOTIFICATIONS_ENABLED =
            "notifications_enabled"

        private const val EXTRA_NAVIGATE_TO =
            "navigate_to"

        private const val DESTINATION_MY_GARDEN =
            "mi_jardin"
    }
}