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
import com.uce.floracare.data.remote.dto.calcularEstadoRiego
import com.uce.floracare.domain.model.WateringStatus
import com.uce.floracare.databinding.ItemPlantCardBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlantAdapter(
    private val onPlantClick: (PlantEntity) -> Unit,
    private val onEditWatering: ((PlantEntity) -> Unit)? = null,
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

            // Lógica de Información de Riego (Último y Próximo)
            binding?.let { b ->
                val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                val lastDate = dateFormat.format(Date(plant.ultimoRiego))
                val nextDateMillis = plant.ultimoRiego + TimeUnit.DAYS.toMillis(plant.wateringFrequencyDays.toLong())
                val nextDate = dateFormat.format(Date(nextDateMillis))

                b.txtLastWatered.text = "Último riego: $lastDate"
                b.txtNextWatering.text = "Próximo riego: $nextDate"
            }

            // Lógica de Badge de Riego
            binding?.let { b ->
                val status = plant.calcularEstadoRiego()
                updateWateringBadge(b, status)

                // CASO B: Visualización de Alerta
                if (status == WateringStatus.URGENTE) {
                    b.root.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.alert_red_soft))
                    b.root.strokeColor = ContextCompat.getColor(itemView.context, R.color.alert_red)
                    b.txtHumidity.visibility = View.VISIBLE
                    // Simulación de métrica falsa
                    b.txtHumidity.text = "Humedad: 20%"
                } else {
                    b.root.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                    b.root.strokeColor = ContextCompat.getColor(itemView.context, R.color.gris_linea)
                    b.txtHumidity.visibility = View.GONE
                }

                // CASO C: Confirmación de Riego (Simulación al hacer clic largo o hover ficticio)
                b.root.setOnLongClickListener {
                    b.overlayConfirmWatering.visibility = View.VISIBLE
                    true
                }

                b.chkConfirmWatering.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        b.overlayConfirmWatering.visibility = View.GONE
                        b.root.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.success_green_soft))
                        b.root.strokeColor = ContextCompat.getColor(itemView.context, R.color.care_low)
                        b.txtWatered.visibility = View.VISIBLE
                        b.txtHumidity.visibility = View.GONE
                        
                        // Cambiar icono de badge a check
                        b.badgeBackground.background = ContextCompat.getDrawable(itemView.context, R.drawable.circle_success_green)
                        b.badgeIcon.setImageResource(android.R.drawable.ic_menu_compass) // Idealmente un check
                        b.badgeIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.white))
                    }
                }

                b.btnEditWatering.setOnClickListener {
                    onEditWatering?.invoke(plant)
                }
            }

            itemView.setOnClickListener { onPlantClick(plant) }
        }

        private fun updateWateringBadge(b: ItemPlantCardBinding, status: WateringStatus) {
            val ctx = b.root.context
            val (bgColor, iconRes, iconTint) = when (status) {
                WateringStatus.URGENTE -> Triple(
                    R.color.care_high_bg, 
                    android.R.drawable.ic_dialog_alert, 
                    R.color.alert_red
                )
                WateringStatus.ATENCION_REQUERIDA -> Triple(
                    R.color.care_medium_bg, 
                    android.R.drawable.ic_popup_reminder, 
                    R.color.care_medium
                )
                WateringStatus.NORMAL -> Triple(
                    R.color.care_low_bg, 
                    android.R.drawable.ic_menu_compass, // Temporalmente, idealmente una gota
                    R.color.care_low
                )
            }

            // Fondo con bordes redondeados y color suave
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(ctx, bgColor))
                setStroke(2, ContextCompat.getColor(ctx, iconTint))
            }
            b.badgeBackground.background = drawable

            // Icono con color vibrante
            b.badgeIcon.setImageResource(iconRes)
            b.badgeIcon.setColorFilter(ContextCompat.getColor(ctx, iconTint))
            
            b.badgeLayout.visibility = View.VISIBLE
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
