package com.uce.floracare.application.fragments

import android.app.Dialog
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
import com.google.android.material.button.MaterialButton
import com.uce.floracare.R
import com.uce.floracare.application.activities.MainActivity
import com.uce.floracare.application.viewmodels.PlantDetailUiState
import com.uce.floracare.application.viewmodels.PlantDetailViewModel
import com.uce.floracare.application.viewmodels.ViewModelFactory
import com.uce.floracare.data.local.database.FloraCareDatabase
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.databinding.FragmentDetallePlantaBinding
import com.uce.floracare.domain.usecase.ActualizarPlantaUsuarioUseCase
import com.uce.floracare.domain.usecase.EliminarPlantaUsuarioUseCase
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import kotlinx.coroutines.launch

class DetallePlantaFragment : Fragment() {

    private var _binding: FragmentDetallePlantaBinding? = null
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
        _binding = FragmentDetallePlantaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        plant = arguments?.getSerializable("plant") as PlantEntity

        loadData()
        configurarEventos()
        setupObservers()
    }

    private fun loadData() {
        binding.txtNombre.text = plant.nombreComun
        binding.txtNombreCientifico.text = plant.nombreCientifico

        Glide.with(requireContext())
            .load(plant.imagen)
            .into(binding.imgPlant)

        binding.txtTipo.text = "Tipo: ${plant.tipo.ifEmpty { "No disponible" }}"
        binding.txtDescripcion.text = plant.descripcion.ifEmpty { "Sin descripción" }
        binding.txtRiego.text = "Frecuencia: ${plant.riego.frecuencia}"
        binding.txtCadaRiego.text = "Cada: ${plant.riego.cadaValor} días"
        binding.txtLuz.text = "Luz: ${plant.luzSolar.joinToString(", ")}"
        binding.txtCicloVida.text = "Ciclo: ${plant.cicloVida}"
        binding.txtNivelCuidado.text = "Cuidado: ${plant.nivelCuidado}"
        binding.txtTemperatura.text = "Temperatura: ${plant.temperatura.descripcion} ${plant.temperatura.min}°-${plant.temperatura.max}°"

        val caracteristicas = mutableListOf<String>()
        if (plant.caracteristicas.indoor) caracteristicas.add("Interior")
        if (plant.caracteristicas.tropical) caracteristicas.add("Tropical")
        if (plant.caracteristicas.medicinal) caracteristicas.add("Medicinal")
        if (plant.caracteristicas.resistenteSequia) caracteristicas.add("Resistente sequía")
        if (plant.caracteristicas.toxicaHumanos) caracteristicas.add("Tóxica humanos")
        if (plant.caracteristicas.toxicaMascotas) caracteristicas.add("Tóxica mascotas")

        binding.txtCaracteristicas.text = if (caracteristicas.isEmpty()) "Ninguna" else caracteristicas.joinToString(", ")
    }

    private fun configurarEventos() {
        binding.btnEditar.setOnClickListener {
            val fragment = EditarPlantaFragment()
            fragment.arguments = arguments
            (activity as MainActivity).loadFragment(fragment)
        }

        binding.btnEliminar.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_delete_plant)

            dialog.findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener {
                dialog.dismiss()
            }

            dialog.findViewById<MaterialButton>(R.id.btnEliminar).setOnClickListener {
                viewModel.eliminarPlanta(plant)
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PlantDetailUiState.Loading -> { /* Show progress */ }
                        is PlantDetailUiState.Success -> {
                            Toast.makeText(requireContext(), "Operación exitosa", Toast.LENGTH_SHORT).show()
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
