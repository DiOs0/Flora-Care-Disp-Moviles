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

        val plantsResult = plantRepository.getMyPlants()

        if (plantsResult.isFailure) {
            return Result.failure(plantsResult.exceptionOrNull()!!)
        }

        val tasksResult = taskRepository.getPendingTasks()

        if (tasksResult.isFailure) {
            return Result.failure(tasksResult.exceptionOrNull()!!)
        }

        val plantas = plantsResult.getOrThrow()

        val tareasActuales = tasksResult.getOrThrow()

        val hoy = System.currentTimeMillis()

        plantas.forEach { planta ->

            val diasRiego =
                planta.riego.cadaValor.toLongOrNull()
                    ?: return@forEach

            val diasTranscurridos =
                TimeUnit.MILLISECONDS.toDays(
                    hoy - planta.ultimoRiego
                )

            if (diasTranscurridos >= diasRiego) {

                val yaExiste = tareasActuales.any {

                    it.plantFirestoreId == planta.firestoreId &&
                            !it.completed

                }

                if (!yaExiste) {

                    val tarea = TaskEntity(

                        plantFirestoreId = planta.firestoreId,

                        plantName = planta.nombreComun,

                        title = "Regar ${planta.nombreComun}",

                        description =
                            "Han pasado $diasTranscurridos días desde el último riego. Es momento de regar esta planta.",

                        createdAt = hoy,

                        completed = false

                    )

                    val resultado = taskRepository.saveTask(tarea)

                    if (resultado.isFailure) {
                        return Result.failure(resultado.exceptionOrNull()!!)
                    }

                }

            }

        }

        return Result.success(Unit)

    }

}