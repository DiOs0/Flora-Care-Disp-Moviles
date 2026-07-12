package com.uce.floracare.repositories

import java.io.File

/**
 * Interfaz para el repositorio de gestión de imágenes.
 */
interface ImageRepository {
    /**
     * Sube una imagen a un servicio externo y retorna la URL pública.
     * @param file El archivo de imagen a subir.
     * @return Result con la URL de la imagen si tiene éxito, o una excepción si falla.
     */
    suspend fun subirImagen(file: File): Result<String>
}
