package com.uce.floracare.application.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.uce.floracare.data.remote.dto.PlantEntity
import com.uce.floracare.domain.usecase.ActualizarPlantaUsuarioUseCase
import com.uce.floracare.databinding.FragmentEditarPlantaBinding
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import kotlinx.coroutines.launch

class EditarPlantaFragment:Fragment() {

    private var _binding:FragmentEditarPlantaBinding?=null
    private val binding get()=_binding!!

    private lateinit var plant:PlantEntity

    val authManager =
        AuthManager()

    private val repository by lazy{

        PlantRepository(
            FirestoreManager(authManager),
            StorageManager(requireContext())
        )

    }

    private val actualizarUseCase by lazy{

        ActualizarPlantaUsuarioUseCase(
            repository
        )

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding=
            FragmentEditarPlantaBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root

    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        plant=
            arguments?.getSerializable(
                "plant"
            ) as PlantEntity

        loadData()

        binding.btnGuardar.setOnClickListener {

            actualizarPlanta()

        }

        binding.btnCancelar.setOnClickListener{

            parentFragmentManager.popBackStack()

        }

    }


    private fun loadData(){

        Glide.with(requireContext())
            .load(
                plant.imagen
            )
            .into(
                binding.imgPlant
            )

        binding.edtNombre.setText(
            plant.nombreComun
        )

        binding.edtDescripcion.setText(
            plant.descripcion
        )

        binding.edtTipo.setText(
            plant.tipo
        )

    }



    private fun actualizarPlanta(){

        val updatedPlant=

            plant.copy(

                nombreComun=
                    binding.edtNombre.text.toString(),

                descripcion=
                    binding.edtDescripcion.text.toString(),

                tipo=
                    binding.edtTipo.text.toString()

            )


        viewLifecycleOwner.lifecycleScope.launch {

            val result=

                actualizarUseCase(
                    updatedPlant
                )

            if(result.isSuccess){

                Toast.makeText(

                    requireContext(),
                    "Planta actualizada",
                    Toast.LENGTH_SHORT

                ).show()

                parentFragmentManager.popBackStack()

            }

            else{

                Toast.makeText(

                    requireContext(),
                    result.exceptionOrNull()?.message,
                    Toast.LENGTH_LONG

                ).show()

            }

        }

    }


    override fun onDestroyView(){

        super.onDestroyView()

        _binding=null

    }

}