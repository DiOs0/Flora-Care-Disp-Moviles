package com.uce.floracare.application.viewmodels

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.PlantRepository
import kotlinx.coroutines.launch

class AddPlantViewModel(private val repository: PlantRepository) : ViewModel() {

    // --- LiveData para manejar el estado de la UI ---
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> get() = _saveSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String> get() = _errorMessage as LiveData<String>

    // --- Datos temporales del formulario ---
    var selectedPhotoUri: Uri? = null
    var selectedLocation: String = "Interior"

    /**
     * Valida los datos y ejecuta el guardado en el repositorio.
     */
    fun savePlant(plantVM : PlantEntity) {
        // Validaciones básicas
        val rules = listOf(
            // --- Datos Básicos ---
            { plantVM.nombreComun.isBlank() } to "El nombre común es obligatorio",
            { plantVM.nombreCientifico.isBlank() } to "El nombre científico es obligatorio",
            { plantVM.tipo.isBlank() } to "Debes especificar el tipo de planta",
            { plantVM.descripcion.isBlank() } to "La descripción no puede estar vacía",

            // --- Ciclo y Cuidados ---
            { plantVM.cicloVida.isBlank() } to "Debes indicar el ciclo de vida",
            { plantVM.nivelCuidado.isBlank() } to "Indica el nivel de cuidado (Ej: Bajo, Medio, Alto)",

            // --- Imagen (Uri capturada en el ViewModel) ---
            { selectedPhotoUri == null } to "Debes capturar o seleccionar una foto de la planta",

            // --- Riego ---
            { plantVM.riego.frecuencia.isBlank() } to "La frecuencia de riego es obligatoria",
            { plantVM.riego.cadaValor.isBlank() } to "Debes indicar cada cuánto tiempo se debe regar",

            // --- Luz Solar (Validación de Lista) ---
            { plantVM.luzSolar.isEmpty() } to "Debes seleccionar al menos un tipo de luz solar",

            // --- Temperatura (Validación de Rangos) ---
            { plantVM.temperatura.min == 0 && plantVM.temperatura.max == 0 } to "Debes registrar rangos de temperatura válidos",
            { plantVM.temperatura.min > plantVM.temperatura.max } to "La temperatura mínima no puede ser mayor a la máxima",
            { plantVM.temperatura.descripcion.isBlank() } to "La descripción de la temperatura es obligatoria"


        )
        val error = rules.find { it.first() }?.second


        if (error != null) {
            _errorMessage.value = error
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            val result = repository.saveNewPlant(
                plantVM, plantVM.imagen.toUri()
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