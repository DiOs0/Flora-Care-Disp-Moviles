package com.uce.floracare.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.uce.floracare.R
import com.uce.floracare.databinding.ActivityMainBinding
import com.uce.floracare.activities.AuxiliarFragment
import com.uce.floracare.activities.Jhon_AddPlant.AddPlantFragment
import com.uce.floracare.activities.Milan_Ajustes.AjustesFragment
import com.uce.floracare.activities.Osorio_Explore.ExploreFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_explorar
            loadFragment(ExploreFragment())
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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}