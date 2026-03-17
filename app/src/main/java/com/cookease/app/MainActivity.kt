package com.cookease.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cookease.app.auth.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        // Handle Deep Link for Password Reset
        handleIntent(intent)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val appLinkAction = intent?.action
        val appLinkData = intent?.data
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            if (appLinkData.scheme == "cookease" && appLinkData.host == "reset-password") {
                
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController

                // Extract tokens from the fragment part of the URL (#access_token=...)
                val fragment = appLinkData.fragment
                if (!fragment.isNullOrEmpty()) {
                    val params = fragment.split("&").associate {
                        val parts = it.split("=")
                        parts[0] to (parts.getOrNull(1) ?: "")
                    }
                    
                    val accessToken = params["access_token"]
                    val refreshToken = params["refresh_token"]
                    
                    if (!accessToken.isNullOrEmpty()) {
                        lifecycleScope.launch {
                            try {
                                // 1. Create and import the session
                                val session = UserSession(
                                    accessToken = accessToken,
                                    refreshToken = refreshToken ?: "",
                                    expiresIn = 3600,
                                    tokenType = "bearer",
                                    user = null
                                )
                                SupabaseClientProvider.client.auth.importSession(session)
                                
                                // 2. Critical: Fetch user info so 'modifyUser' has a valid 'sub' (User ID)
                                SupabaseClientProvider.client.auth.retrieveUserForCurrentSession(updateSession = true)
                                
                                withContext(Dispatchers.Main) {
                                    navController.navigate(R.id.resetPasswordFragment)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    navController.navigate(R.id.resetPasswordFragment)
                                }
                            }
                        }
                    } else {
                        navController.navigate(R.id.resetPasswordFragment)
                    }
                } else {
                    navController.navigate(R.id.resetPasswordFragment)
                }
            }
        }
    }
}