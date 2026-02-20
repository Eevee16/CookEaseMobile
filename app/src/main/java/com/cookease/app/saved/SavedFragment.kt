package com.cookease.app.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.Recipe
import com.cookease.app.Resource
import com.cookease.app.databinding.FragmentSavedBinding
import com.cookease.app.ui.recipe.RecipeAdapter

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedViewModel by viewModels()
    private lateinit var adapter: RecipeAdapter

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
        setupObservers()
        viewModel.loadSavedRecipes()
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter { recipe ->
            Toast.makeText(requireContext(), "Clicked: ${recipe.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvSavedRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSavedRecipes.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.savedRecipes.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    adapter.submitList(result.data)
                    showEmptyState(result.data?.isEmpty() != false)
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError("Failed to load saved recipes: ${result.message}")
                }
            }
        }

        viewModel.removeRecipeResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> Toast.makeText(requireContext(), "Recipe removed from saved", Toast.LENGTH_SHORT).show()
                is Resource.Error -> showError("Failed to remove recipe: ${result.message}")
                else -> {}
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.loadingProgress.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvSavedRecipes.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        binding.layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvSavedRecipes.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}