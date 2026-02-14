package com.cookease.app.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.R
import com.cookease.app.Recipe
import com.cookease.app.RecipeAdapter
import com.cookease.app.databinding.FragmentSearchBinding
import com.google.android.material.tabs.TabLayout

/**
 * SearchFragment - Advanced recipe search with filters and categories
 * Shows tabs: "Popular", "Most Liked", "Recent"
 * Includes filters for cuisine and ingredients
 *
 * TODO: API Integration Checklist
 * 1. Add ViewModel for search logic
 * 2. Implement search API endpoint
 * 3. Add filter by cuisine API call
 * 4. Add filter by ingredients API call
 * 5. Implement debounced search (wait for user to stop typing)
 * 6. Add search history/suggestions
 * 7. Add pagination for search results
 */
class SearchFragment : Fragment() {

    // ==================== VIEW BINDING ====================
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    // ==================== ADAPTER & DATA ====================
    private lateinit var recipeAdapter: RecipeAdapter
    private val recipes = mutableListOf<Recipe>()
    private var currentCategory = CATEGORY_POPULAR
    private var currentCuisineFilter: String? = null
    private var currentIngredientsFilter: List<String>? = null

    // TODO: Add ViewModel here when integrating API
    // private val viewModel: SearchViewModel by viewModels()

    // ==================== CONSTANTS ====================
    companion object {
        private const val CATEGORY_POPULAR = "popular"
        private const val CATEGORY_MOST_LIKED = "most_liked"
        private const val CATEGORY_RECENT = "recent"

        private const val TAB_POPULAR = 0
        private const val TAB_MOST_LIKED = 1
        private const val TAB_RECENT = 2
    }

    // ==================== LIFECYCLE METHODS ====================
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchBar()
        setupTabs()
        setupFilterButton()
        loadRecipes(CATEGORY_POPULAR)

        // TODO: Observe search results from ViewModel
        // observeSearchResults()
        // observeFilteredRecipes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== UI SETUP ====================
    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(recipes) { recipe ->
            onRecipeClicked(recipe)
        }

        // TODO: Add RecyclerView to your layout if not present
        // binding.rvSearchResults?.apply {
        //     layoutManager = GridLayoutManager(requireContext(), 2)
        //     adapter = recipeAdapter
        //
        //     // Add scroll listener for pagination
        //     addOnScrollListener(object : RecyclerView.OnScrollListener() {
        //         override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        //             // Load more when reaching bottom
        //         }
        //     })
        // }
    }

    private fun setupSearchBar() {
        // TODO: Add search EditText to your layout if not present
        // binding.searchEditText?.addTextChangedListener { text ->
        //     val query = text.toString()
        //     if (query.isNotEmpty()) {
        //         // Debounce search - wait 500ms after user stops typing
        //         searchHandler.removeCallbacks(searchRunnable)
        //         searchRunnable = Runnable { performSearch(query) }
        //         searchHandler.postDelayed(searchRunnable, 500)
        //     } else {
        //         // Show all recipes when search is cleared
        //         loadRecipes(currentCategory)
        //     }
        // }

        // binding.searchButton?.setOnClickListener {
        //     val query = binding.searchEditText?.text.toString()
        //     if (query.isNotEmpty()) {
        //         performSearch(query)
        //     }
        // }
    }

    private fun setupTabs() {
        binding.tabLayout.apply {
            removeAllTabs()
            addTab(newTab().setText("Popular"))
            addTab(newTab().setText("Most Liked"))
            addTab(newTab().setText("Recent"))

            // Select first tab after layout is ready
            post {
                getTabAt(TAB_POPULAR)?.select()
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    currentCategory = when (it.position) {
                        TAB_POPULAR -> CATEGORY_POPULAR
                        TAB_MOST_LIKED -> CATEGORY_MOST_LIKED
                        TAB_RECENT -> CATEGORY_RECENT
                        else -> CATEGORY_POPULAR
                    }
                    loadRecipes(currentCategory)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.filter_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.filter_cuisine -> {
                        showCuisineFilterDialog()
                        true
                    }
                    R.id.filter_ingredients -> {
                        showIngredientsFilterDialog()
                        true
                    }
                    R.id.filter_difficulty -> {
                        // TODO: Add difficulty filter
                        showDifficultyFilterDialog()
                        true
                    }
                    R.id.filter_clear -> {
                        clearAllFilters()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    // ==================== DATA LOADING ====================
    private fun loadRecipes(category: String) {
        // TODO: Replace with actual API call
        // viewModel.loadRecipesByCategory(category, currentCuisineFilter, currentIngredientsFilter)
        loadSampleRecipes(category)
    }

    private fun performSearch(query: String) {
        // TODO: Replace with actual API search
        // viewModel.searchRecipes(query, currentCuisineFilter, currentIngredientsFilter)
        Toast.makeText(context, "Searching for: $query", Toast.LENGTH_SHORT).show()
    }

    /**
     * TEMPORARY: Sample data loader
     * DELETE THIS METHOD when API is integrated
     */
    private fun loadSampleRecipes(category: String) {
        showLoading(true)
        recipes.clear()

        Toast.makeText(context, "Loading $category recipes", Toast.LENGTH_SHORT).show()

        // Sample recipes based on category
        val sampleRecipes = when (category) {
            CATEGORY_POPULAR -> listOf(
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
                    id = "9",
                    title = "Lumpiang Shanghai",
                    description = "Filipino spring rolls filled with ground pork and vegetables",
                    image = "https://images.unsplash.com/photo-1617196982693-b6d12fa1c4e1?w=400",
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
                )
            )
            CATEGORY_MOST_LIKED -> listOf(
                Recipe(
                    id = "9",
                    title = "Lumpiang Shanghai",
                    description = "Filipino spring rolls filled with ground pork and vegetables",
                    image = "https://images.unsplash.com/photo-1617196982693-b6d12fa1c4e1?w=400",
                    rating = 4.8f,
                    difficulty = "Easy",
                    cuisine = "Filipino"
                ),
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
                    id = "3",
                    title = "Lechon Kawali",
                    description = "Crispy pan-fried pork belly with dipping sauce",
                    image = "https://images.unsplash.com/photo-1571091655789-9d57f1f689fc?w=400",
                    rating = 4.7f,
                    difficulty = "Medium",
                    cuisine = "Filipino"
                )
            )
            CATEGORY_RECENT -> listOf(
                Recipe(
                    id = "20",
                    title = "Menudo",
                    description = "Filipino pork and liver stew with tomato sauce and vegetables",
                    image = "https://images.unsplash.com/photo-1586201375761-83865001c2b9?w=400",
                    rating = 4.6f,
                    difficulty = "Medium",
                    cuisine = "Filipino"
                ),
                Recipe(
                    id = "19",
                    title = "Dinuguan",
                    description = "Savory Filipino pork blood stew with vinegar and spices",
                    image = "https://images.unsplash.com/photo-1598866535622-1a78bfad0fa1?w=400",
                    rating = 4.5f,
                    difficulty = "Medium",
                    cuisine = "Filipino"
                ),
                Recipe(
                    id = "18",
                    title = "Buko Pandan",
                    description = "Refreshing dessert with young coconut, pandan jelly, and cream",
                    image = "https://images.unsplash.com/photo-1619478252306-8b4f13f45e72?w=400",
                    rating = 4.6f,
                    difficulty = "Easy",
                    cuisine = "Filipino"
                )
            )
            else -> emptyList()
        }

        recipes.addAll(sampleRecipes)
        recipeAdapter.updateRecipes(recipes)
        showLoading(false)
        showEmptyState(recipes.isEmpty())
    }

    // TODO: Add this method for API integration
    // private fun observeSearchResults() {
    //     viewModel.searchResults.observe(viewLifecycleOwner) { result ->
    //         when (result) {
    //             is Resource.Success -> {
    //                 recipes.clear()
    //                 recipes.addAll(result.data)
    //                 recipeAdapter.updateRecipes(recipes)
    //                 showLoading(false)
    //                 showEmptyState(recipes.isEmpty())
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

    // ==================== FILTER DIALOGS ====================
    private fun showCuisineFilterDialog() {
        // TODO: Create a dialog with cuisine options
        // Options: Filipino, Chinese, Japanese, Italian, etc.
        Toast.makeText(context, "Cuisine filter dialog", Toast.LENGTH_SHORT).show()

        // TODO: When user selects cuisine:
        // currentCuisineFilter = selectedCuisine
        // loadRecipes(currentCategory)
    }

    private fun showIngredientsFilterDialog() {
        // TODO: Create a dialog for ingredient selection
        // Allow multiple ingredient selection
        Toast.makeText(context, "Ingredients filter dialog", Toast.LENGTH_SHORT).show()

        // TODO: When user selects ingredients:
        // currentIngredientsFilter = selectedIngredients
        // loadRecipes(currentCategory)
    }

    private fun showDifficultyFilterDialog() {
        // TODO: Create a dialog with difficulty options
        // Options: Easy, Medium, Hard
        Toast.makeText(context, "Difficulty filter dialog", Toast.LENGTH_SHORT).show()
    }

    private fun clearAllFilters() {
        currentCuisineFilter = null
        currentIngredientsFilter = null
        loadRecipes(currentCategory)
        Toast.makeText(context, "Filters cleared", Toast.LENGTH_SHORT).show()
    }

    // ==================== USER INTERACTIONS ====================
    private fun onRecipeClicked(recipe: Recipe) {
        // TODO: Navigate to recipe detail screen
        Toast.makeText(requireContext(), "Clicked: ${recipe.title}", Toast.LENGTH_SHORT).show()
        // val action = SearchFragmentDirections.actionSearchToRecipeDetail(recipe.id)
        // findNavController().navigate(action)
    }

    // ==================== UI STATE HELPERS ====================
    private fun showLoading(show: Boolean) {
        // TODO: Add loading indicator to your layout
        // binding.loadingProgress?.visibility = if (show) View.VISIBLE else View.GONE
        // binding.rvSearchResults?.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        // TODO: Add empty state view to your layout
        // val emptyMessage = "No recipes found.\nTry different filters or search terms."
        // binding.emptyState?.visibility = if (show) View.VISIBLE else View.GONE
        // binding.emptyStateText?.text = emptyMessage
    }

    // TODO: Add error handling method
    // private fun showError(message: String?) {
    //     Toast.makeText(
    //         requireContext(),
    //         message ?: "Failed to load recipes",
    //         Toast.LENGTH_LONG
    //     ).show()
    // }
}