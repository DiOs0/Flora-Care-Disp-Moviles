package com.uce.floracare.activities.Reyes_MiJardin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uce.floracare.R
import com.uce.floracare.databinding.ItemTaskCardBinding
import com.uce.floracare.domain.model.PlantTask
import com.uce.floracare.domain.model.TaskType

class TaskAdapter : ListAdapter<PlantTask, TaskAdapter.TaskViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(private val binding: ItemTaskCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: PlantTask) {
            binding.txtTaskTitle.text = task.title
            binding.txtTaskDesc.text = task.description

            val context = itemView.context
            
            // Personalización según el tipo de tarea
            when (task.taskType) {
                TaskType.WATERING -> {
                    binding.icTaskIcon.setImageResource(android.R.drawable.ic_menu_compass) // Reemplazar por icono de agua
                    binding.icTaskIcon.imageTintList = ContextCompat.getColorStateList(context, android.R.color.holo_blue_dark)
                    binding.icTaskIcon.background = ContextCompat.getDrawable(context, R.drawable.circle_blue_light)
                }
                TaskType.MISTING -> {
                    binding.icTaskIcon.setImageResource(android.R.drawable.ic_menu_day)
                    binding.icTaskIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.forest_green)
                    binding.icTaskIcon.background = ContextCompat.getDrawable(context, R.drawable.badge_rounded_green)
                }
                TaskType.INFO -> {
                    binding.icTaskIcon.setImageResource(android.R.drawable.ic_dialog_info)
                    binding.icTaskIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.text_slate)
                    binding.icTaskIcon.background = ContextCompat.getDrawable(context, R.drawable.circle_alert_red)
                }
                else -> {}
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<PlantTask>() {
        override fun areItemsTheSame(oldItem: PlantTask, newItem: PlantTask) = oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: PlantTask, newItem: PlantTask) = oldItem == newItem
    }
}
