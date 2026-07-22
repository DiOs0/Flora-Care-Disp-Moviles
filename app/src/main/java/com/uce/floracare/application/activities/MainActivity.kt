package com.uce.floracare.application.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.uce.floracare.R
import com.uce.floracare.application.fragments.AddPlantFragment
import com.uce.floracare.application.fragments.AjustesFragment
import com.uce.floracare.application.fragments.ExploreFragment
import com.uce.floracare.application.fragments.MiJardinFragment
import com.uce.floracare.data.notifications.NotificationHelper
import com.uce.floracare.data.notifications.WelcomeNotification
import com.uce.floracare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notificationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                showFirstInstallNotification()
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityMainBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        NotificationHelper.createChannel(
            this
        )

        setupBottomNavigation()

        if (savedInstanceState == null) {

            val openedFromNotification =
                handleNotificationIntent(
                    intent
                )

            if (!openedFromNotification) {

                binding.bottomNavigation.selectedItemId =
                    R.id.nav_jardin
            }
        }

        requestNotificationPermission()
    }

    private fun setupBottomNavigation() {

        binding.bottomNavigation
            .setOnItemSelectedListener { menuItem ->

                when (menuItem.itemId) {

                    R.id.nav_jardin -> {

                        loadFragment(
                            MiJardinFragment()
                        )

                        true
                    }

                    R.id.nav_explorar -> {

                        loadFragment(
                            ExploreFragment()
                        )

                        true
                    }

                    R.id.nav_add -> {

                        loadFragment(
                            AddPlantFragment()
                        )

                        true
                    }

                    R.id.nav_ajustes -> {

                        loadFragment(
                            AjustesFragment()
                        )

                        true
                    }

                    else -> false
                }
            }
    }

    fun loadFragment(
        fragment: Fragment
    ) {

        if (isFinishing || isDestroyed) {
            return
        }

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_container,
                fragment
            )
            .addToBackStack(null)
            .commit()
    }

    fun setSelectedMenuItem(
        itemId: Int
    ) {

        binding.bottomNavigation.selectedItemId =
            itemId
    }

    private fun requestNotificationPermission() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                notificationPermission.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )

            } else {

                showFirstInstallNotification()
            }

        } else {

            showFirstInstallNotification()
        }
    }

    private fun showFirstInstallNotification() {

        val prefs =
            getSharedPreferences(
                "floracare",
                MODE_PRIVATE
            )

        val first =
            prefs.getBoolean(
                "first_install",
                true
            )

        if (first) {

            WelcomeNotification.show(
                this
            )

            prefs.edit()
                .putBoolean(
                    "first_install",
                    false
                )
                .apply()
        }
    }

    private fun handleNotificationIntent(
        intent: Intent?
    ): Boolean {

        val destination =
            intent?.getStringExtra(
                "navigate_to"
            )

        if (destination != "mi_jardin") {
            return false
        }

        binding.bottomNavigation.selectedItemId =
            R.id.nav_jardin

        intent.removeExtra(
            "navigate_to"
        )

        return true
    }

    override fun onNewIntent(
        intent: Intent
    ) {
        super.onNewIntent(intent)

        setIntent(intent)

        handleNotificationIntent(
            intent
        )
    }
}