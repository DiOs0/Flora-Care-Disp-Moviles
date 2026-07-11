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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uce.floracare.R
import com.uce.floracare.application.activities.Login
import com.uce.floracare.application.viewmodels.AjustesUiState
import com.uce.floracare.application.viewmodels.AjustesViewModel
import com.uce.floracare.application.viewmodels.ViewModelFactory
import com.uce.floracare.databinding.FragmentAjustesBinding
import com.uce.floracare.domain.usecase.GetUserProfileUC
import com.uce.floracare.repositories.UserRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AjustesFragment : Fragment() {

    private var _binding: FragmentAjustesBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null

    private val viewModel: AjustesViewModel by viewModels {
        ViewModelFactory {
            AjustesViewModel(UserRepository(GetUserProfileUC()))
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this).load(it).circleCrop().into(binding.imgPerfil)
            showEditMode(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAjustesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initCloudinary()
        setupObservers()
        setupListeners()

        if (viewModel.uiState.value is AjustesUiState.Idle) {
            viewModel.loadUserProfile()
        }
    }

    private fun initCloudinary() {
        try {
            val config = mapOf(
                "cloud_name" to "deqhd3bmp",
                "api_key" to "188973848385489",
                "api_secret" to "bmPFYmcccVKbOhp5g0U6LyHn8aE"
            )
            MediaManager.init(requireContext(), config)
        } catch (e: Exception) { /* Ya inicializado */ }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AjustesUiState.Loading -> setLoadingState(true)
                        is AjustesUiState.Success -> {
                            setLoadingState(false)
                            showEditMode(false)
                            val profile = state.profile
                            binding.tvNombre.text = profile.name
                            binding.tvCorreo.text = profile.email
                            Glide.with(this@AjustesFragment)
                                .load(profile.photoUrl)
                                .placeholder(R.drawable.baseline_person_24)
                                .error(R.drawable.baseline_person_24)
                                .circleCrop()
                                .into(binding.imgPerfil)
                        }
                        is AjustesUiState.Error -> {
                            setLoadingState(false)
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        is AjustesUiState.Logout -> {
                            val intent = Intent(requireContext(), Login::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        is AjustesUiState.Idle -> {}
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnEditPhoto.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.tvNombre.setOnClickListener { showEditMode(true) }
        binding.btnGuardarPerfil.setOnClickListener { handleSaveAction() }
        binding.btnCerrarSesion.setOnClickListener { showLogoutDialog() }
    }

    private fun handleSaveAction() {
        val nuevoNombre = binding.etNombre.text.toString().trim()
        val file = selectedImageUri?.let { uriToFile(it) }
        viewModel.updateProfile(nuevoNombre, file)
    }

    private fun showEditMode(edit: Boolean) {
        binding.tilNombre.isVisible = edit
        binding.btnGuardarPerfil.isVisible = edit
        binding.tvNombre.isVisible = !edit
        if (edit && binding.etNombre.text.isNullOrBlank()) {
            binding.etNombre.setText(binding.tvNombre.text)
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val file = File(requireContext().cacheDir, "profile_temp.jpg")
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            file
        } catch (e: Exception) { null }
    }

    private fun setLoadingState(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.btnGuardarPerfil.isEnabled = !loading
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Está seguro de cerrar sesión?")
            .setPositiveButton("Sí") { _, _ -> viewModel.logout() }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
