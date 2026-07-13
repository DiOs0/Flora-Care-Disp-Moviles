package com.uce.floracare.domain.usecase

import com.uce.floracare.repositories.PlantRepository
import java.util.concurrent.TimeUnit

class ActualizarRiegoUseCase(

    private val repository:
    PlantRepository

) {

    suspend operator fun invoke(
        plantId: String,
        wateringFrequencyDays: Int
    ): Result<Unit> {

        val currentTime =
            System.currentTimeMillis()

        val nextWatering =
            currentTime + TimeUnit.DAYS.toMillis(wateringFrequencyDays.toLong())
        // Solo para pruebas:
//        val nextWatering =
//            currentTime + 10_000L

        return repository.updateWatering(
            plantId = plantId,
            lastWatered = currentTime,
            nextWatering = nextWatering
        )
    }
}