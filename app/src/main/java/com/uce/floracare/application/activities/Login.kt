package com.uce.floracare.application.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uce.floracare.R
import com.uce.floracare.application.fragments.LoginFragment
import com.uce.floracare.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .commit()
        }
    }
}