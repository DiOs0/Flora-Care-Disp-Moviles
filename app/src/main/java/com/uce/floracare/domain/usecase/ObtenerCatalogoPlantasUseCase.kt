package com.uce.floracare.domain.usecase

import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.PlantRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de Uso para obtener el flujo reactivo del catálogo de plantas desde la DB local (SSOT).
 */
class ObtenerCatalogoPlantasUseCase(
    private val repository: PlantRepository
) {
    operator fun invoke(): Flow<List<PlantEntity>> {
        return repository.getCatalogPlantsStream()
    }
}
