package com.uce.floracare.activities.Osorio_Explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.uce.floracare.R
import com.uce.floracare.activities.Jhon_AddPlant.AddPlantFragment
import com.uce.floracare.databinding.FragmentExploreBinding

class ExploreFragment : Fragment() {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

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