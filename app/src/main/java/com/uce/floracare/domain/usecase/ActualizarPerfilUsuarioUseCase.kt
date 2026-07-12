package com.uce.floracare.domain.usecase

import com.uce.floracare.repositories.UserRepository
import com.uce.floracare.repositories.connections.remote.cloudinary.CloudinaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * Caso de Uso para actualizar los datos del perfil (nombre y foto).
 */
class ActualizarPerfilUsuarioUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        newName: String,
        photoUrl: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            repository.updateUserProfile(newName, photoUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
