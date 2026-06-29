package com.uce.floracare.domain.usecase

import com.uce.floracare.repositories.PlantRepository

class EliminarPlantaUsuarioUseCase(
    private val repository:PlantRepository
){

    suspend operator fun invoke(
        firestoreId:String
    ):Result<Unit>{

        return repository
            .deletePlant(
                firestoreId
            )

    }

}