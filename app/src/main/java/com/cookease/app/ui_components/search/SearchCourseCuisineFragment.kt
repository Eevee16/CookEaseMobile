package com.cookease.app.ui_components.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.databinding.FragmentSearchCourseCuisineBinding
import com.cookease.app.ui_components.recipe.RecipeAdapter

class SearchCourseCuisineFragment : Fragment() {

    private var _binding: FragmentSearchCourseCuisineBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchCourseCuisineViewModel by viewModels {
        SearchCourseCuisineViewModelFactory(requireContext())
    }

    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchCourseCuisineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()
        viewModel.fetchRecipes()
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                val action = SearchCourseCuisineFragmentDirections.actionSearchCourseCuisineToRecipeDetail(recipe.id)
                findNavController().navigate(action)
            },
            onAuthorClick = { userId ->
                val action = SearchCourseCuisineFragmentDirections.actionSearchCourseCuisineToProfile(userId)
                findNavController().navigate(action)
            }
        )
        binding.rvRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecipes.adapter = recipeAdapter
    }

    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingContainer.isVisible = isLoading
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val list = listOf("All Categories") + categories
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }

        viewModel.cuisines.observe(viewLifecycleOwner) { cuisines ->
            val list = listOf("All Cuisines") + cuisines
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCuisine.adapter = adapter
        }

        viewModel.filteredRecipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)
            binding.tvResultsCount.text = "${recipes.size} recipes found"
            binding.emptyState.isVisible = recipes.isEmpty()
            
            binding.btnClearFilters.isVisible = viewModel.selectedCategory.isNotEmpty() || viewModel.selectedCuisine.isNotEmpty()
        }
    }

    private fun setupListeners() {
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = parent?.getItemAtPosition(position).toString()
                viewModel.selectedCategory = if (selected == "All Categories") "" else selected
                viewModel.applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerCuisine.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = parent?.getItemAtPosition(position).toString()
                viewModel.selectedCuisine = if (selected == "All Cuisines") "" else selected
                viewModel.applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnClearFilters.setOnClickListener {
            binding.spinnerCategory.setSelection(0)
            binding.spinnerCuisine.setSelection(0)
            viewModel.clearFilters()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
