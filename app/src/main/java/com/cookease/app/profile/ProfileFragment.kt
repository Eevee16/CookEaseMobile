package com.cookease.app.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.databinding.FragmentProfileBinding
import com.cookease.app.ui.auth.AuthViewModel
import com.cookease.app.ui_components.recipe.RecipeAdapter
import com.cookease.app.viewmodels.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabs()
        setupButtons()
        setupObservers()
        setupModeratorAccess()

        profileViewModel.fetchUserData()
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter { recipe ->
            val action = ProfileFragmentDirections.actionProfileToRecipeDetail(recipe.id)
            findNavController().navigate(action)
        }
        binding.rvUserRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvUserRecipes.adapter = recipeAdapter
    }

    private fun setupTabs() {
        binding.tabAll.setOnClickListener { setActiveTab("all") }
        binding.tabApproved.setOnClickListener { setActiveTab("approved") }
        binding.tabPending.setOnClickListener { setActiveTab("pending") }
        binding.tabRejected.setOnClickListener { setActiveTab("rejected") }
    }

    private fun setActiveTab(tab: String) {
        val activeColor = resources.getColor(R.color.purple_primary, null)
        val white = resources.getColor(android.R.color.white, null)
        val transparent = resources.getColor(android.R.color.transparent, null)

        listOf(binding.tabAll, binding.tabApproved, binding.tabPending, binding.tabRejected)
            .forEach { btn ->
                btn.setBackgroundColor(transparent)
                btn.setTextColor(activeColor)
            }

        val activeBtn = when (tab) {
            "approved" -> binding.tabApproved
            "pending" -> binding.tabPending
            "rejected" -> binding.tabRejected
            else -> binding.tabAll
        }
        activeBtn.setBackgroundColor(activeColor)
        activeBtn.setTextColor(white)

        profileViewModel.setTab(tab)
    }

    private fun setupButtons() {
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ -> profileViewModel.logout() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.editRecipeFragment)
        }

        binding.btnEditProfile.setOnClickListener {
            // TODO: Navigate to edit profile fragment
        }
    }

    private fun setupObservers() {
        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe

            binding.tvUserName.text = user.name.ifBlank { "User" }
            binding.tvUserEmail.text = user.email

            if (!user.photoUrl.isNullOrBlank()) {
                binding.profileImage.isVisible = true
                binding.tvInitials.isVisible = false
                Glide.with(this)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .circleCrop()
                    .into(binding.profileImage)
            } else {
                binding.profileImage.isVisible = false
                binding.tvInitials.isVisible = true
                val initials = user.name
                    .split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
                    .ifBlank { "U" }
                binding.tvInitials.text = initials
            }
        }

        profileViewModel.role.observe(viewLifecycleOwner) { role ->
            if (!role.isNullOrBlank()) {
                binding.tvRoleBadge.isVisible = true
                binding.tvRoleBadge.text = when (role) {
                    "moderator" -> "⭐ Moderator"
                    "admin" -> "🛡 Admin"
                    else -> "👤 User"
                }
            } else {
                binding.tvRoleBadge.isVisible = false
            }
        }

        profileViewModel.recipeStats.observe(viewLifecycleOwner) { stats ->
            binding.tvTotalRecipes.text = stats.total.toString()
            binding.tvApprovedRecipes.text = stats.approved.toString()
            binding.tvPendingRecipes.text = stats.pending.toString()
            binding.tvRejectedRecipes.text = stats.rejected.toString()

            binding.tabAll.text = "All (${stats.total})"
            binding.tabApproved.text = "Approved (${stats.approved})"
            binding.tabPending.text = "Pending (${stats.pending})"
            binding.tabRejected.text = "Rejected (${stats.rejected})"
        }

        profileViewModel.userRecipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)
            val isEmpty = recipes.isEmpty()
            binding.emptyState.isVisible = isEmpty
            binding.rvUserRecipes.isVisible = !isEmpty
        }

        profileViewModel.logoutResult.observe(viewLifecycleOwner) { success ->
            if (success) findNavController().navigate(R.id.action_profile_to_login)
        }
    }

    private fun setupModeratorAccess() {
        // Show/hide the whole card for moderators
        profileViewModel.role.observe(viewLifecycleOwner) { role ->
            binding.cardModeratorSection.isVisible = role == "moderator" || role == "admin"
        }

        binding.btnModeratorDashboard.setOnClickListener {
            findNavController().navigate(R.id.moderatorDashboardFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}