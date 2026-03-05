package com.cookease.app.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.R
import com.cookease.app.Resource
import com.cookease.app.databinding.FragmentHomeBinding
import com.cookease.app.ui_components.recipe.RecipeAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupObservers()

        viewModel.fetchApprovedRecipes()
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                val action = HomeFragmentDirections.actionHomeToRecipeDetail(recipe.id)
                findNavController().navigate(action)
            }
        )
        binding.rvRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecipes.adapter = recipeAdapter
    }

    private fun setupSearch() {
        // Search on button click
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchRecipes(query)
            } else {
                viewModel.fetchApprovedRecipes()
            }
        }

        // Clear search when text is cleared
        binding.searchEditText.addTextChangedListener { text ->
            if (text.isNullOrEmpty()) {
                viewModel.fetchApprovedRecipes()
            }
        }
    }

    private fun setupObservers() {
        viewModel.recipes.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.loadingProgress.visibility = View.VISIBLE
                    binding.rvRecipes.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.loadingProgress.visibility = View.GONE
                    binding.rvRecipes.visibility = View.VISIBLE
                    recipeAdapter.submitList(result.data)
                    binding.emptyState.visibility =
                        if (result.data.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
                is Resource.Error -> {
                    binding.loadingProgress.visibility = View.GONE
                    binding.rvRecipes.visibility = View.GONE
                    
                    // Improved error handling for offline state
                    val errorMsg = if (result.message?.contains("Unable to resolve host", ignoreCase = true) == true) {
                        "App is offline. Check your connection."
                    } else {
                        result.message ?: "Failed to load recipes"
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
