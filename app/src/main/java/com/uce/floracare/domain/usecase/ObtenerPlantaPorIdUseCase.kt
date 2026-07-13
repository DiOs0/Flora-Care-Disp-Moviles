package com.uce.floracare.domain.usecase


import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.PlantRepository


class ObtenerPlantaPorIdUseCase(
    private val repository: PlantRepository
) {


    suspend operator fun invoke(
        firestoreId:String
    ): Result<PlantEntity>{

        return repository.getPlantByFirestoreId(
            firestoreId
        )

    }

}