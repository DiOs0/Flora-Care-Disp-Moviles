package com.uce.floracare.activities.Milan_Ajustes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uce.floracare.activities.Jhon_AddPlant.Login
import com.uce.floracare.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
    }

    private fun initListeners() {

        binding.btnComenzar.setOnClickListener {

            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}