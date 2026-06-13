package com.uce.floracare.api_ingreso.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()
    private val plantsRef = db.collection("plants")

    suspend fun uploadPlant(plant: PlantEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            plantsRef.document(plant.id.toString()).set(plant).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPlants(
        plants: List<PlantEntity>,
        onProgress: (Int) -> Unit = {}
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var count = 0
            for (plant in plants) {
                plantsRef.document(plant.id.toString()).set(plant).await()
                count++
                onProgress(count)
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlants(limit: Int = 0): Result<List<PlantEntity>> =
        withContext(Dispatchers.IO) {
            try {
                val query = if (limit > 0) plantsRef.limit(limit.toLong()) else plantsRef
                val snapshot = query.get().await()
                val plants = snapshot.documents.mapNotNull {
                    it.toObject(PlantEntity::class.java)
                }
                Result.success(plants)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getPlantsByCategory(category: String, limit: Int = 10): Result<List<PlantEntity>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = plantsRef
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
                val snapshot = plantsRef
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

    suspend fun getExistingIds(): Set<Int> = withContext(Dispatchers.IO) {
        try {
            val snapshot = plantsRef.get().await()
            snapshot.documents.mapNotNull { it.id.toIntOrNull() }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}
