package com.uce.floracare.domain.usecase

import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Caso de Uso para obtener el flujo reactivo de las plantas del jardín del usuario actual (SSOT).
 */
class ObtenerPlantasJardinUseCase(
    private val repository: PlantRepository,
    private val authManager: AuthManager
) {
    operator fun invoke(): Flow<List<PlantEntity>> {
        val userId = authManager.getCurrentUserId() ?: return flowOf(emptyList())
        return repository.getGardenPlantsStream(userId)
    }
}
