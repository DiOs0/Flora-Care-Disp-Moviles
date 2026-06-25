package com.uce.floracare.repositories.connections.remote.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.uce.floracare.data.remote.dto.PlantEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreManager(private val authManager: AuthManager) {

    private val db = FirebaseFirestore.getInstance()
    // Referencia GLOBAL para el catálogo/explorar (Como una API)
    private val globalPlantsRef = db.collection("plants")

    /**
     * Retorna la referencia a la subcolección de plantas del usuario actual.
     * Ruta: users -> {userId} -> my_plants
     */
    private fun getMyPlantsCollection(): CollectionReference {
        val userId = authManager.getCurrentUserId()
            ?: throw IllegalStateException("Usuario no autenticado en Firebase")
        return db.collection("users").document(userId).collection("my_plants")
    }

    /**
     * Guarda una planta en la subcolección privada del usuario.
     */
    suspend fun uploadUserPlant(plant: PlantEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            getMyPlantsCollection().add(plant).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene solo las plantas pertenecientes al usuario actual (Su Jardín).
     */
    suspend fun getUserPlants(): Result<List<PlantEntity>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = getMyPlantsCollection().get().await()
            val plants = snapshot.documents.mapNotNull {
                it.toObject(PlantEntity::class.java)
            }
            Result.success(plants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los IDs de los documentos existentes en la colección del usuario.
     */
    suspend fun getExistingIds(): Set<Int> = withContext(Dispatchers.IO) {
        try {
            val snapshot = getMyPlantsCollection().get().await()
            snapshot.documents.mapNotNull { it.id.toIntOrNull() }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    // --- MÉTODOS PARA EL CATÁLOGO GLOBAL (EXPLORAR) ---

    suspend fun getPlants(limit: Int = 0): Result<List<PlantEntity>> = withContext(Dispatchers.IO) {
        try {
            val query = if (limit > 0) globalPlantsRef.limit(limit.toLong()) else globalPlantsRef
            val snapshot = query.get().await()
            val plants = snapshot.documents.mapNotNull { it.toObject(PlantEntity::class.java) }
            Result.success(plants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlantsByCategory(category: String, limit: Int = 10): Result<List<PlantEntity>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = globalPlantsRef
                    .whereEqualTo("tipo", category)
                    .limit(limit.toLong())
                    .get()
                    .await()
                val plants = snapshot.documents.mapNotNull {
                    it.toObject(PlantEntity::class.java)
                }
                Result.success(plants)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getPlantsByIndoor(indoor: Boolean, limit: Int = 10): Result<List<PlantEntity>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = globalPlantsRef
                    .whereEqualTo("caracteristicas.indoor", indoor)
                    .limit(limit.toLong())
                    .get()
                    .await()
                val plants = snapshot.documents.mapNotNull {
                    it.toObject(PlantEntity::class.java)
                }
                Result.success(plants)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}