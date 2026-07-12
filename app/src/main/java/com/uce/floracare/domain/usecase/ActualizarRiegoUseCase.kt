package com.uce.floracare.domain.usecase

import com.uce.floracare.repositories.PlantRepository

/**
 * Caso de Uso para actualizar la fecha del último riego de una planta.
 * Actualiza el SSOT (Room) y sincroniza con el remoto (Firestore).
 */
class ActualizarRiegoUseCase(private val repository: PlantRepository) {
    suspend operator fun invoke(plantId: String): Result<Unit> {
        val currentTime = System.currentTimeMillis()
        return repository.updateWatering(plantId, currentTime)
    }
}
