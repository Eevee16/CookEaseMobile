package com.cookease.app.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.databinding.FragmentEditProfileBinding
import com.cookease.app.viewmodels.ProfileViewModel

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Glide.with(this).load(uri).circleCrop().into(binding.ivProfileImage)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        // Pre-fill current data from User object
        viewModel.user.value?.let { user ->
            binding.etDisplayName.setText(user.name)
            binding.etFirstName.setText(user.firstName)
            binding.etLastName.setText(user.lastName)
            
            Glide.with(this)
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_placeholder)
                .circleCrop()
                .into(binding.ivProfileImage)
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val displayName = binding.etDisplayName.text.toString().trim()
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()

        // Validation - at least some name is needed
        if (displayName.isEmpty() && firstName.isEmpty()) {
            binding.tilDisplayName.error = "Please provide a display name or first name"
            return
        }
        binding.tilDisplayName.error = null

        binding.loadingOverlay.isVisible = true
        
        var imageBytes: ByteArray? = null
        var mimeType: String? = null
        
        selectedImageUri?.let { uri ->
            try {
                val contentResolver = requireContext().contentResolver
                imageBytes = contentResolver.openInputStream(uri)?.readBytes()
                mimeType = contentResolver.getType(uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModel.updateProfile(firstName, lastName, displayName, imageBytes, mimeType)
    }

    private fun setupObservers() {
        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            binding.loadingOverlay.isVisible = false
            result.onSuccess {
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
