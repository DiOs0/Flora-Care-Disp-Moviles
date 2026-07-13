package com.uce.floracare.application.adapters.reyes_milan_osorio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uce.floracare.databinding.ItemPendingTaskBinding
import com.uce.floracare.domain.model.TaskEntity


class TaskAdapter(
    private val onWaterClick: (TaskEntity) -> Unit
) : ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder>(DiffCallback) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskViewHolder {

        val binding =
            ItemPendingTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return TaskViewHolder(binding)
    }



    override fun onBindViewHolder(
        holder: TaskViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )

    }



    inner class TaskViewHolder(
        private val binding: ItemPendingTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(task: TaskEntity) {


            binding.txtTitulo.text =
                task.title


            binding.txtDescripcion.text =
                task.description



            /*
             * Nuevo flujo:
             * El usuario presiona REGAR
             */
            binding.btnRegar.setOnClickListener {

                onWaterClick(task)

            }



        }

    }



    private object DiffCallback :
        DiffUtil.ItemCallback<TaskEntity>() {


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