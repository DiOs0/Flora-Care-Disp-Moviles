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
import com.uce.floracare.activities.MainActivity
import com.uce.floracare.activities.Jhon_AddPlant.AddPlantFragment
import com.uce.floracare.api_ingreso.data.FirestoreManager
import com.uce.floracare.databinding.FragmentExploreBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class ExploreFragment : Fragment() {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val firestoreManager = FirestoreManager()

    private lateinit var featuredAdapter: PlantAdapter
    private lateinit var catalogAdapter: PlantAdapter

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

        setupRecyclerViews()
        setupChips()
        loadData()
    }

    private fun setupRecyclerViews() {
        featuredAdapter = PlantAdapter(
            onPlantClick = { plant ->
                navigateToDetail(plant)
            },
            layoutRes = R.layout.item_featured_plant
        )

        catalogAdapter = PlantAdapter(
            onPlantClick = { plant ->
                navigateToDetail(plant)
            },
            layoutRes = R.layout.item_catalog_plant
        )

        binding.rvFeaturedPlants.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredAdapter
        }

        binding.rvCatalogPlants.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = catalogAdapter
        }
    }

    private fun navigateToDetail(plant: Plant) {
        val addPlantFragment = AddPlantFragment.newInstance(
            name = plant.nombre,
            species = plant.nombreCientifico,
            isIndoor = plant.indoor
        )

        (activity as? MainActivity)?.apply {
            setSelectedMenuItem(R.id.nav_add)
            loadFragment(addPlantFragment)
        }
    }

    private fun setupChips() {
        val chips = listOf(
            binding.chipSuculentas,
            binding.chipInterior,
            binding.chipExterior
        )
        chips.forEach { chip ->
            chip.isCheckable = true
            chip.setOnClickListener {
                chips.forEach { it.isChecked = false }
                chip.isChecked = true
                when (chip.id) {
                    R.id.chipInterior -> loadCatalog(indoor = true)
                    R.id.chipExterior -> loadCatalog(indoor = false)
                    else -> loadCatalog(null)
                }
            }
        }
    }

    private fun loadData() {
        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val featuredResult = withContext(Dispatchers.IO) {
                firestoreManager.getPlants(limit = 5)
            }
            featuredResult.onSuccess { entities ->
                featuredAdapter.submitList(entities.map { it.toExplorePlant() })
            }
            featuredResult.onFailure { e ->
                Toast.makeText(requireContext(), "Error al carrar destacadas: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            loadCatalog(null)
            showLoading(false)
        }
    }

    private fun loadCatalog(indoor: Boolean? = null) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                when (indoor) {
                    true -> firestoreManager.getPlantsByIndoor(true, limit = 10)
                    false -> firestoreManager.getPlantsByIndoor(false, limit = 10)
                    null -> firestoreManager.getPlants(limit = 10)
                }
            }
            result.onSuccess { entities ->
                catalogAdapter.submitList(entities.map { it.toExplorePlant() })
            }
            result.onFailure { e ->
                Toast.makeText(requireContext(), "Error al carrar catálogo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
