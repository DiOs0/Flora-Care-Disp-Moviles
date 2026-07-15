package com.uce.floracare.repositories

import android.net.Uri
import android.util.Log
import com.uce.floracare.data.local.dao.PlantDao
import com.uce.floracare.data.local.entity.PlantEntity as LocalPlantEntity
import com.uce.floracare.data.local.entity.toRemoteEntity
import com.uce.floracare.data.remote.dto.PlantEntity as RemotePlantEntity
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PlantRepository(
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager,
    private val authManager: AuthManager,
    private val plantDao: PlantDao,
    private val taskRepository: TaskRepository
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    suspend fun saveNewPlant(plant: RemotePlantEntity, photoUri: Uri): Result<Unit> {
        return try {
            val imagenResult = storageManager.saveImageLocally(photoUri)
            val localPath = imagenResult.getOrElse { error ->
                return Result.failure(error)
            }
            val plantWithImage = plant.copy(imagen = localPath)
            firestoreManager.uploadUserPlant(plantWithImage)
            // Sincronizar localmente después de subir
            refreshGardenPlants()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error en saveNewPlant", e)
            Result.failure(e)
        }
    }

    suspend fun savePlantDirectly(plant: RemotePlantEntity): Result<Unit> {
        return try {
            firestoreManager.uploadUserPlant(plant)
            // Sincronizar localmente
            refreshGardenPlants()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error en savePlantDirectly", e)
            Result.failure(e)
        }
    }

    // SSOT: La UI observa este Flow (Devolvemos RemotePlantEntity para compatibilidad con Adapters)
    fun getGardenPlantsStream(userId: String): Flow<List<RemotePlantEntity>> {
        // Disparamos la actualización en background
        repositoryScope.launch {
            refreshGardenPlants()
        }
        return plantDao.getGardenPlants(userId).map { list ->
            list.map { it.toRemoteEntity() }
        }
    }

    suspend fun refreshGardenPlants() {
        val userId = authManager.getCurrentUserId() ?: return
        val remoteResult = firestoreManager.getUserPlants()
        remoteResult.onSuccess { remotePlants ->
            val localEntities = remotePlants.map { it.toLocalEntity(userId) }
            plantDao.deletePlantsByUserId(userId)
            plantDao.insertPlants(localEntities)
        }
    }

    // SSOT para Catálogo
    fun getCatalogPlantsStream(): Flow<List<RemotePlantEntity>> {
        repositoryScope.launch {
            refreshCatalog()
        }
        return plantDao.getAllCatalogPlants().map { list ->
            list.map { it.toRemoteEntity() }
        }
    }

    suspend fun refreshCatalog(): Result<Unit> {
        val remoteResult = firestoreManager.getPlants(limit = 20)
        return remoteResult.fold(
            onSuccess = { remotePlants ->
                val localEntities = remotePlants.map { it.toLocalEntity("catalog") }
                plantDao.deletePlantsByUserId("catalog")
                plantDao.insertPlants(localEntities)
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun getPlantsForExplore(indoor: Boolean? = null): Result<List<RemotePlantEntity>> {
        return when (indoor) {
            true -> firestoreManager.getPlantsByIndoor(true, limit = 10)
            false -> firestoreManager.getPlantsByIndoor(false, limit = 10)
            null -> firestoreManager.getPlants(limit = 10)
        }
    }

    // Extension function para mapeo
    private fun RemotePlantEntity.toLocalEntity(
        userId: String
    ): LocalPlantEntity {

        return LocalPlantEntity(

            id =
                this.firestoreId.ifEmpty {
                    this.id.toString()
                },

            nombreComun =
                this.nombreComun,

            nombreCientifico =
                this.nombreCientifico,

            imagen =
                this.imagen,

            tipo =
                this.tipo,

            descripcion =
                this.descripcion,

            cicloVida =
                this.cicloVida,

            nivelCuidado =
                this.nivelCuidado,

            medicinal =
                this.caracteristicas.medicinal,

            esInterior =
                this.caracteristicas.indoor,

            tropical =
                this.caracteristicas.tropical,

            resistenteSequia =
                this.caracteristicas.resistenteSequia,

            toxicaHumanos =
                this.caracteristicas.toxicaHumanos,

            toxicaMascotas =
                this.caracteristicas.toxicaMascotas,

            frecuenciaRiego =
                this.riego.frecuencia,

            cadaValorRiego =
                this.riego.cadaValor,

            luzSolar =
                this.luzSolar.joinToString("|"),

            temperaturaMin =
                this.temperatura.min,

            temperaturaMax =
                this.temperatura.max,

            temperaturaDescripcion =
                this.temperatura.descripcion,

            userId =
                userId,

            wateringFrequencyDays =
                this.wateringFrequencyDays,

            lastWateredDate =
                this.ultimoRiego,

            nextWateringTimestamp =
                this.nextWateringTimestamp
        )
    }

    suspend fun getMyPlants(): Result<List<RemotePlantEntity>> {
        return firestoreManager.getUserPlants()
    }

    suspend fun getGardenPlantsFromLocal(): Result<List<RemotePlantEntity>> {
        return try {
            val userId = authManager.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))
            val localPlants = plantDao.getGardenPlantsList(userId)
            Result.success(localPlants.map { it.toRemoteEntity() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlantById(plantId: Int): Result<RemotePlantEntity> {
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

    suspend fun updatePlant(plant: RemotePlantEntity): Result<Unit> {
        return try {
            val userId = authManager.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // 1. Actualizar localmente para feedback inmediato
            val localPlant = plant.toLocalEntity(userId)
            plantDao.insertPlants(listOf(localPlant))

            // 2. Sincronizar con Firestore
            firestoreManager.updateUserPlant(plant).getOrThrow()

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("PlantRepository", "Error actualizando planta", e)
            Result.failure(e)
        }
    }

    suspend fun deletePlant(firestoreId: String): Result<Unit> {

        return try {

            // 1. Eliminar tareas pendientes asociadas
            taskRepository.deleteTasksByPlantId(firestoreId)


            // 2. Eliminar planta de Firestore
            firestoreManager.deleteUserPlant(firestoreId)


            // 3. Eliminar planta local si existe
            plantDao.deletePlantById(firestoreId)


            Result.success(Unit)


        } catch(e: Exception){

            Result.failure(e)

        }
    }

    suspend fun updateWatering(
        plantId: String,
        lastWatered: Long,
        nextWatering: Long
    ): Result<Unit> {

        return try {

            // Obtener la planta desde Room
            val localPlant = plantDao.getPlantById(plantId)
                ?: return Result.failure(Exception("Planta no encontrada"))

            // Actualizar datos
            val updatedLocal = localPlant.copy(
                lastWateredDate = lastWatered,
                nextWateringTimestamp = nextWatering
            )

            // Guardar en Room
            plantDao.insertPlants(listOf(updatedLocal))

            // Sincronizar con Firestore
            val remotePlant = updatedLocal.toRemoteEntity()

            firestoreManager.updateUserPlant(remotePlant).getOrThrow()

            Result.success(Unit)

        } catch (e: Exception) {

            Log.e("PlantRepository", "Error actualizando riego", e)

            Result.failure(e)
        }
    }


    suspend fun getPlantByFirestoreId(
        firestoreId: String
    ): Result<RemotePlantEntity> {
        val result = firestoreManager.getUserPlants()
        return result.fold(
            onSuccess = { list ->
                val plant = list.find { it.firestoreId == firestoreId }
                if (plant != null) {
                    Result.success(plant)
                } else {
                    Result.failure(Exception("Planta no encontrada"))
                }
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }
}
