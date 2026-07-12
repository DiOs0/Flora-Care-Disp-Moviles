package com.uce.floracare.domain.usecase

import com.uce.floracare.repositories.UserRepository

/**
 * Caso de Uso para cerrar la sesión del usuario.
 */
class CerrarSesionUseCase(
    private val repository: UserRepository
) {
    operator fun invoke() {
        repository.logout()
    }
}
