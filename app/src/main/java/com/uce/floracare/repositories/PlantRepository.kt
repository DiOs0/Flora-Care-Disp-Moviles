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

    /**
     * Guarda nueva planta
     */
    suspend fun saveNewPlant(
        plant: PlantEntity,
        photoUri: Uri
    ): Result<Unit> {

        return try {

            val imagenResult =
                storageManager
                    .saveImageLocally(
                        photoUri
                    )

            val localPath =
                imagenResult.getOrElse {

                        error ->

                    return Result.failure(
                        error
                    )

                }

            val plantWithImage =
                plant.copy(
                    imagen = localPath
                )

            firestoreManager
                .uploadUserPlant(
                    plantWithImage
                )

            Result.success(Unit)

        } catch (e: Exception) {

            Log.e(
                "PlantRepository",
                "Error saveNewPlant",
                e
            )

            Result.failure(e)

        }

    }


    /**
     * Obtener todas las plantas del usuario
     */
    suspend fun getMyPlants():
            Result<List<PlantEntity>> {

        return firestoreManager
            .getUserPlants()

    }


    /**
     * Obtener detalle por ID
     */

    suspend fun getPlantById(
        plantId:Int
    ):Result<PlantEntity>{

        return try{

            val result =
                firestoreManager
                    .getUserPlants()

            result.fold(

                onSuccess = { list ->

                    val plant =
                        list.find {

                            it.id==plantId
                        }

                    if(plant!=null){

                        Result.success(
                            plant
                        )

                    }else{

                        Result.failure(
                            Exception(
                                "Planta no encontrada"
                            )
                        )

                    }

                },

                onFailure = {

                    Result.failure(it)

                }

            )

        }catch(e:Exception){

            Result.failure(e)

        }

    }


    /**
     * Actualizar planta
     */

    suspend fun updatePlant(
        plant: PlantEntity
    ): Result<Unit> {

        return firestoreManager
            .updateUserPlant(
                plant
            )

    }

    /**
     * Eliminar planta
     */

    suspend fun deletePlant(
        firestoreId:String
    ):Result<Unit>{

        return firestoreManager
            .deleteUserPlant(
                firestoreId
            )

    }
}