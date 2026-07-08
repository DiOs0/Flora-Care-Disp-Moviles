package com.uce.floracare.domain.usecase

import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.TaskRepository

class CompletarTareaPendienteUseCase(

    private val plantRepository: PlantRepository,
    private val taskRepository: TaskRepository

) {

    suspend operator fun invoke(

        taskFirestoreId: String,

        plantFirestoreId: String

    ): Result<Unit> {

        // Obtener la planta

        val plantResult =
            plantRepository.getPlantByFirestoreId(
                plantFirestoreId
            )

        if (plantResult.isFailure) {
            return Result.failure(
                plantResult.exceptionOrNull()!!
            )
        }

        val plant =
            plantResult.getOrThrow()

        // Reiniciar contador

        plant.ultimoRiego =
            System.currentTimeMillis()

        // Actualizar planta

        val updateResult =
            plantRepository.updatePlant(plant)

        if (updateResult.isFailure) {
            return updateResult
        }

        // Eliminar tarea

        return taskRepository.deleteTask(
            taskFirestoreId
        )

    }

}