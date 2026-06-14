package com.uce.floracare.activities.Jhon_AddPlant

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uce.floracare.activities.Jhon_AddPlant.utils.AuthManager
import com.uce.floracare.activities.MainActivity
import com.uce.floracare.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {


    lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        


        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
    }


    private fun initListeners() {

        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        var msg = ""

        binding.btnLogin.setOnClickListener {
            val email = binding.txtEmail.text.toString().trim()
            val password = binding.txtPassword.text.toString().trim()

            // Logica de autenticacion

            if (email.isNotEmpty() && password.isNotEmpty()) {
                AuthManager().getAuthInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Login exitoso
                            val userId = AuthManager().getCurrentUserId()
                            val intent = Intent(this, MainActivity::class.java)
                            Toast.makeText(this, "Bienvenido: $userId", Toast.LENGTH_SHORT).show()
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this,
                                "Error: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }else {
                Toast.makeText(this, "Por favor, llena ambos campos", Toast.LENGTH_SHORT).show()
            }

        }




    }
}