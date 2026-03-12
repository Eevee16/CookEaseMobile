package com.cookease.app.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.cookease.app.R
import com.cookease.app.databinding.FragmentSearchIngredientsBinding
import com.cookease.app.ui_components.recipe.RecipeAdapter

class SearchIngredientsFragment : Fragment() {

    private var _binding: FragmentSearchIngredientsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchIngredientsViewModel by viewModels {
        SearchIngredientsViewModelFactory(requireContext())
    }

    private lateinit var ingredientAdapter: IngredientCardAdapter
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchIngredientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupObservers()
        setupListeners()
        // fetchData() is called in ViewModel init, but we can call it again if needed
        viewModel.fetchData()
    }

    private fun setupAdapters() {
        ingredientAdapter = IngredientCardAdapter(emptyList(), emptySet()) { ingredientName ->
            viewModel.toggleIngredient(ingredientName)
        }
        binding.rvIngredients.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvIngredients.adapter = ingredientAdapter

        recipeAdapter = RecipeAdapter { /* handle recipe click if needed */ }
        binding.rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecipes.adapter = recipeAdapter
    }

    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingContainer.isVisible = isLoading
            binding.contentContainer.isVisible = !isLoading
        }

        viewModel.allIngredients.observe(viewLifecycleOwner) { ingredients ->
            binding.tvIngredientCount.text = "${ingredients.size} total"
        }

        viewModel.filteredIngredients.observe(viewLifecycleOwner) { ingredients ->
            val selected = viewModel.selectedIngredients.value ?: emptySet()
            ingredientAdapter.updateData(ingredients, selected)
        }

        viewModel.selectedIngredients.observe(viewLifecycleOwner) { selected ->
            val filtered = viewModel.filteredIngredients.value ?: emptyList()
            ingredientAdapter.updateData(filtered, selected)

            binding.chipGroupSelected.removeAllViews()
            selected.forEach { ingName ->
                val chip = Chip(requireContext()).apply {
                    text = ingName
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { viewModel.toggleIngredient(ingName) }
                }
                binding.chipGroupSelected.addView(chip)
            }

            binding.selectedHeader.isVisible = selected.isNotEmpty()
            binding.tvSelectedCount.text = "Selected (${selected.size})"
        }

        viewModel.filteredRecipes.observe(viewLifecycleOwner) { recipes ->
            val hasSelected = (viewModel.selectedIngredients.value?.size ?: 0) > 0
            binding.tvResultsCount.text = "${recipes.size} found"
            binding.emptySelectState.isVisible = !hasSelected
            binding.emptyResultsState.isVisible = hasSelected && recipes.isEmpty()
            binding.rvRecipes.isVisible = hasSelected && recipes.isNotEmpty()
            recipeAdapter.submitList(recipes)
        }
    }

    private fun setupListeners() {
        // Keep real-time filtering but also add the button for a definite action
        binding.etIngredientSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) =
                viewModel.filterIngredients(s?.toString() ?: "")
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Explicit search button listener
        binding.btnSearchIngredients.setOnClickListener {
            viewModel.filterIngredients(binding.etIngredientSearch.text.toString())
        }

        binding.tvClearAll.setOnClickListener { 
            binding.etIngredientSearch.setText("")
            viewModel.clearAll() 
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}