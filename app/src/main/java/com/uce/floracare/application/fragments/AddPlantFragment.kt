package com.uce.floracare.application.fragments

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cloudinary.android.MediaManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.uce.floracare.application.viewmodels.AddPlantViewModel
import com.uce.floracare.utils.CameraManager
import com.uce.floracare.application.activities.Login
import com.uce.floracare.data.remote.dto.Caracteristicas
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.data.remote.dto.Riego
import com.uce.floracare.data.remote.dto.Temperatura
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import com.uce.floracare.databinding.FragmentAddPlantBinding
import com.uce.floracare.repositories.connections.remote.cloudinary.CloudinaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import kotlin.coroutines.resume

class AddPlantFragment : Fragment() {


    private var _binding: FragmentAddPlantBinding? = null
    private val binding get() = _binding!!

    // Instanciamos nuestro CameraManager y ViewModel
    private lateinit var cameraManager: CameraManager

    // Variables para detectar el flujo "Explorar" y rastrear cambios
    private var isExploreFlow: Boolean = false
    private var originalPlantName: String? = null
    private var originalPlantImage: String? = null
    private var originalTempDescription: String? = null

    private var db = Firebase.firestore

    private val viewModel: AddPlantViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val authManager = AuthManager()
                val repository =
                    PlantRepository(FirestoreManager(authManager), StorageManager(requireContext()))
                return AddPlantViewModel(repository) as T
            }
        }
    }


    // Registramos el contrato para pedir permisos de cámara
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            binding.viewFinder.isVisible = true
            cameraManager.startCamera(viewLifecycleOwner, binding.viewFinder)
        } else {
            Toast.makeText(requireContext(), "Se requiere permiso de cámara para registrar la planta", Toast.LENGTH_SHORT).show()
        }
    }




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraManager = CameraManager(requireContext())

        // Inicializar credenciales de Cloudinary
        initVariables()

        setupDropdowns()
        setupObservers()
        initListeners()
        // Recuperar datos pasados desde ExploreFragment (después de setupDropdowns para AutoCompleteTextView)
        populateFormFromArguments()
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

                binding.etPlantName.setText(entity.nombreComun)
                binding.etPlantSpecies.setText(entity.nombreCientifico)
                if (entity.tipo.isNotBlank()) binding.autoCompleteType.setText(entity.tipo, false)
                binding.etDescription.setText(entity.descripcion)
                if (entity.cicloVida.isNotBlank()) binding.autoCompleteCycle.setText(entity.cicloVida, false)
                if (entity.nivelCuidado.isNotBlank()) binding.autoCompleteCareLevel.setText(entity.nivelCuidado, false)
                binding.etWateringFreq.setText(entity.riego.frecuencia)
                binding.etWateringValue.setText(entity.riego.cadaValor)

                binding.chipGroupSunlight.clearCheck()
                entity.luzSolar.forEach { luz ->
                    when (luz.lowercase().trim()) {
                        "sol directo", "full sun" -> binding.solDirecto.isChecked = true
                        "sombra parcial", "part sun", "part shade", "partial sun", "partial shade", "filtered shade" -> binding.sombraParcial.isChecked = true
                        "sombra", "shade", "deep shade" -> binding.sombra.isChecked = true
                    }
                }

                binding.etTempMin.setText(entity.temperatura.min.toString())
                binding.etTempMax.setText(entity.temperatura.max.toString())
                originalTempDescription = entity.temperatura.descripcion

                binding.chipMedicinal.isChecked = entity.caracteristicas.medicinal
                binding.chipIndoor.isChecked = entity.caracteristicas.indoor
                binding.chipTropical.isChecked = entity.caracteristicas.tropical
                binding.chipDrought.isChecked = entity.caracteristicas.resistenteSequia
                binding.chipToxicHumans.isChecked = entity.caracteristicas.toxicaHumanos
                binding.chipToxicPets.isChecked = entity.caracteristicas.toxicaMascotas

                viewModel.selectedLocation = if (entity.caracteristicas.indoor) "Interior" else "Exterior"
            } else {
                val name = it.getString(ARG_PLANT_NAME)
                val species = it.getString(ARG_PLANT_SPECIES)
                val isIndoor = it.getBoolean(ARG_IS_INDOOR, true)
                val imageUrl = it.getString(ARG_PLANT_IMAGE)

                if (!name.isNullOrBlank()) binding.etPlantName.setText(name)
                if (!species.isNullOrBlank()) binding.etPlantSpecies.setText(species)

                if (imageUrl != null) {
                    isExploreFlow = true
                    originalPlantName = name
                    originalPlantImage = imageUrl
                }

                viewModel.selectedLocation = if (isIndoor) "Interior" else "Exterior"
            }
        }
    }

    private fun setupDropdowns() {
        val types = arrayOf("Suculenta", "Cactus", "Follaje", "Flor", "Arbusto", "Árbol")
        val cycles = arrayOf("Perenne", "Anual", "Bienal")
        val careLevels = arrayOf("Bajo", "Medio", "Alto")

        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.autoCompleteType.setAdapter(typeAdapter)

        val cycleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cycles)
        binding.autoCompleteCycle.setAdapter(cycleAdapter)

        val careAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, careLevels)
        binding.autoCompleteCareLevel.setAdapter(careAdapter)
    }

    private fun setupObservers() {
        // Observamos el estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            setLoadingState(isLoading)
        }

        // Observamos si el guardado fue exitoso
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "¡Planta guardada con éxito!", Toast.LENGTH_SHORT).show()
                // Regresar a la pantalla anterior o limpiar formulario
                parentFragmentManager.popBackStack()
            }
        }

        // Observamos mensajes de error
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }



    private fun initListeners() {

        // cerrar app

        binding.closeFromAddPlant.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar sesion")
                .setMessage("¿Esta seguro de salir de la aplicacion?")
                .setCancelable(true)
                .setPositiveButton("Si"){
                        dialog , id ->
                    val intent = Intent(requireContext(), Login::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("No"){
                        dialog, id -> dialog.dismiss()
                }.setNeutralButton("Cancelar"){
                        dialog, id -> dialog.dismiss()
                }
                .show()
        }





        // BOTON DE LA CAMARA
        binding.btnCapturePhoto.setOnClickListener {
            if (binding.viewFinder.isVisible) {
                capturePhoto()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.imgPlantPhoto.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }


        binding.btnSavePlant.setOnClickListener {
            savePlantData()
        }
    }


    /*
    METODO PARA INGRESAR A LA CAMARA
     */
    
    private fun capturePhoto() {
        cameraManager.takePhoto(
            onPhotoSaved = { uri ->
                binding.viewFinder.isVisible = false
                binding.imgPlantPhoto.setImageURI(uri)
                viewModel.selectedPhotoUri = uri
                Toast.makeText(requireContext(), "Foto capturada con éxito", Toast.LENGTH_SHORT).show()
            },
            onError = { _ ->
                Toast.makeText(requireContext(), "Error al tomar foto", Toast.LENGTH_SHORT).show()
            }
        )
    }



    private fun savePlantData() {
        val photoUri = viewModel.selectedPhotoUri
        
        // Si no hay foto nueva y no venimos de "Explorar", es error
        if (photoUri == null && !isExploreFlow) {
            Toast.makeText(requireContext(), "Debes capturar una foto", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            setLoadingState(true)

            val finalImageUrl: String? = if (photoUri != null) {
                // Hay una foto nueva, subirla a Cloudinary
                subirImagenYObtenerUrl(photoUri)
            } else {
                // No hay foto nueva, mantener la original (si existe)
                originalPlantImage
            }

            if (finalImageUrl != null) {
                val plantEntity = buildPlantEntity(finalImageUrl)

                // Flujo "Explorar": verificar si hubo cambios reales
                if (isExploreFlow) {
                    val currentName = binding.etPlantName.text.toString().trim()
                    val nameUnchanged = currentName == originalPlantName
                    val photoUnchanged = photoUri == null

                    if (nameUnchanged && photoUnchanged) {
                        setLoadingState(false)
                        showUnsavedAlert(plantEntity)
                        return@launch
                    }
                }

                viewModel.savePlant(plantEntity)
            } else {
                setLoadingState(false)
                Toast.makeText(requireContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun subirImagenYObtenerUrl(uri: Uri): String? = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            // Convertimos Uri a File para usar el CloudinaryService existente
            val file = uriToFile(uri)
            if (file != null && file.exists()) {
                CloudinaryService.subirImagenFirmada(file) { success, result ->
                    if (success) {
                        continuation.resume(result) // result es la URL
                    } else {
                        continuation.resume(null)
                    }
                }
            } else {
                continuation.resume(null)
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        // En este proyecto, CameraManager guarda en context.externalCacheDir
        // Intentamos obtener el archivo directamente si es posible
        return try {
            val fileName = uri.lastPathSegment ?: "temp_img.jpg"
            File(requireContext().externalCacheDir, fileName)
        } catch (e: Exception) {
            null
        }
    }

    private fun buildPlantEntity(imageUrl: String): PlantEntity {
        val plantName = binding.etPlantName.text.toString().trim()
        val plantSpecies = binding.etPlantSpecies.text.toString().trim()
        val type = binding.autoCompleteType.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val cycle = binding.autoCompleteCycle.text.toString().trim()
        val careLevel = binding.autoCompleteCareLevel.text.toString().trim()
        val wateringFreq = binding.etWateringFreq.text.toString().trim()
        val wateringValue = binding.etWateringValue.text.toString().trim()

        val sunlight = binding.chipGroupSunlight.checkedChipIds.mapNotNull { id ->
            when (id) {
                binding.solDirecto.id -> "Sol Directo"
                binding.sombraParcial.id -> "Sombra Parcial"
                binding.sombra.id -> "Sombra"
                else -> null
            }
        }

        val tempMin = binding.etTempMin.text.toString().trim().toIntOrNull() ?: 0
        val tempMax = binding.etTempMax.text.toString().trim().toIntOrNull() ?: 0

        val caracteristicas = Caracteristicas(
            medicinal = binding.chipMedicinal.isChecked,
            indoor = binding.chipIndoor.isChecked,
            tropical = binding.chipTropical.isChecked,
            resistenteSequia = binding.chipDrought.isChecked,
            toxicaHumanos = binding.chipToxicHumans.isChecked,
            toxicaMascotas = binding.chipToxicPets.isChecked
        )

        return PlantEntity(
            nombreComun = plantName,
            nombreCientifico = plantSpecies,
            tipo = type,
            descripcion = description,
            cicloVida = cycle,
            nivelCuidado = careLevel,
            caracteristicas = caracteristicas,
            riego = Riego(frecuencia = wateringFreq, cadaValor = wateringValue),
            luzSolar = sunlight,
            temperatura = Temperatura(min = tempMin, max = tempMax, descripcion = originalTempDescription ?: "Personalizada"),
            imagen = imageUrl
        )
    }

    private fun showUnsavedAlert(plantEntity: PlantEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sin cambios detectados")
            .setMessage("No has modificado el nombre común ni has tomado una nueva foto. ¿Deseas guardar la planta de todas formas?")
            .setPositiveButton("Sí, guardar") { _, _ ->
                viewModel.savePlant(plantEntity, fromExplore = true)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setLoadingState(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.btnSavePlant.isEnabled = !loading
        binding.btnCapturePhoto.isEnabled = !loading
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PLANT_NAME = "plant_name"
        private const val ARG_PLANT_SPECIES = "plant_species"
        private const val ARG_IS_INDOOR = "is_indoor"
        private const val ARG_PLANT_IMAGE = "plant_image"
        private const val ARG_PLANT_ENTITY = "plant_entity"

        fun newInstance(name: String, species: String, isIndoor: Boolean, imageUrl: String? = null, plantEntity: PlantEntity? = null): AddPlantFragment {
            val fragment = AddPlantFragment()
            val args = Bundle()
            args.putString(ARG_PLANT_NAME, name)
            args.putString(ARG_PLANT_SPECIES, species)
            args.putBoolean(ARG_IS_INDOOR, isIndoor)
            args.putString(ARG_PLANT_IMAGE, imageUrl)
            if (plantEntity != null) {
                args.putSerializable(ARG_PLANT_ENTITY, plantEntity)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private fun subirImagen(){
        val archivoCache = File(requireContext().cacheDir, "img_temp.jpg")

        CloudinaryService.subirImagenFirmada(archivoCache){
                isExitoso, resultado ->

            lifecycleScope.launch(Dispatchers.IO) {
                val resultText = if(isExitoso){
                    "La imagen se subio correctamente en ${resultado}"
                }else{
                    "Error: ${resultado}"
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        resultText,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    private fun initVariables() {
        db = Firebase.firestore
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
}