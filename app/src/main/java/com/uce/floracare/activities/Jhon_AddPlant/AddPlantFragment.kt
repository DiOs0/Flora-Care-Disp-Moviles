package com.uce.floracare.activities.Jhon_AddPlant

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Camera
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uce.floracare.databinding.FragmentAddPlantBinding
import java.util.Calendar


class AddPlantFragment : Fragment() {

    // === METODOS Osorio_Explore - INICIO ===
    companion object {
        private const val ARG_PLANT_NAME = "plant_name_osorio"
        fun newInstance(plantName: String? = null): AddPlantFragment {
            val fragment = AddPlantFragment()
            val args = Bundle()
            args.putString(ARG_PLANT_NAME, plantName)
            fragment.arguments = args
            return fragment
        }
    }
    // === METODOS Osorio_Explore - FIN ===

    private var _binding : FragmentAddPlantBinding? = null
    // Se pone '!!' para que no sea NULO
    private val binding get() = _binding!!

    // Variable para guardar la ubicacion seleccionada temporalmente
    private var selectedLocation : String = "Salon"


    private lateinit var cameraManager : CameraManager


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

        // === METODOS Osorio_Explore - INICIO ===
        arguments?.getString(ARG_PLANT_NAME)?.let { name ->
            binding.etPlantName.setText(name)
        }
        // === METODOS Osorio_Explore - FIN ===

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

        cameraManager = CameraManager(this) { uri ->
            if (uri != null) {
                binding.imgPlantPhoto.setImageURI(uri) // Mostramos la foto
            } else {
                Toast.makeText(requireContext(), "Captura cancelada", Toast.LENGTH_SHORT).show()
            }
        }


        // CALENDARIO
        binding.etLastWatered.setOnClickListener {
            showDatePickerDialog()
        }

        // BOTON DE LA CAMARA
        binding.btnCapturePhoto.setOnClickListener {
            cameraManager.openCamera(requireContext())
        }

        binding.imgPlantPhoto.setOnClickListener {
            cameraManager.openCamera(requireContext())
        }

        // SELECCION DE UBICACION
        binding.btnLocSalon.setOnClickListener {
            selectedLocation = "Salón"
            Toast.makeText(requireContext(), "Ubicación: Salón", Toast.LENGTH_SHORT).show()
            updateLocationSelection(it)
        }
        binding.btnLocTerraza.setOnClickListener {
            selectedLocation = "Terraza"
            Toast.makeText(requireContext(), "Ubicación: Terraza", Toast.LENGTH_SHORT).show()
            updateLocationSelection(it)
        }
        binding.btnLocCocina.setOnClickListener {
            selectedLocation = "Cocina"
            Toast.makeText(requireContext(), "Ubicación: Cocina", Toast.LENGTH_SHORT).show()
            updateLocationSelection(it)
        }

        binding.btnLocDormitorio.setOnClickListener {
            selectedLocation = "Dormitorio"
            Toast.makeText(requireContext(), "Ubicación: Dormitorio", Toast.LENGTH_SHORT).show()
            updateLocationSelection(it)
        }

        binding.btnSavePlant.setOnClickListener {
            savePlantData()
        }
    }


    /*
    METODO PARA INGRESAR A LA CAMARA
     */




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

        // Crear el objeto (Aquí luego usarás tu DTO real en lugar de un Toast)
        val summary = """
            Planta: $plantName
            Especie: $plantSpecies
            Ubicación: $selectedLocation
            Último Riego: $lastWatered
        """.trimIndent()

        Toast.makeText(requireContext(), "Planta Guardada:\n$summary", Toast.LENGTH_LONG).show()

        // TODO: Enviar el DTO a la base de datos o ViewModel

        // TODO: Navegar hacia atrás usando Jetpack Navigation
        // findNavController().popBackStack()
    }



    private fun updateLocationSelection(view: View) {
        val buttons = listOf(
            binding.btnLocSalon,
            binding.btnLocTerraza,
            binding.btnLocCocina,
            binding.btnLocDormitorio
        )

        buttons.forEach { button ->
            val isSelected = button.id == view.id
            button.strokeWidth = if (isSelected) 2 else 0
            button.backgroundTintList = android.content.res.ColorStateList.valueOf(
                if (isSelected) android.graphics.Color.parseColor("#f3f4ed") else android.graphics.Color.WHITE
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}