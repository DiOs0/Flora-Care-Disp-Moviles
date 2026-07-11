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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.uce.floracare.application.activities.MainActivity
import com.uce.floracare.application.adapters.PlantAdapter
import com.uce.floracare.application.adapters.reyes_milan_osorio.TaskAdapter
import com.uce.floracare.application.viewmodels.MiJardinUiState
import com.uce.floracare.application.viewmodels.MiJardinViewModel
import com.uce.floracare.application.viewmodels.ViewModelFactory
import com.uce.floracare.databinding.ActivityMiJardinBinding
import com.uce.floracare.domain.usecase.CompletarTareaPendienteUseCase
import com.uce.floracare.domain.usecase.GenerarTareasPendientesUseCase
import com.uce.floracare.domain.usecase.ObtenerTareasPendientesUseCase
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.TaskRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import kotlinx.coroutines.launch

class MiJardinFragment : Fragment() {

    private var _binding: ActivityMiJardinBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MiJardinViewModel by viewModels {
        ViewModelFactory {
            val authManager = AuthManager()
            val firestoreManager = FirestoreManager(authManager)
            val plantRepository = PlantRepository(firestoreManager, StorageManager(requireContext()))
            val taskRepository = TaskRepository(firestoreManager)
            
            MiJardinViewModel(
                plantRepository,
                GenerarTareasPendientesUseCase(plantRepository, taskRepository),
                ObtenerTareasPendientesUseCase(taskRepository),
                CompletarTareaPendienteUseCase(plantRepository, taskRepository)
            )
        }
    }

    private lateinit var plantAdapter: PlantAdapter
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityMiJardinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupObservers()

        if (viewModel.uiState.value is MiJardinUiState.Idle) {
            viewModel.fetchData()
        }
    }

    private fun setupRecyclerViews() {
        plantAdapter = PlantAdapter(
            onPlantClick = { plant ->
                val bundle = Bundle().apply { putSerializable("plant", plant) }
                val fragment = DetallePlantaFragment().apply { arguments = bundle }
                (activity as MainActivity).loadFragment(fragment)
            }
        )

        taskAdapter = TaskAdapter { task ->
            viewModel.completarTarea(task)
        }

        binding.rvPlants.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = plantAdapter
        }

        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is MiJardinUiState.Loading -> {
                            // Opcional: mostrar un shimmer o progress
                        }
                        is MiJardinUiState.Success -> {
                            plantAdapter.submitList(state.plants)
                            taskAdapter.submitList(state.tasks)
                            binding.badgeRemaining.text = "${state.tasks.size} restantes"
                        }
                        is MiJardinUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        is MiJardinUiState.Idle -> {}
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
