package com.uce.floracare.activities.Osorio_Explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uce.floracare.databinding.ItemCatalogPlantBinding

class ExploreAdapter(
    private val onPlantClick: (Plant) -> Unit
) : ListAdapter<Plant, ExploreAdapter.ExploreViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        val binding = ItemCatalogPlantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExploreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExploreViewHolder(
        private val binding: ItemCatalogPlantBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(plant: Plant) {
            binding.ivCatalogPlantImage.setImageResource(plant.imagenRes)
            binding.tvCatalogPlantName.text = plant.nombre
            binding.tvCatalogPlantScientific.text = plant.nombreCientifico
            binding.tvCatalogPlantType.text = plant.tipo
            binding.tvCatalogPlantLight.text = plant.luz
            binding.tvCatalogPlantWater.text = plant.riego
            binding.root.setOnClickListener { onPlantClick(plant) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem == newItem
    }
}