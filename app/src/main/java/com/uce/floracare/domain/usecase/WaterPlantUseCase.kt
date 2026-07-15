package com.uce.floracare.domain.usecase

import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.data.scheduler.WateringScheduler
import com.uce.floracare.repositories.PlantRepository

class WaterPlantUseCase(

    private val plantRepository:
    PlantRepository,

    private val wateringScheduler:
    WateringScheduler

) {

    suspend operator fun invoke(
        plant: PlantEntity
    ): Result<Unit> {

        return try {

            val now =
                System.currentTimeMillis()

//            val nextWatering =
//                now + (plant.wateringFrequencyDays.toLong() * 24L * 60L * 60L * 1000L)

            // Solo para pruebas:
            // próxima notificación en 10 segundos.
            val nextWatering =
                now + 10_000L

            val updatedPlant =
                plant.copy(
                    ultimoRiego = now,

                    ultimaActualizacion =
                        now,

                    nextWateringTimestamp =
                        nextWatering
                )

            // Intentar actualizar la planta
            val updateResult = plantRepository.updatePlant(updatedPlant)

            // Programar la alarma SIEMPRE que se haya actualizado localmente (o intentado actualizar)
            // para asegurar que el recordatorio se programe.
            wateringScheduler.cancel(
                plant
            )

            wateringScheduler.schedule(
                updatedPlant
            )

            updateResult.getOrThrow()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}