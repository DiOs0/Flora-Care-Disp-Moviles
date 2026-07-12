package com.uce.floracare.application.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import android.widget.ArrayAdapter
import com.uce.floracare.application.viewmodels.PlantDetailUiState
import com.uce.floracare.application.viewmodels.PlantDetailViewModel
import com.uce.floracare.application.viewmodels.ViewModelFactory
import com.uce.floracare.data.local.database.FloraCareDatabase
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.databinding.FragmentEditarPlantaBinding
import com.uce.floracare.domain.usecase.ActualizarPlantaUsuarioUseCase
import com.uce.floracare.domain.usecase.EliminarPlantaUsuarioUseCase
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import com.uce.floracare.utils.PlantConstants
import kotlinx.coroutines.launch

class EditarPlantaFragment : Fragment() {

    private var _binding: FragmentEditarPlantaBinding? = null
    private val binding get() = _binding!!

    private lateinit var plant: PlantEntity

    private val viewModel: PlantDetailViewModel by viewModels {
        ViewModelFactory {
            val authManager = AuthManager()
            val firestoreManager = FirestoreManager(authManager)
            val database = FloraCareDatabase.getDatabase(requireContext())
            val plantRepository = PlantRepository(
                firestoreManager,
                StorageManager(requireContext()),
                authManager,
                database.plantDao()
            )
            PlantDetailViewModel(
                eliminarPlantaUsuarioUseCase = EliminarPlantaUsuarioUseCase(plantRepository),
                actualizarPlantaUsuarioUseCase = ActualizarPlantaUsuarioUseCase(plantRepository)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditarPlantaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        plant = arguments?.getSerializable("plant") as PlantEntity

        loadData()
        setupObservers()

        binding.btnGuardar.setOnClickListener {
            actualizarPlanta()
        }

        binding.btnCancelar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadData() {
        Glide.with(requireContext())
            .load(plant.imagen)
            .into(binding.imgPlant)

        binding.edtNombre.setText(plant.nombreComun)
        binding.edtDescripcion.setText(plant.descripcion)
        binding.edtTipo.setText(plant.tipo)

        setupWateringDropdown()
    }

    private fun setupWateringDropdown() {
        val options = PlantConstants.wateringOptions.keys.toTypedArray()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
        binding.autoCompleteWateringFreq.setAdapter(adapter)

        // Pre-seleccionar la frecuencia actual
        val currentFreqText = PlantConstants.wateringOptions.entries.find { it.value == plant.wateringFrequencyDays }?.key
        if (currentFreqText != null) {
            binding.autoCompleteWateringFreq.setText(currentFreqText, false)
        }
    }

    private fun actualizarPlanta() {
        val selectedFreqText = binding.autoCompleteWateringFreq.text.toString()
        val frequencyDays = PlantConstants.wateringOptions[selectedFreqText] ?: plant.wateringFrequencyDays

        val updatedPlant = plant.copy(
            nombreComun = binding.edtNombre.text.toString(),
            descripcion = binding.edtDescripcion.text.toString(),
            tipo = binding.edtTipo.text.toString(),
            wateringFrequencyDays = frequencyDays
        )
        viewModel.actualizarPlanta(updatedPlant)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PlantDetailUiState.Loading -> { /* Show progress */ }
                        is PlantDetailUiState.Success -> {
                            Toast.makeText(requireContext(), "Planta actualizada", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }
                        is PlantDetailUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        is PlantDetailUiState.Idle -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
