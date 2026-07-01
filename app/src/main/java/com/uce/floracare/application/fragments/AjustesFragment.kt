package com.uce.floracare.application.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uce.floracare.R
import com.uce.floracare.application.activities.Login
import com.uce.floracare.application.viewmodels.AjustesViewModel
import com.uce.floracare.databinding.FragmentAjustesBinding
import com.uce.floracare.domain.usecase.GetUserProfileUC
import com.uce.floracare.repositories.connections.remote.cloudinary.CloudinaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

class AjustesFragment : Fragment() {

    private var _binding: FragmentAjustesBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Previsualizar la imagen seleccionada inmediatamente
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.imgPerfil)
            
            // Mostrar botón de guardar y campo de edición
            showEditMode(true)
        }
    }

    private val viewModel: AjustesViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AjustesViewModel(GetUserProfileUC()) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAjustesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCloudinary()
        setupObservers()
        setupListeners()

        viewModel.loadUserProfile()
    }

    private fun initCloudinary() {
        try {
            val config = mapOf(
                "cloud_name" to "deqhd3bmp",
                "api_key" to "188973848385489",
                "api_secret" to "bmPFYmcccVKbOhp5g0U6LyHn8aE"
            )
            MediaManager.init(requireContext(), config)
        } catch (e: Exception) {
            // Ya inicializado
        }
    }

    private fun showEditMode(edit: Boolean) {
        binding.tilNombre.isVisible = edit
        binding.btnGuardarPerfil.isVisible = edit
        binding.tvNombre.isVisible = !edit
        
        if (edit && binding.etNombre.text.isNullOrBlank()) {
            binding.etNombre.setText(binding.tvNombre.text)
        }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.tvNombre.text = it.name
                binding.tvCorreo.text = it.email

                Glide.with(this)
                    .load(it.photoUrl)
                    .placeholder(R.drawable.baseline_person_24)
                    .error(R.drawable.baseline_person_24)
                    .circleCrop()
                    .into(binding.imgPerfil)
            }
        }
    }

    private fun setupListeners() {
        binding.btnEditPhoto.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.tvNombre.setOnClickListener {
            showEditMode(true)
        }

        binding.btnGuardarPerfil.setOnClickListener {
            saveProfile()
        }

        binding.btnCerrarSesion.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun saveProfile() {
        val nuevoNombre = binding.etNombre.text.toString().trim()
        if (nuevoNombre.isEmpty()) {
            Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = currentUser.uid

        lifecycleScope.launch {
            setLoadingState(true)

            var photoUrl = viewModel.userProfile.value?.photoUrl

            // 1. Si el usuario seleccionó una nueva foto, subirla a Cloudinary
            selectedImageUri?.let { uri ->
                val file = uriToFile(uri)
                if (file != null) {
                    val cloudinaryUrl = subirACloudinary(file)
                    if (cloudinaryUrl != null) {
                        photoUrl = cloudinaryUrl
                    } else {
                        setLoadingState(false)
                        Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }
            }

            // 2. Guardar en Firestore
            val userMap = mapOf(
                "uid" to uid,
                "name" to nuevoNombre,
                "photoUrl" to photoUrl,
                "email" to currentUser.email
            )

            try {
                FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .set(userMap)
                    .await()

                Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                showEditMode(false)
                viewModel.loadUserProfile() // Recargar datos
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoadingState(false)
            }
        }
    }

    private suspend fun subirACloudinary(file: File): String? = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            CloudinaryService.subirImagenFirmada(file) { success, result ->
                if (success) continuation.resume(result)
                else continuation.resume(null)
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val file = File(requireContext().cacheDir, "profile_temp.jpg")
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun setLoadingState(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.btnGuardarPerfil.isEnabled = !loading
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Está seguro de cerrar sesión?")
            .setCancelable(true)
            .setPositiveButton("Sí") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}