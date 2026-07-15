package com.uce.floracare.application.fragments

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
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
import com.uce.floracare.data.local.database.FloraCareDatabase
import com.uce.floracare.data.remote.dto.Caracteristicas
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.data.remote.dto.Riego
import com.uce.floracare.data.remote.dto.Temperatura
import com.uce.floracare.databinding.FragmentAddPlantBinding
import com.uce.floracare.domain.usecase.ObtenerCatalogoPlantasUseCase
import com.uce.floracare.domain.usecase.RegistrarPlantaEnJardinUseCase
import com.uce.floracare.domain.usecase.SubirImagenUseCase
import com.uce.floracare.repositories.ImageRepositoryImpl
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.TaskRepository
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

    private val viewModel: AddPlantViewModel by viewModels {
        ViewModelFactory {
            val authManager = AuthManager()
            val firestoreManager = FirestoreManager(authManager)
            val database = FloraCareDatabase.getDatabase(requireContext())
            val taskRepository = TaskRepository(firestoreManager, authManager, database.taskDao())
            val plantRepository = PlantRepository(firestoreManager, StorageManager(requireContext()), authManager, database.plantDao(), taskRepository)
            val imageRepository = ImageRepositoryImpl()

            AddPlantViewModel(
                registrarPlantaEnJardinUseCase = RegistrarPlantaEnJardinUseCase(plantRepository),
                subirImagenUseCase = SubirImagenUseCase(imageRepository),
                obtenerCatalogoPlantasUseCase = ObtenerCatalogoPlantasUseCase(plantRepository)
            )
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCameraPreview()
        } else {
            Toast.makeText(requireContext(), "Se requiere permiso de cámara para esta experiencia", Toast.LENGTH_LONG).show()
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
        setupObservers()
        initListeners()
        populateFormFromArguments()
        
        // Iniciar cámara inmediatamente si no venimos de un catálogo
        if (!isExploreFlow) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCameraPreview() {
        binding.viewFinder.isVisible = true
        cameraManager.startCamera(viewLifecycleOwner, binding.viewFinder)
    }

    private fun initListeners() {
        binding.closeFromAddPlant.setOnClickListener { showLogoutDialog() }

        binding.etPlantSpecies.setOnItemClickListener { parent, _, position, _ ->
            val selectedSpecies = parent.getItemAtPosition(position) as String
            viewModel.onSpeciesSelected(selectedSpecies)
        }

        binding.btnCapturePhoto.setOnClickListener {
            capturePhoto()
        }

        binding.btnRetakePhoto.setOnClickListener {
            resetToCamera()
        }

        binding.btnSavePlant.setOnClickListener { handleSaveAction() }
    }

    private fun capturePhoto() {
        // Efecto visual de obturador
        binding.cameraOverlay.animate().alpha(0.8f).setDuration(50).withEndAction {
            binding.cameraOverlay.animate().alpha(0.3f).setDuration(100).start()
        }.start()

        cameraManager.takePhoto(
            onPhotoSaved = { uri ->
                showFormAfterCapture(uri)
            },
            onError = { 
                Toast.makeText(requireContext(), "Error al capturar la imagen", Toast.LENGTH_SHORT).show() 
            }
        )
    }

    private fun showFormAfterCapture(uri: Uri) {
        // 1. Guardar Uri en el ViewModel
        viewModel.selectedPhotoUri = uri
        binding.imgPlantPhoto.setImageURI(uri)

        // 2. Animación de transición: Cámara -> Formulario
        binding.cameraLayer.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                binding.cameraLayer.isVisible = false
                revealForm()
            }.start()
    }

    private fun revealForm() {
        binding.formLayer.isVisible = true
        binding.formLayer.alpha = 0f
        binding.formLayer.translationY = 200f
        
        binding.formLayer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun resetToCamera() {
        binding.formLayer.animate()
            .alpha(0f)
            .translationY(200f)
            .setDuration(300)
            .withEndAction {
                binding.formLayer.isVisible = false
                binding.cameraLayer.isVisible = true
                binding.cameraLayer.alpha = 0f
                binding.cameraLayer.scaleX = 0.8f
                binding.cameraLayer.scaleY = 0.8f
                
                binding.cameraLayer.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .start()
                
                startCameraPreview()
            }.start()
    }

    private fun setupObservers() {
        // Observar sugerencias de especies
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.speciesSuggestions.collect { speciesList ->
                    val adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        speciesList
                    )
                    binding.etPlantSpecies.setAdapter(adapter)
                }
            }
        }

        // Observar planta seleccionada del catálogo para auto-completar campos
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedCatalogPlant.collect { plant ->
                    plant?.let {
                        // Solo auto-completar si el campo está vacío para no sobreescribir al usuario
                        if (binding.etPlantName.text.isNullOrBlank()) {
                            binding.etPlantName.setText(it.nombreComun)
                        }
                        // Actualizar la especie siempre que se seleccione del catálogo
                        if (binding.etPlantSpecies.text.isNullOrBlank() || binding.etPlantSpecies.text.toString() != it.nombreCientifico) {
                            binding.etPlantSpecies.setText(it.nombreCientifico, false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AddPlantUiState.Loading -> setLoadingState(true)
                        is AddPlantUiState.Success -> {
                            setLoadingState(false)
                            Toast.makeText(requireContext(), "¡Planta guardada con éxito!", Toast.LENGTH_SHORT).show()
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

    private fun handleSaveAction() {
        val name = binding.etPlantName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etPlantName.error = "Dale un nombre a tu planta"
            return
        }

        val plantEntity = buildPlantEntity("")
        val photoUri = viewModel.selectedPhotoUri
        val photoFile = photoUri?.let { uriToFile(it) }

        viewModel.savePlant(plantEntity, isExploreFlow, photoFile)
    }

    private fun buildPlantEntity(currentImageUrl: String): PlantEntity {
        // Si seleccionamos del catálogo, usamos esos datos como base, pero permitimos editar el nombre
        val basePlant = viewModel.selectedCatalogPlant.value
        
        return PlantEntity(
            nombreComun = binding.etPlantName.text.toString().trim(),
            nombreCientifico = binding.etPlantSpecies.text.toString().trim(),
            tipo = basePlant?.tipo ?: "Planta Capturada",
            descripcion = basePlant?.descripcion ?: "Planta añadida desde cámara",
            cicloVida = basePlant?.cicloVida ?: "Anual",
            nivelCuidado = basePlant?.nivelCuidado ?: "Medio",
            caracteristicas = basePlant?.caracteristicas ?: Caracteristicas(),
            riego = basePlant?.riego ?: Riego(frecuencia = "Moderado", cadaValor = "1-3"),
            wateringFrequencyDays = basePlant?.wateringFrequencyDays ?: 3,
            luzSolar = basePlant?.luzSolar ?: listOf("Sombra Parcial"),
            temperatura = basePlant?.temperatura ?: Temperatura(min = 15, max = 30, descripcion = "Óptima"),
            imagen = currentImageUrl.ifEmpty { basePlant?.imagen ?: "" }
        )
    }

    private fun initCloudinary() {
        try {
            val config = mapOf(
                "cloud_name" to "deqhd3bmp",
                "api_key" to "188973848385489",
                "api_secret" to "bmPFYmcccVKbOhp5g0U6LyHn8aE"
            )
            MediaManager.init(requireContext(), config)
        } catch (e: Exception) {}
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, "temp_img_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun setLoadingState(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.btnSavePlant.isEnabled = !loading
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Salir")
            .setMessage("¿Deseas cancelar el proceso?")
            .setPositiveButton("Sí") { _, _ -> parentFragmentManager.popBackStack() }
            .setNegativeButton("No", null)
            .show()
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
                viewModel.setInitialPlant(entity) // Sincronizar con el ViewModel

                binding.apply {
                    etPlantName.setText(entity.nombreComun)
                    etPlantSpecies.setText(entity.nombreCientifico)
                    // Mostrar el formulario directamente si ya tenemos los datos
                    cameraLayer.isVisible = false
                    formLayer.isVisible = true
                    formLayer.alpha = 1f
                    formLayer.translationY = 0f
                    imgPlantPhoto.setImageURI(null) // O cargar desde URL si es necesario
                }
            }
        }
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