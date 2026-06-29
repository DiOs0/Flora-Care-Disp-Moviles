package com.uce.floracare.domain.usecase

import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.PlantRepository

class ActualizarPlantaUsuarioUseCase(

    private val repository:PlantRepository

){

    suspend operator fun invoke(
        plant:PlantEntity
    ):Result<Unit>{

        return repository
            .updatePlant(
                plant
            )

    }

}