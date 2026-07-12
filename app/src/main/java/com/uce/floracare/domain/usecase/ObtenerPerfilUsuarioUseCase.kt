package com.uce.floracare.domain.usecase

import com.uce.floracare.domain.model.UserProfile
import com.uce.floracare.repositories.UserRepository

/**
 * Caso de Uso para obtener los datos del perfil del usuario actual.
 */
class ObtenerPerfilUsuarioUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<UserProfile?> {
        return repository.getCurrentUserProfile()
    }
}
