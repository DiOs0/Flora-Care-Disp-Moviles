package com.uce.floracare.application.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.domain.model.UserProfile
import com.uce.floracare.repositories.UserRepository
import com.uce.floracare.repositories.connections.remote.cloudinary.CloudinaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

sealed class AjustesUiState {
    object Idle : AjustesUiState()
    object Loading : AjustesUiState()
    data class Success(val profile: UserProfile) : AjustesUiState()
    data class Error(val message: String) : AjustesUiState()
    object Logout : AjustesUiState()
}

class AjustesViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AjustesUiState>(AjustesUiState.Idle)
    val uiState: StateFlow<AjustesUiState> = _uiState.asStateFlow()

    fun loadUserProfile() {
        _uiState.value = AjustesUiState.Loading
        viewModelScope.launch {
            userRepository.getCurrentUserProfile().fold(
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

        _uiState.value = AjustesUiState.Loading
        viewModelScope.launch {
            try {
                var photoUrl = (uiState.value as? AjustesUiState.Success)?.profile?.photoUrl

                if (imageFile != null) {
                    val cloudinaryUrl = subirACloudinary(imageFile)
                    if (cloudinaryUrl != null) {
                        photoUrl = cloudinaryUrl
                    } else {
                        _uiState.value = AjustesUiState.Error("Error al subir la imagen a Cloudinary")
                        return@launch
                    }
                }

                userRepository.updateUserProfile(newName, photoUrl).fold(
                    onSuccess = {
                        loadUserProfile() // Recargar para mostrar cambios
                    },
                    onFailure = { e ->
                        _uiState.value = AjustesUiState.Error(e.localizedMessage ?: "Error al actualizar perfil")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = AjustesUiState.Error(e.localizedMessage ?: "Error inesperado")
            }
        }
    }

    private suspend fun subirACloudinary(file: File): String? = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            CloudinaryService.subirImagenFirmada(file) { success, result ->
                if (success) continuation.resume(result)
                else continuation.resume(null)
            }
        }
    }

    fun logout() {
        userRepository.logout()
        _uiState.value = AjustesUiState.Logout
    }
}
