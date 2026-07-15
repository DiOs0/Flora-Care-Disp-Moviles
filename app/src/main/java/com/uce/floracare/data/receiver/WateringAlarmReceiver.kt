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

import com.uce.floracare.data.local.database.FloraCareDatabase
import com.uce.floracare.domain.usecase.GenerarTareasPendientesUseCase
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.TaskRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WateringAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val pendingResult = goAsync()

        Log.d(
            TAG,
            "Alarma de riego recibida - Acción: ${intent.action}"
        )

        // Ejecutar generación de tareas y notificación en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleAlarm(context, intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error procesando alarma", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleAlarm(context: Context, intent: Intent) {
        val plantId = intent.getStringExtra(AndroidWateringScheduler.EXTRA_PLANT_ID)
        val plantName = intent.getStringExtra(AndroidWateringScheduler.EXTRA_PLANT_NAME) ?: "Tu planta"

        if (plantId.isNullOrBlank()) {
            Log.e(TAG, "La alarma no contiene el identificador de la planta")
            return
        }

        // 1. Mostrar la notificación si está habilitada (Feedback inmediato)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

        if (notificationsEnabled) {
            showNotification(context, plantId, plantName)
        } else {
            Log.d(TAG, "Recordatorios de riego desactivados")
        }

        // 2. Generar la tarea pendiente en la base de datos
        generarTarea(context)
    }

    private suspend fun generarTarea(context: Context) {
        try {
            val authManager = AuthManager()
            val firestoreManager = FirestoreManager(authManager)
            val database = FloraCareDatabase.getDatabase(context)
            
            val taskRepository = TaskRepository(
                firestoreManager,
                authManager,
                database.taskDao()
            )

            val plantRepository = PlantRepository(
                firestoreManager,
                StorageManager(context),
                authManager,
                database.plantDao(),
                taskRepository
            )

            val useCase = GenerarTareasPendientesUseCase(plantRepository, taskRepository)
            useCase.invoke()
            Log.d(TAG, "Tarea generada exitosamente desde el Receiver")
        } catch (e: Exception) {
            Log.e(TAG, "Error al generar tarea en el Receiver", e)
        }
    }

    private fun showNotification(context: Context, plantId: String, plantName: String) {
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "El permiso POST_NOTIFICATIONS no está concedido")
            return
        }

        NotificationHelper.createChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAVIGATE_TO, DESTINATION_MY_GARDEN)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            plantId.hashCode(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hora de regar")
            .setContentText("$plantName necesita agua")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Tu planta $plantName necesita agua. Ingresa a FloraCare para registrar el riego."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(plantId.hashCode(), notification)
        Log.d(TAG, "Notificación enviada para $plantName")
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