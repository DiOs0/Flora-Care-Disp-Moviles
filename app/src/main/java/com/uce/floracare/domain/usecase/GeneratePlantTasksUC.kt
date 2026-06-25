package com.uce.floracare.domain.usecase

import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.domain.model.PlantTask
import com.uce.floracare.domain.model.TaskType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GeneratePlantTasksUC {

    operator fun invoke(plants: List<PlantEntity>): List<PlantTask> {
        val tasks = mutableListOf<PlantTask>()

        // 1. Tarea Inicial: Conteo de plantas
        tasks.add(
            PlantTask(
                title = "Estado del Jardín",
                description = "Tienes un total de ${plants.size} plantas registradas.",
                taskType = TaskType.INFO
            )
        )

        val plantsToWater = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = Calendar.getInstance().time

        plants.forEach { plant ->
            // Lógica de Riego
            val lastWateredDate = parseDate(plant.riego.frecuencia, dateFormat)
            val daysSinceWatering = if (lastWateredDate != null) {
                getDaysDifference(lastWateredDate, today)
            } else {
                Long.MAX_VALUE // Si no hay fecha, necesita riego
            }

            when {
                plant.tipo.contains("Suculenta", ignoreCase = true) && daysSinceWatering > 14 -> {
                    plantsToWater.add(plant.nombreComun)
                }
                (plant.tipo.contains("Interior", ignoreCase = true) || plant.tipo.contains("Exterior", ignoreCase = true))
                        && !plant.tipo.contains("Suculenta", ignoreCase = true) && daysSinceWatering > 5 -> {
                    plantsToWater.add(plant.nombreComun)
                }
            }

            // Lógica de Humedad Ambiental (Tropical)
            if (plant.tipo.contains("Tropical", ignoreCase = true)) {
                tasks.add(
                    PlantTask(
                        title = "Rociar ${plant.nombreComun}",
                        description = "Mantener humedad alta para esta especie tropical.",
                        taskType = TaskType.MISTING
                    )
                )
            }
        }

        // Agrupar tareas de riego
        if (plantsToWater.isNotEmpty()) {
            tasks.add(
                PlantTask(
                    title = "Regar ${plantsToWater.size} plantas",
                    description = "Las siguientes plantas necesitan agua: ${
                        plantsToWater.joinToString(
                            ", "
                        )
                    }",
                    taskType = TaskType.WATERING
                )
            )
        }

        return tasks
    }

    private fun parseDate(frecuenciaStr: String, format: SimpleDateFormat): Date? {
        // El repositorio guarda: "Último riego registrado: dd/MM/yyyy"
        return try {
            val datePart = frecuenciaStr.substringAfterLast(": ").trim()
            format.parse(datePart)
        } catch (e: Exception) {
            null
        }
    }

    private fun getDaysDifference(startDate: Date, endDate: Date): Long {
        val diff = endDate.time - startDate.time
        return TimeUnit.MILLISECONDS.toDays(diff)
    }
}