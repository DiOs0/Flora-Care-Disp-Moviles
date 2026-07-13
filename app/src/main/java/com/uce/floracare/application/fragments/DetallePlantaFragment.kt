package com.uce.floracare.application.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import com.uce.floracare.repositories.TaskRepository
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

            val taskRepository = TaskRepository(
                firestoreManager,
                authManager,
                database.taskDao()
            )

            val plantRepository = PlantRepository(
                firestoreManager,
                StorageManager(requireContext()),
                authManager,
                database.plantDao(),
                taskRepository
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

        cargarCaracteristicas()
    }

    private fun cargarCaracteristicas() {
        val chips = mutableListOf<Pair<String, Int>>()

        chips.add(if (plant.caracteristicas.indoor) "Interior" to R.drawable.interior else "Exterior" to R.drawable.exterior)
        if (plant.caracteristicas.tropical) chips.add("Tropical" to R.drawable.tropical)
        if (plant.caracteristicas.medicinal) chips.add("Medicinal" to R.drawable.medicine_plant)
        if (plant.caracteristicas.resistenteSequia) chips.add("Resistente sequía" to R.drawable.sequia)
        if (plant.caracteristicas.toxicaHumanos) chips.add("Tóxico humanos" to R.drawable.humans_bad)
        if (plant.caracteristicas.toxicaMascotas) chips.add("Tóxico mascotas" to R.drawable.no_dog)

        val container = binding.layoutCaracteristicasItems
        container.removeAllViews()

        if (chips.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "Ninguna"
                setTextColor(resources.getColor(R.color.gris_natural, null))
                textSize = 16f
            }
            container.addView(tv)
            return
        }

        val chipsPorFila = 2
        chips.chunked(chipsPorFila).forEach { fila ->
            val row = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
            }

            fila.forEach { (label, iconRes) ->
                val chip = layoutInflater.inflate(R.layout.item_char_chip, row, false)
                chip.findViewById<ImageView>(R.id.imgCharIcon).setImageResource(iconRes)
                chip.findViewById<TextView>(R.id.txtCharLabel).text = label
                row.addView(chip)
            }

            container.addView(row)
        }
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
