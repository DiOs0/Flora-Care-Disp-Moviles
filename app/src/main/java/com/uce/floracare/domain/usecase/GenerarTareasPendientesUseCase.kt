package com.uce.floracare.domain.usecase

import com.uce.floracare.domain.model.TaskEntity
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.TaskRepository
import java.util.concurrent.TimeUnit

class GenerarTareasPendientesUseCase(
    private val plantRepository: PlantRepository,
    private val taskRepository: TaskRepository
) {

    suspend operator fun invoke(): Result<Unit> {

        return try {

            val plantas =
                plantRepository
                    .getMyPlants()
                    .getOrThrow()

            val tareasActuales =
                taskRepository
                    .getPendingTasks()
                    .getOrThrow()

            val ahora =
                System.currentTimeMillis()

            plantas.forEach { planta ->

                if (planta.firestoreId.isBlank()) {
                    return@forEach
                }

                val necesitaRiego =
                    planta.nextWateringTimestamp <= ahora

                if (!necesitaRiego) {
                    return@forEach
                }

                val yaExiste =
                    tareasActuales.any { tarea ->

                        tarea.plantFirestoreId ==
                                planta.firestoreId &&
                                !tarea.completed
                    }

                if (yaExiste) {
                    return@forEach
                }

                val diasTranscurridos =
                    TimeUnit.MILLISECONDS.toDays(
                        ahora - planta.ultimoRiego
                    ).coerceAtLeast(0)

                val textoDias =
                    when (diasTranscurridos) {

                        0L ->
                            "La planta necesita ser regada"

                        1L ->
                            "Ha pasado 1 día desde el último riego"

                        else ->
                            "Han pasado $diasTranscurridos días desde el último riego"
                    }

                val tarea =
                    TaskEntity(
                        plantFirestoreId =
                            planta.firestoreId,

                        plantName =
                            planta.nombreComun,

                        title =
                            "🌱 Regar ${planta.nombreComun}",

                        description =
                            textoDias,

                        createdAt =
                            ahora,

                        completed =
                            false
                    )

                taskRepository
                    .saveTask(tarea)
                    .getOrThrow()
            }

            taskRepository.refreshTasks()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}