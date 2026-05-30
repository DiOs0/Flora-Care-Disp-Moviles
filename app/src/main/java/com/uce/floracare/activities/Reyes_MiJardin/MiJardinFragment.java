package com.uce.floracare.activities.Reyes_MiJardin;

import static androidx.appcompat.widget.ResourceManagerInternal.get;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.uce.floracare.R
import com.uce.floracare.databinding.ActivityMiJardinBinding
import com.uce.floracare.activities.adapters.PlantAdapter
import com.uce.floracare.activities.models.Plant

class MiJardinFragment : Fragment() {

    private var _binding: ActivityMiJardinBinding? = null
    private val binding get() = _binding!!

            override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = ActivityMiJardinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar el adaptador usando la expresión lambda para clics
        val adapter = PlantAdapter { clickedPlant ->
            // Aquí puedes ejecutar acciones (ej: abrir una ventana de detalles)
        }

        // 2. Configurar el RecyclerView con el Grid de 2 columnas y enlazar el adaptador
        binding.rvPlants.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvPlants.adapter = adapter

        // 3. Crear los datos de prueba para alimentar tu jardín
        val plantList = listOf(
                Plant(1, "Aloe Vera", "Aloe barbadensis", "https://images.unsplash.com/photo-1596547613931-98b64093990e?w=500&auto=format&fit=crop&q=80", true),
                Plant(2, "Lirio de Paz", "Spathiphyllum", "https://images.unsplash.com/photo-1593696954577-ab3d39317b97?w=500&auto=format&fit=crop&q=80", false),
                Plant(3, "Sansevieria", "Sansevieria trifasciata", "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?w=500&auto=format&fit=crop&q=80", true),
                Plant(4, "Costilla de Adán", "Monstera deliciosa", "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?w=500&auto=format&fit=crop&q=80", false)
        )

        // 4. Enviar los datos de forma optimizada
        adapter.submitList(plantList)

        // 5. Calcular de forma dinámica cuántas plantas necesitan atención urgente
        val tasksPending = plantList.filter { it.needsWater }.size
        binding.badgeRemaining.text = "$tasksPending restantes"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
