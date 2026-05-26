package com.uce.floracare.activities.Osorio_Explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uce.floracare.databinding.ItemFeaturedPlantBinding

class FeaturedAdapter(
    private val onPlantClick: (Plant) -> Unit
) : ListAdapter<Plant, FeaturedAdapter.FeaturedViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedViewHolder {
        val binding = ItemFeaturedPlantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeaturedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeaturedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FeaturedViewHolder(
        private val binding: ItemFeaturedPlantBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(plant: Plant) {
            binding.ivPlantImage.setImageResource(plant.imagenRes)
            binding.tvPlantTag.text = plant.tipo
            binding.tvPlantName.text = plant.nombre
            binding.tvPlantLight.text = plant.luz
            binding.tvPlantWater.text = plant.riego
            binding.root.setOnClickListener { onPlantClick(plant) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem == newItem
    }
}