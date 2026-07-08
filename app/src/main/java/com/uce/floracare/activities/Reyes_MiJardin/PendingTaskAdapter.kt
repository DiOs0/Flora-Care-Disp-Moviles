package com.uce.floracare.activities.Reyes_MiJardin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uce.floracare.application.viewholder.PendingTaskViewHolder
import com.uce.floracare.databinding.ItemPendingTaskBinding
import com.uce.floracare.domain.model.TaskEntity

class PendingTaskAdapter(

    private val onCheck:(TaskEntity)->Unit

): RecyclerView.Adapter<PendingTaskViewHolder>(){

    private val lista=
        mutableListOf<TaskEntity>()

    fun submitList(
        nueva:List<TaskEntity>
    ){

        lista.clear()
        lista.addAll(nueva)
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType:Int
    ):PendingTaskViewHolder{

        val binding=
            ItemPendingTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return PendingTaskViewHolder(binding)

    }

    override fun getItemCount()=
        lista.size

    override fun onBindViewHolder(
        holder:PendingTaskViewHolder,
        position:Int
    ){

        val task=
            lista[position]

        holder.binding.txtTitulo.text=
            task.title

        holder.binding.txtDescripcion.text=
            task.description

        holder.binding.checkTask.setOnClickListener{

            onCheck(task)

        }

    }

}