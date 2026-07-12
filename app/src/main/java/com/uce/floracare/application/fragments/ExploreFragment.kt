package com.uce.floracare.application.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.uce.floracare.R
import com.uce.floracare.application.activities.MainActivity
import com.uce.floracare.application.adapters.PlantAdapter
import com.uce.floracare.application.viewmodels.ExploreUiState
import com.uce.floracare.application.viewmodels.ExploreViewModel
import com.uce.floracare.application.viewmodels.ViewModelFactory
import com.uce.floracare.data.local.database.FloraCareDatabase
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.databinding.FragmentExploreBinding
import com.uce.floracare.domain.usecase.ObtenerCatalogoPlantasUseCase
import com.uce.floracare.domain.usecase.SincronizarCatalogoUseCase
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExploreViewModel by viewModels {
        ViewModelFactory {
            val authManager = AuthManager()
            val firestoreManager = FirestoreManager(authManager)
            val database = FloraCareDatabase.getDatabase(requireContext())
            val plantRepository = PlantRepository(
                firestoreManager,
                StorageManager(requireContext()),
                authManager,
                database.plantDao()
            )
            ExploreViewModel(
                obtenerCatalogoPlantasUseCase = ObtenerCatalogoPlantasUseCase(plantRepository),
                sincronizarCatalogoUseCase = SincronizarCatalogoUseCase(plantRepository)
            )
        }
    }

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
        setupObservers()

        if (viewModel.uiState.value is ExploreUiState.Idle) {
            viewModel.loadInitialData()
        }
    }

    private fun setupRecyclerViews() {
        featuredAdapter = PlantAdapter(
            onPlantClick = { plant -> navigateToDetail(plant) },
            layoutRes = R.layout.item_featured_plant
        )

        catalogAdapter = PlantAdapter(
            onPlantClick = { plant -> navigateToDetail(plant) },
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
                    R.id.chipInterior -> viewModel.loadCatalog(indoor = true)
                    R.id.chipExterior -> viewModel.loadCatalog(indoor = false)
                    else -> viewModel.loadCatalog(null)
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

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ExploreUiState.Loading -> showLoading(true)
                        is ExploreUiState.Success -> {
                            showLoading(false)
                            featuredAdapter.submitFullList(state.featured)
                            catalogAdapter.submitFullList(state.catalog)
                            
                            // Re-apply filter if text exists
                            val query = binding.etSearchPlants.text.toString()
                            if (query.isNotBlank()) {
                                catalogAdapter.filter(query)
                            }
                        }
                        is ExploreUiState.Error -> {
                            showLoading(false)
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        is ExploreUiState.Idle -> {}
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.loadingContainer.visibility = View.VISIBLE
            binding.loadingAnimation.playAnimation()
            binding.rvCatalogPlants.visibility = View.GONE
            binding.rvFeaturedPlants.visibility = View.GONE
        } else {
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
