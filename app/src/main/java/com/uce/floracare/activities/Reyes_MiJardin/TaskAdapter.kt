package com.uce.floracare.activities.Reyes_MiJardin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uce.floracare.databinding.ItemTaskCardBinding
import com.uce.floracare.domain.model.TaskEntity

class TaskAdapter(

    private val onTaskCompleted: (TaskEntity) -> Unit

) : ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskViewHolder {

        val binding =
            ItemTaskCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return TaskViewHolder(
            binding,
            onTaskCompleted
        )

    }

    override fun onBindViewHolder(
        holder: TaskViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )

    }

    class TaskViewHolder(

        private val binding: ItemTaskCardBinding,

        private val onTaskCompleted: (TaskEntity) -> Unit

    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskEntity) {

            binding.txtTaskTitle.text =
                task.title

            binding.txtTaskDesc.text =
                task.description


            binding.chkTask.setOnCheckedChangeListener(null)

            binding.chkTask.isChecked =
                task.completed

            binding.chkTask.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) {

                    onTaskCompleted(task)

                }

            }

        }

    }

    private object DiffCallback : DiffUtil.ItemCallback<TaskEntity>() {

        override fun areItemsTheSame(
            oldItem: TaskEntity,
            newItem: TaskEntity
        ): Boolean {

            return oldItem.firestoreId ==
                    newItem.firestoreId

        }

        override fun areContentsTheSame(
            oldItem: TaskEntity,
            newItem: TaskEntity
        ): Boolean {

            return oldItem == newItem

        }

    }

}