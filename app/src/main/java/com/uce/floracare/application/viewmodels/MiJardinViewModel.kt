package com.uce.floracare.application.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.domain.model.PlantTask
import com.uce.floracare.domain.usecase.GeneratePlantTasksUC
import kotlinx.coroutines.launch

class MiJardinViewModel(
    private val repository: PlantRepository,
    private val generatePlantTasksUC: GeneratePlantTasksUC
) : ViewModel() {

    private val _plantsList = MutableLiveData<List<PlantEntity>>()
    val plantsList: LiveData<List<PlantEntity>> get() = _plantsList

    private val _pendingTasks = MutableLiveData<List<PlantTask>>()
    val pendingTasks: LiveData<List<PlantTask>> get() = _pendingTasks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    /**
     * Carga las plantas del usuario desde el repositorio.
     */
    fun fetchPlants() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getMyPlants()
            result.fold(
                onSuccess = { list ->
                    _plantsList.value = list
                    _pendingTasks.value = generatePlantTasksUC(list)
                },
                onFailure = { error ->
                    _errorMessage.value = error.localizedMessage ?: "Error al cargar tus plantas"
                }
            )
            _isLoading.value = false
        }
    }
}