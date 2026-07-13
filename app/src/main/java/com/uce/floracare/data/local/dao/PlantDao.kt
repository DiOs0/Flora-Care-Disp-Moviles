package com.uce.floracare.data.local.dao

import androidx.room.*
import com.uce.floracare.data.local.entity.PlantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    // --- CONSULTAS REACTIVAS ---

    // Obtener catálogo general
    @Query("SELECT * FROM plants WHERE userId = 'catalog' ORDER BY nombreComun ASC")
    fun getAllCatalogPlants(): Flow<List<PlantEntity>>

    // Obtener plantas de un usuario específico (Mi Jardín)
    @Query("SELECT * FROM plants WHERE userId = :userId ORDER BY id DESC")
    fun getGardenPlants(userId: String): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlantById(id: String): PlantEntity?

    // --- OPERACIONES DE ESCRITURA ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlants(plants: List<PlantEntity>)

    // Borrado selectivo para refrescar datos sin afectar a otros módulos
    @Query("DELETE FROM plants WHERE userId = :userId")
    suspend fun deletePlantsByUserId(userId: String)

    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    @Query(
        "DELETE FROM plants WHERE id = :plantId"
    )
    suspend fun deletePlantById(
        plantId: String
    )
}