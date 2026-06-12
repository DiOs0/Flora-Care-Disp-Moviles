package com.uce.floracare.activities.Osorio_Explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uce.floracare.R
import com.uce.floracare.activities.Jhon_AddPlant.AddPlantFragment
import com.uce.floracare.api_ingreso.data.FirestoreManager
import com.uce.floracare.api_ingreso.data.toPlantEntity
import com.uce.floracare.databinding.FragmentExploreBinding
import com.uce.floracare.api_ingreso.network.PerenualApiService
import com.uce.floracare.api_ingreso.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExploreFragment : Fragment() {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val firestoreManager = FirestoreManager()
    private val apiService = RetrofitClient.instance.create(PerenualApiService::class.java)
    private var currentPlantId = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val plants = listOf(
            Plant(1, "Monstera", "Monstera deliciosa", "Tropical", "Luz indirecta", "1 vez/semana", R.drawable.ic_launcher_foreground, true),
            Plant(2, "Poto", "Epipremnum aureum", "Interior", "Luz media", "Cada 7 días", R.drawable.ic_launcher_background, true),
            Plant(3, "Lavanda", "Lavandula angustifolia", "Exterior", "Sol pleno", "Cada 10 días", R.drawable.ic_home, false),
            Plant(4, "Helecho", "Nephrolepis exaltata", "Interior", "Sombra luminosa", "2 veces/semana", R.drawable.ic_settings, false),
            Plant(5, "Albahaca", "Ocimum basilicum", "Tropical", "Luz alta", "Cada 4 días", R.drawable.ic_search, true)
        )

        val featuredPlants = plants.filter { it.esDestacada }

        val featuredAdapter = FeaturedAdapter { plant -> navigateToAddPlant(plant) }
        val catalogAdapter = ExploreAdapter { plant -> navigateToAddPlant(plant) }

        binding.rvFeaturedPlants.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFeaturedPlants.adapter = featuredAdapter
        featuredAdapter.submitList(featuredPlants)

        binding.rvCatalogPlants.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCatalogPlants.adapter = catalogAdapter
        binding.rvCatalogPlants.setHasFixedSize(true)
        catalogAdapter.submitList(plants)

        binding.btnSeedData.setOnClickListener {
            fetchAndUploadPlant()
        }
    }

    private fun fetchAndUploadPlant() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSeedData.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getPlantDetails(currentPlantId)
                }

                val entity = response.toPlantEntity()

                val result = withContext(Dispatchers.IO) {
                    firestoreManager.uploadPlant(entity)
                }

                result.fold(
                    onSuccess = {
                        Toast.makeText(
                            requireContext(),
                            "✓ Planta #$currentPlantId subida: ${entity.nombreComun}",
                            Toast.LENGTH_SHORT
                        ).show()
                        currentPlantId++
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            requireContext(),
                            "✗ Error al subir: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "✗ Error en API (ID $currentPlantId): ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSeedData.isEnabled = true
            }
        }
    }

    private fun navigateToAddPlant(plant: Plant) {
        val fragment = AddPlantFragment.newInstance(plant.nombre)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("explore")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
