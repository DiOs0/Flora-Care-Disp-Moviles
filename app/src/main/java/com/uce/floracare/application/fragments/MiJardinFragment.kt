package com.uce.floracare.application.fragments

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uce.floracare.R
import com.uce.floracare.application.activities.MainActivity
import com.uce.floracare.application.adapters.PlantAdapter
import com.uce.floracare.application.adapters.PlantViewMode
import com.uce.floracare.application.adapters.reyes_milan_osorio.TaskAdapter
import com.uce.floracare.application.viewmodels.MiJardinUiState
import com.uce.floracare.application.viewmodels.MiJardinViewModel
import com.uce.floracare.application.viewmodels.ViewModelFactory
import com.uce.floracare.data.local.database.FloraCareDatabase
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.data.scheduler.AndroidWateringScheduler
import com.uce.floracare.databinding.ActivityMiJardinBinding
import com.uce.floracare.domain.usecase.ActualizarRiegoFrecuenciaUseCase
import com.uce.floracare.domain.usecase.CompletarTareaPendienteUseCase
import com.uce.floracare.domain.usecase.GenerarTareasPendientesUseCase
import com.uce.floracare.domain.usecase.ObtenerPlantaPorIdUseCase
import com.uce.floracare.domain.usecase.ObtenerPlantasJardinUseCase
import com.uce.floracare.domain.usecase.ObtenerTareasPendientesUseCase
import com.uce.floracare.domain.usecase.WaterPlantUseCase
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.TaskRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import kotlinx.coroutines.launch

class MiJardinFragment : Fragment() {

    private var _binding:
            ActivityMiJardinBinding? = null

    private val binding
        get() = _binding!!

    private val wateringOptions =
        mapOf(
            "Cada día" to 1,
            "Cada 2 días" to 2,
            "Cada 3 días" to 3,
            "Cada 5 días" to 5,
            "Cada semana" to 7,
            "Cada 10 días" to 10,
            "Cada 2 semanas" to 14,
            "Cada mes" to 30
        )

    private val viewModel:
            MiJardinViewModel by viewModels {

        ViewModelFactory {

            val authManager =
                AuthManager()

            val firestoreManager =
                FirestoreManager(
                    authManager
                )

            val database =
                FloraCareDatabase
                    .getDatabase(
                        requireContext()
                    )

            val taskRepository =
                TaskRepository(
                    firestoreManager,
                    authManager,
                    database.taskDao()
                )

            val plantRepository =
                PlantRepository(
                    firestoreManager,
                    StorageManager(
                        requireContext()
                    ),
                    authManager,
                    database.plantDao(),
                    taskRepository
                )

            val wateringScheduler =
                AndroidWateringScheduler(
                    requireContext()
                        .applicationContext
                )

            MiJardinViewModel(

                obtenerPlantasJardinUseCase =
                    ObtenerPlantasJardinUseCase(
                        plantRepository,
                        authManager
                    ),

                generarTareasPendientesUseCase =
                    GenerarTareasPendientesUseCase(
                        plantRepository,
                        taskRepository
                    ),

                obtenerTareasPendientesUseCase =
                    ObtenerTareasPendientesUseCase(
                        taskRepository,
                        authManager
                    ),

                completarTareaPendienteUseCase =
                    CompletarTareaPendienteUseCase(
                        taskRepository
                    ),

                actualizarRiegoFrecuenciaUseCase =
                    ActualizarRiegoFrecuenciaUseCase(
                        plantRepository
                    ),

                waterPlantUseCase =
                    WaterPlantUseCase(
                        plantRepository,
                        wateringScheduler
                    ),

                obtenerPlantaPorIdUseCase =
                    ObtenerPlantaPorIdUseCase(
                        plantRepository
                    )
            )
        }
    }

    private lateinit var plantAdapter:
            PlantAdapter

    private lateinit var taskAdapter:
            TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            ActivityMiJardinBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        // Si venimos de Login, mostramos el overlay inmediatamente
        if (requireActivity().intent.getBooleanExtra("FROM_LOGIN", false)) {
            binding.loadingOverlay.loadingOverlayRoot.visibility = View.VISIBLE
        }

        cargarFotoPerfil()
        setupRecyclerViews()
        setupToggleButton()
        setupObservers()

        if (
            viewModel.uiState.value
                    is MiJardinUiState.Idle
        ) {

            viewModel.fetchData()
        }
    }

    private fun cargarFotoPerfil() {

        val userId =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid
                ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                val currentBinding =
                    _binding
                        ?: return@addOnSuccessListener

                val photoUrl =
                    document.getString(
                        "photoUrl"
                    )

                Glide.with(
                    currentBinding.ivProfile
                )
                    .load(photoUrl)
                    .placeholder(
                        R.drawable.baseline_person_24
                    )
                    .error(
                        R.drawable.baseline_person_24
                    )
                    .centerCrop()
                    .into(
                        currentBinding.ivProfile
                    )
            }
            .addOnFailureListener {

                val currentBinding =
                    _binding
                        ?: return@addOnFailureListener

                currentBinding.ivProfile
                    .setImageResource(
                        R.drawable.baseline_person_24
                    )
            }
    }

    private fun setupRecyclerViews() {

        plantAdapter =
            PlantAdapter(

                onPlantClick = { plant ->

                    val bundle =
                        Bundle().apply {

                            putSerializable(
                                "plant",
                                plant
                            )
                        }

                    val fragment =
                        DetallePlantaFragment()
                            .apply {

                                arguments =
                                    bundle
                            }

                    (activity as MainActivity)
                        .loadFragment(
                            fragment
                        )
                },

                onEditWatering = { plant ->

                    showEditWateringDialog(
                        plant
                    )
                },

                onWaterPlant = { plant ->
                    showWateringAnimation()
                    viewModel.regarPlanta(plant)
                }
            )

        taskAdapter =
            TaskAdapter { task ->

                val plant =
                    viewModel
                        .obtenerPlantaDeTarea(
                            task
                        )

                if (plant != null) {
                    showWateringAnimation()
                    viewModel.regarPlanta(
                        plant,
                        task
                    )

                } else {

                    showErrorSnackbar(
                        "No se encontró la planta asociada"
                    )
                }
            }

        binding.rvPlants.apply {

            layoutManager =
                GridLayoutManager(
                    requireContext(),
                    2
                )

            adapter =
                plantAdapter
        }

        binding.rvTasks.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                taskAdapter
        }
    }

    private fun setupToggleButton() {
        binding.btnToggleView.setOnClickListener {
            toggleViewMode()
        }
    }

    private fun toggleViewMode() {
        val isGrid = plantAdapter.viewMode == PlantViewMode.GRID
        plantAdapter.viewMode = if (isGrid) PlantViewMode.LIST else PlantViewMode.GRID

        binding.rvPlants.layoutManager = if (isGrid) {
            LinearLayoutManager(requireContext())
        } else {
            GridLayoutManager(requireContext(), 2)
        }

        binding.btnToggleView.setIconResource(
            if (isGrid) android.R.drawable.ic_menu_sort_by_size
            else android.R.drawable.ic_menu_view
        )
    }

    private fun setupObservers() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel.uiState
                            .collect { state ->

                                when (state) {

                                    is MiJardinUiState.Loading -> {
                                        // Solo mostrar si no venimos de Login para evitar parpadeos
                                        if (!requireActivity().intent.getBooleanExtra("FROM_LOGIN", false)) {
                                            binding.loadingOverlay.loadingOverlayRoot.visibility = View.VISIBLE
                                        }
                                    }

                                    is MiJardinUiState.Success -> {
                                        binding.loadingOverlay.loadingOverlayRoot.visibility = View.GONE
                                        // Limpiar flag para futuras cargas normales
                                        requireActivity().intent.removeExtra("FROM_LOGIN")

                                        plantAdapter.submitList(
                                            state.plants
                                        )

                                        if (state.plants.isEmpty()) {
                                            binding.rvPlants.visibility = View.GONE
                                            binding.layoutEmptyPlants.visibility = View.VISIBLE
                                        } else {
                                            binding.rvPlants.visibility = View.VISIBLE
                                            binding.layoutEmptyPlants.visibility = View.GONE
                                        }

                                        taskAdapter.submitList(
                                            state.tasks
                                        )

                                        binding.badgeRemaining.text =
                                            "${state.tasks.size} restantes"

                                        if (
                                            state.tasks.isEmpty()
                                        ) {

                                            binding.tvEmptyTasks.visibility =
                                                View.VISIBLE

                                            binding.tvEmptyTasks.text =
                                                "¡Buen trabajo! Todas tus plantas están hidratadas"

                                        } else {

                                            binding.tvEmptyTasks.visibility =
                                                View.GONE
                                        }
                                    }

                                    is MiJardinUiState.Error -> {

                                        showErrorSnackbar(
                                            state.message
                                        )
                                    }

                                    is MiJardinUiState.UpdateSuccess -> {

                                        showSuccessSnackbar(
                                            "¡Frecuencia de riego actualizada!"
                                        )
                                    }

                                    is MiJardinUiState.Idle -> {
                                        // Sin acción
                                    }
                                }
                            }
                    }
            }
    }

    private fun showEditWateringDialog(
        plant: PlantEntity
    ) {

        val options =
            wateringOptions.keys
                .toTypedArray()

        var selectedOption =
            options.find {

                wateringOptions[it] ==
                        plant.wateringFrequencyDays

            } ?: options[0]

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                "Editar frecuencia de riego"
            )
            .setSingleChoiceItems(
                options,
                options.indexOf(
                    selectedOption
                )
            ) { _, which ->

                selectedOption =
                    options[which]
            }
            .setPositiveButton(
                "Actualizar"
            ) { _, _ ->

                val frequency =
                    wateringOptions[
                        selectedOption
                    ] ?: 7

                viewModel
                    .actualizarFrecuenciaRiego(
                        plant.firestoreId,
                        frequency
                    )
            }
            .setNegativeButton(
                "Cancelar",
                null
            )
            .show()
    }

    private fun showSuccessSnackbar(
        message: String
    ) {

        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        )
            .setBackgroundTint(
                resources.getColor(
                    R.color.care_low,
                    null
                )
            )
            .setTextColor(
                resources.getColor(
                    R.color.white,
                    null
                )
            )
            .show()
    }

    private fun showErrorSnackbar(
        message: String
    ) {

        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(
                "REINTENTAR"
            ) {

                viewModel.fetchData()
            }
            .setBackgroundTint(
                resources.getColor(
                    R.color.alert_red,
                    null
                )
            )
            .setActionTextColor(
                resources.getColor(
                    R.color.white,
                    null
                )
            )
            .show()
    }

    private fun showWateringAnimation() {
        binding.overlayWateringModal.apply {
            visibility = View.VISIBLE
            binding.lottieModalWatering.playAnimation()
            
            binding.lottieModalWatering.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                }
                override fun onAnimationCancel(animation: Animator) {
                    visibility = View.GONE
                }
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.fetchData()
            cargarFotoPerfil()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}