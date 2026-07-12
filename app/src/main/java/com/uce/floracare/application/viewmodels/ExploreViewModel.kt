package com.uce.floracare.application.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.domain.usecase.ObtenerCatalogoPlantasUseCase
import com.uce.floracare.domain.usecase.SincronizarCatalogoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ExploreUiState {
    object Idle : ExploreUiState()
    object Loading : ExploreUiState()
    data class Success(val featured: List<PlantEntity>, val catalog: List<PlantEntity>) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

/**
 * ViewModel para la pantalla de Explorar.
 * Desacoplado de Repositorios mediante Use Cases.
 */
class ExploreViewModel(
    private val obtenerCatalogoPlantasUseCase: ObtenerCatalogoPlantasUseCase,
    private val sincronizarCatalogoUseCase: SincronizarCatalogoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Idle)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var currentFeatured: List<PlantEntity> = emptyList()
    private var currentCatalog: List<PlantEntity> = emptyList()

    init {
        observeCatalog()
    }

    /**
     * Observa el catálogo desde el SSOT (Room) a través del Use Case.
     */
    private fun observeCatalog() {
        viewModelScope.launch {
            obtenerCatalogoPlantasUseCase().collect { plants ->
                // Mantenemos la lógica de negocio de "destacados" aquí
                currentFeatured = plants.take(5)
                currentCatalog = plants
                if (plants.isNotEmpty()) {
                    _uiState.value = ExploreUiState.Success(currentFeatured, currentCatalog)
                }
            }
        }
    }

    /**
     * Dispara la sincronización inicial del catálogo.
     */
    fun loadInitialData() {
        _uiState.value = ExploreUiState.Loading
        viewModelScope.launch {
            sincronizarCatalogoUseCase().onFailure { e ->
                _uiState.value = ExploreUiState.Error(e.localizedMessage ?: "Error al sincronizar")
            }
        }
    }

    /**
     * Carga el catálogo con filtros, sincronizando desde la nube si es necesario.
     */
    fun loadCatalog(indoor: Boolean?) {
        _uiState.value = ExploreUiState.Loading
        viewModelScope.launch {
            // Nota: En una arquitectura ideal, el filtro se pasaría al Use Case de obtención
            // o al de sincronización. Para este refactor mantenemos la cohesión actual.
            sincronizarCatalogoUseCase().onFailure { e ->
                _uiState.value = ExploreUiState.Error(e.localizedMessage ?: "Error al actualizar catálogo")
            }
        }
    }
}
