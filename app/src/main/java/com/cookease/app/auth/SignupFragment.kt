package com.cookease.app.auth

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.SupabaseClientProvider
import com.cookease.app.databinding.FragmentSignupBinding
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private var profileImageUri: Uri? = null
    private var passwordStrength = 0

    // Letters, spaces, and only . , - ' allowed. Must start with a letter.
    private val nameRegex = Regex("^[a-zA-Z][a-zA-Z .,\\-']*$")

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
        if (uri != null) {
            binding.ivImagePreview.visibility = View.VISIBLE
            Glide.with(this).load(uri).circleCrop().into(binding.ivImagePreview)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        // Profile image upload
        binding.btnUploadProfileImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Password strength indicator (matching web behavior)
        binding.etPassword.addTextChangedListener { text ->
            val password = text?.toString() ?: ""
            if (password.isNotEmpty()) {
                passwordStrength = calculatePasswordStrength(password)
                updatePasswordStrengthUI(passwordStrength)
                binding.passwordStrengthContainer.visibility = View.VISIBLE
            } else {
                binding.passwordStrengthContainer.visibility = View.GONE
                passwordStrength = 0
            }
        }

        // Confirm password matching check
        binding.etConfirmPassword.addTextChangedListener { text ->
            val password = binding.etPassword.text?.toString() ?: ""
            val confirm = text?.toString() ?: ""
            // Show mismatch indicator if needed (optional visual feedback)
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            handleSignup()
        }

        // Login link
        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
    }

    /**
     * Calculate password strength (0-6 scale) - matches web implementation
     */
    private fun calculatePasswordStrength(password: String): Int {
        var score = 0
        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.contains(Regex("[A-Z]"))) score++  // Uppercase
        if (password.contains(Regex("[a-z]"))) score++  // Lowercase
        if (password.contains(Regex("\\d"))) score++     // Digit
        if (password.contains(Regex("[@$!%*?&]"))) score++  // Special char
        return score
    }

    /**
     * Get strength label - matches web: Weak, Medium, Strong
     */
    private fun getStrengthLabel(score: Int): String = when {
        score <= 2 -> "Weak"
        score <= 4 -> "Medium"
        else -> "Strong"
    }

    /**
     * Update password strength visual indicator
     */
    private fun updatePasswordStrengthUI(score: Int) {
        val label = getStrengthLabel(score)
        val (color, barWidth) = when {
            score <= 2 -> Pair(Color.parseColor("#EF4444"), 0.33f) // Red/Weak
            score <= 4 -> Pair(Color.parseColor("#F59E0B"), 0.66f) // Orange/Medium
            else -> Pair(Color.parseColor("#10B981"), 1.0f)        // Green/Strong
        }

        binding.tvPasswordStrengthLabel.text = "Password strength: $label"
        binding.tvPasswordStrengthLabel.setTextColor(color)

        // Animate strength bar width
        val strengthBar = binding.strengthBar
        val parent = strengthBar.parent as View
        val parentWidth = parent.width
        if (parentWidth > 0) {
            val params = strengthBar.layoutParams
            params.width = (parentWidth * barWidth).toInt()
            strengthBar.layoutParams = params
        }
        strengthBar.progress = score
    }

    private fun handleSignup() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName  = binding.etLastName.text.toString().trim()
        val email     = binding.etEmail.text.toString().trim()
        val password  = binding.etPassword.text.toString()
        val confirm   = binding.etConfirmPassword.text.toString()

        // Clear previous error
        hideError()

        // ── Empty fields ─────────────────────────────────────────────
        if (firstName.isEmpty()) { showError("Please enter your first name."); return }
        if (lastName.isEmpty())  { showError("Please enter your last name."); return }
        if (email.isEmpty())     { showError("Please enter your email address."); return }
        if (password.isEmpty())  { showError("Please enter a password."); return }
        if (confirm.isEmpty())   { showError("Please confirm your password."); return }

        // ── First name ────────────────────────────────────────────────
        if (firstName.length < 2) {
            showError("First name is too short — needs at least 2 characters.")
            return
        }
        if (!nameRegex.matches(firstName)) {
            showError("First name can only have letters. Allowed symbols: . , - '")
            return
        }

        // ── Last name ─────────────────────────────────────────────────
        if (lastName.length < 2) {
            showError("Last name is too short — needs at least 2 characters.")
            return
        }
        if (!nameRegex.matches(lastName)) {
            showError("Last name can only have letters. Allowed symbols: . , - '")
            return
        }

        // ── Email ─────────────────────────────────────────────────────
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("That email doesn't look right. Please double-check it.")
            return
        }

        // ── Password rules (matching web) ─────────────────────────────
        if (password.length < 6) {
            showError("Password must be at least 6 characters long.")
            return
        }

        // Check for uppercase, lowercase, and number (matching web validation)
        if (!password.contains(Regex("[A-Z]"))) {
            showError("Password must include at least one uppercase letter.")
            return
        }
        if (!password.contains(Regex("[a-z]"))) {
            showError("Password must include at least one lowercase letter.")
            return
        }
        if (!password.contains(Regex("\\d"))) {
            showError("Password must include at least one number.")
            return
        }

        // ── Confirm password ──────────────────────────────────────────
        if (password != confirm) {
            showError("Passwords do not match.")
            return
        }

        // ── All good, submit ──────────────────────────────────────────
        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.text = "Creating Account..."

        lifecycleScope.launch {
            try {
                val client = SupabaseClientProvider.client

                // 1. Create auth account with metadata (matching web)
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("first_name", firstName)
                        put("last_name", lastName)
                    }
                }

                val userId = client.auth.currentUserOrNull()?.id

                // 2. Save profile data to profiles table
                if (userId != null) {
                    try {
                        client.postgrest.from("profiles").upsert(
                            buildJsonObject {
                                put("id", userId)
                                put("email", email)
                                put("first_name", firstName)
                                put("last_name", lastName)
                            }
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // 3. Upload profile photo if selected (matching web)
                if (profileImageUri != null && userId != null) {
                    try {
                        val contentResolver = requireContext().contentResolver
                        val mimeType = contentResolver.getType(profileImageUri!!) ?: "image/jpeg"
                        val ext = mimeType.substringAfter("/")
                        val filePath = "$userId/avatar.$ext"
                        val bytes = contentResolver.openInputStream(profileImageUri!!)?.readBytes()

                        if (bytes != null) {
                            client.storage.from("avatars").upload(filePath, bytes, upsert = true)
                            val publicUrl = client.storage.from("avatars").publicUrl(filePath)

                            client.postgrest.from("profiles").update({
                                set("photo_url", publicUrl)
                            }) {
                                filter { eq("id", userId) }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Show success message (matching web)
                Snackbar.make(
                    binding.root,
                    "Account created! Check your email to confirm.",
                    Snackbar.LENGTH_LONG
                ).show()

                // Navigate to login
                findNavController().navigate(R.id.action_signupFragment_to_loginFragment)

            } catch (e: Exception) {
                val msg = e.message ?: ""
                when {
                    msg.contains("network", ignoreCase = true) ||
                            msg.contains("Unable to resolve", ignoreCase = true) ||
                            msg.contains("ConnectException", ignoreCase = true) ->
                        showError("App is offline. Check your network.")
                    msg.contains("rate limit", ignoreCase = true) ->
                        showError("Too many signup attempts. Please wait a minute and try again.")
                    msg.contains("already registered", ignoreCase = true) ||
                            msg.contains("User already registered", ignoreCase = true) ->
                        showError("This email is already registered. Try logging in instead.")
                    msg.contains("invalid email", ignoreCase = true) ->
                        showError("Invalid email address.")
                    else ->
                        showError("Signup failed: $msg")
                }
            } finally {
                binding.btnSubmit.isEnabled = true
                binding.btnSubmit.text = "Create Account"
            }
        }
    }

    private fun showError(message: String) {
        binding.tvErrorMessage.text = message
        binding.cardError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.cardError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
