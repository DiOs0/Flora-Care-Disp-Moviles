package com.uce.floracare.domain.usecase

import android.net.Uri
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.connections.remote.cloudinary.CloudinaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * Caso de Uso para registrar una nueva planta en el jardín del usuario.
 * Orquesta: Guardado local (latencia cero), subida a Cloudinary y persistencia en Firestore.
 */
class RegistrarPlantaEnJardinUseCase(
    private val repository: PlantRepository
) {
    suspend operator fun invoke(
        plant: PlantEntity
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Persistencia (El Repositorio maneja Firestore y sincronización local)
            // Según la implementación actual de PlantRepository.saveNewPlant:
            // - Sube a Firestore (via firestoreManager)
            // - Llama a refreshGardenPlants() para actualizar Room
            repository.saveNewPlant(plant, Uri.parse(plant.imagen))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
