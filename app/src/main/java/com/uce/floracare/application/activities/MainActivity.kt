package com.uce.floracare.application.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.uce.floracare.R
import com.uce.floracare.application.fragments.AddPlantFragment
import com.uce.floracare.application.fragments.AjustesFragment
import com.uce.floracare.application.fragments.ExploreFragment
import com.uce.floracare.application.fragments.MiJardinFragment
import com.uce.floracare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_jardin
            loadFragment(MiJardinFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_jardin->{
                    loadFragment(MiJardinFragment())
                    true
                }
                R.id.nav_explorar -> {
                    loadFragment(ExploreFragment())
                    true
                }

                R.id.nav_add -> {
                    loadFragment(AddPlantFragment())
                    true
                }

                R.id.nav_ajustes -> {
                    loadFragment(AjustesFragment())
                    true
                }

                else -> false
            }
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Permitir volver atrás
            .commit()
    }

    fun setSelectedMenuItem(itemId: Int) {
        binding.bottomNavigation.selectedItemId = itemId
    }
}