package com.uce.floracare.activities.Jhon_AddPlant

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uce.floracare.api_ingreso.data.StorageManager
import com.uce.floracare.databinding.FragmentAddPlantBinding
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible


class AddPlantFragment : Fragment() {


    private var _binding: FragmentAddPlantBinding? = null
    private val binding get() = _binding!!

    // Instanciamos nuestro CameraManager
    private lateinit var cameraManager: CameraManager
    private lateinit var storageManager: StorageManager

    // Variable para guardar temporalmente la foto capturada
    private var photoUri: Uri? = null

    private var selectedLocation: String = "Interior"


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

        // Recuperar datos pasados desde ExploreFragment
        arguments?.let {
            val name = it.getString(ARG_PLANT_NAME)
            val species = it.getString(ARG_PLANT_SPECIES)
            val isIndoor = it.getBoolean(ARG_IS_INDOOR, true) // Por defecto true
            
            if (!name.isNullOrBlank()) binding.etPlantName.setText(name)
            if (!species.isNullOrBlank()) binding.etPlantSpecies.setText(species)
            
            selectedLocation = if (isIndoor) "Interior" else "Exterior"
        }

        // Inicializamos el manager
        cameraManager = CameraManager(requireContext())
        storageManager = StorageManager(requireContext())

        // Inicializar el estado visual de los botones de ubicación según lo seleccionado
        updateLocationSelection(if (selectedLocation == "Interior") binding.btnInterior else binding.btnExterior)

        initListeners()
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



        // CALENDARIO
        binding.etLastWatered.setOnClickListener {
            showDatePickerDialog()
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

        // SELECCION DE UBICACION
        binding.btnInterior.setOnClickListener {
            selectedLocation = "Interior"
            Toast.makeText(requireContext(), "Ubicación: Interior", Toast.LENGTH_SHORT).show()
            updateLocationSelection(it)
        }
        binding.btnExterior.setOnClickListener {
            selectedLocation = "Exterior"
            Toast.makeText(requireContext(), "Ubicación: Exterior", Toast.LENGTH_SHORT).show()
            updateLocationSelection(it)
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
                photoUri = uri
                Toast.makeText(requireContext(), "Foto capturada con éxito", Toast.LENGTH_SHORT).show()
            },
            onError = { _ ->
                Toast.makeText(requireContext(), "Error al tomar foto", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Los meses en Calendar empiezan en 0, así que sumamos 1
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.etLastWatered.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun savePlantData() {
        // Recolectar datos de las vistas
        val plantName = binding.etPlantName.text.toString().trim()
        val plantSpecies = binding.etPlantSpecies.text.toString().trim()
        val lastWatered = binding.etLastWatered.text.toString().trim()

        // Validaciones súper básicas
        if (plantName.isEmpty()) {
            binding.etPlantName.error = "Dale un nombre a tu planta"
            return
        }

        if (plantSpecies.isEmpty()) {
            binding.etPlantSpecies.error = "Ingresa la especie"
            return
        }

        if (photoUri == null) {
            Toast.makeText(requireContext(), "Por favor, captura una foto de la planta", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar cargando y desactivar botón
        setLoadingState(true)

        lifecycleScope.launch {
            // 1. Subir imagen a Firebase Storage
            val uploadResult = storageManager.uploadUserPlantImage(photoUri!!)

            uploadResult.fold(
                onSuccess = { downloadUrl ->
                    // 2. Imagen subida con éxito
                    setLoadingState(false)
                    
                    val summary = """
                        Planta: $plantName
                        Especie: $plantSpecies
                        Ubicación: $selectedLocation
                        Último Riego: $lastWatered
                        URL Imagen: $downloadUrl
                    """.trimIndent()

                    Toast.makeText(requireContext(), "Planta Guardada y Foto Subida:\n$summary", Toast.LENGTH_LONG).show()
                    
                    // TODO: Guardar en Firestore usando el downloadUrl
                    // TODO: findNavController().popBackStack()
                },
                onFailure = { error ->
                    // 3. Error en la subida
                    setLoadingState(false)
                    val errorMessage = "Error al subir la imagen: ${error.localizedMessage ?: error.message}"
                    Log.e("AddPlantFragment", errorMessage, error)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun setLoadingState(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.btnSavePlant.isEnabled = !loading
        binding.btnCapturePhoto.isEnabled = !loading
    }



    private fun updateLocationSelection(view: View) {
        val buttons = listOf(
            binding.btnInterior,
            binding.btnExterior
        )

        buttons.forEach { button ->
            val isSelected = button.id == view.id

            button.strokeWidth = if (isSelected) 2 else 0
            button.strokeColor = android.content.res.ColorStateList.valueOf("#2D5A27".toColorInt())
            button.backgroundTintList = android.content.res.ColorStateList.valueOf(
                if (isSelected) "#f3f4ed".toColorInt() else Color.WHITE
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PLANT_NAME = "plant_name"
        private const val ARG_PLANT_SPECIES = "plant_species"
        private const val ARG_IS_INDOOR = "is_indoor"

        fun newInstance(name: String, species: String, isIndoor: Boolean): AddPlantFragment {
            val fragment = AddPlantFragment()
            val args = Bundle()
            args.putString(ARG_PLANT_NAME, name)
            args.putString(ARG_PLANT_SPECIES, species)
            args.putBoolean(ARG_IS_INDOOR, isIndoor)
            fragment.arguments = args
            return fragment
        }
    }
}