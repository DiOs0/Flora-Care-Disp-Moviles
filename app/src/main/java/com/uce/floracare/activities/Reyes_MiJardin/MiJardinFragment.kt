package com.uce.floracare.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.uce.floracare.activities.Reyes_MiJardin.toPlant
import com.uce.floracare.activities.adapters.PlantAdapter
import com.uce.floracare.api_ingreso.data.FirestoreManager
import com.uce.floracare.databinding.ActivityMiJardinBinding
import kotlinx.coroutines.launch

class MiJardinFragment : Fragment() {

    private var _binding: ActivityMiJardinBinding? = null
    private val binding get() = _binding!!

    private val firestoreManager = FirestoreManager()

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

        super.onViewCreated(view, savedInstanceState)

        val adapter = PlantAdapter { plant ->

            // Acción al tocar planta

        }

        binding.rvPlants.layoutManager =
            GridLayoutManager(requireContext(), 2)

        binding.rvPlants.adapter = adapter

        cargarPlantas(adapter)
    }

    private fun cargarPlantas(
        adapter: PlantAdapter
    ) {

        viewLifecycleOwner.lifecycleScope.launch {

            val result =
                firestoreManager.getPlants()

            result.onSuccess { entities ->

                val plants =
                    entities.map {
                        it.toPlant()
                    }

                adapter.submitList(plants)

                val tasksPending =
                    plants.count {
                        it.necesitaAgua
                    }

                binding.badgeRemaining.text =
                    "$tasksPending restantes"
            }

            result.onFailure {

                binding.badgeRemaining.text =
                    "Error al cargar"

                it.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}