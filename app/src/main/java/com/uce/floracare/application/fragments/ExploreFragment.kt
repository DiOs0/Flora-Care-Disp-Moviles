package com.uce.floracare.application.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uce.floracare.R
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.activities.Osorio_Explore.PlantAdapter
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.application.activities.MainActivity
import com.uce.floracare.databinding.FragmentExploreBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExploreFragment : Fragment() {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val firestoreManager = FirestoreManager(AuthManager())

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
        setupSearchListener()
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
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredAdapter
        }

        binding.rvCatalogPlants.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = catalogAdapter
        }
    }

    private fun navigateToDetail(plant: PlantEntity) {
        val addPlantFragment = AddPlantFragment.newInstance(
            name = plant.nombreComun,
            species = plant.nombreCientifico,
            isIndoor = plant.caracteristicas.indoor,
            imageUrl = plant.imagen,
            plantEntity = plant
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

    private fun setupSearchListener() {
        binding.etSearchPlants.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                val hasResults = catalogAdapter.filter(query)
                binding.tvNoResults.visibility = if (query.isNotBlank() && !hasResults) View.VISIBLE else View.GONE
            }
        })
    }

    private fun loadData() {
        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val featuredResult = withContext(Dispatchers.IO) {
                firestoreManager.getPlants(limit = 5)
            }
            featuredResult.onSuccess { entities ->
                featuredAdapter.submitFullList(entities)
            }
            featuredResult.onFailure { e ->
                Toast.makeText(requireContext(), "Error al carrar destacadas: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            loadCatalog(null)
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

                catalogAdapter.submitFullList(entities)

                showLoading(false)

                val query = binding.etSearchPlants.text.toString()

                if (query.isNotBlank()) {

                    val hasResults = catalogAdapter.filter(query)

                    binding.tvNoResults.visibility =
                        if (!hasResults) View.VISIBLE else View.GONE
                }
            }
            result.onFailure { e ->

                showLoading(false)

                Toast.makeText(
                    requireContext(),
                    "Error al cargar catálogo: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {

        if(show){

            binding.loadingContainer.visibility = View.VISIBLE
            binding.loadingAnimation.playAnimation()

            binding.rvCatalogPlants.visibility = View.GONE
            binding.rvFeaturedPlants.visibility = View.GONE

        }else{

            binding.loadingAnimation.cancelAnimation()
            binding.loadingContainer.visibility = View.GONE

            binding.rvCatalogPlants.visibility = View.VISIBLE
            binding.rvFeaturedPlants.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
