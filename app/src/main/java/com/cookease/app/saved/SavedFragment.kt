package com.cookease.app.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.Recipe
import com.cookease.app.RecipeAdapter
import com.cookease.app.databinding.FragmentSavedBinding

/**
 * SavedFragment - Displays user's saved/favorited recipes
 *
 * TODO: API Integration Checklist
 * 1. Add ViewModel for saved recipes
 * 2. Implement local database (Room) for offline saved recipes
 * 3. Add sync with backend API
 * 4. Implement remove from saved functionality
 * 5. Add pull-to-refresh
 * 6. Handle empty state when no saved recipes
 */
class SavedFragment : Fragment() {

    // ==================== VIEW BINDING ====================
    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    // ==================== ADAPTER & DATA ====================
    private lateinit var recipeAdapter: RecipeAdapter
    private val savedRecipes = mutableListOf<Recipe>()

    // TODO: Add ViewModel here when integrating API
    // private val viewModel: SavedViewModel by viewModels()

    // TODO: Add Room Database DAO
    // private lateinit var recipeDao: RecipeDao

    // ==================== LIFECYCLE METHODS ====================
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadSavedRecipes()

        // TODO: Observe saved recipes from ViewModel
        // observeSavedRecipes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== UI SETUP ====================
    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(savedRecipes) { recipe ->
            onRecipeClicked(recipe)
        }

        binding.rvSavedRecipes.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = recipeAdapter

            // TODO: Add swipe-to-delete gesture for removing saved recipes
            // val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
            // itemTouchHelper.attachToRecyclerView(this)
        }

        // TODO: Add pull-to-refresh
        // binding.swipeRefreshLayout?.setOnRefreshListener {
        //     loadSavedRecipes()
        // }
    }

    // ==================== DATA LOADING ====================
    private fun loadSavedRecipes() {
        // TODO: Replace with actual database/API call
        // Option 1: Load from local Room database
        // viewModel.loadSavedRecipes()

        // Option 2: Sync with backend API
        // viewModel.syncSavedRecipes()

        loadSampleSavedRecipes()
    }

    /**
     * TEMPORARY: Sample data loader
     * DELETE THIS METHOD when API/Database is integrated
     */
    private fun loadSampleSavedRecipes() {
        showLoading(true)
        savedRecipes.clear()

        // Sample saved recipes - Replace with actual data
        savedRecipes.addAll(
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
                    id = "10",
                    title = "Kare-Kare",
                    description = "Oxtail stew with peanut sauce served with vegetables and bagoong",
                    image = "https://images.unsplash.com/photo-1607076786357-1c0f35265f62?w=400",
                    rating = 4.6f,
                    difficulty = "Hard",
                    cuisine = "Filipino"
                ),
                Recipe(
                    id = "5",
                    title = "Halo-Halo",
                    description = "Refreshing Filipino shaved ice dessert with sweet beans and fruits",
                    image = "https://images.unsplash.com/photo-1563805042-7684a24f32e8?w=400",
                    rating = 4.4f,
                    difficulty = "Easy",
                    cuisine = "Filipino"
                )
            )
        )

        recipeAdapter.updateRecipes(savedRecipes)
        showLoading(false)
        showEmptyState(savedRecipes.isEmpty())
    }

    // TODO: Add this method for API/Database integration
    // private fun observeSavedRecipes() {
    //     viewModel.savedRecipes.observe(viewLifecycleOwner) { result ->
    //         when (result) {
    //             is Resource.Success -> {
    //                 savedRecipes.clear()
    //                 savedRecipes.addAll(result.data)
    //                 recipeAdapter.updateRecipes(savedRecipes)
    //                 showLoading(false)
    //                 showEmptyState(savedRecipes.isEmpty())
    //             }
    //             is Resource.Error -> {
    //                 showLoading(false)
    //                 showError(result.message)
    //             }
    //             is Resource.Loading -> {
    //                 showLoading(true)
    //             }
    //         }
    //     }
    // }

    // ==================== USER INTERACTIONS ====================
    private fun onRecipeClicked(recipe: Recipe) {
        // TODO: Navigate to recipe detail screen
        Toast.makeText(requireContext(), "Clicked: ${recipe.title}", Toast.LENGTH_SHORT).show()
        // val action = SavedFragmentDirections.actionSavedToRecipeDetail(recipe.id)
        // findNavController().navigate(action)
    }

    // TODO: Implement remove from saved
    // private fun removeFromSaved(recipe: Recipe) {
    //     viewModel.removeFromSaved(recipe.id)
    //     Toast.makeText(requireContext(), "${recipe.title} removed from saved", Toast.LENGTH_SHORT).show()
    // }

    // TODO: Add swipe-to-delete callback
    // private val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(
    //     0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    // ) {
    //     override fun onMove(...): Boolean = false
    //
    //     override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    //         val position = viewHolder.adapterPosition
    //         val recipe = savedRecipes[position]
    //         removeFromSaved(recipe)
    //     }
    // }

    // ==================== UI STATE HELPERS ====================
    private fun showLoading(show: Boolean) {
        // TODO: Add loading indicator to your layout
        // binding.loadingProgress?.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvSavedRecipes.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        // TODO: Add empty state view to your layout
        // binding.emptyState?.visibility = if (show) View.VISIBLE else View.GONE
        // binding.emptyStateText?.text = "No saved recipes yet.\nStart saving your favorites!"
    }

    // TODO: Add error handling method
    // private fun showError(message: String?) {
    //     Toast.makeText(
    //         requireContext(),
    //         message ?: "Failed to load saved recipes",
    //         Toast.LENGTH_LONG
    //     ).show()
    // }
}