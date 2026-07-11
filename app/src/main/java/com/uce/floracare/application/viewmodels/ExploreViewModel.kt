package com.uce.floracare.application.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ExploreUiState {
    object Idle : ExploreUiState()
    object Loading : ExploreUiState()
    data class Success(val featured: List<PlantEntity>, val catalog: List<PlantEntity>) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

class ExploreViewModel(private val firestoreManager: FirestoreManager) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Idle)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var currentFeatured: List<PlantEntity> = emptyList()
    private var currentCatalog: List<PlantEntity> = emptyList()

    fun loadInitialData() {
        _uiState.value = ExploreUiState.Loading
        viewModelScope.launch {
            try {
                val featuredResult = withContext(Dispatchers.IO) {
                    firestoreManager.getPlants(limit = 5)
                }
                
                featuredResult.onSuccess { featured ->
                    currentFeatured = featured
                    loadCatalog(null)
                }.onFailure { e ->
                    _uiState.value = ExploreUiState.Error(e.localizedMessage ?: "Error al cargar destacadas")
                }
            } catch (e: Exception) {
                _uiState.value = ExploreUiState.Error(e.localizedMessage ?: "Error inesperado")
            }
        }
    }

    fun loadCatalog(indoor: Boolean?) {
        viewModelScope.launch {
            if (_uiState.value !is ExploreUiState.Success) {
                _uiState.value = ExploreUiState.Loading
            }
            
            try {
                val result = withContext(Dispatchers.IO) {
                    when (indoor) {
                        true -> firestoreManager.getPlantsByIndoor(true, limit = 10)
                        false -> firestoreManager.getPlantsByIndoor(false, limit = 10)
                        null -> firestoreManager.getPlants(limit = 10)
                    }
                }

                result.onSuccess { catalog ->
                    currentCatalog = catalog
                    _uiState.value = ExploreUiState.Success(currentFeatured, currentCatalog)
                }.onFailure { e ->
                    _uiState.value = ExploreUiState.Error(e.localizedMessage ?: "Error al cargar catálogo")
                }
            } catch (e: Exception) {
                _uiState.value = ExploreUiState.Error(e.localizedMessage ?: "Error inesperado")
            }
        }
    }
}
