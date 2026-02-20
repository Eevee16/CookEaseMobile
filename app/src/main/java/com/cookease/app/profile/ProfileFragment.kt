package com.cookease.app.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.databinding.FragmentProfileBinding
import com.cookease.app.ui.recipe.RecipeAdapter
import com.cookease.app.viewmodels.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by activityViewModels()
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

        recipeAdapter = RecipeAdapter { recipe ->
            val action = ProfileFragmentDirections.actionProfileToRecipeDetail(recipe.id)
            findNavController().navigate(action)
        }
        binding.rvUserRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvUserRecipes.adapter = recipeAdapter

        // Observe user data
        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = user.name.ifBlank { "Anonymous" }
                binding.tvUserEmail.text = user.email

                if (!user.photoUrl.isNullOrBlank()) {
                    Glide.with(this)
                        .load(user.photoUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .circleCrop()
                        .into(binding.profileImage)
                }
            }
        }

        // Observe recipe stats
        profileViewModel.recipeStats.observe(viewLifecycleOwner) { stats ->
            binding.tvTotalRecipes.text = "Total: ${stats.total}"
            binding.tvApprovedRecipes.text = "Approved: ${stats.approved}"
            binding.tvPendingRecipes.text = "Pending: ${stats.pending}"
            binding.tvRejectedRecipes.text = "Rejected: ${stats.rejected}"
        }

        // Observe user recipes
        profileViewModel.userRecipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)
            showEmptyState(recipes.isEmpty())
        }

        // Observe logout
        profileViewModel.logoutResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().navigate(R.id.action_profile_to_login)
            }
        }

        profileViewModel.fetchUserData()

        binding.btnLogout.setOnClickListener {
            profileViewModel.logout()
        }

        binding.btnEditProfile.setOnClickListener {
            // TODO: Navigate to Edit Profile screen
        }

        binding.btnAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.editRecipeFragment)
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvUserRecipes.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}