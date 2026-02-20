package com.cookease.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cookease.app.ui.auth.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setupWithNavController(navController)

        // Handle Add button (middle nav item)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add -> {
                    navController.navigate(R.id.editRecipeFragment)
                    true
                }
                else -> {
                    androidx.navigation.ui.NavigationUI.onNavDestinationSelected(
                        item, navController
                    )
                    true
                }
            }
        }

        // Hide bottom nav on auth screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility = when (destination.id) {
                R.id.loginFragment,
                R.id.signupFragment,
                R.id.resetPasswordFragment -> View.GONE
                else -> View.VISIBLE
            }
        }

        // Check session on launch — skip login if session already exists
        lifecycleScope.launch {
            try {
                val session = SupabaseClientProvider.client.auth.currentSessionOrNull()
                if (session != null) {
                    val current = navController.currentDestination?.id
                    if (current == R.id.loginFragment) {
                        navController.navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}