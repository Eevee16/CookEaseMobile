package com.cookease.app.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.R
import com.cookease.app.Resource
import com.cookease.app.databinding.FragmentHomeBinding
import com.cookease.app.ui_components.recipe.RecipeAdapter
import com.cookease.app.utils.ConnectivityObserver
import com.cookease.app.utils.NetworkConnectivityObserver
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var connectivityObserver: NetworkConnectivityObserver

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

        connectivityObserver = NetworkConnectivityObserver(requireContext())
        
        setupRecyclerView()
        setupSearch()
        setupObservers()
        setupListeners()
        setupConnectivityObserver()

        viewModel.fetchApprovedRecipes()
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                val action = HomeFragmentDirections.actionHomeToRecipeDetail(recipe.id)
                findNavController().navigate(action)
            },
            onAuthorClick = { userId ->
                val action = HomeFragmentDirections.actionHomeToProfile(userId)
                findNavController().navigate(action)
            }
        )
        binding.rvRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecipes.adapter = recipeAdapter
    }

    private fun setupSearch() {
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchRecipes(query)
            } else {
                viewModel.fetchApprovedRecipes()
            }
        }

        binding.searchEditText.addTextChangedListener { text ->
            if (text.isNullOrEmpty()) {
                viewModel.fetchApprovedRecipes()
            }
        }
    }

    private fun setupListeners() {
        binding.btnHideError.setOnClickListener {
            binding.cardError.isVisible = false
        }
    }

    private fun setupConnectivityObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                connectivityObserver.observe().collectLatest { status ->
                    when (status) {
                        ConnectivityObserver.Status.Lost, ConnectivityObserver.Status.Unavailable -> {
                            showBannerError("App is offline. Check your connection.")
                        }
                        ConnectivityObserver.Status.Available -> {
                            if (binding.cardError.isVisible && binding.tvErrorMessage.text.toString().contains("offline", ignoreCase = true)) {
                                binding.cardError.isVisible = false
                                viewModel.fetchApprovedRecipes()
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.recipes.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.loadingProgress.isVisible = true
                    binding.rvRecipes.isVisible = false
                    binding.emptyState.isVisible = false
                    binding.cardError.isVisible = false
                }
                is Resource.Success -> {
                    binding.loadingProgress.isVisible = false
                    binding.rvRecipes.isVisible = true
                    recipeAdapter.submitList(result.data)
                    binding.emptyState.isVisible = result.data.isNullOrEmpty()
                    binding.cardError.isVisible = false
                }
                is Resource.Error -> {
                    binding.loadingProgress.isVisible = false
                    binding.rvRecipes.isVisible = false
                    
                    if (!connectivityObserver.isCurrentlyConnected()) {
                        showBannerError("App is offline. Check your connection.")
                    } else {
                        showBannerError(result.message ?: "Failed to load recipes")
                    }
                }
            }
        }
    }

    private fun showBannerError(message: String) {
        binding.tvErrorMessage.text = message
        binding.cardError.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
