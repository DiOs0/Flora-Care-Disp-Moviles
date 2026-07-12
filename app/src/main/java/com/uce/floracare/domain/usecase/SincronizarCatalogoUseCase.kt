package com.uce.floracare.domain.usecase

import com.uce.floracare.repositories.PlantRepository

/**
 * Caso de Uso para disparar la sincronización del catálogo desde la nube a la DB local.
 */
class SincronizarCatalogoUseCase(
    private val repository: PlantRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshCatalog()
    }
}
