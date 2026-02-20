package com.cookease.app.ui.auth

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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

        binding.btnUploadProfileImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSubmit.setOnClickListener {
            handleSignup()
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
    }

    private fun handleSignup() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill all fields.")
            return
        }

        if (firstName.length < 2 || lastName.length < 2) {
            showError("First and Last name must be at least 2 characters.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Invalid email address.")
            return
        }

        if (password.length < 6) {
            showError("Password must be at least 6 characters.")
            return
        }

        if (!password.contains(Regex("[A-Z]")) ||
            !password.contains(Regex("[a-z]")) ||
            !password.contains(Regex("[0-9]"))) {
            showError("Password must include uppercase, lowercase, and a number.")
            return
        }

        if (password != confirmPassword) {
            showError("Passwords do not match.")
            return
        }

        hideError()
        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.text = "Creating Account..."

        lifecycleScope.launch {
            try {
                val client = SupabaseClientProvider.client
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("first_name", firstName)
                        put("last_name", lastName)
                    }
                }

                val userId = client.auth.currentUserOrNull()?.id

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

                Snackbar.make(binding.root, "Account created! Check your email to confirm.", Snackbar.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_signupFragment_to_loginFragment)

            } catch (e: Exception) {
                val msg = e.message ?: "Signup failed."
                when {
                    msg.contains("rate limit", ignoreCase = true) ->
                        showError("Too many attempts. Please wait a minute and try again.")
                    msg.contains("already registered", ignoreCase = true) ->
                        showError("An account with this email already exists.")
                    else -> showError("Signup failed. Please try again.")
                }
            } finally {
                binding.btnSubmit.isEnabled = true
                binding.btnSubmit.text = "Create Account"
            }
        }
    }

    private fun showError(message: String) {
        binding.tvErrorMessage.text = message
        binding.tvErrorMessage.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvErrorMessage.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}