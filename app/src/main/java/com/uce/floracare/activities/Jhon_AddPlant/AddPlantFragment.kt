package com.uce.floracare.activities.Jhon_AddPlant

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.uce.floracare.databinding.FragmentAddPlantBinding // Cambia al paquete de tu app
import java.util.Calendar


class AddPlantFragment : Fragment() {

    private var _binding : FragmentAddPlantBinding? = null
    // Se pone '!!' para que no sea NULO
    private val binding get() = _binding!!

    // Variable para guardar la ubicacion seleccionada temporalmente
    private var selectedLocation : String = "Salon"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        initUI()
        initListeners()
    }

    private fun initUI(){}

    private fun initListeners() {

        // CALENDARIO
        binding.etLastWatered.setOnClickListener {
            showDatePickerDialog()
        }

        // BOTON DE LA CAMARA
        binding.btnCapturePhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir camara...", Toast.LENGTH_SHORT)
            // TODO: hacer un Intenet para acceder a la camara
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
        // Aquí se puede agregar lógica para cambiar el estado visual de los botones
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}