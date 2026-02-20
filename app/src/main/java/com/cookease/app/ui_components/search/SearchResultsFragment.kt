package com.cookease.app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.databinding.FragmentSearchResultsBinding
import com.cookease.app.ui.recipe.RecipeAdapter

class SearchResultsFragment : Fragment() {

    private var _binding: FragmentSearchResultsBinding? = null
    private val binding get() = _binding!!

    private val args: SearchResultsFragmentArgs by navArgs()

    private val viewModel: SearchResultsViewModel by viewModels {
        SearchResultsViewModelFactory(requireContext())
    }

    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeAdapter = RecipeAdapter { }
        binding.rvRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecipes.adapter = recipeAdapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            resetViews()
            when (state) {
                is SearchResultsState.Loading ->
                    binding.loadingContainer.isVisible = true
                is SearchResultsState.Success -> {
                    binding.tvQueryLabel.isVisible = true
                    binding.tvQueryLabel.text = "Showing results for: \"${state.query}\""
                    val count = state.recipes.size
                    binding.tvResultsCount.text = "$count ${if (count == 1) "recipe" else "recipes"} found"
                    binding.rvRecipes.isVisible = true
                    recipeAdapter.submitList(state.recipes)
                }
                is SearchResultsState.Empty -> {
                    binding.tvQueryLabel.isVisible = true
                    binding.tvQueryLabel.text = "Showing results for: \"${state.query}\""
                    binding.tvResultsCount.text = "0 recipes found"
                    binding.emptyResultsState.isVisible = true
                    binding.tvNoResultsHint.text =
                        "We couldn't find any recipes matching \"${state.query}\".\nTry different keywords."
                }
                is SearchResultsState.NoQuery ->
                    binding.emptyQueryState.isVisible = true
                is SearchResultsState.Error -> {
                    binding.tvError.isVisible = true
                    binding.tvError.text = state.message
                }
                else -> {}
            }
        }

        viewModel.search(args.query)
    }

    private fun resetViews() {
        binding.loadingContainer.isVisible = false
        binding.tvError.isVisible = false
        binding.emptyQueryState.isVisible = false
        binding.emptyResultsState.isVisible = false
        binding.rvRecipes.isVisible = false
        binding.tvQueryLabel.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}