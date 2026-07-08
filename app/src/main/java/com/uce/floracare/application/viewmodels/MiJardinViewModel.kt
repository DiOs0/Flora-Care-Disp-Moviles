package com.uce.floracare.application.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.domain.model.TaskEntity
import com.uce.floracare.domain.usecase.CompletarTareaPendienteUseCase
import com.uce.floracare.domain.usecase.GenerarTareasPendientesUseCase
import com.uce.floracare.domain.usecase.ObtenerTareasPendientesUseCase
import com.uce.floracare.repositories.PlantRepository
import kotlinx.coroutines.launch

class MiJardinViewModel(

    private val plantRepository: PlantRepository,

    private val generarTareasPendientesUseCase: GenerarTareasPendientesUseCase,

    private val obtenerTareasPendientesUseCase: ObtenerTareasPendientesUseCase,

    private val completarTareaPendienteUseCase: CompletarTareaPendienteUseCase

) : ViewModel() {

    private val _plantsList =
        MutableLiveData<List<PlantEntity>>()

    val plantsList: LiveData<List<PlantEntity>>
        get() = _plantsList


    private val _pendingTasks =
        MutableLiveData<List<TaskEntity>>()

    val pendingTasks: LiveData<List<TaskEntity>>
        get() = _pendingTasks


    private val _isLoading =
        MutableLiveData<Boolean>()

    val isLoading: LiveData<Boolean>
        get() = _isLoading


    private val _errorMessage =
        MutableLiveData<String>()

    val errorMessage: LiveData<String>
        get() = _errorMessage


    /**
     * Obtiene las plantas del usuario.
     */
    fun fetchPlants() {

        _isLoading.value = true

        viewModelScope.launch {

            val result =
                plantRepository.getMyPlants()

            result.fold(

                onSuccess = {

                    _plantsList.value = it

                },

                onFailure = {

                    _errorMessage.value =
                        it.localizedMessage
                            ?: "Error al obtener las plantas"

                }

            )

            _isLoading.value = false

        }

    }


    /**
     * Genera automáticamente las tareas pendientes.
     */
    fun generarTareas() {

        viewModelScope.launch {

            val result =
                generarTareasPendientesUseCase()

            result.onFailure {

                _errorMessage.value =
                    it.localizedMessage
                        ?: "Error al generar tareas"

            }

        }

    }


    /**
     * Obtiene las tareas almacenadas.
     */
    fun cargarTareas() {

        viewModelScope.launch {

            val result =
                obtenerTareasPendientesUseCase()

            result.fold(

                onSuccess = {

                    _pendingTasks.value = it

                },

                onFailure = {

                    _errorMessage.value =
                        it.localizedMessage
                            ?: "Error al cargar tareas"

                }

            )

        }

    }


    /**
     * Se ejecuta cuando el usuario marca el Check.
     */
    fun completarTarea(
        task: TaskEntity
    ) {

        viewModelScope.launch {

            val result =
                completarTareaPendienteUseCase(

                    task.firestoreId,

                    task.plantFirestoreId

                )

            result.fold(

                onSuccess = {

                    generarTareas()

                    cargarTareas()

                    fetchPlants()

                },

                onFailure = {

                    _errorMessage.value =
                        it.localizedMessage
                            ?: "Error al completar tarea"

                }

            )

        }

    }

}