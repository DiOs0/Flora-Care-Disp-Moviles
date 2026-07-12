package com.uce.floracare.domain.usecase

import com.uce.floracare.repositories.ImageRepository
import java.io.File

/**
 * Caso de Uso para subir una imagen.
 * Encapsula la lógica de negocio para la carga de archivos multimedia.
 */
class SubirImagenUseCase(
    private val repository: ImageRepository
) {
    /**
     * Ejecuta la subida de la imagen.
     * @param imageFile El archivo de imagen proveniente de la UI (Cámara o Galería).
     * @return Result con la URL pública de la imagen.
     */
    suspend operator fun invoke(imageFile: File): Result<String> {
        return repository.subirImagen(imageFile)
    }
}
