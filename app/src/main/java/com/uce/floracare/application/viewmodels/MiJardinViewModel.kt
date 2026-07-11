package com.uce.floracare.application.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.domain.model.TaskEntity
import com.uce.floracare.domain.usecase.CompletarTareaPendienteUseCase
import com.uce.floracare.domain.usecase.GenerarTareasPendientesUseCase
import com.uce.floracare.domain.usecase.ObtenerTareasPendientesUseCase
import com.uce.floracare.repositories.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MiJardinUiState {
    object Idle : MiJardinUiState()
    object Loading : MiJardinUiState()
    data class Success(val plants: List<PlantEntity>, val tasks: List<TaskEntity>) : MiJardinUiState()
    data class Error(val message: String) : MiJardinUiState()
}

class MiJardinViewModel(
    private val plantRepository: PlantRepository,
    private val generarTareasPendientesUseCase: GenerarTareasPendientesUseCase,
    private val obtenerTareasPendientesUseCase: ObtenerTareasPendientesUseCase,
    private val completarTareaPendienteUseCase: CompletarTareaPendienteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MiJardinUiState>(MiJardinUiState.Idle)
    val uiState: StateFlow<MiJardinUiState> = _uiState.asStateFlow()

    private var currentPlants: List<PlantEntity> = emptyList()
    private var currentTasks: List<TaskEntity> = emptyList()

    fun fetchData() {
        _uiState.value = MiJardinUiState.Loading
        viewModelScope.launch {
            try {
                // 1. Cargar Plantas
                val plantsResult = plantRepository.getMyPlants()
                plantsResult.onSuccess { plants ->
                    currentPlants = plants
                    // 2. Generar/Cargar Tareas
                    generarTareas()
                }.onFailure { e ->
                    _uiState.value = MiJardinUiState.Error(e.localizedMessage ?: "Error al cargar plantas")
                }
            } catch (e: Exception) {
                _uiState.value = MiJardinUiState.Error(e.localizedMessage ?: "Error inesperado")
            }
        }
    }

    private fun generarTareas() {
        viewModelScope.launch {
            val result = generarTareasPendientesUseCase()
            result.onSuccess {
                cargarTareas()
            }.onFailure { e ->
                _uiState.value = MiJardinUiState.Error(e.localizedMessage ?: "Error al generar tareas")
            }
        }
    }

    fun cargarTareas() {
        viewModelScope.launch {
            val result = obtenerTareasPendientesUseCase()
            result.onSuccess { tasks ->
                currentTasks = tasks
                _uiState.value = MiJardinUiState.Success(currentPlants, currentTasks)
            }.onFailure { e ->
                _uiState.value = MiJardinUiState.Error(e.localizedMessage ?: "Error al cargar tareas")
            }
        }
    }

    fun completarTarea(task: TaskEntity) {
        viewModelScope.launch {
            _uiState.value = MiJardinUiState.Loading
            val result = completarTareaPendienteUseCase(task.firestoreId, task.plantFirestoreId)
            result.fold(
                onSuccess = {
                    fetchData() // Recargar todo para actualizar estados
                },
                onFailure = { e ->
                    _uiState.value = MiJardinUiState.Error(e.localizedMessage ?: "Error al completar tarea")
                }
            )
        }
    }
}
