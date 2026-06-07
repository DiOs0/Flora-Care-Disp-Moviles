// app/src/main/java/com/uce/floracare/activities/adapters/PlantAdapter.kt
package com.uce.floracare.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uce.floracare.R
import com.uce.floracare.databinding.ItemPlantCardBinding
import com.uce.floracare.activities.Reyes_MiJardin.Plant

class PlantAdapter(
    private val onPlantClick: (Plant) -> Unit
) : ListAdapter<Plant, PlantAdapter.PlantViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val binding = ItemPlantCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlantViewHolder(
        private val binding: ItemPlantCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(plant: Plant) {
            val context = itemView.context

            // Mostrar nombre y especie de la planta adaptados del modelo en español
            binding.txtPlantName.text = plant.nombre
            binding.txtPlantSpecies.text = plant.nombreCientifico.uppercase()

            // Cargar imagen de forma remota o local
            if (plant.imageUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(plant.imageUrl)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(binding.imgPlantPhoto)
            } else {
                binding.imgPlantPhoto.setImageResource(plant.imagenRes)
            }

            // Estilizar dinámicamente el badge de alerta usando necesitaAgua
            if (plant.necesitaAgua) {
                binding.badgeBackground.background = ContextCompat.getDrawable(context, R.drawable.circle_alert_red)
                binding.badgeIcon.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.stat_notify_chat)) // Alerta / Gota
                binding.badgeIcon.imageTintList = ContextCompat.getColorStateList(context, android.R.color.white)
            } else {
                binding.badgeBackground.background = ContextCompat.getDrawable(context, R.drawable.badge_rounded_green)
                binding.badgeIcon.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.checkbox_on_background)) // Check
                binding.badgeIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.forest_green)
            }

            binding.root.setOnClickListener {
                onPlantClick(plant)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem == newItem
    }
}


// MODELO DE DATOS - Guardar como app/src/main/java/com/uce/floracare/activities/Osorio_Explore/Plant.kt
/*
package com.uce.floracare.activities.Osorio_Explore

import androidx.annotation.DrawableRes

data class Plant(
    val id: Int,
    val nombre: String,
    val nombreCientifico: String,
    val tipo: String,
    val luz: String,
    val riego: String,
    @DrawableRes val imagenRes: Int,
    val esDestacada: Boolean,

    //Prueba para MiJardin
    val necesitaAgua : Boolean,
    val imageUrl : String // En tu modelo pusiste R.string; para Glide, usar el tipo String permite pasar links directos a internet.
)
*/