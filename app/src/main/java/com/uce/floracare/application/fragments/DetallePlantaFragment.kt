package com.uce.floracare.application.fragments

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.uce.floracare.R
import com.uce.floracare.application.activities.MainActivity
import com.uce.floracare.application.viewmodels.PlantDetailUiState
import com.uce.floracare.application.viewmodels.PlantDetailViewModel
import com.uce.floracare.application.viewmodels.ViewModelFactory
import com.uce.floracare.data.local.database.FloraCareDatabase
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.databinding.FragmentDetallePlantaBinding
import com.uce.floracare.domain.usecase.ActualizarPlantaUsuarioUseCase
import com.uce.floracare.domain.usecase.EliminarPlantaUsuarioUseCase
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.TaskRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import kotlinx.coroutines.launch

class DetallePlantaFragment : Fragment() {

    private var _binding: FragmentDetallePlantaBinding? = null
    private val binding get() = _binding!!

    private lateinit var plant: PlantEntity

    private val viewModel: PlantDetailViewModel by viewModels {
        ViewModelFactory {
            val authManager = AuthManager()
            val firestoreManager = FirestoreManager(authManager)
            val database = FloraCareDatabase.getDatabase(requireContext())

            val taskRepository = TaskRepository(
                firestoreManager,
                authManager,
                database.taskDao()
            )

            val plantRepository = PlantRepository(
                firestoreManager,
                StorageManager(requireContext()),
                authManager,
                database.plantDao(),
                taskRepository
            )

            PlantDetailViewModel(
                eliminarPlantaUsuarioUseCase = EliminarPlantaUsuarioUseCase(plantRepository),
                actualizarPlantaUsuarioUseCase = ActualizarPlantaUsuarioUseCase(plantRepository)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetallePlantaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        plant = arguments?.getSerializable("plant") as PlantEntity

        loadData()
        configurarEventos()
        setupObservers()
    }

    private fun loadData() {
        binding.txtNombre.text = plant.nombreComun
        binding.txtNombreCientifico.text = plant.nombreCientifico

        Glide.with(requireContext())
            .load(plant.imagen)
            .into(binding.imgPlant)

        binding.txtTipo.text = "Tipo: ${plant.tipo.ifEmpty { "No disponible" }}"
        binding.txtDescripcion.text = plant.descripcion.ifEmpty { "Sin descripción" }

        cargarCuidados()
        cargarCaracteristicas()
    }

    private fun cargarCuidados() {
        val container = binding.layoutCuidadosItems
        container.removeAllViews()

        val row1 = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        val row2 = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        val diasRestantes = calcularDiasRestantes()
        val textRiego = when {
            diasRestantes < 0L -> "¡Hoy!"
            diasRestantes == 0L -> "¡Hoy!"
            diasRestantes == 1L -> "1 día"
            else -> "$diasRestantes días"
        }

        row1.addView(crearIndicator(
            icono = R.drawable.rain,
            titulo = "RIEGO",
            valor = textRiego,
            sub = "Cada ${plant.wateringFrequencyDays} días",
            parentRow = row1
        ))

        val luzValue = plant.luzSolar.firstOrNull() ?: ""
        val sunsLayout = crearSoles(luzValue)
        row1.addView(crearIndicator(
            icono = R.drawable.sun,
            titulo = "LUZ",
            sunsLayout = sunsLayout,
            sub = luzValue,
            parentRow = row1
        ))

        val tempText = "${plant.temperatura.min}° - ${plant.temperatura.max}°"
        val tempBg = colorParaTemperatura(plant.temperatura.min, plant.temperatura.max)
        row2.addView(crearIndicator(
            icono = R.drawable.termometro,
            titulo = "TEMPERATURA",
            valor = tempText,
            sub = plant.temperatura.descripcion,
            backgroundColor = tempBg,
            parentRow = row2
        ))

        row2.addView(crearIndicator(
            icono = R.drawable.calendar,
            titulo = "CICLO",
            valor = plant.cicloVida.ifEmpty { "—" },
            sub = plant.tipo.ifEmpty { "" },
            parentRow = row2
        ))

        container.addView(row1)
        container.addView(row2)
        container.addView(crearBarraCuidado())
    }

    private fun colorParaTemperatura(min: Int, max: Int): Int {
        val avg = (min + max) / 2
        return when {
            avg <= 7 -> R.color.water_blue
            avg <= 12 -> R.color.care_low_bg
            avg <= 20 -> R.color.care_medium_bg
            avg <= 28 -> R.color.care_high_bg
            else -> R.color.alert_red
        }
    }

    private fun calcularDiasRestantes(): Long {
        val hoy = System.currentTimeMillis()
        val proximoRiego = plant.ultimoRiego + (plant.wateringFrequencyDays.toLong() * 86400000L)
        val diff = proximoRiego - hoy
        return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff)
    }

    private fun crearIndicator(
        icono: Int,
        titulo: String,
        valor: String? = null,
        sub: String? = null,
        sunsLayout: LinearLayout? = null,
        backgroundColor: Int? = null,
        parentRow: LinearLayout? = null
    ): View {
        val inflater = LayoutInflater.from(requireContext())
        val card = inflater.inflate(R.layout.item_cuidado_indicator, parentRow, false) as com.google.android.material.card.MaterialCardView
        card.findViewById<ImageView>(R.id.imgIndicatorIcon).setImageResource(icono)
        card.findViewById<TextView>(R.id.txtIndicatorTitle).text = titulo

        if (backgroundColor != null) {
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColor))
        }

        val txtValor = card.findViewById<TextView>(R.id.txtIndicatorValue)
        val txtSub = card.findViewById<TextView>(R.id.txtIndicatorSub)
        val layoutSuns = card.findViewById<LinearLayout>(R.id.layoutIndicatorSuns)

        if (sunsLayout != null) {
            txtValor.visibility = View.GONE
            layoutSuns.visibility = View.VISIBLE
            layoutSuns.removeAllViews()
            for (i in 0 until sunsLayout.childCount) {
                val sun = ImageView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(20, 20)
                    setImageResource(R.drawable.sun)
                }
                layoutSuns.addView(sun)
            }
        } else {
            txtValor.text = valor ?: "—"
            layoutSuns.visibility = View.GONE
        }

        txtSub.text = sub ?: ""
        txtSub.visibility = if (sub.isNullOrEmpty()) View.GONE else View.VISIBLE

        return card
    }

    private fun crearSoles(luz: String): LinearLayout? {
        val count = when {
            luz.contains("Sol Directo", ignoreCase = true) -> 3
            luz.contains("Sombra Parcial", ignoreCase = true) -> 2
            luz.contains("Sombra", ignoreCase = true) -> 1
            luz.contains("full sun", ignoreCase = true) -> 3
            luz.contains("part", ignoreCase = true) -> 2
            luz.contains("shade", ignoreCase = true) -> 1
            luz.isBlank() -> 0
            else -> 2
        }
        if (count == 0) return null
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        for (i in 0 until count) {
            layout.addView(ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(20, 20)
                setImageResource(R.drawable.sun)
            })
        }
        return layout
    }

    private fun crearBarraCuidado(): View {
        val nivel = plant.nivelCuidado.lowercase()
        val (nivelLabel, nivelColor, nivelesLlenos) = when {
            nivel.contains("alto") || nivel.contains("high") -> Triple("Avanzado", R.color.care_high, 3)
            nivel.contains("medio") || nivel.contains("medium") || nivel.contains("moderate") -> Triple("Medio", R.color.care_medium, 2)
            nivel.contains("bajo") || nivel.contains("low") -> Triple("Fácil", R.color.care_low, 1)
            else -> Triple(plant.nivelCuidado.ifEmpty { "—" }, R.color.primary_green, 1)
        }

        val root = LayoutInflater.from(requireContext()).inflate(R.layout.item_cuidado_indicator, null) as com.google.android.material.card.MaterialCardView

        root.findViewById<ImageView>(R.id.imgIndicatorIcon).visibility = View.GONE
        root.findViewById<TextView>(R.id.txtIndicatorTitle).text = "NIVEL DE CUIDADO"
        root.findViewById<TextView>(R.id.txtIndicatorValue).text = nivelLabel
        root.findViewById<TextView>(R.id.txtIndicatorValue).setTextColor(
            ContextCompat.getColor(requireContext(), nivelColor)
        )
        root.findViewById<TextView>(R.id.txtIndicatorSub).visibility = View.GONE

        val barraContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10, 0, 0)
        }

        val segmentColor = ContextCompat.getColor(requireContext(), nivelColor)
        val emptyColor = ContextCompat.getColor(requireContext(), R.color.gris_linea)

        for (i in 0 until 3) {
            val segment = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, 8, 1f).apply {
                    setMargins(3, 0, 3, 0)
                }
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 4f
                    setColor(if (i < nivelesLlenos) segmentColor else emptyColor)
                }
                background = bg
            }
            barraContainer.addView(segment)
        }

        val labelsRow = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(3, 4, 3, 0)
        }

        val labelColor = ContextCompat.getColor(requireContext(), R.color.gris_natural)
        listOf("Fácil", "Medio", "Avanzado").forEach { label ->
            labelsRow.addView(TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = label
                textSize = 10f
                setTextColor(labelColor)
                gravity = android.view.Gravity.CENTER
            })
        }

        val innerLayout = root.findViewById<LinearLayout>(R.id.indicatorInner)
        innerLayout.addView(barraContainer)
        innerLayout.addView(labelsRow)

        return root
    }

    private fun cargarCaracteristicas() {
        val chips = mutableListOf<Pair<String, Int>>()

        chips.add(if (plant.caracteristicas.indoor) "Interior" to R.drawable.interior else "Exterior" to R.drawable.exterior)
        if (plant.caracteristicas.tropical) chips.add("Tropical" to R.drawable.tropical)
        if (plant.caracteristicas.medicinal) chips.add("Medicinal" to R.drawable.medicine_plant)
        if (plant.caracteristicas.resistenteSequia) chips.add("Resistente sequía" to R.drawable.sequia)
        if (plant.caracteristicas.toxicaHumanos) chips.add("Tóxico humanos" to R.drawable.humans_bad)
        if (plant.caracteristicas.toxicaMascotas) chips.add("Tóxico mascotas" to R.drawable.no_dog)

        val container = binding.layoutCaracteristicasItems
        container.removeAllViews()

        if (chips.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "Ninguna"
                setTextColor(resources.getColor(R.color.gris_natural, null))
                textSize = 16f
            }
            container.addView(tv)
            return
        }

        val chipsPorFila = 2
        chips.chunked(chipsPorFila).forEach { fila ->
            val row = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
            }

            fila.forEach { (label, iconRes) ->
                val chip = layoutInflater.inflate(R.layout.item_char_chip, row, false)
                chip.findViewById<ImageView>(R.id.imgCharIcon).setImageResource(iconRes)
                chip.findViewById<TextView>(R.id.txtCharLabel).text = label
                row.addView(chip)
            }

            container.addView(row)
        }
    }

    private fun configurarEventos() {
        binding.btnEditar.setOnClickListener {
            val fragment = EditarPlantaFragment()
            fragment.arguments = arguments
            (activity as MainActivity).loadFragment(fragment)
        }

        binding.btnEliminar.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_delete_plant)

            dialog.findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener {
                dialog.dismiss()
            }

            dialog.findViewById<MaterialButton>(R.id.btnEliminar).setOnClickListener {
                viewModel.eliminarPlanta(plant)
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PlantDetailUiState.Loading -> { /* Show progress */ }
                        is PlantDetailUiState.Success -> {
                            Toast.makeText(requireContext(), "Operación exitosa", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }
                        is PlantDetailUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        is PlantDetailUiState.Idle -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
