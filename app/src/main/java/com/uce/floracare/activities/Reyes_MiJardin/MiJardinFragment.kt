// app/src/main/java/com/uce/floracare/activities/MiJardinFragment.kt
package com.uce.floracare.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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

        // 3. Mock Data de plantas (con el nuevo modelo de Osorio_Explore)
        val plantList = listOf(
            Plant(1, "Aloe Vera", "Aloe barbadensis", "Suculenta", "Sol directo", "Bajo", android.R.drawable.sym_def_app_icon, true, true, "https://images.unsplash.com/photo-1596547613931-98b64093990e?w=500&auto=format&fit=crop&q=80"),
            Plant(2, "Lirio de Paz", "Spathiphyllum", "Interior", "Sombra", "Medio", android.R.drawable.sym_def_app_icon, false, false, "https://images.unsplash.com/photo-1593696954577-ab3d39317b97?w=500&auto=format&fit=crop&q=80"),
            Plant(3, "Sansevieria", "Sansevieria trifasciata", "Rústica", "Semisombra", "Muy bajo", android.R.drawable.sym_def_app_icon, true, true, "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?w=500&auto=format&fit=crop&q=80"),
            Plant(4, "Costilla de Adán", "Monstera deliciosa", "Tropical", "Luz indirecta", "Medio", android.R.drawable.sym_def_app_icon, false, false, "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?w=500&auto=format&fit=crop&q=80")
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