package com.uce.floracare.activities.Reyes_MiJardin;

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uce.floracare.R
import com.uce.floracare.activities.Osorio_Explore.ExploreAdapter;
import com.uce.floracare.databinding.ItemPlantCardBinding
import com.uce.floracare.activities.Osorio_Explore.Plant

import kotlin.Unit;

class PlantAdapter(
        private val onPlantClick: (Plant) -> Unit
) : ListAdapter<Plant, PlantAdapter.PlantViewHolder>(ExploreAdapter.DiffCallback) {

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

        // Asignar el nombre y especie de la planta
        binding.txtPlantName.text = plant.name
        binding.txtPlantSpecies.text = plant.species.uppercase()

        // Cargar imagen de forma remota usando Glide (con placeholder de carga)
        Glide.with(context)
                .load(plant.imageUrl)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .error(android.R.drawable.sym_def_app_icon)
                .into(binding.imgPlantPhoto)

        // Estilizar el badge de alerta (Rojo si falta regar, Verde si está bien)
        if (plant.needsWater) {
            binding.badgeBackground.background = ContextCompat.getDrawable(context, R.drawable.circle_alert_red)
            binding.badgeIcon.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.stat_notify_chat))
            binding.badgeIcon.imageTintList = ContextCompat.getColorStateList(context, android.R.color.white)
        } else {
            binding.badgeBackground.background = ContextCompat.getDrawable(context, R.drawable.badge_rounded_green)
            binding.badgeIcon.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.checkbox_on_background))
            binding.badgeIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.forest_green)
        }

        root.setOnClickListener {
            onPlantClick(plant)
        }
    }
}

// El DiffCallback calcula de forma inteligente qué elementos cambiaron
private object DiffCallback : DiffUtil.ItemCallback<Plant>() {
override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem.id == newItem.id

override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem == newItem
    }
            }
