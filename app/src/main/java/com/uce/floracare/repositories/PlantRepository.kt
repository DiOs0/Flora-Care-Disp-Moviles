package com.uce.floracare.repositories

import android.net.Uri
import android.util.Log
import com.uce.floracare.data.remote.dto.Caracteristicas
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.data.remote.dto.Riego
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager

class PlantRepository(
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager
) {

    /**
     * Guarda una nueva planta realizando el guardado local de la imagen (workaround para Spark plan)
     * y el guardado de datos en Firestore.
     */
    suspend fun saveNewPlant(plant: PlantEntity, photoUri: Uri): Result<Unit> {
        return try {
            // 1. Guardar imagen LOCALMENTE (ya que Storage está bloqueado por el plan Spark)
            val imagenResult = storageManager.saveImageLocally(photoUri)

            val localPath = imagenResult.getOrElse {
                error -> return Result.failure(error)
            }

            val platWithImage = plant.copy(imagen = localPath)
            // Crea una propia coleccion para el usuario con el ID del usuario actual
            firestoreManager.uploadUserPlant(platWithImage)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error en saveNewPlant", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene la lista de plantas creadas por el usuario desde Firestore.
     */
    suspend fun getMyPlants(): Result<List<PlantEntity>> {
        return firestoreManager.getUserPlants()
    }
}