package com.uce.floracare.domain.usecase

import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.connections.remote.cloudinary.CloudinaryService

class EliminarPlantaUsuarioUseCase(
    private val repository: PlantRepository
) {
    suspend operator fun invoke(plant: PlantEntity): Result<Unit> {
        // 1. Eliminar de Firestore PRIMERO.
        // Si falla aquí, la planta sigue existiendo y la imagen también. Estado consistente.
        val result = repository.deletePlant(plant.firestoreId)

        if (result.isSuccess) {
            // 2. Intentar eliminar de Cloudinary (Tarea de limpieza)
            val publicId = CloudinaryService.extraerPublicId(plant.imagen)
            if (publicId != null) {
                CloudinaryService.eliminarImagen(publicId)
            }
        }

        return result
    }
}
