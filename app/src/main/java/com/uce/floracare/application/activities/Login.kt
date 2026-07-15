package com.uce.floracare.application.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uce.floracare.R
import com.uce.floracare.application.fragments.LoginFragment
import com.uce.floracare.databinding.ActivityLoginBinding
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Verificación de Sesión Automática
        val authManager = AuthManager()
        if (authManager.getCurrentUserId() != null) {
            // Usuario ya autenticado, redirigir a MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Evitar que el usuario regrese al Login con el botón Atrás
            return // Detener la ejecución de onCreate
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .commit()
        }
    }
}