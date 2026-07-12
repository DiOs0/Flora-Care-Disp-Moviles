package com.uce.floracare.application.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.domain.model.TaskEntity
import com.uce.floracare.domain.usecase.ActualizarRiegoFrecuenciaUseCase
import com.uce.floracare.domain.usecase.CompletarTareaPendienteUseCase
import com.uce.floracare.domain.usecase.GenerarTareasPendientesUseCase
import com.uce.floracare.domain.usecase.ObtenerPlantasJardinUseCase
import com.uce.floracare.domain.usecase.ObtenerTareasPendientesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MiJardinUiState {
    object Idle : MiJardinUiState()
    object Loading : MiJardinUiState()
    data class Success(val plants: List<PlantEntity>, val tasks: List<TaskEntity>) : MiJardinUiState()
    data class Error(val message: String) : MiJardinUiState()
    object UpdateSuccess : MiJardinUiState()
}

/**
 * ViewModel para Mi Jardín.
 * Desacoplado de Repositorios mediante Use Cases.
 */
class MiJardinViewModel(
    private val obtenerPlantasJardinUseCase: ObtenerPlantasJardinUseCase,
    private val generarTareasPendientesUseCase: GenerarTareasPendientesUseCase,
    private val obtenerTareasPendientesUseCase: ObtenerTareasPendientesUseCase,
    private val completarTareaPendienteUseCase: CompletarTareaPendienteUseCase,
    private val actualizarRiegoFrecuenciaUseCase: ActualizarRiegoFrecuenciaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MiJardinUiState>(MiJardinUiState.Idle)
    val uiState: StateFlow<MiJardinUiState> = _uiState.asStateFlow()

    private var currentPlants: List<PlantEntity> = emptyList()
    private var currentTasks: List<TaskEntity> = emptyList()

    init {
        observeData()
    }

    private fun observeData() {
        // Observar Plantas (SSOT)
        viewModelScope.launch {
            obtenerPlantasJardinUseCase().collect { plants ->
                currentPlants = plants
                updateState()
            }
        }
        // Observar Tareas (SSOT) - Nuevo requisito reactivo
        viewModelScope.launch {
            obtenerTareasPendientesUseCase().collect { tasks ->
                currentTasks = tasks
                updateState()
            }
        }
    }

    private fun updateState() {
        // Solo mostramos Success si hay algo que mostrar o ya terminamos de cargar
        _uiState.value = MiJardinUiState.Success(currentPlants, currentTasks)
    }

    fun fetchData() {
        // fetchData ahora solo dispara la generación de tareas si es necesario, 
        // la UI se actualizará sola gracias a los flows.
        generarTareas()
    }

    private fun generarTareas() {
        viewModelScope.launch {
            _uiState.value = MiJardinUiState.Loading
            val result = generarTareasPendientesUseCase()
            result.onFailure { e ->
                _uiState.value = MiJardinUiState.Error(e.localizedMessage ?: "Error al generar tareas")
            }
            // No necesitamos llamar a cargarTareas() porque el Flow de Room lo hará por nosotros
        }
    }

    fun completarTarea(task: TaskEntity) {
        viewModelScope.launch {
            _uiState.value = MiJardinUiState.Loading
            val result = completarTareaPendienteUseCase(task.firestoreId, task.plantFirestoreId)
            result.onFailure { e ->
                _uiState.value = MiJardinUiState.Error(e.localizedMessage ?: "Error al completar tarea")
            }
            // El Flow se encargará de actualizar la lista tras el borrado en Firebase/Local
        }
    }

    fun actualizarFrecuenciaRiego(plantId: String, frequency: Int) {
        viewModelScope.launch {
            _uiState.value = MiJardinUiState.Loading
            val result = actualizarRiegoFrecuenciaUseCase(plantId, frequency)
            result.onSuccess {
                _uiState.value = MiJardinUiState.UpdateSuccess
                // Restauramos el estado a Success para que la UI siga mostrando la lista
                updateState()
            }.onFailure { e ->
                _uiState.value = MiJardinUiState.Error(e.localizedMessage ?: "Error al actualizar frecuencia")
            }
        }
    }
}
