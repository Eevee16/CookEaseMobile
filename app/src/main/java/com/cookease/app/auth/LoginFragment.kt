package com.cookease.app.ui.auth

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cookease.app.R
import com.cookease.app.databinding.FragmentLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.tilPassword.setEndIconOnClickListener {
            togglePasswordVisibility()
        }

        binding.etEmail.addTextChangedListener {
            binding.cardError.visibility = View.GONE
            binding.tilEmail.error = null
        }

        binding.etPassword.addTextChangedListener {
            binding.cardError.visibility = View.GONE
            binding.tilPassword.error = null
            val text = it.toString()
            if (text.isNotEmpty()) {
                binding.tvPasswordStrength.visibility = View.VISIBLE
                updatePasswordStrength(text)
            } else {
                binding.tvPasswordStrength.visibility = View.GONE
            }
        }

        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.btnForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> setLoadingState(true)
                    is AuthState.Success -> {
                        setLoadingState(false)
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                    is AuthState.Error -> {
                        setLoadingState(false)
                        val friendlyMessage = when {
                            state.message.contains("Invalid login credentials", ignoreCase = true) ->
                                "Incorrect password. Please try again."
                            state.message.contains("Email not confirmed", ignoreCase = true) ->
                                "Please confirm your email before logging in."
                            state.message.contains("User not found", ignoreCase = true) ||
                                    state.message.contains("invalid email", ignoreCase = true) ->
                                "No account found with this email address."
                            state.message.contains("rate limit", ignoreCase = true) ->
                                "Too many attempts. Please wait a minute and try again."
                            state.message.contains("network", ignoreCase = true) ||
                                    state.message.contains("Unable to resolve", ignoreCase = true) ->
                                "No internet connection. Please check your network."
                            else -> "Login failed: ${state.message}"
                        }
                        showError(friendlyMessage)
                        when {
                            state.message.contains("password", ignoreCase = true) ->
                                binding.tilPassword.error = "Incorrect password"
                            state.message.contains("email", ignoreCase = true) ||
                                    state.message.contains("User not found", ignoreCase = true) ->
                                binding.tilEmail.error = "No account found with this email"
                        }
                    }
                    else -> setLoadingState(false)
                }
            }
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (!isValidEmail(email)) {
            binding.tilEmail.error = "Please enter a valid email address"
            return
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return
        } else {
            binding.tilPassword.error = null
        }

        binding.cardError.visibility = View.GONE
        viewModel.login(email, password)
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            binding.etPassword.transformationMethod = null
            binding.tilPassword.setEndIconDrawable(R.drawable.ic_eye_visible)
        } else {
            binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.tilPassword.setEndIconDrawable(R.drawable.ic_eye_hidden)
        }
        binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
    }

    private fun showForgotPasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_forgot_password, null)

        val emailInput = dialogView.findViewById<TextInputLayout>(R.id.tilEmail)
        val emailEditText = dialogView.findViewById<EditText>(R.id.etEmail)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reset_password_title)
            .setMessage(R.string.reset_password_message)
            .setView(dialogView)
            .setPositiveButton(R.string.send_reset_link, null)
            .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val email = emailEditText.text.toString().trim()
                if (email.isEmpty()) {
                    emailInput.error = "Email is required"
                    return@setOnClickListener
                }
                if (!isValidEmail(email)) {
                    emailInput.error = "Enter a valid email address"
                    return@setOnClickListener
                }
                emailInput.error = null
                positiveButton.isEnabled = false
                positiveButton.text = getString(R.string.sending)
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.sendPasswordReset(email)
                    dialog.dismiss()
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Email Sent")
                        .setMessage("Password reset email sent! Check your inbox.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
        dialog.show()
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.cardError.visibility = View.VISIBLE
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) {
            getString(R.string.logging_in)
        } else {
            getString(R.string.login)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun updatePasswordStrength(password: String) {
        val strength = calculatePasswordStrength(password)
        val (label, color) = when {
            strength <= 2 -> Pair("Weak", R.color.error)
            strength <= 4 -> Pair("Medium", R.color.warning)
            else -> Pair("Strong", R.color.success)
        }
        binding.tvPasswordStrength.text = "Password strength: $label"
        binding.tvPasswordStrength.setTextColor(resources.getColor(color, null))
    }

    private fun calculatePasswordStrength(password: String): Int {
        var strength = 0
        if (password.length >= 6) strength++
        if (password.length >= 8) strength++
        if (password.contains(Regex("[A-Z]"))) strength++
        if (password.contains(Regex("[a-z]"))) strength++
        if (password.contains(Regex("[0-9]"))) strength++
        if (password.contains(Regex("[@$!%*?&]"))) strength++
        return strength
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetState()
        _binding = null
    }
}