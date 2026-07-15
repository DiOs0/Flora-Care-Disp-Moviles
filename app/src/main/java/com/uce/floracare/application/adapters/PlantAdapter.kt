package com.uce.floracare.application.adapters

import android.animation.Animator
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.uce.floracare.R
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.data.remote.dto.calcularEstadoRiego
import com.uce.floracare.databinding.ItemPlantCardBinding
import com.uce.floracare.domain.model.WateringStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class PlantViewMode {
    GRID, LIST
}

class PlantAdapter(
    private val onPlantClick: (PlantEntity) -> Unit,
    private val onEditWatering: ((PlantEntity) -> Unit)? = null,
    private val onWaterPlant: ((PlantEntity) -> Unit)? = null,
    private val layoutRes: Int? = null
) : ListAdapter<PlantEntity, RecyclerView.ViewHolder>(DiffCallback) {

    var viewMode: PlantViewMode = PlantViewMode.GRID
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    private var fullList: List<PlantEntity> = emptyList()

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

    override fun getItemViewType(position: Int): Int {
        if (layoutRes != null) return 0
        return viewMode.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        if (layoutRes != null) {
            val view = inflater.inflate(layoutRes, parent, false)
            return LegacyViewHolder(view)
        }

        return when (viewType) {
            PlantViewMode.LIST.ordinal -> {
                val view = inflater.inflate(R.layout.item_plant_card_horizontal, parent, false)
                ListViewHolder(view)
            }
            else -> {
                val binding = ItemPlantCardBinding.inflate(inflater, parent, false)
                GridViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GridViewHolder -> holder.bind(getItem(position))
            is ListViewHolder -> holder.bind(getItem(position))
            is LegacyViewHolder -> holder.bind(getItem(position))
        }
    }

    inner class LegacyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivImage: ImageView? = view.findViewById(R.id.ivPlantImage) ?: view.findViewById(R.id.imgPlantPhoto)
        private val tvName: TextView? = view.findViewById(R.id.tvPlantName) ?: view.findViewById(R.id.txtPlantName)
        private val tvScientific: TextView? = view.findViewById(R.id.tvPlantScientific) ?: view.findViewById(R.id.txtPlantSpecies)
        private val tvTag: TextView? = view.findViewById(R.id.tvPlantTag)
        private val tvCareLevel: TextView? = view.findViewById(R.id.tvPlantCareLevel)

        fun bind(plant: PlantEntity) {
            ivImage?.let {
                Glide.with(it.context)
                    .load(plant.imagen)
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
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

            itemView.setOnClickListener { onPlantClick(plant) }
        }
    }

    inner class GridViewHolder(private val binding: ItemPlantCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plant: PlantEntity) {
            binding.overlayConfirmWatering.visibility = View.GONE
            binding.chkConfirmWatering.setOnCheckedChangeListener(null)
            binding.chkConfirmWatering.isChecked = false
            binding.txtWatered.visibility = View.GONE

            Glide.with(binding.imgPlantPhoto.context)
                .load(plant.imagen)
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_logo)
                .centerCrop()
                .into(binding.imgPlantPhoto)

            binding.txtPlantName.text = plant.nombreComun
            binding.txtPlantSpecies.text = plant.nombreCientifico

            val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val lastDate = dateFormat.format(Date(plant.ultimoRiego))
            val nextDateMillis = plant.ultimoRiego + TimeUnit.DAYS.toMillis(plant.wateringFrequencyDays.toLong())
            val nextDate = dateFormat.format(Date(nextDateMillis))

            binding.txtLastWatered.text = "Último riego: $lastDate"
            binding.txtNextWatering.text = "Próximo riego: $nextDate"

            val status = plant.calcularEstadoRiego()
            updateWateringBadge(binding.badgeBackground, binding.badgeIcon, binding.badgeLayout, status)

            if (status == WateringStatus.URGENTE) {
                binding.root.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.alert_red_soft))
                binding.root.strokeColor = ContextCompat.getColor(itemView.context, R.color.alert_red)
                binding.txtHumidity.visibility = View.VISIBLE
                binding.txtHumidity.text = "Humedad: 20%"
            } else {
                binding.root.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                binding.root.strokeColor = ContextCompat.getColor(itemView.context, R.color.gris_linea)
                binding.txtHumidity.visibility = View.GONE
            }

            binding.root.setOnLongClickListener {
                binding.overlayConfirmWatering.visibility = View.VISIBLE
                true
            }

            binding.chkConfirmWatering.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.overlayConfirmWatering.visibility = View.GONE
                    
                    binding.root.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.success_green_soft))
                    binding.root.strokeColor = ContextCompat.getColor(itemView.context, R.color.care_low)
                    binding.txtWatered.visibility = View.VISIBLE
                    binding.txtHumidity.visibility = View.GONE

                    binding.badgeBackground.background = ContextCompat.getDrawable(itemView.context, R.drawable.circle_success_green)
                    binding.badgeIcon.setImageResource(android.R.drawable.ic_menu_compass)
                    binding.badgeIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.white))

                    onWaterPlant?.invoke(plant)
                }
            }

            binding.btnEditWatering.setOnClickListener {
                onEditWatering?.invoke(plant)
            }

            itemView.setOnClickListener { onPlantClick(plant) }
        }
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPlantPhoto: ImageView = itemView.findViewById(R.id.imgPlantPhoto)
        private val txtPlantName: TextView = itemView.findViewById(R.id.txtPlantName)
        private val txtPlantSpecies: TextView = itemView.findViewById(R.id.txtPlantSpecies)
        private val txtLastWatered: TextView = itemView.findViewById(R.id.txtLastWatered)
        private val txtNextWatering: TextView = itemView.findViewById(R.id.txtNextWatering)
        private val txtWateringFrequency: TextView = itemView.findViewById(R.id.txtWateringFrequency)
        private val txtHumidity: TextView = itemView.findViewById(R.id.txtHumidity)
        private val txtWatered: TextView = itemView.findViewById(R.id.txtWatered)
        private val tvPlantCareLevel: TextView = itemView.findViewById(R.id.tvPlantCareLevel)
        private val tvPlantTag: TextView = itemView.findViewById(R.id.tvPlantTag)
        private val overlayConfirmWatering: FrameLayout = itemView.findViewById(R.id.overlayConfirmWatering)
        private val chkConfirmWatering = itemView.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.chkConfirmWatering)
        private val badgeLayout: FrameLayout = itemView.findViewById(R.id.badgeLayout)
        private val badgeBackground: View = itemView.findViewById(R.id.badgeBackground)
        private val badgeIcon: ImageView = itemView.findViewById(R.id.badgeIcon)
        private val btnEditWatering: ImageView = itemView.findViewById(R.id.btnEditWatering)

        fun bind(plant: PlantEntity) {
            overlayConfirmWatering.visibility = View.GONE
            chkConfirmWatering.setOnCheckedChangeListener(null)
            chkConfirmWatering.isChecked = false
            txtWatered.visibility = View.GONE

            Glide.with(imgPlantPhoto.context)
                .load(plant.imagen)
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_logo)
                .centerCrop()
                .into(imgPlantPhoto)

            txtPlantName.text = plant.nombreComun
            txtPlantSpecies.text = plant.nombreCientifico

            setCareLevel(tvPlantCareLevel, plant.nivelCuidado)

            tvPlantTag.apply {
                text = if (plant.caracteristicas.indoor) "Interior" else "Exterior"
                visibility = View.VISIBLE
            }

            val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val lastDate = dateFormat.format(Date(plant.ultimoRiego))
            val nextDateMillis = plant.ultimoRiego + TimeUnit.DAYS.toMillis(plant.wateringFrequencyDays.toLong())
            val nextDate = dateFormat.format(Date(nextDateMillis))

            txtLastWatered.text = "Último riego: $lastDate"
            txtNextWatering.text = "Próximo riego: $nextDate"
            txtWateringFrequency.text = "Cada ${plant.wateringFrequencyDays} días"

            val cardRoot = itemView as com.google.android.material.card.MaterialCardView
            val status = plant.calcularEstadoRiego()
            updateWateringBadge(badgeBackground, badgeIcon, badgeLayout, status)

            if (status == WateringStatus.URGENTE) {
                cardRoot.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.alert_red_soft))
                cardRoot.strokeColor = ContextCompat.getColor(itemView.context, R.color.alert_red)
                txtHumidity.visibility = View.VISIBLE
                txtHumidity.text = "Humedad: 20%"
            } else {
                cardRoot.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                cardRoot.strokeColor = ContextCompat.getColor(itemView.context, R.color.gris_linea)
                txtHumidity.visibility = View.GONE
            }

            itemView.setOnLongClickListener {
                overlayConfirmWatering.visibility = View.VISIBLE
                true
            }

            chkConfirmWatering.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    overlayConfirmWatering.visibility = View.GONE
                    
                    cardRoot.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.success_green_soft))
                    cardRoot.strokeColor = ContextCompat.getColor(itemView.context, R.color.care_low)
                    txtWatered.visibility = View.VISIBLE
                    txtHumidity.visibility = View.GONE

                    badgeBackground.background = ContextCompat.getDrawable(itemView.context, R.drawable.circle_success_green)
                    badgeIcon.setImageResource(android.R.drawable.ic_menu_compass)
                    badgeIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.white))

                    onWaterPlant?.invoke(plant)
                }
            }

            btnEditWatering.setOnClickListener {
                onEditWatering?.invoke(plant)
            }

            itemView.setOnClickListener { onPlantClick(plant) }
        }
    }

    private fun updateWateringBadge(
        badgeBackground: View,
        badgeIcon: ImageView,
        badgeLayout: FrameLayout,
        status: WateringStatus
    ) {
        val ctx = badgeBackground.context
        val (bgColor, iconRes, iconTint) = when (status) {
            WateringStatus.URGENTE -> Triple(
                R.color.care_high_bg,
                R.drawable.mood_bad_24px,
                R.color.alert_red
            )
            WateringStatus.ATENCION_REQUERIDA -> Triple(
                R.color.care_medium_bg,
                R.drawable.mood_bad_24px,
                R.color.care_medium
            )
            WateringStatus.NORMAL -> Triple(
                R.color.care_low_bg,
                R.drawable.mood_heart_24px,
                R.color.care_low
            )
        }

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(ContextCompat.getColor(ctx, bgColor))
            setStroke(2, ContextCompat.getColor(ctx, iconTint))
        }
        badgeBackground.background = drawable

        badgeIcon.setImageResource(iconRes)
        badgeIcon.setColorFilter(ContextCompat.getColor(ctx, iconTint))

        badgeLayout.visibility = View.VISIBLE
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
