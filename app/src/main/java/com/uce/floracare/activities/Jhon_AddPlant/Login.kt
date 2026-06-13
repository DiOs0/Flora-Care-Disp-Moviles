package com.uce.floracare.activities.Jhon_AddPlant

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uce.floracare.activities.MainActivity
import com.uce.floracare.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {


    lateinit var binding : ActivityLoginBinding


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
            val email = binding.txtEmail.text.toString()
            val password = binding.txtPassword.text.toString()

            // Logica de autenticacion

            if(email == "a" && password == "a") {

                msg = "Usuario autentificado correctamente"

                val intentMain = Intent(
                    this,
                    MainActivity::class.java
                )
                Toast.makeText(this, msg,
                    Toast.LENGTH_SHORT).show()

                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

                startActivity(intentMain)
            }
            else{

                msg = "Usuario incorrecto, vuelva a internar!"

                Toast.makeText(this, msg, Toast.LENGTH_SHORT)
                    .show()

            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}