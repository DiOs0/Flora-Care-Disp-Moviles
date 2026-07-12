package com.uce.floracare.domain.usecase

import com.uce.floracare.domain.model.TaskEntity
import com.uce.floracare.repositories.TaskRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ObtenerTareasPendientesUseCase(
    private val repository: TaskRepository,
    private val authManager: AuthManager
) {
    operator fun invoke(): Flow<List<TaskEntity>> {
        val userId = authManager.getCurrentUserId() ?: return flowOf(emptyList())
        return repository.getTasksStream(userId)
    }
}
