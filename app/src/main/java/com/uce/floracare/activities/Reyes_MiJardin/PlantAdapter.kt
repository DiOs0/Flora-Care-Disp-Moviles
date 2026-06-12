package com.uce.floracare.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uce.floracare.R
import com.uce.floracare.activities.Reyes_MiJardin.Plant
import com.uce.floracare.databinding.ItemPlantCardBinding

class PlantAdapter(
    private val onPlantClick: (Plant) -> Unit
) : ListAdapter<Plant, PlantAdapter.PlantViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlantViewHolder {

        val binding = ItemPlantCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PlantViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PlantViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class PlantViewHolder(
        private val binding: ItemPlantCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(plant: Plant) {

            val context = itemView.context

            binding.txtPlantName.text = plant.nombre
            binding.txtPlantSpecies.text =
                plant.nombreCientifico.uppercase()

            // Cargar imagen desde URL
            if (plant.imageUrl.isNotBlank()) {

                Glide.with(context)
                    .load(plant.imageUrl)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(binding.imgPlantPhoto)

            } else {

                binding.imgPlantPhoto.setImageResource(
                    plant.imagenRes
                )
            }

            if (plant.necesitaAgua) {

                binding.badgeBackground.background =
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.circle_alert_red
                    )

                binding.badgeIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        android.R.drawable.stat_notify_chat
                    )
                )

                binding.badgeIcon.imageTintList =
                    ContextCompat.getColorStateList(
                        context,
                        android.R.color.white
                    )

            } else {

                binding.badgeBackground.background =
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.badge_rounded_green
                    )

                binding.badgeIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        android.R.drawable.checkbox_on_background
                    )
                )

                binding.badgeIcon.imageTintList =
                    ContextCompat.getColorStateList(
                        context,
                        R.color.forest_green
                    )
            }

            binding.root.setOnClickListener {
                onPlantClick(plant)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Plant>() {

        override fun areItemsTheSame(
            oldItem: Plant,
            newItem: Plant
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Plant,
            newItem: Plant
        ): Boolean = oldItem == newItem
    }
}