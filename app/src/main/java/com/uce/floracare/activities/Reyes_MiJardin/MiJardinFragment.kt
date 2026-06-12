// app/src/main/java/com/uce/floracare/activities/MiJardinFragment.kt
package com.uce.floracare.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.uce.floracare.R
import com.uce.floracare.databinding.ActivityMiJardinBinding
import com.uce.floracare.activities.adapters.PlantAdapter
import com.uce.floracare.activities.Reyes_MiJardin.Plant

class MiJardinFragment : Fragment() {

    private var _binding: ActivityMiJardinBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos el diseño de tu jardín (activity_mi_jardin)
        _binding = ActivityMiJardinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar e integrar el adaptador (usando el patrón ListAdapter)
        val adapter = PlantAdapter { clickedPlant ->
            // Aquí puedes reaccionar cuando el usuario toca una de tus plantas
        }

        // 2. Configurar el RecyclerView con una malla (Grid) de 2 columnas
        binding.rvPlants.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvPlants.adapter = adapter

        // 3. Mock Data de plantas (con tu nuevo modelo real y referencias R.string)
        val plantList = listOf(
            Plant(
                id = 1,
                nombre = "Anturio Blanco",
                nombreCientifico = "Anthurium andraeanum",
                tipo = "Interior",
                luz = "Sombra parcial",
                riego = "Frequent",
                imagenRes = android.R.drawable.sym_def_app_icon,
                esDestacada = true,
                necesitaAgua = true,
                imageUrl = R.drawable.perennial
            ),
            Plant(
                id = 2,
                nombre = "Sansevieria",
                nombreCientifico = "Sansevieria trifasciata",
                tipo = "Rústicas",
                luz = "Semisombra",
                riego = "Rare",
                imagenRes = android.R.drawable.sym_def_app_icon,
                esDestacada = false,
                necesitaAgua = false,
                imageUrl = R.drawable.save
            )
        )

        // 4. Enviar los datos al adaptador usando ListAdapter.submitList()
        adapter.submitList(plantList)

        // 5. Calcular dinámicamente tareas pendientes del jardín usando el campo necesitaAgua
        val tasksPending = plantList.filter { it.necesitaAgua }.size
        binding.badgeRemaining.text = "$tasksPending restantes"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}