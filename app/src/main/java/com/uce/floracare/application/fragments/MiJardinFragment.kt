package com.uce.floracare.application.fragments

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
import com.uce.floracare.activities.Reyes_MiJardin.PlantAdapter
import com.uce.floracare.activities.Reyes_MiJardin.TaskAdapter
import com.uce.floracare.application.activities.MainActivity
import com.uce.floracare.application.viewmodels.MiJardinViewModel
import com.uce.floracare.databinding.ActivityMiJardinBinding
import com.uce.floracare.domain.usecase.CompletarTareaPendienteUseCase
import com.uce.floracare.domain.usecase.GenerarTareasPendientesUseCase
import com.uce.floracare.domain.usecase.ObtenerTareasPendientesUseCase
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.TaskRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager

class MiJardinFragment : Fragment() {

    private var _binding: ActivityMiJardinBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MiJardinViewModel by viewModels {

        object : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(modelClass: Class<T>): T {

                val authManager = AuthManager()

                val firestoreManager =
                    FirestoreManager(authManager)

                val plantRepository =
                    PlantRepository(
                        firestoreManager,
                        StorageManager(requireContext())
                    )

                val taskRepository =
                    TaskRepository(
                        firestoreManager
                    )

                val generarUC =
                    GenerarTareasPendientesUseCase(
                        plantRepository,
                        taskRepository
                    )

                val obtenerUC =
                    ObtenerTareasPendientesUseCase(
                        taskRepository
                    )

                val completarUC =
                    CompletarTareaPendienteUseCase(
                        plantRepository,
                        taskRepository
                    )

                @Suppress("UNCHECKED_CAST")
                return MiJardinViewModel(
                    plantRepository,
                    generarUC,
                    obtenerUC,
                    completarUC
                ) as T
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

        setupRecyclerViews()

        setupObservers()

        viewModel.fetchPlants()

        viewModel.generarTareas()

        viewModel.cargarTareas()
    }

    private fun setupRecyclerViews() {

        plantAdapter =
            PlantAdapter { plant ->

                val bundle =
                    Bundle()

                bundle.putSerializable(
                    "plant",
                    plant
                )

                val fragment =
                    DetallePlantaFragment()

                fragment.arguments =
                    bundle

                (activity as MainActivity)
                    .loadFragment(fragment)
            }

        taskAdapter = TaskAdapter { task ->

            viewModel.completarTarea(task)

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

    private fun setupObservers() {

        viewModel.plantsList.observe(viewLifecycleOwner) { plants ->

            plantAdapter.submitList(plants)

        }

        viewModel.pendingTasks.observe(viewLifecycleOwner) { tasks ->

            taskAdapter.submitList(tasks)

            binding.badgeRemaining.text =
                "${tasks.size} restantes"

        }

        viewModel.isLoading.observe(viewLifecycleOwner) {

            // ProgressBar opcional

        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->

            Toast.makeText(
                requireContext(),
                error,
                Toast.LENGTH_LONG
            ).show()

        }





    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

}