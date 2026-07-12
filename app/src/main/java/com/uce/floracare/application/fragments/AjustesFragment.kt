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
import com.uce.floracare.R
import com.uce.floracare.application.activities.Login
import com.uce.floracare.application.viewmodels.AjustesUiState
import com.uce.floracare.application.viewmodels.AjustesViewModel
import com.uce.floracare.application.viewmodels.ViewModelFactory
import com.uce.floracare.data.local.database.FloraCareDatabase
import com.uce.floracare.databinding.FragmentAjustesBinding
import com.uce.floracare.domain.usecase.*
import com.uce.floracare.repositories.ImageRepositoryImpl
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.TaskRepository
import com.uce.floracare.repositories.UserRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class AjustesFragment : Fragment() {

    private var _binding: FragmentAjustesBinding? = null
    private val binding get() = _binding!!

    private var selectedImageFile: File? = null

    private val viewModel: AjustesViewModel by viewModels {
        ViewModelFactory {
            // Inicialización manual de dependencias (Simulando Inyección)
            val authManager = AuthManager()
            val firestoreManager = FirestoreManager(authManager)
            val storageManager = StorageManager(requireContext())
            val db = FloraCareDatabase.getDatabase(requireContext())
            
            val userRepository = UserRepository(GetUserProfileUC())
            val plantRepository = PlantRepository(firestoreManager, storageManager, authManager, db.plantDao())
            val taskRepository = TaskRepository(firestoreManager, authManager, db.taskDao())
            val imageRepository = ImageRepositoryImpl()
            
            AjustesViewModel(
                obtenerPerfilUsuarioUseCase = ObtenerPerfilUsuarioUseCase(userRepository),
                actualizarPerfilUsuarioUseCase = ActualizarPerfilUsuarioUseCase(userRepository),
                cerrarSesionUseCase = CerrarSesionUseCase(userRepository),
                subirImagenUseCase = SubirImagenUseCase(imageRepository),
                obtenerEstadisticasJardinUseCase = ObtenerEstadisticasJardinUseCase(plantRepository, taskRepository, authManager)
            )
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            binding.imgPerfil.setImageURI(it)
            selectedImageFile = uriToFile(it)
            binding.btnGuardarPerfil.isVisible = true
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
        
        setupListeners()
        setupObservers()
        viewModel.loadUserProfile()
    }

    private fun setupListeners() {
        binding.btnEditPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.tvNombre.setOnClickListener {
            toggleEditName(true)
        }

        binding.btnGuardarPerfil.setOnClickListener {
            val newName = binding.etNombre.text.toString().trim()
            viewModel.updateProfile(newName, selectedImageFile)
        }

        binding.btnCerrarSesion.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun toggleEditName(isEditing: Boolean) {
        binding.tilNombre.isVisible = isEditing
        binding.tvNombre.isVisible = !isEditing
        binding.btnGuardarPerfil.isVisible = isEditing || selectedImageFile != null
        if (isEditing) {
            binding.etNombre.setText(binding.tvNombre.text)
            binding.etNombre.requestFocus()
        }
    }

    private fun setupObservers() {
        // Observar Perfil y Estado General
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AjustesUiState.Loading -> showLoading(true)
                        is AjustesUiState.Success -> {
                            showLoading(false)
                            toggleEditName(false)
                            selectedImageFile = null
                            
                            binding.tvNombre.text = state.profile.name
                            binding.tvCorreo.text = state.profile.email
                            
                            Glide.with(this@AjustesFragment)
                                .load(state.profile.photoUrl)
                                .placeholder(R.drawable.baseline_person_24)
                                .into(binding.imgPerfil)
                            
                            Glide.with(this@AjustesFragment)
                                .load(state.profile.photoUrl)
                                .placeholder(R.mipmap.ic_launcher_round)
                                .into(binding.ivProfile)
                        }
                        is AjustesUiState.Error -> {
                            showLoading(false)
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        is AjustesUiState.Logout -> {
                            startActivity(Intent(requireContext(), Login::class.java))
                            requireActivity().finish()
                        }
                        is AjustesUiState.Idle -> {}
                    }
                }
            }
        }

        // Observar Estadísticas de forma independiente (Reactividad SSOT)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estadisticas.collect { stats ->
                    binding.apply {
                        tvTotalPlantas.text = stats.totalPlantas.toString()
                        tvTareasPendientes.text = stats.tareasPendientes.toString()
                        tvSaludGeneral.text = "${stats.saludGeneral}%"
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnGuardarPerfil.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, "temp_profile_img_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
