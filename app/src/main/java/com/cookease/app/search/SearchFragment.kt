package com.cookease.app.ui.search

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.R
import com.cookease.app.databinding.FragmentSearchBinding
import com.cookease.app.ui.recipe.RecipeAdapter

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        recipeAdapter = RecipeAdapter { recipe ->
            val action = SearchFragmentDirections.actionSearchToSearchResults(recipe.id)
            findNavController().navigate(action)
        }

        binding.rvRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecipes.adapter = recipeAdapter

        // Setup spinners
        val categories = listOf("All", "Breakfast", "Lunch", "Dinner", "Snack", "Dessert")
        binding.spCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val cuisines = listOf("All", "Filipino", "Italian", "Chinese", "Japanese", "American")
        binding.spCuisine.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cuisines
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Observe results
        searchViewModel.filteredRecipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)
            binding.tvResultCount.text = "${recipes.size} recipe(s) found"
            binding.tvResultCount.visibility = View.VISIBLE
            binding.emptyState.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
        }

        // Search button
        binding.btnFilter.setOnClickListener {
            performSearch()
        }

        // Apply filters button
        binding.btnApplyFilters.setOnClickListener {
            performSearch()
        }

        // Load all recipes on start
        searchViewModel.fetchAllRecipes()
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        val category = binding.spCategory.selectedItem.toString()
        val cuisine = binding.spCuisine.selectedItem.toString()
        searchViewModel.filterRecipes(
            category = if (category == "All") "" else category,
            cuisine = if (cuisine == "All") "" else cuisine,
            searchTerm = query
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}