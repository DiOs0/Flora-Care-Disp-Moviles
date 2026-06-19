package com.uce.floracare.activities.Jhon_AddPlant

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.api_ingreso.data.PlantRepository
import kotlinx.coroutines.launch

class AddPlantViewModel(private val repository: PlantRepository) : ViewModel() {

    // --- LiveData para manejar el estado de la UI ---
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> get() = _saveSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // --- Datos temporales del formulario ---
    var selectedPhotoUri: Uri? = null
    var selectedLocation: String = "Interior"

    /**
     * Valida los datos y ejecuta el guardado en el repositorio.
     */
    fun savePlant(name: String, species: String, lastWatered: String) {
        // Validaciones básicas
        if (name.isBlank()) {
            _errorMessage.value = "El nombre de la planta es obligatorio"
            return
        }
        if (species.isBlank()) {
            _errorMessage.value = "La especie es obligatoria"
            return
        }
        if (selectedPhotoUri == null) {
            _errorMessage.value = "Debes capturar una foto de la planta"
            return
        }

        _isLoading.value = true
        
        viewModelScope.launch {
            val result = repository.saveNewPlant(
                name = name,
                species = species,
                location = selectedLocation,
                lastWatered = lastWatered,
                photoUri = selectedPhotoUri!!
            )

            result.fold(
                onSuccess = {
                    _saveSuccess.value = true
                },
                onFailure = { error ->
                    _errorMessage.value = error.localizedMessage ?: "Error desconocido al guardar"
                }
            )
            _isLoading.value = false
        }
    }
}
