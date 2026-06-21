package com.uce.floracare.activities.Reyes_MiJardin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.uce.floracare.activities.Jhon_AddPlant.utils.AuthManager
import com.uce.floracare.api_ingreso.data.FirestoreManager
import com.uce.floracare.api_ingreso.data.PlantRepository
import com.uce.floracare.api_ingreso.data.StorageManager
import com.uce.floracare.domain.usecase.GeneratePlantTasksUC
import com.uce.floracare.databinding.ActivityMiJardinBinding

class MiJardinFragment : Fragment() {

    private var _binding: ActivityMiJardinBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MiJardinViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val authManager = AuthManager()
                val repository = PlantRepository(FirestoreManager(authManager), StorageManager(requireContext()))
                val generatePlantTasksUC = GeneratePlantTasksUC()
                return MiJardinViewModel(repository, generatePlantTasksUC) as T
            }
        }
    }

    private lateinit var plantAdapter: PlantAdapter
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityMiJardinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        viewModel.fetchPlants()
    }

    private fun setupRecyclerView() {
        plantAdapter = PlantAdapter { plant ->
            // Acción al tocar planta (navegar a detalles, etc.)
            Toast.makeText(requireContext(), "Planta: ${plant.nombreComun}", Toast.LENGTH_SHORT).show()
        }

        taskAdapter = TaskAdapter()

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
        viewModel.plantsList.observe(viewLifecycleOwner) { plants ->
            plantAdapter.submitList(plants)
        }

        viewModel.pendingTasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
            binding.badgeRemaining.text = "${tasks.size} restantes"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Si tienes un progress bar en tu layout, actívalo aquí
            // binding.progressBar.isVisible = isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            binding.badgeRemaining.text = "Error al cargar"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
