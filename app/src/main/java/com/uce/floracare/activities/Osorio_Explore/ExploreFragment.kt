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
import com.uce.floracare.api_ingreso.data.StorageManager
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
    private val storageManager = StorageManager()
    private val apiService = RetrofitClient.instance.create(PerenualApiService::class.java)
    private var currentPlantId = 1

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

        featuredAdapter = PlantAdapter(
            onPlantClick = { plant -> navigateToAddPlant(plant) },
            layoutRes = R.layout.item_featured_plant
        )
        catalogAdapter = PlantAdapter(
            onPlantClick = { plant -> navigateToAddPlant(plant) },
            layoutRes = R.layout.item_catalog_plant
        )

        binding.rvFeaturedPlants.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFeaturedPlants.adapter = featuredAdapter

        binding.rvCatalogPlants.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCatalogPlants.adapter = catalogAdapter

        setupChips()
        loadData()

        binding.btnSeedData.setOnClickListener { fetchAndUploadPlant() }

        binding.btnUploadId.setOnClickListener {
            val idText = binding.etManualId.text.toString().trim()
            if (idText.isEmpty()) {
                binding.etManualId.error = "Ingresa un ID"
                return@setOnClickListener
            }
            val id = idText.toIntOrNull()
            if (id == null || id <= 0) {
                binding.etManualId.error = "ID inválido"
                return@setOnClickListener
            }
            uploadPlantById(id)
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
        if (!show) {
            binding.tvUploadStatus.visibility = View.GONE
        }
    }

    private fun setUploadStatus(text: String) {
        binding.tvUploadStatus.apply {
            this.text = text
            visibility = View.VISIBLE
        }
    }

    private fun fetchAndUploadPlant() {
        showLoading(true)
        setUploadStatus("Obteniendo datos de la API...")
        binding.btnSeedData.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val existingIds = withContext(Dispatchers.IO) { firestoreManager.getExistingIds() }
                while (existingIds.contains(currentPlantId)) { currentPlantId++ }

                val response = withContext(Dispatchers.IO) { apiService.getPlantDetails(currentPlantId) }
                var entity = response.toPlantEntity()

                if (entity.imagen.isNotBlank()) {
                    setUploadStatus("Descargando imagen...")
                    val downloadResult = withContext(Dispatchers.IO) {
                        storageManager.uploadPlantImage(currentPlantId, entity.imagen)
                    }
                    downloadResult.fold(
                        onSuccess = { permanentUrl ->
                            entity = entity.copy(imagen = permanentUrl)
                            setUploadStatus("Subiendo a Firebase Storage...")
                        },
                        onFailure = { error ->
                            setUploadStatus("Error en imagen, guardando sin ella...")
                            entity = entity.copy(imagen = "")
                        }
                    )
                } else {
                    setUploadStatus("Guardando en Firestore...")
                }

                val result = withContext(Dispatchers.IO) { firestoreManager.uploadPlant(entity) }
                result.fold(
                    onSuccess = {
                        setUploadStatus("")
                        showLoading(false)
                        Toast.makeText(requireContext(), "\u2713 Planta #$currentPlantId subida: ${entity.nombreComun}", Toast.LENGTH_SHORT).show()
                        currentPlantId++
                        loadData()
                    },
                    onFailure = { error ->
                        setUploadStatus("Error: ${error.message}")
                        Toast.makeText(requireContext(), "\u2717 Error al subir: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                setUploadStatus("Error: ${e.message}")
                Toast.makeText(requireContext(), "\u2717 Error en API (ID $currentPlantId): ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
                binding.btnSeedData.isEnabled = true
            }
        }
    }

    private fun uploadPlantById(id: Int) {
        showLoading(true)
        setUploadStatus("Obteniendo datos de la API...")
        binding.btnUploadId.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { apiService.getPlantDetails(id) }
                var entity = response.toPlantEntity()

                if (entity.imagen.isNotBlank()) {
                    setUploadStatus("Descargando imagen...")
                    val downloadResult = withContext(Dispatchers.IO) {
                        storageManager.uploadPlantImage(id, entity.imagen)
                    }
                    downloadResult.fold(
                        onSuccess = { permanentUrl ->
                            entity = entity.copy(imagen = permanentUrl)
                            setUploadStatus("Subiendo a Firebase Storage...")
                        },
                        onFailure = { error ->
                            setUploadStatus("Error en imagen, guardando sin ella...")
                            entity = entity.copy(imagen = "")
                        }
                    )
                } else {
                    setUploadStatus("Guardando en Firestore...")
                }

                val result = withContext(Dispatchers.IO) { firestoreManager.uploadPlant(entity) }
                result.fold(
                    onSuccess = {
                        setUploadStatus("")
                        showLoading(false)
                        Toast.makeText(requireContext(), "\u2713 Planta #$id subida: ${entity.nombreComun}", Toast.LENGTH_SHORT).show()
                        binding.etManualId.text.clear()
                        loadData()
                    },
                    onFailure = { error ->
                        setUploadStatus("Error: ${error.message}")
                        Toast.makeText(requireContext(), "\u2717 Error al subir #$id: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                setUploadStatus("Error: ${e.message}")
                Toast.makeText(requireContext(), "\u2717 Error en API (ID $id): ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
                binding.btnUploadId.isEnabled = true
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
