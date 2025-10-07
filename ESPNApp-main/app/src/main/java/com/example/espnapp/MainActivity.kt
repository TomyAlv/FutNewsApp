package com.example.espnapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.espnapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isDarkModeEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme BEFORE inflating views
        isDarkModeEnabled = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getBoolean(KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ActionBar
        setSupportActionBar(binding.decorToolbar)
        binding.decorToolbar.title = "ESPN"

        // NavController
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        // Link tabs with the Navigation Component
        binding.bottomNavigation.setupWithNavController(navController)

        // Intercept the "action_toggle_theme" item to avoid navigation and toggle the theme
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_toggle_theme -> {
                    // Alternar y persistir
                    isDarkModeEnabled = !isDarkModeEnabled
                    persistThemePreference(isDarkModeEnabled)
                    AppCompatDelegate.setDefaultNightMode(
                        if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                    // Returning false prevents BottomNavigation from marking it as selected
                    false
                }
                else -> {
                    // For the other items, let Navigation handle navigation
                    NavigationUI.onNavDestinationSelected(item, navController)
                }
            }
        }

        // Optional: do nothing on reselection (prevents reloading the current fragment)
        binding.bottomNavigation.setOnItemReselectedListener { /* no-op */ }
    }

    private fun persistThemePreference(isDarkMode: Boolean) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit {
            putBoolean(KEY_DARK_MODE, isDarkMode)
        }
    }

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}
