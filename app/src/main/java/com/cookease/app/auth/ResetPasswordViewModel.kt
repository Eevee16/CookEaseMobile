package com.cookease.app.auth

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cookease.app.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

class ResetPasswordViewModel(context: Context) : ViewModel() {

    private val _state = MutableLiveData<ResetPasswordState>(ResetPasswordState.Idle)
    val state: LiveData<ResetPasswordState> = _state

    private val _passwordStrength = MutableLiveData(0)
    val passwordStrength: LiveData<Int> = _passwordStrength

    fun calculateStrength(password: String) {
        var score = 0
        if (password.length >= 8) score++
        if (password.contains(Regex("[A-Z]"))) score++
        if (password.contains(Regex("[a-z]"))) score++
        if (password.contains(Regex("[0-9]"))) score++
        if (password.contains(Regex("[@$!%*?&]"))) score++
        _passwordStrength.value = score
    }

    fun getStrengthLabel(score: Int): String = when {
        score <= 2 -> "Weak"
        score <= 4 -> "Medium"
        else -> "Strong"
    }

    fun validateAndReset(password: String, confirm: String) {
        val validationError = validatePassword(password)
        if (validationError != null) {
            _state.value = ResetPasswordState.Error(validationError)
            return
        }
        
        if (password != confirm) {
            _state.value = ResetPasswordState.Error("Passwords do not match")
            return
        }
        _state.value = ResetPasswordState.Loading
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.modifyUser {
                    this.password = password
                }
                SupabaseClientProvider.client.auth.signOut()
                _state.value = ResetPasswordState.Success
            } catch (e: Exception) {
                _state.value = ResetPasswordState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.length < 8 -> "Password must be at least 8 characters"
            !password.contains(Regex("[A-Z]")) -> "Password must contain at least one uppercase letter"
            !password.contains(Regex("[a-z]")) -> "Password must contain at least one lowercase letter"
            !password.contains(Regex("[0-9]")) -> "Password must contain at least one number"
            !password.contains(Regex("[@$!%*?&]")) -> "Password must contain at least one special character (@$!%*?&)"
            else -> null
        }
    }
}

class ResetPasswordViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ResetPasswordViewModel(context) as T
    }
}