package com.uce.floracare.application.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.domain.usecase.ActualizarPlantaUsuarioUseCase
import com.uce.floracare.domain.usecase.EliminarPlantaUsuarioUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlantDetailUiState {
    object Idle : PlantDetailUiState()
    object Loading : PlantDetailUiState()
    object Success : PlantDetailUiState()
    data class Error(val message: String) : PlantDetailUiState()
}

/**
 * ViewModel para el detalle y edición de plantas.
 * Desacoplado de Repositorios mediante Use Cases.
 */
class PlantDetailViewModel(
    private val eliminarPlantaUsuarioUseCase: EliminarPlantaUsuarioUseCase,
    private val actualizarPlantaUsuarioUseCase: ActualizarPlantaUsuarioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlantDetailUiState>(PlantDetailUiState.Idle)
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()

    fun eliminarPlanta(plant: PlantEntity) {
        _uiState.value = PlantDetailUiState.Loading
        viewModelScope.launch {
            eliminarPlantaUsuarioUseCase(plant).fold(
                onSuccess = { _uiState.value = PlantDetailUiState.Success },
                onFailure = { e -> _uiState.value = PlantDetailUiState.Error(e.localizedMessage ?: "Error al eliminar") }
            )
        }
    }

    fun actualizarPlanta(plant: PlantEntity) {
        _uiState.value = PlantDetailUiState.Loading
        viewModelScope.launch {
            actualizarPlantaUsuarioUseCase(plant).fold(
                onSuccess = { _uiState.value = PlantDetailUiState.Success },
                onFailure = { e -> _uiState.value = PlantDetailUiState.Error(e.localizedMessage ?: "Error al actualizar") }
            )
        }
    }

    fun resetState() {
        _uiState.value = PlantDetailUiState.Idle
    }
}
