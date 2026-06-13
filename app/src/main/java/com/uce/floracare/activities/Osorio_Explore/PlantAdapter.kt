package com.uce.floracare.activities.Osorio_Explore

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

class PlantAdapter(
    private val onPlantClick: (Plant) -> Unit,
    private val layoutRes: Int
) : ListAdapter<Plant, PlantAdapter.PlantViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivPlantImage)
        private val tvName: TextView = itemView.findViewById(R.id.tvPlantName)
        private val tvScientific: TextView = itemView.findViewById(R.id.tvPlantScientific)
        private val tvTag: TextView? = itemView.findViewById(R.id.tvPlantTag)
        private val tvCareLevel: TextView = itemView.findViewById(R.id.tvPlantCareLevel)

        fun bind(plant: Plant) {
            Glide.with(ivImage.context)
                .load(plant.imagenUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(ivImage)

            tvName.text = plant.nombre
            tvScientific.text = plant.nombreCientifico

            tvTag?.apply {
                text = if (plant.indoor) "Interior" else "Exterior"
                visibility = View.VISIBLE
            }

            setCareLevel(tvCareLevel, plant.nivelCuidado)
            itemView.setOnClickListener { onPlantClick(plant) }
        }
    }

    private fun setCareLevel(textView: TextView, level: String) {
        val (text, dotColor, bgColor) = when (level.lowercase()) {
            "low" -> Triple("Fácil", R.color.care_low, R.color.care_low_bg)
            "medium", "moderate" -> Triple("Medio", R.color.care_medium, R.color.care_medium_bg)
            "high" -> Triple("Avanzado", R.color.care_high, R.color.care_high_bg)
            else -> Triple(level.ifEmpty { "—" }, R.color.primary_green, R.color.accent_green)
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

    private object DiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem == newItem
    }
}
