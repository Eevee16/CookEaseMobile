package com.cookease.app.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cookease.app.R
import com.cookease.app.databinding.FragmentLoginBinding
import com.cookease.app.Resource

/**
 * LoginFragment - User login screen
 *
 * Features:
 * - Email/password login
 * - Form validation
 * - Remember me checkbox
 * - Navigate to register
 * - Forgot password
 */
class LoginFragment : Fragment() {

    // ==================== VIEW BINDING ====================
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // ==================== VIEW MODEL ====================
    private val authViewModel: AuthViewModel by viewModels()

    // ==================== LIFECYCLE METHODS ====================
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
        setupClickListeners()
        observeLoginResult()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== UI SETUP ====================
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                login(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            navigateToRegister()
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        // TODO: Add social login buttons if needed
        // binding.btnGoogleLogin?.setOnClickListener {
        //     loginWithGoogle()
        // }
        // binding.btnFacebookLogin?.setOnClickListener {
        //     loginWithFacebook()
        // }
    }

    // ==================== LOGIN ====================
    private fun login(email: String, password: String) {
        val rememberMe = binding.cbRememberMe?.isChecked ?: false
        authViewModel.login(email, password, rememberMe)
    }

    private fun observeLoginResult() {
        authViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(result.message ?: "Login failed")
                }
                is Resource.Loading -> {
                    showLoading(true)
                }
            }
        }
    }

    // ==================== VALIDATION ====================
    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        // Validate email
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // Validate password
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    // ==================== NAVIGATION ====================
    private fun navigateToRegister() {
        // TODO: Add navigation to RegisterFragment
        // findNavController().navigate(R.id.action_login_to_register)
        Toast.makeText(requireContext(), "Navigate to Register", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        // 1. Perform the navigation using the Action ID from your nav_graph.xml
        try {
            findNavController().navigate(R.id.action_login_to_home)

            // 2. Helpful toast for feedback
            Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // If it fails, it usually means the ID in the XML doesn't match this ID
            Toast.makeText(requireContext(), "Navigation Error: Check Action ID", Toast.LENGTH_LONG).show()
        }
    }

    private fun showForgotPasswordDialog() {
        // TODO: Implement forgot password dialog/screen
        Toast.makeText(requireContext(), "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
    }

    // ==================== UI STATE HELPERS ====================
    private fun showLoading(show: Boolean) {
        binding.btnLogin.isEnabled = !show
        binding.progressBar?.visibility = if (show) View.VISIBLE else View.GONE

        if (show) {
            binding.btnLogin.text = "Logging in..."
        } else {
            binding.btnLogin.text = "Login"
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        // OR use Snackbar for better UX
        // Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    // TODO: Implement social login if needed
    // private fun loginWithGoogle() {
    //     // Implement Google Sign-In
    // }

    // private fun loginWithFacebook() {
    //     // Implement Facebook Login
    // }
}