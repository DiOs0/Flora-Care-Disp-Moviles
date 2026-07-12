package com.uce.floracare.repositories

import com.uce.floracare.repositories.connections.remote.cloudinary.CloudinaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * Implementación de [ImageRepository] utilizando Cloudinary.
 */
class ImageRepositoryImpl(
    private val cloudinaryService: CloudinaryService = CloudinaryService
) : ImageRepository {

    override suspend fun subirImagen(file: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { continuation ->
                cloudinaryService.subirImagenFirmada(file) { success, result ->
                    if (success) {
                        continuation.resume(Result.success(result))
                    } else {
                        continuation.resume(Result.failure(Exception(result)))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
