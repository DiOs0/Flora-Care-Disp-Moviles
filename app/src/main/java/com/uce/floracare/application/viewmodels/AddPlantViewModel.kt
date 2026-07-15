package com.uce.floracare.application.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.domain.usecase.ObtenerCatalogoPlantasUseCase
import com.uce.floracare.domain.usecase.RegistrarPlantaEnJardinUseCase
import com.uce.floracare.domain.usecase.SubirImagenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed class AddPlantUiState {
    object Idle : AddPlantUiState()
    object Loading : AddPlantUiState()
    object Success : AddPlantUiState()
    data class Error(val message: String) : AddPlantUiState()
}

/**
 * ViewModel para agregar plantas.
 * Desacoplado de Cloudinary y Repositorios mediante el Use Case RegistrarPlantaEnJardinUseCase.
 */
class AddPlantViewModel(
    private val registrarPlantaEnJardinUseCase: RegistrarPlantaEnJardinUseCase,
    private val subirImagenUseCase: SubirImagenUseCase,
    private val obtenerCatalogoPlantasUseCase: ObtenerCatalogoPlantasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddPlantUiState>(AddPlantUiState.Idle)
    val uiState: StateFlow<AddPlantUiState> = _uiState.asStateFlow()

    // Flujo de especies para el AutoComplete
    private val _catalogPlants = obtenerCatalogoPlantasUseCase()
    val speciesSuggestions: StateFlow<List<String>> = _catalogPlants
        .map { list -> list.map { it.nombreCientifico }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado para la planta seleccionada del catálogo
    private val _selectedCatalogPlant = MutableStateFlow<PlantEntity?>(null)
    val selectedCatalogPlant = _selectedCatalogPlant.asStateFlow()

    // Datos temporales del formulario
    var selectedPhotoUri: Uri? = null
    var selectedLocation: String = "Interior"

    /**
     * Establece la planta inicial (útil para el flujo desde el catálogo/explorar).
     */
    fun setInitialPlant(plant: PlantEntity) {
        _selectedCatalogPlant.value = plant
    }

    /**
     * Busca una planta por su nombre científico en el catálogo local y actualiza el estado.
     */
    fun onSpeciesSelected(speciesName: String) {
        viewModelScope.launch {
            // Buscamos en el flujo actual (ya que es Room, el valor está disponible)
            val plant = _catalogPlants.map { list -> 
                list.find { it.nombreCientifico == speciesName } 
            }.firstOrNull()
            
            _selectedCatalogPlant.value = plant
        }
    }

    /**
     * Valida y delega el registro de la planta al Use Case.
     */
    fun savePlant(plant: PlantEntity, fromExplore: Boolean = false, photoFile: File? = null) {
        if (!validate(plant, fromExplore)) return

        viewModelScope.launch {
            _uiState.value = AddPlantUiState.Loading

            var finalPlant = plant

            // 1. Si hay una nueva imagen, subirla
            if (photoFile != null) {
                val uploadResult = subirImagenUseCase(photoFile)
                uploadResult.fold(
                    onSuccess = { url ->
                        finalPlant = plant.copy(imagen = url)
                    },
                    onFailure = { error ->
                        _uiState.value = AddPlantUiState.Error("Error al subir imagen: ${error.localizedMessage}")
                        return@launch
                    }
                )
            }

            // 2. Registrar la planta
            val result = registrarPlantaEnJardinUseCase(finalPlant)

            result.fold(
                onSuccess = { 
                    _uiState.value = AddPlantUiState.Success 
                },
                onFailure = { error -> 
                    _uiState.value = AddPlantUiState.Error(error.localizedMessage ?: "Error al guardar") 
                }
            )
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
