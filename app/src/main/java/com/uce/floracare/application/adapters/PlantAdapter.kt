package com.uce.floracare.application.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uce.floracare.R
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.databinding.ItemPlantCardBinding

class PlantAdapter(
    private val onPlantClick: (PlantEntity) -> Unit,
    private val layoutRes: Int? = null
) : ListAdapter<PlantEntity, PlantAdapter.PlantViewHolder>(DiffCallback) {

    private var fullList: List<PlantEntity> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (layoutRes != null) {
            val view = layoutInflater.inflate(layoutRes, parent, false)
            PlantViewHolder(view)
        } else {
            val binding = ItemPlantCardBinding.inflate(layoutInflater, parent, false)
            PlantViewHolder(binding.root, binding)
        }
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun submitFullList(list: List<PlantEntity>) {
        fullList = list
        submitList(list)
    }

    fun filter(query: String): Boolean {
        val filtered = if (query.isBlank()) {
            fullList
        } else {
            fullList.filter { plant ->
                plant.nombreComun.lowercase().contains(query.lowercase()) ||
                plant.nombreCientifico.lowercase().contains(query.lowercase())
            }
        }
        submitList(filtered)
        return filtered.isNotEmpty()
    }

    inner class PlantViewHolder(view: View, private val binding: ItemPlantCardBinding? = null) : RecyclerView.ViewHolder(view) {
        private val ivImage: ImageView? = view.findViewById(R.id.ivPlantImage) ?: view.findViewById(R.id.imgPlantPhoto)
        private val tvName: TextView? = view.findViewById(R.id.tvPlantName) ?: view.findViewById(R.id.txtPlantName)
        private val tvScientific: TextView? = view.findViewById(R.id.tvPlantScientific) ?: view.findViewById(R.id.txtPlantSpecies)
        private val tvTag: TextView? = view.findViewById(R.id.tvPlantTag)
        private val tvCareLevel: TextView? = view.findViewById(R.id.tvPlantCareLevel)

        fun bind(plant: PlantEntity) {
            ivImage?.let {
                Glide.with(it.context)
                    .load(plant.imagen)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(it)
            }

            tvName?.text = plant.nombreComun
            tvScientific?.text = plant.nombreCientifico

            tvTag?.apply {
                text = if (plant.caracteristicas.indoor) "Interior" else "Exterior"
                visibility = View.VISIBLE
            }

            tvCareLevel?.let { setCareLevel(it, plant.nivelCuidado) }

            // Lógica específica de ItemPlantCard (Reyes) si binding no es nulo
            binding?.let {
                // TODO: Implement watering badge logic if needed for MiJardin
            }

            itemView.setOnClickListener { onPlantClick(plant) }
        }
    }

    private fun setCareLevel(textView: TextView, level: String) {
        val (text, dotColor, bgColor) = when (level.lowercase()) {
            "bajo", "low" -> Triple("Fácil", R.color.care_low, R.color.care_low_bg)
            "medio", "medium", "moderate" -> Triple("Medio", R.color.care_medium, R.color.care_medium_bg)
            "alto", "high" -> Triple("Avanzado", R.color.care_high, R.color.care_high_bg)
            else -> Triple(level.ifEmpty { "\u2014" }, R.color.primary_green, R.color.accent_green)
        }

        textView.text = text
        val ctx = textView.context
        val colorInt = ContextCompat.getColor(ctx, dotColor)

        textView.setTextColor(colorInt)

        val dot = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setSize(10, 10)
            setColor(colorInt)
        }
        dot.setBounds(0, 0, 10, 10)
        textView.setCompoundDrawables(dot, null, null, null)
        textView.compoundDrawablePadding = 6

        val bg = GradientDrawable().apply {
            setColor(ContextCompat.getColor(ctx, bgColor))
            cornerRadius = 999f
        }
        textView.background = bg
    }

    private object DiffCallback : DiffUtil.ItemCallback<PlantEntity>() {
        override fun areItemsTheSame(oldItem: PlantEntity, newItem: PlantEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PlantEntity, newItem: PlantEntity): Boolean = oldItem == newItem
    }
}
