package com.uce.floracare.domain.usecase

import com.uce.floracare.repositories.TaskRepository

class CompletarTareaPendienteUseCase(

    private val taskRepository:
    TaskRepository

) {

    suspend operator fun invoke(
        taskFirestoreId: String
    ): Result<Unit> {

        if (taskFirestoreId.isBlank()) {

            return Result.failure(
                Exception(
                    "El identificador de la tarea está vacío"
                )
            )
        }

        return taskRepository.deleteTask(
            taskFirestoreId
        )
    }
}