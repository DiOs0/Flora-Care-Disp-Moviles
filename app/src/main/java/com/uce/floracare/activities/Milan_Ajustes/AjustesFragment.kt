package com.uce.floracare.activities.Milan_Ajustes

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uce.floracare.R
import com.uce.floracare.databinding.FragmentAjustesBinding
import com.uce.floracare.databinding.FragmentAuxiliarBinding

//////////////////////
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import com.uce.floracare.activities.Jhon_AddPlant.Login
import com.uce.floracare.activities.Milan_Ajustes.dto.Usuario


class AjustesFragment : Fragment() {

    private var _binding: FragmentAjustesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAjustesBinding.inflate(inflater, container, false)

        cerrarSesion()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usuario = obtenerUsuario()

        usuario?.let {

            binding.tvNombre.text = it.nombre
            binding.tvCorreo.text = it.correo


            Picasso.get()
                .load(it.fotoPerfil)
                .placeholder(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_person_24)
                .into(binding.imgPerfil)

            binding.switchRiego.isChecked =
                it.preferencias.recordatoriosRiego

            binding.switchCatalogo.isChecked =
                it.preferencias.notificacionesCatalogo
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
//////////////////////////////
    private fun leerJson(nombreArchivo: String): String {
        return requireContext()
            .assets
            .open(nombreArchivo)
            .bufferedReader()
            .use { it.readText() }
    }

    private fun obtenerUsuario(): Usuario? {

        val json = leerJson("usuarios.json")

        val type = object : TypeToken<List<Usuario>>() {}.type

        val usuarios: List<Usuario> =
            Gson().fromJson(json, type)

        return usuarios.firstOrNull()
    }

    private fun cerrarSesion() {

        binding.btnCerrarSesion.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Está seguro de cerrar sesión?")
                .setCancelable(true)

                .setPositiveButton("Sí") { _, _ ->

                    val intent = Intent(
                        requireContext(),
                        Login::class.java
                    )

                    startActivity(intent)

                    requireActivity().finish()
                }

                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

                .show()
        }
    }
}