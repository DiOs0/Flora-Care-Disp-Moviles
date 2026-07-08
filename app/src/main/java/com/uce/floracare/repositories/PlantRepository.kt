package com.uce.floracare.repositories

import android.net.Uri
import android.util.Log
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager

class PlantRepository(
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager
) {

    suspend fun saveNewPlant(plant: PlantEntity, photoUri: Uri): Result<Unit> {
        return try {
            val imagenResult = storageManager.saveImageLocally(photoUri)
            val localPath = imagenResult.getOrElse { error ->
                return Result.failure(error)
            }
            val plantWithImage = plant.copy(imagen = localPath)
            firestoreManager.uploadUserPlant(plantWithImage)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error en saveNewPlant", e)
            Result.failure(e)
        }
    }

    suspend fun savePlantDirectly(plant: PlantEntity): Result<Unit> {
        return try {
            firestoreManager.uploadUserPlant(plant)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error en savePlantDirectly", e)
            Result.failure(e)
        }
    }

    suspend fun getMyPlants(): Result<List<PlantEntity>> {
        return firestoreManager.getUserPlants()
    }

    suspend fun getPlantById(plantId: Int): Result<PlantEntity> {
        return try {
            val result = firestoreManager.getUserPlants()
            result.fold(
                onSuccess = { list ->
                    val plant = list.find { it.id == plantId }
                    if (plant != null) Result.success(plant)
                    else Result.failure(Exception("Planta no encontrada"))
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePlant(plant: PlantEntity): Result<Unit> {
        return firestoreManager.updateUserPlant(plant)
    }

    suspend fun deletePlant(firestoreId: String): Result<Unit> {
        return firestoreManager.deleteUserPlant(firestoreId)
    }


    suspend fun getPlantByFirestoreId(

        firestoreId: String

    ): Result<PlantEntity> {

        val result =
            firestoreManager.getUserPlants()

        return result.fold(

            onSuccess = { list ->

                val plant =
                    list.find {

                        it.firestoreId == firestoreId

                    }

                if (plant != null) {

                    Result.success(plant)

                } else {

                    Result.failure(
                        Exception("Planta no encontrada")
                    )

                }

            },

            onFailure = {

                Result.failure(it)

            }

        )

    }


}
