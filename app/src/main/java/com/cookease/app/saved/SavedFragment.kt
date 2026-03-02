package com.cookease.app.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.Resource
import com.cookease.app.databinding.FragmentSavedBinding
import com.cookease.app.ui_components.recipe.RecipeAdapter

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedViewModel by viewModels()
    private lateinit var adapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                // TODO: Navigate to RecipeDetailFragment
                Toast.makeText(requireContext(), "Opening: ${recipe.title}", Toast.LENGTH_SHORT).show()
            },
            onRemoveClick = { recipe ->
                viewModel.removeFromSaved(recipe.id)
            }
        )
        binding.rvSavedRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSavedRecipes.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.savedRecipes.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes)
            showEmptyState(recipes.isEmpty())
        }

        viewModel.syncState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> showSyncIndicator(true)
                is Resource.Success -> showSyncIndicator(false)
                is Resource.Error -> {
                    showSyncIndicator(false)
                    if (result.message != "Not logged in") {
                        showOfflineBanner()
                    }
                }
            }
        }

        viewModel.removeResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> Toast.makeText(requireContext(), "Recipe removed", Toast.LENGTH_SHORT).show()
                is Resource.Error -> Toast.makeText(requireContext(), "Failed to remove: ${result.message}", Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    private fun showEmptyState(show: Boolean) {
        binding.layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvSavedRecipes.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showSyncIndicator(show: Boolean) {
        binding.loadingProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showOfflineBanner() {
        Toast.makeText(requireContext(), "You're offline — showing cached recipes", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}