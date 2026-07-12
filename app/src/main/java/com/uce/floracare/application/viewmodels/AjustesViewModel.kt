package com.uce.floracare.application.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.domain.model.EstadisticasJardin
import com.uce.floracare.domain.model.UserProfile
import com.uce.floracare.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

sealed class AjustesUiState {
    object Idle : AjustesUiState()
    object Loading : AjustesUiState()
    data class Success(
        val profile: UserProfile,
        val estadisticas: EstadisticasJardin = EstadisticasJardin()
    ) : AjustesUiState()
    data class Error(val message: String) : AjustesUiState()
    object Logout : AjustesUiState()
}

/**
 * ViewModel de Ajustes.
 * Desacoplado de UserRepository y CloudinaryService mediante Use Cases.
 */
class AjustesViewModel(
    private val obtenerPerfilUsuarioUseCase: ObtenerPerfilUsuarioUseCase,
    private val actualizarPerfilUsuarioUseCase: ActualizarPerfilUsuarioUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
    private val subirImagenUseCase: SubirImagenUseCase,
    private val obtenerEstadisticasJardinUseCase: ObtenerEstadisticasJardinUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AjustesUiState>(AjustesUiState.Idle)
    val uiState: StateFlow<AjustesUiState> = _uiState.asStateFlow()

    // Flow reactivo de estadísticas
    val estadisticas: StateFlow<EstadisticasJardin> = obtenerEstadisticasJardinUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EstadisticasJardin()
        )

    fun loadUserProfile() {
        _uiState.value = AjustesUiState.Loading
        viewModelScope.launch {
            obtenerPerfilUsuarioUseCase().fold(
                onSuccess = { profile ->
                    if (profile != null) {
                        _uiState.value = AjustesUiState.Success(profile)
                    } else {
                        _uiState.value = AjustesUiState.Error("Perfil no encontrado")
                    }
                },
                onFailure = { e ->
                    _uiState.value = AjustesUiState.Error(e.localizedMessage ?: "Error al cargar perfil")
                }
            )
        }
    }

    fun updateProfile(newName: String, imageFile: File?) {
        if (newName.isBlank()) {
            _uiState.value = AjustesUiState.Error("El nombre no puede estar vacío")
            return
        }

        val currentProfile = (uiState.value as? AjustesUiState.Success)?.profile
        
        _uiState.value = AjustesUiState.Loading
        viewModelScope.launch {
            var photoUrl = currentProfile?.photoUrl

            if (imageFile != null) {
                val uploadResult = subirImagenUseCase(imageFile)
                uploadResult.fold(
                    onSuccess = { url ->
                        photoUrl = url
                    },
                    onFailure = { error ->
                        _uiState.value = AjustesUiState.Error("Error al subir imagen: ${error.localizedMessage}")
                        return@launch
                    }
                )
            }

            actualizarPerfilUsuarioUseCase(
                newName = newName,
                photoUrl = photoUrl
            ).fold(
                onSuccess = {
                    loadUserProfile() // Recargar para mostrar cambios
                },
                onFailure = { e ->
                    _uiState.value = AjustesUiState.Error(e.localizedMessage ?: "Error al actualizar perfil")
                }
            )
        }
    }

    fun logout() {
        cerrarSesionUseCase()
        _uiState.value = AjustesUiState.Logout
    }
}
