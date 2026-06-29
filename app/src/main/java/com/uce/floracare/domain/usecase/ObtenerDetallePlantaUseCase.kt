package com.uce.floracare.domain.usecase

import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.PlantRepository

class ObtenerDetallePlantaUseCase(

    private val repository:PlantRepository

){

    suspend operator fun invoke(
        plantId:Int
    ):Result<PlantEntity>{

        return repository
            .getPlantById(
                plantId
            )

    }

}