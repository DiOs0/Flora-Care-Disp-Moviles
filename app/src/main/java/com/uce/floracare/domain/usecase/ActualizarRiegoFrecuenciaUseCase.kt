package com.uce.floracare.domain.usecase

import com.uce.floracare.repositories.PlantRepository

class ActualizarRiegoFrecuenciaUseCase(private val repository: PlantRepository) {
    suspend operator fun invoke(plantId: String, frequency: Int): Result<Unit> {
        return try {
            val plantResult = repository.getPlantByFirestoreId(plantId)
            plantResult.fold(
                onSuccess = { plant ->
                    val updatedPlant = plant.copy(wateringFrequencyDays = frequency)
                    repository.updatePlant(updatedPlant)
                    // Después de actualizar en Firestore, refrescamos para el SSOT de Room
                    repository.refreshGardenPlants()
                    Result.success(Unit)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
