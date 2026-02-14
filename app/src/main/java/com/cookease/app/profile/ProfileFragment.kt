package com.cookease.app.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.Recipe
import com.cookease.app.RecipeAdapter
import com.cookease.app.databinding.FragmentProfileBinding
import com.google.android.material.tabs.TabLayout

/**
 * ProfileFragment - Displays user profile and their recipes
 * Shows two tabs: "CREATED" (recipes user created) and "COOKED" (recipes user has cooked)
 *
 * TODO: API Integration Checklist
 * 1. Add ViewModel for user data and recipes
 * 2. Implement user authentication/session management
 * 3. Load user profile from API
 * 4. Load created recipes from API
 * 5. Load cooked recipes from API
 * 6. Add edit profile functionality
 * 7. Add logout functionality
 */
class ProfileFragment : Fragment() {

    // ==================== VIEW BINDING ====================
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // ==================== ADAPTER & DATA ====================
    private lateinit var recipeAdapter: RecipeAdapter
    private val createdRecipes = mutableListOf<Recipe>()
    private val cookedRecipes = mutableListOf<Recipe>()
    private var currentTab = TAB_CREATED

    // TODO: Add ViewModel here when integrating API
    // private val viewModel: ProfileViewModel by viewModels()

    // ==================== CONSTANTS ====================
    companion object {
        private const val TAB_CREATED = 0
        private const val TAB_COOKED = 1
    }

    // ==================== LIFECYCLE METHODS ====================
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
        loadUserProfile()
        loadCreatedRecipes()

        // TODO: Observe user profile and recipes from ViewModel
        // observeUserProfile()
        // observeCreatedRecipes()
        // observeCookedRecipes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== UI SETUP ====================
    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(createdRecipes) { recipe ->
            onRecipeClicked(recipe)
        }

        binding.rvProfileRecipes.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = recipeAdapter
        }
    }

    private fun setupTabs() {
        binding.tabLayoutProfile.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    TAB_CREATED -> {
                        currentTab = TAB_CREATED
                        showCreatedRecipes()
                    }
                    TAB_COOKED -> {
                        currentTab = TAB_COOKED
                        showCookedRecipes()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // TODO: Add edit profile button click listener
        // binding.btnEditProfile?.setOnClickListener {
        //     // Navigate to edit profile screen
        //     findNavController().navigate(R.id.action_profile_to_editProfile)
        // }

        // TODO: Add logout button click listener
        // binding.btnLogout?.setOnClickListener {
        //     viewModel.logout()
        //     // Navigate to login screen
        // }
    }

    // ==================== DATA LOADING ====================
    private fun loadUserProfile() {
        // TODO: Replace with actual API call
        // viewModel.loadUserProfile()
        loadSampleUserProfile()
    }

    private fun loadCreatedRecipes() {
        // TODO: Replace with actual API call
        // viewModel.loadCreatedRecipes()
        loadSampleCreatedRecipes()
    }

    private fun loadCookedRecipes() {
        // TODO: Replace with actual API call
        // viewModel.loadCookedRecipes()
        loadSampleCookedRecipes()
    }

    /**
     * TEMPORARY: Sample user profile loader
     * DELETE THIS METHOD when API is integrated
     */
    private fun loadSampleUserProfile() {
        // TODO: Update these views with real user data
        // binding.tvUserName?.text = "Juan Dela Cruz"
        // binding.tvUserEmail?.text = "juan@example.com"
        // binding.tvRecipeCount?.text = "${createdRecipes.size} Recipes"
        // binding.tvFollowers?.text = "128 Followers"
        // binding.tvFollowing?.text = "45 Following"
        // Glide.with(this).load(userProfileImageUrl).into(binding.ivUserAvatar)
    }

    /**
     * TEMPORARY: Sample created recipes loader
     * DELETE THIS METHOD when API is integrated
     */
    private fun loadSampleCreatedRecipes() {
        showLoading(true)
        createdRecipes.clear()

        // Sample recipes user created
        createdRecipes.addAll(
            listOf(
                Recipe(
                    id = "101",
                    title = "My Special Adobo",
                    description = "My family's secret adobo recipe",
                    image = "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400",
                    rating = 4.9f,
                    difficulty = "Easy",
                    cuisine = "Filipino"
                ),
                Recipe(
                    id = "102",
                    title = "Grandma's Sinigang",
                    description = "Traditional sinigang passed down from my lola",
                    image = "https://images.unsplash.com/photo-1591644571062-d43b471916f5?w=400",
                    rating = 4.8f,
                    difficulty = "Medium",
                    cuisine = "Filipino"
                )
            )
        )

        if (currentTab == TAB_CREATED) {
            recipeAdapter.updateRecipes(createdRecipes)
        }
        showLoading(false)
        showEmptyState(createdRecipes.isEmpty() && currentTab == TAB_CREATED)
    }

    /**
     * TEMPORARY: Sample cooked recipes loader
     * DELETE THIS METHOD when API is integrated
     */
    private fun loadSampleCookedRecipes() {
        showLoading(true)
        cookedRecipes.clear()

        // Sample recipes user has cooked
        cookedRecipes.addAll(
            listOf(
                Recipe(
                    id = "1",
                    title = "Chicken Adobo",
                    description = "Classic Filipino chicken braised in soy sauce and vinegar",
                    image = "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400",
                    rating = 4.8f,
                    difficulty = "Easy",
                    cuisine = "Filipino"
                ),
                Recipe(
                    id = "7",
                    title = "Bicol Express",
                    description = "Spicy pork stew cooked in coconut milk with chili peppers",
                    image = "https://images.unsplash.com/photo-1617196038278-1d8e9c0ed48f?w=400",
                    rating = 4.7f,
                    difficulty = "Hard",
                    cuisine = "Filipino"
                ),
                Recipe(
                    id = "10",
                    title = "Kare-Kare",
                    description = "Oxtail stew with peanut sauce served with vegetables and bagoong",
                    image = "https://images.unsplash.com/photo-1607076786357-1c0f35265f62?w=400",
                    rating = 4.6f,
                    difficulty = "Hard",
                    cuisine = "Filipino"
                )
            )
        )

        if (currentTab == TAB_COOKED) {
            recipeAdapter.updateRecipes(cookedRecipes)
        }
        showLoading(false)
        showEmptyState(cookedRecipes.isEmpty() && currentTab == TAB_COOKED)
    }

    // TODO: Add this method for API integration
    // private fun observeUserProfile() {
    //     viewModel.userProfile.observe(viewLifecycleOwner) { result ->
    //         when (result) {
    //             is Resource.Success -> {
    //                 val user = result.data
    //                 binding.tvUserName?.text = user.name
    //                 binding.tvUserEmail?.text = user.email
    //                 // Update other profile fields
    //             }
    //             is Resource.Error -> {
    //                 showError(result.message)
    //             }
    //             is Resource.Loading -> {
    //                 // Show loading state
    //             }
    //         }
    //     }
    // }

    // TODO: Add this method for API integration
    // private fun observeCreatedRecipes() {
    //     viewModel.createdRecipes.observe(viewLifecycleOwner) { result ->
    //         when (result) {
    //             is Resource.Success -> {
    //                 createdRecipes.clear()
    //                 createdRecipes.addAll(result.data)
    //                 if (currentTab == TAB_CREATED) {
    //                     recipeAdapter.updateRecipes(createdRecipes)
    //                     showEmptyState(createdRecipes.isEmpty())
    //                 }
    //             }
    //             is Resource.Error -> showError(result.message)
    //             is Resource.Loading -> showLoading(true)
    //         }
    //     }
    // }

    // TODO: Add this method for API integration
    // private fun observeCookedRecipes() {
    //     viewModel.cookedRecipes.observe(viewLifecycleOwner) { result ->
    //         when (result) {
    //             is Resource.Success -> {
    //                 cookedRecipes.clear()
    //                 cookedRecipes.addAll(result.data)
    //                 if (currentTab == TAB_COOKED) {
    //                     recipeAdapter.updateRecipes(cookedRecipes)
    //                     showEmptyState(cookedRecipes.isEmpty())
    //                 }
    //             }
    //             is Resource.Error -> showError(result.message)
    //             is Resource.Loading -> showLoading(true)
    //         }
    //     }
    // }

    // ==================== TAB SWITCHING ====================
    private fun showCreatedRecipes() {
        recipeAdapter.updateRecipes(createdRecipes)
        showEmptyState(createdRecipes.isEmpty())
    }

    private fun showCookedRecipes() {
        if (cookedRecipes.isEmpty()) {
            loadCookedRecipes()
        } else {
            recipeAdapter.updateRecipes(cookedRecipes)
            showEmptyState(cookedRecipes.isEmpty())
        }
    }

    // ==================== USER INTERACTIONS ====================
    private fun onRecipeClicked(recipe: Recipe) {
        // TODO: Navigate to recipe detail screen
        Toast.makeText(requireContext(), "Clicked: ${recipe.title}", Toast.LENGTH_SHORT).show()
        // val action = ProfileFragmentDirections.actionProfileToRecipeDetail(recipe.id)
        // findNavController().navigate(action)
    }

    // ==================== UI STATE HELPERS ====================
    private fun showLoading(show: Boolean) {
        // TODO: Add loading indicator to your layout
        // binding.loadingProgress?.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvProfileRecipes.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        // TODO: Add empty state view to your layout
        // val emptyMessage = when (currentTab) {
        //     TAB_CREATED -> "You haven't created any recipes yet.\nStart sharing your recipes!"
        //     TAB_COOKED -> "You haven't cooked any recipes yet.\nTry cooking something new!"
        //     else -> "No recipes found"
        // }
        // binding.emptyState?.visibility = if (show) View.VISIBLE else View.GONE
        // binding.emptyStateText?.text = emptyMessage
    }

    // TODO: Add error handling method
    // private fun showError(message: String?) {
    //     Toast.makeText(
    //         requireContext(),
    //         message ?: "Failed to load profile data",
    //         Toast.LENGTH_LONG
    //     ).show()
    // }
}