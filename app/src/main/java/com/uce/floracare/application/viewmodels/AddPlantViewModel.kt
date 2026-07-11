package com.uce.floracare.application.viewmodels

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.PlantRepository
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

sealed class AddPlantUiState {
    object Idle : AddPlantUiState()
    object Loading : AddPlantUiState()
    object Success : AddPlantUiState()
    data class Error(val message: String) : AddPlantUiState()
}

class AddPlantViewModel(private val repository: PlantRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AddPlantUiState>(AddPlantUiState.Idle)
    val uiState: StateFlow<AddPlantUiState> = _uiState.asStateFlow()

    // Datos temporales del formulario
    var selectedPhotoUri: Uri? = null
    var selectedLocation: String = "Interior"

    /**
     * Valida y guarda la planta. Si hay una foto nueva, la sube a Cloudinary primero.
     */
    fun savePlant(plant: PlantEntity, fromExplore: Boolean = false, photoFile: File? = null) {
        if (!validate(plant, fromExplore)) return

        viewModelScope.launch {
            _uiState.value = AddPlantUiState.Loading

            try {
                val finalImageUrl = if (photoFile != null) {
                    subirACloudinary(photoFile) ?: throw Exception("Error al subir imagen a Cloudinary")
                } else {
                    plant.imagen
                }

                val plantToSave = plant.copy(imagen = finalImageUrl)
                
                val result = if (fromExplore && photoFile == null) {
                    repository.savePlantDirectly(plantToSave)
                } else {
                    // El Repositorio maneja el guardado en Firestore y opcionalmente local
                    repository.saveNewPlant(plantToSave, plantToSave.imagen.toUri())
                }

                result.fold(
                    onSuccess = { 
                        _uiState.value = AddPlantUiState.Success 
                    },
                    onFailure = { error -> 
                        _uiState.value = AddPlantUiState.Error(error.localizedMessage ?: "Error al guardar") 
                    }
                )
            } catch (e: Exception) {
                _uiState.value = AddPlantUiState.Error(e.localizedMessage ?: "Error inesperado")
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

    private fun validate(plant: PlantEntity, fromExplore: Boolean): Boolean {
        val error = when {
            plant.nombreComun.isBlank() -> "El nombre común es obligatorio"
            plant.nombreCientifico.isBlank() -> "El nombre científico es obligatorio"
            plant.tipo.isBlank() -> "Debes especificar el tipo de planta"
            plant.descripcion.isBlank() -> "La descripción no puede estar vacía"
            plant.cicloVida.isBlank() -> "Debes indicar el ciclo de vida"
            plant.nivelCuidado.isBlank() -> "Indica el nivel de cuidado (Ej: Bajo, Medio, Alto)"
            !fromExplore && selectedPhotoUri == null -> "Debes capturar una foto"
            plant.riego.frecuencia.isBlank() -> "La frecuencia de riego es obligatoria"
            plant.luzSolar.isEmpty() -> "Debes seleccionar al menos un tipo de luz solar"
            plant.temperatura.min > plant.temperatura.max -> "La temperatura mínima no puede ser mayor a la máxima"
            else -> null
        }

        if (error != null) {
            _uiState.value = AddPlantUiState.Error(error)
            return false
        }
        return true
    }

    fun resetState() {
        _uiState.value = AddPlantUiState.Idle
    }
}
