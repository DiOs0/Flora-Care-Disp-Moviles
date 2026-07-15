package com.uce.floracare.application.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.domain.model.TaskEntity
import com.uce.floracare.domain.usecase.ActualizarRiegoFrecuenciaUseCase
import com.uce.floracare.domain.usecase.CompletarTareaPendienteUseCase
import com.uce.floracare.domain.usecase.GenerarTareasPendientesUseCase
import com.uce.floracare.domain.usecase.ObtenerPlantaPorIdUseCase
import com.uce.floracare.domain.usecase.ObtenerPlantasJardinUseCase
import com.uce.floracare.domain.usecase.ObtenerTareasPendientesUseCase
import com.uce.floracare.domain.usecase.WaterPlantUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class MiJardinUiState {

    object Idle : MiJardinUiState()

    object Loading : MiJardinUiState()

    data class Success(
        val plants: List<PlantEntity>,
        val tasks: List<TaskEntity>
    ) : MiJardinUiState()

    data class Error(
        val message: String
    ) : MiJardinUiState()

    object UpdateSuccess : MiJardinUiState()
}

class MiJardinViewModel(

    private val obtenerPlantasJardinUseCase:
    ObtenerPlantasJardinUseCase,

    private val generarTareasPendientesUseCase:
    GenerarTareasPendientesUseCase,

    private val obtenerTareasPendientesUseCase:
    ObtenerTareasPendientesUseCase,

    private val completarTareaPendienteUseCase:
    CompletarTareaPendienteUseCase,

    private val actualizarRiegoFrecuenciaUseCase:
    ActualizarRiegoFrecuenciaUseCase,

    private val waterPlantUseCase:
    WaterPlantUseCase,

    private val obtenerPlantaPorIdUseCase:
    ObtenerPlantaPorIdUseCase

) : ViewModel() {

    private val _uiState =
        MutableStateFlow<MiJardinUiState>(
            MiJardinUiState.Idle
        )

    val uiState: StateFlow<MiJardinUiState> =
        _uiState.asStateFlow()

    private var currentPlants:
            List<PlantEntity> = emptyList()

    private var currentTasks:
            List<TaskEntity> = emptyList()

    init {
        observeData()
    }

    private fun observeData() {

        viewModelScope.launch {

            obtenerPlantasJardinUseCase()
                .collect { plants ->

                    currentPlants = plants

                    updateState()
                }
        }

        viewModelScope.launch {

            obtenerTareasPendientesUseCase()
                .collect { tasks ->

                    currentTasks = tasks

                    updateState()
                }
        }
    }

    private fun updateState() {

        _uiState.value =
            MiJardinUiState.Success(
                plants = currentPlants,
                tasks = currentTasks
            )
    }

    fun fetchData() {

        viewModelScope.launch {

            _uiState.value =
                MiJardinUiState.Loading

            cleanInvalidTasks()

            val result =
                generarTareasPendientesUseCase()

            result.onFailure { error ->

                _uiState.value =
                    MiJardinUiState.Error(
                        error.localizedMessage
                            ?: "Error al generar tareas"
                    )
            }

            if (result.isSuccess) {
                updateState()
            }
        }
    }

    fun completarTarea(
        task: TaskEntity
    ) {

        viewModelScope.launch {

            _uiState.value =
                MiJardinUiState.Loading

            val result =
                completarTareaPendienteUseCase(
                    task.firestoreId
                )

            result.onFailure { error ->

                _uiState.value =
                    MiJardinUiState.Error(
                        error.localizedMessage
                            ?: "No se pudo completar la tarea"
                    )
            }

            if (result.isSuccess) {
                updateState()
            }
        }
    }

    fun actualizarFrecuenciaRiego(
        plantId: String,
        frequency: Int
    ) {

        viewModelScope.launch {

            _uiState.value =
                MiJardinUiState.Loading

            val result =
                actualizarRiegoFrecuenciaUseCase(
                    plantId,
                    frequency
                )

            result.onSuccess {

                _uiState.value =
                    MiJardinUiState.UpdateSuccess

                updateState()

            }.onFailure { error ->

                _uiState.value =
                    MiJardinUiState.Error(
                        error.localizedMessage
                            ?: "Error al actualizar frecuencia"
                    )
            }
        }
    }

    fun regarPlanta(
        plant: PlantEntity,
        task: TaskEntity
    ) {

        viewModelScope.launch {

            _uiState.value =
                MiJardinUiState.Loading

            val waterResult =
                waterPlantUseCase(plant)

            if (waterResult.isFailure) {

                _uiState.value =
                    MiJardinUiState.Error(
                        waterResult.exceptionOrNull()
                            ?.localizedMessage
                            ?: "No se pudo registrar el riego"
                    )

                return@launch
            }

            val taskResult =
                completarTareaPendienteUseCase(
                    task.firestoreId
                )

            if (taskResult.isFailure) {

                _uiState.value =
                    MiJardinUiState.Error(
                        taskResult.exceptionOrNull()
                            ?.localizedMessage
                            ?: "No se pudo completar la tarea"
                    )

                return@launch
            }

            updateState()
        }
    }

    fun regarPlanta(plant: PlantEntity) {
        val task = currentTasks.find {
            it.plantFirestoreId == plant.firestoreId
        }
        if (task != null) {
            regarPlanta(plant, task)
        } else {
            viewModelScope.launch {
                _uiState.value = MiJardinUiState.Loading
                val result = waterPlantUseCase(plant)
                if (result.isSuccess) {
                    updateState()
                } else {
                    _uiState.value = MiJardinUiState.Error(
                        result.exceptionOrNull()?.localizedMessage
                            ?: "Error al registrar riego"
                    )
                }
            }
        }
    }

    fun obtenerPlantaDeTarea(
        task: TaskEntity
    ): PlantEntity? {

        return currentPlants.find { plant ->

            plant.firestoreId ==
                    task.plantFirestoreId
        }
    }

    private suspend fun cleanInvalidTasks() {

        val plants =
            obtenerPlantasJardinUseCase()
                .first()

        val plantIds =
            plants.map { plant ->
                plant.firestoreId
            }

        val tasks =
            obtenerTareasPendientesUseCase()
                .first()

        tasks.forEach { task ->

            if (
                task.plantFirestoreId
                !in plantIds
            ) {

                completarTareaPendienteUseCase(
                    task.firestoreId
                )
            }
        }
    }
}