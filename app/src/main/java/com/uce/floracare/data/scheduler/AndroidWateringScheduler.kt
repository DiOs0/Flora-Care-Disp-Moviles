package com.uce.floracare.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.uce.floracare.data.receiver.WateringAlarmReceiver
import com.uce.floracare.data.remote.dto.PlantEntity

class AndroidWateringScheduler(
    context: Context
) : WateringScheduler {

    private val appContext =
        context.applicationContext

    private val alarmManager =
        appContext.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    override fun schedule(
        plant: PlantEntity
    ) {
        Log.d(TAG, "intentando programar alarma para ${plant.nombreComun} con ID ${plant.firestoreId}")
        if (plant.firestoreId.isBlank()) {

            Log.e(
                TAG,
                "No se programó la alarma: firestoreId vacío"
            )

            return
        }

        val triggerTime =
            plant.nextWateringTimestamp

        Log.d(TAG, "Programando alarma para ${plant.nombreComun} en $triggerTime (Ahora: ${System.currentTimeMillis()})")

        val pendingIntent =
            createSchedulePendingIntent(plant)

        try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S
            ) {

                if (
                    alarmManager
                        .canScheduleExactAlarms()
                ) {

                    alarmManager
                        .setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )

                    Log.d(
                        TAG,
                        "Alarma exacta programada para $triggerTime"
                    )

                } else {

                    alarmManager
                        .setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )

                    Log.w(
                        TAG,
                        "Alarma aproximada programada para $triggerTime"
                    )
                }

            } else if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M
            ) {

                alarmManager
                    .setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )

                Log.d(
                    TAG,
                    "Alarma programada para $triggerTime"
                )

            } else {

                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

        } catch (e: SecurityException) {

            Log.e(
                TAG,
                "No se pudo programar la alarma",
                e
            )
        }
    }

    override fun cancel(
        plant: PlantEntity
    ) {

        if (plant.firestoreId.isBlank()) {
            return
        }

        val pendingIntent =
            findExistingPendingIntent(
                plant
            )

        pendingIntent?.let {

            alarmManager.cancel(it)

            it.cancel()

            Log.d(
                TAG,
                "Alarma cancelada para ${plant.nombreComun}"
            )
        }
    }

    private fun createSchedulePendingIntent(
        plant: PlantEntity
    ): PendingIntent {

        val intent =
            createAlarmIntent(plant)

        return PendingIntent.getBroadcast(
            appContext,
            plant.firestoreId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun findExistingPendingIntent(
        plant: PlantEntity
    ): PendingIntent? {

        val intent =
            createAlarmIntent(plant)

        return PendingIntent.getBroadcast(
            appContext,
            plant.firestoreId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAlarmIntent(
        plant: PlantEntity
    ): Intent {

        return Intent(
            appContext,
            WateringAlarmReceiver::class.java
        ).apply {

            action =
                ACTION_WATERING_REMINDER

            putExtra(
                EXTRA_PLANT_ID,
                plant.firestoreId
            )

            putExtra(
                EXTRA_PLANT_NAME,
                plant.nombreComun
            )
        }
    }

    companion object {

        private const val TAG =
            "WateringScheduler"

        const val ACTION_WATERING_REMINDER =
            "com.uce.floracare.WATERING_REMINDER"

        const val EXTRA_PLANT_ID =
            "plant_id"

        const val EXTRA_PLANT_NAME =
            "plant_name"
    }
}