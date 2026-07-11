package com.uce.floracare.application.fragments

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cloudinary.android.MediaManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uce.floracare.application.activities.Login
import com.uce.floracare.application.viewmodels.AddPlantUiState
import com.uce.floracare.application.viewmodels.AddPlantViewModel
import com.uce.floracare.application.viewmodels.ViewModelFactory
import com.uce.floracare.data.remote.dto.Caracteristicas
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.data.remote.dto.Riego
import com.uce.floracare.data.remote.dto.Temperatura
import com.uce.floracare.databinding.FragmentAddPlantBinding
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import com.uce.floracare.utils.CameraManager
import kotlinx.coroutines.launch
import java.io.File

class AddPlantFragment : Fragment() {

    private var _binding: FragmentAddPlantBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraManager: CameraManager

    private var isExploreFlow: Boolean = false
    private var originalPlantName: String? = null
    private var originalPlantImage: String? = null
    private var originalTempDescription: String? = null

    private val viewModel: AddPlantViewModel by viewModels {
        ViewModelFactory {
            val authManager = AuthManager()
            val storageManager = StorageManager(requireContext())
            val firestoreManager = FirestoreManager(authManager)
            val repository = PlantRepository(firestoreManager, storageManager)
            AddPlantViewModel(repository)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            binding.viewFinder.isVisible = true
            cameraManager.startCamera(viewLifecycleOwner, binding.viewFinder)
        } else {
            Toast.makeText(requireContext(), "Se requiere permiso de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraManager = CameraManager(requireContext())
        
        initCloudinary()
        setupDropdowns()
        setupObservers()
        initListeners()
        populateFormFromArguments()
    }

    private fun initCloudinary() {
        try {
            val config = mapOf(
                "cloud_name" to "deqhd3bmp",
                "api_key" to "188973848385489",
                "api_secret" to "bmPFYmcccVKbOhp5g0U6LyHn8aE"
            )
            MediaManager.init(requireContext(), config)
        } catch (e: Exception) {
            // Ya inicializado
        }
    }

    private fun setupDropdowns() {
        val types = arrayOf("Suculenta", "Cactus", "Follaje", "Flor", "Arbusto", "Árbol")
        val cycles = arrayOf("Perenne", "Anual", "Bienal")
        val careLevels = arrayOf("Bajo", "Medio", "Alto")

        binding.autoCompleteType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types))
        binding.autoCompleteCycle.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cycles))
        binding.autoCompleteCareLevel.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, careLevels))
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AddPlantUiState.Loading -> setLoadingState(true)
                        is AddPlantUiState.Success -> {
                            setLoadingState(false)
                            Toast.makeText(requireContext(), "¡Planta guardada!", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }
                        is AddPlantUiState.Error -> {
                            setLoadingState(false)
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        is AddPlantUiState.Idle -> setLoadingState(false)
                    }
                }
            }
        }
    }

    private fun initListeners() {
        binding.closeFromAddPlant.setOnClickListener { showLogoutDialog() }

        binding.btnCapturePhoto.setOnClickListener {
            if (binding.viewFinder.isVisible) capturePhoto()
            else requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.imgPlantPhoto.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnSavePlant.setOnClickListener { handleSaveAction() }
    }

    private fun capturePhoto() {
        cameraManager.takePhoto(
            onPhotoSaved = { uri ->
                binding.viewFinder.isVisible = false
                binding.imgPlantPhoto.setImageURI(uri)
                viewModel.selectedPhotoUri = uri
            },
            onError = { Toast.makeText(requireContext(), "Error al tomar foto", Toast.LENGTH_SHORT).show() }
        )
    }

    private fun handleSaveAction() {
        val plantEntity = buildPlantEntity(originalPlantImage ?: "")
        val photoUri = viewModel.selectedPhotoUri
        val photoFile = photoUri?.let { uriToFile(it) }

        if (isExploreFlow && photoUri == null) {
            val currentName = binding.etPlantName.text.toString().trim()
            if (currentName == originalPlantName) {
                showUnsavedAlert(plantEntity)
                return
            }
        }

        viewModel.savePlant(plantEntity, isExploreFlow, photoFile)
    }

    private fun buildPlantEntity(currentImageUrl: String): PlantEntity {
        return PlantEntity(
            nombreComun = binding.etPlantName.text.toString().trim(),
            nombreCientifico = binding.etPlantSpecies.text.toString().trim(),
            tipo = binding.autoCompleteType.text.toString().trim(),
            descripcion = binding.etDescription.text.toString().trim(),
            cicloVida = binding.autoCompleteCycle.text.toString().trim(),
            nivelCuidado = binding.autoCompleteCareLevel.text.toString().trim(),
            caracteristicas = Caracteristicas(
                medicinal = binding.chipMedicinal.isChecked,
                indoor = binding.chipIndoor.isChecked,
                tropical = binding.chipTropical.isChecked,
                resistenteSequia = binding.chipDrought.isChecked,
                toxicaHumanos = binding.chipToxicHumans.isChecked,
                toxicaMascotas = binding.chipToxicPets.isChecked
            ),
            riego = Riego(
                frecuencia = binding.etWateringFreq.text.toString().trim(),
                cadaValor = binding.etWateringValue.text.toString().trim()
            ),
            luzSolar = binding.chipGroupSunlight.checkedChipIds.mapNotNull { id ->
                when (id) {
                    binding.solDirecto.id -> "Sol Directo"
                    binding.sombraParcial.id -> "Sombra Parcial"
                    binding.sombra.id -> "Sombra"
                    else -> null
                }
            },
            temperatura = Temperatura(
                min = binding.etTempMin.text.toString().trim().toIntOrNull() ?: 0,
                max = binding.etTempMax.text.toString().trim().toIntOrNull() ?: 0,
                descripcion = originalTempDescription ?: "Personalizada"
            ),
            imagen = currentImageUrl
        )
    }

    private fun showUnsavedAlert(plantEntity: PlantEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sin cambios detectados")
            .setMessage("¿Deseas guardar la planta de todas formas?")
            .setPositiveButton("Sí") { _, _ -> viewModel.savePlant(plantEntity, true) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val fileName = uri.lastPathSegment ?: "temp_img.jpg"
            File(requireContext().externalCacheDir, fileName)
        } catch (e: Exception) {
            null
        }
    }

    private fun setLoadingState(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.btnSavePlant.isEnabled = !loading
        binding.btnCapturePhoto.isEnabled = !loading
    }

    private fun populateFormFromArguments() {
        arguments?.let {
            val entity = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_PLANT_ENTITY, PlantEntity::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getSerializable(ARG_PLANT_ENTITY) as? PlantEntity
            }

            if (entity != null) {
                isExploreFlow = true
                originalPlantName = entity.nombreComun
                originalPlantImage = entity.imagen
                originalTempDescription = entity.temperatura.descripcion

                binding.apply {
                    etPlantName.setText(entity.nombreComun)
                    etPlantSpecies.setText(entity.nombreCientifico)
                    autoCompleteType.setText(entity.tipo, false)
                    etDescription.setText(entity.descripcion)
                    autoCompleteCycle.setText(entity.cicloVida, false)
                    autoCompleteCareLevel.setText(entity.nivelCuidado, false)
                    etWateringFreq.setText(entity.riego.frecuencia)
                    etWateringValue.setText(entity.riego.cadaValor)
                    etTempMin.setText(entity.temperatura.min.toString())
                    etTempMax.setText(entity.temperatura.max.toString())

                    chipMedicinal.isChecked = entity.caracteristicas.medicinal
                    chipIndoor.isChecked = entity.caracteristicas.indoor
                    chipTropical.isChecked = entity.caracteristicas.tropical
                    chipDrought.isChecked = entity.caracteristicas.resistenteSequia
                    chipToxicHumans.isChecked = entity.caracteristicas.toxicaHumanos
                    chipToxicPets.isChecked = entity.caracteristicas.toxicaMascotas

                    entity.luzSolar.forEach { luz ->
                        when (luz.lowercase().trim()) {
                            "sol directo", "full sun" -> solDirecto.isChecked = true
                            "sombra parcial", "part sun", "part shade", "partial sun", "partial shade" -> sombraParcial.isChecked = true
                            "sombra", "shade" -> sombra.isChecked = true
                        }
                    }
                }
                viewModel.selectedLocation = if (entity.caracteristicas.indoor) "Interior" else "Exterior"
            }
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que desea salir?")
            .setPositiveButton("Sí") { _, _ ->
                startActivity(Intent(requireContext(), Login::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PLANT_ENTITY = "plant_entity"
        fun newInstance(name: String, species: String, isIndoor: Boolean, imageUrl: String? = null, plantEntity: PlantEntity? = null): AddPlantFragment {
            return AddPlantFragment().apply {
                arguments = Bundle().apply {
                    putString("plant_name", name)
                    putString("plant_species", species)
                    putBoolean("is_indoor", isIndoor)
                    putString("plant_image", imageUrl)
                    if (plantEntity != null) putSerializable(ARG_PLANT_ENTITY, plantEntity)
                }
            }
        }
    }
}
