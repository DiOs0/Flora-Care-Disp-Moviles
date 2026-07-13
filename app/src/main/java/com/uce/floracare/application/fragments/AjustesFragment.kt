package com.uce.floracare.application.fragments

import android.content.Context
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
import com.google.firebase.auth.FirebaseAuth
import com.uce.floracare.R
import com.uce.floracare.application.activities.Login
import com.uce.floracare.application.viewmodels.AjustesUiState
import com.uce.floracare.application.viewmodels.AjustesViewModel
import com.uce.floracare.application.viewmodels.ViewModelFactory
import com.uce.floracare.data.local.database.FloraCareDatabase
import com.uce.floracare.databinding.FragmentAjustesBinding
import com.uce.floracare.domain.usecase.ActualizarPerfilUsuarioUseCase
import com.uce.floracare.domain.usecase.CerrarSesionUseCase
import com.uce.floracare.domain.usecase.GetUserProfileUC
import com.uce.floracare.domain.usecase.ObtenerEstadisticasJardinUseCase
import com.uce.floracare.domain.usecase.ObtenerPerfilUsuarioUseCase
import com.uce.floracare.domain.usecase.SubirImagenUseCase
import com.uce.floracare.repositories.ImageRepositoryImpl
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.TaskRepository
import com.uce.floracare.repositories.UserRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import com.uce.floracare.repositories.connections.remote.firebase.StorageManager
import kotlinx.coroutines.launch
import java.io.File

class AjustesFragment : Fragment() {

    private var _binding: FragmentAjustesBinding? = null

    private val binding: FragmentAjustesBinding
        get() = _binding!!

    private var selectedImageFile: File? = null

    private val viewModel: AjustesViewModel by viewModels {

        ViewModelFactory {

            val authManager =
                AuthManager()

            val firestoreManager =
                FirestoreManager(
                    authManager
                )

            val storageManager =
                StorageManager(
                    requireContext()
                )

            val database =
                FloraCareDatabase.getDatabase(
                    requireContext()
                )

            val userRepository =
                UserRepository(
                    GetUserProfileUC()
                )

            val taskRepository =
                TaskRepository(
                    firestoreManager,
                    authManager,
                    database.taskDao()
                )

            val plantRepository =
                PlantRepository(
                    firestoreManager,
                    storageManager,
                    authManager,
                    database.plantDao(),
                    taskRepository
                )

            val imageRepository =
                ImageRepositoryImpl()

            val estadisticasUseCase =
                ObtenerEstadisticasJardinUseCase(
                    plantRepository = plantRepository,
                    taskRepository = taskRepository,
                    authManager = authManager
                )

            AjustesViewModel(
                obtenerPerfilUsuarioUseCase =
                    ObtenerPerfilUsuarioUseCase(
                        userRepository
                    ),

                actualizarPerfilUsuarioUseCase =
                    ActualizarPerfilUsuarioUseCase(
                        userRepository
                    ),

                cerrarSesionUseCase =
                    CerrarSesionUseCase(
                        userRepository
                    ),

                subirImagenUseCase =
                    SubirImagenUseCase(
                        imageRepository
                    ),

                obtenerEstadisticasJardinUseCase =
                    estadisticasUseCase
            )
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {

                binding.imgPerfil.setImageURI(
                    it
                )

                binding.ivProfile.setImageURI(
                    it
                )

                selectedImageFile =
                    uriToFile(
                        it
                    )

                binding.btnGuardarPerfil.isVisible =
                    true
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentAjustesBinding.inflate(
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

        cargarDatosGoogle()

        setupNotificationSwitch()

        setupListeners()

        setupObservers()

        viewModel.loadUserProfile()
    }

    private fun setupNotificationSwitch() {

        val prefs =
            requireContext()
                .getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )

        val notificationsEnabled =
            prefs.getBoolean(
                KEY_NOTIFICATIONS_ENABLED,
                true
            )

        binding.switchNotifications
            .setOnCheckedChangeListener(
                null
            )

        binding.switchNotifications.isChecked =
            notificationsEnabled

        binding.switchNotifications
            .setOnCheckedChangeListener { _, isChecked ->

                prefs.edit()
                    .putBoolean(
                        KEY_NOTIFICATIONS_ENABLED,
                        isChecked
                    )
                    .apply()

                val message =
                    if (isChecked) {

                        "Recordatorios de riego activados"

                    } else {

                        "Recordatorios de riego desactivados"
                    }

                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun cargarDatosGoogle() {

        val firebaseUser =
            FirebaseAuth.getInstance()
                .currentUser

        val prefs =
            requireContext()
                .getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )

        val nombreGuardado =
            prefs.getString(
                KEY_USER_NAME,
                "Usuario FloraCare"
            )
                ?: "Usuario FloraCare"

        val correoGuardado =
            prefs.getString(
                KEY_USER_EMAIL,
                ""
            )
                ?: ""

        val fotoGuardada =
            prefs.getString(
                KEY_USER_PHOTO,
                null
            )

        val nombre =
            firebaseUser
                ?.displayName
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: nombreGuardado

        val correo =
            firebaseUser
                ?.email
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: correoGuardado

        val foto =
            firebaseUser
                ?.photoUrl
                ?.toString()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: fotoGuardada

        binding.tvNombre.text =
            nombre

        binding.tvCorreo.text =
            correo

        cargarFotoPerfil(
            foto
        )

        prefs.edit()
            .putString(
                KEY_USER_NAME,
                nombre
            )
            .putString(
                KEY_USER_EMAIL,
                correo
            )
            .putString(
                KEY_USER_PHOTO,
                foto
            )
            .apply()
    }

    private fun cargarFotoPerfil(
        photoUrl: String?
    ) {

        Glide.with(this)
            .load(
                photoUrl
            )
            .placeholder(
                R.drawable.baseline_person_24
            )
            .error(
                R.drawable.baseline_person_24
            )
            .circleCrop()
            .into(
                binding.imgPerfil
            )

        Glide.with(this)
            .load(
                photoUrl
            )
            .placeholder(
                R.drawable.baseline_person_24
            )
            .error(
                R.drawable.baseline_person_24
            )
            .circleCrop()
            .into(
                binding.ivProfile
            )
    }

    private fun setupListeners() {

        binding.btnEditPhoto.setOnClickListener {

            pickImageLauncher.launch(
                "image/*"
            )
        }

        binding.tvNombre.setOnClickListener {

            toggleEditName(
                true
            )
        }

        binding.btnGuardarPerfil.setOnClickListener {

            val newName =
                binding.etNombre.text
                    ?.toString()
                    ?.trim()
                    .orEmpty()

            if (
                newName.isBlank() &&
                selectedImageFile == null
            ) {

                Toast.makeText(
                    requireContext(),
                    "Ingresa un nombre o selecciona una foto",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            viewModel.updateProfile(
                newName,
                selectedImageFile
            )
        }

        binding.btnCerrarSesion.setOnClickListener {

            viewModel.logout()
        }
    }

    private fun toggleEditName(
        editing: Boolean
    ) {

        binding.tilNombre.isVisible =
            editing

        binding.tvNombre.isVisible =
            !editing

        binding.btnGuardarPerfil.isVisible =
            editing ||
                    selectedImageFile != null

        if (editing) {

            binding.etNombre.setText(
                binding.tvNombre.text
            )

            binding.etNombre.requestFocus()
        }
    }

    private fun setupObservers() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.uiState.collect { state ->

                    when (state) {

                        is AjustesUiState.Loading -> {

                            showLoading(
                                true
                            )
                        }

                        is AjustesUiState.Success -> {

                            showLoading(
                                false
                            )

                            selectedImageFile =
                                null

                            binding.btnGuardarPerfil.isVisible =
                                false

                            binding.tilNombre.isVisible =
                                false

                            binding.tvNombre.isVisible =
                                true

                            actualizarPerfilEnPantalla(
                                profileName =
                                    state.profile.name,

                                profileEmail =
                                    state.profile.email,

                                profilePhoto =
                                    state.profile.photoUrl
                            )
                        }

                        is AjustesUiState.Error -> {

                            showLoading(
                                false
                            )

                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is AjustesUiState.Logout -> {

                            startActivity(
                                Intent(
                                    requireContext(),
                                    Login::class.java
                                )
                            )

                            requireActivity()
                                .finish()
                        }

                        AjustesUiState.Idle -> {
                            // No se realiza ninguna acción.
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.estadisticas.collect { stats ->

                    binding.tvTotalPlantas.text =
                        stats.totalPlantas.toString()

                    binding.tvTareasPendientes.text =
                        stats.tareasPendientes.toString()

                    binding.tvSaludGeneral.text =
                        "${stats.saludGeneral}%"
                }
            }
        }
    }

    private fun actualizarPerfilEnPantalla(
        profileName: String?,
        profileEmail: String?,
        profilePhoto: String?
    ) {

        val firebaseUser =
            FirebaseAuth.getInstance()
                .currentUser

        val prefs =
            requireContext()
                .getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )

        val name =
            profileName
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: firebaseUser
                    ?.displayName
                    ?.takeIf {
                        it.isNotBlank()
                    }
                ?: prefs.getString(
                    KEY_USER_NAME,
                    "Usuario FloraCare"
                )
                ?: "Usuario FloraCare"

        val email =
            profileEmail
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: firebaseUser
                    ?.email
                    ?.takeIf {
                        it.isNotBlank()
                    }
                ?: prefs.getString(
                    KEY_USER_EMAIL,
                    ""
                )
                ?: ""

        val photo =
            profilePhoto
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: firebaseUser
                    ?.photoUrl
                    ?.toString()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                ?: prefs.getString(
                    KEY_USER_PHOTO,
                    null
                )

        binding.tvNombre.text =
            name

        binding.tvCorreo.text =
            email

        cargarFotoPerfil(
            photo
        )

        prefs.edit()
            .putString(
                KEY_USER_NAME,
                name
            )
            .putString(
                KEY_USER_EMAIL,
                email
            )
            .putString(
                KEY_USER_PHOTO,
                photo
            )
            .apply()
    }

    private fun showLoading(
        loading: Boolean
    ) {

        binding.btnGuardarPerfil.isEnabled =
            !loading

        binding.btnEditPhoto.isEnabled =
            !loading

        binding.progressBar.visibility =
            if (loading) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun uriToFile(
        uri: Uri
    ): File? {

        return try {

            val file =
                File(
                    requireContext().cacheDir,
                    "profile_${System.currentTimeMillis()}.jpg"
                )

            requireContext()
                .contentResolver
                .openInputStream(
                    uri
                )
                ?.use { input ->

                    file.outputStream()
                        .use { output ->

                            input.copyTo(
                                output
                            )
                        }
                }
                ?: return null

            file

        } catch (e: Exception) {

            Toast.makeText(
                requireContext(),
                "No se pudo leer la imagen seleccionada",
                Toast.LENGTH_SHORT
            ).show()

            null
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding =
            null
    }

    companion object {

        private const val PREFS_NAME =
            "flora_care_prefs"

        private const val KEY_NOTIFICATIONS_ENABLED =
            "notifications_enabled"

        private const val KEY_USER_NAME =
            "user_name"

        private const val KEY_USER_EMAIL =
            "user_email"

        private const val KEY_USER_PHOTO =
            "user_photo_url"
    }
}
