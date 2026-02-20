package com.cookease.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.Resource
import com.cookease.app.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ==================== DATA MODELS ====================

data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val token: String
)

// ==================== AUTH STATE ====================

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: AuthUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

// ==================== VIEWMODEL ====================

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _loginResult = MutableLiveData<Resource<AuthUser>>()
    val loginResult: LiveData<Resource<AuthUser>> = _loginResult

    private val _registerResult = MutableLiveData<Resource<AuthUser>>()
    val registerResult: LiveData<Resource<AuthUser>> = _registerResult

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    init {
        checkLoginStatus()
    }

    // ==================== LOGIN ====================

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                val session = SupabaseClient.client.auth.currentSessionOrNull()
                val userId = session?.user?.id ?: ""
                val userEmail = session?.user?.email ?: email

                val user = AuthUser(
                    id = userId,
                    name = userEmail,
                    email = userEmail,
                    token = session?.accessToken ?: ""
                )

                _authState.value = AuthState.Success(user)
                _isLoggedIn.value = true

            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    when {
                        e.message?.contains("Email not confirmed") == true ->
                            "Please confirm your email before logging in"
                        e.message?.contains("Invalid login credentials") == true ->
                            "Invalid email or password"
                        else -> "Login failed: ${e.message}"
                    }
                )
            }
        }
    }

    // ==================== REGISTER ====================

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _registerResult.value = Resource.Loading()

                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                val user = AuthUser(
                    id = "",
                    name = name,
                    email = email,
                    token = ""
                )

                _registerResult.value = Resource.Success(user)

            } catch (e: Exception) {
                _registerResult.value = Resource.Error("Registration failed: ${e.message}")
            }
        }
    }

    // ==================== PASSWORD RESET ====================

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.resetPasswordForEmail(email)
            } catch (e: Exception) {
                // Error handled silently — dialog already shown
            }
        }
    }

    fun sendPasswordResetEmail(email: String) = sendPasswordReset(email)

    // ==================== SESSION ====================

    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                _isLoggedIn.value = session != null
            } catch (e: Exception) {
                _isLoggedIn.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
                _authState.value = AuthState.Idle
                _isLoggedIn.value = false
            } catch (e: Exception) {
                _authState.value = AuthState.Idle
                _isLoggedIn.value = false
            }
        }
    }

    fun getCurrentUser(): LiveData<AuthUser?> {
        val result = MutableLiveData<AuthUser?>()
        viewModelScope.launch {
            try {
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                result.value = session?.user?.let {
                    AuthUser(
                        id = it.id,
                        name = it.email ?: "",
                        email = it.email ?: "",
                        token = session.accessToken
                    )
                }
            } catch (e: Exception) {
                result.value = null
            }
        }
        return result
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}