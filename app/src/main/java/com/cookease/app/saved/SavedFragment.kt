package com.cookease.app.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.Resource
import com.cookease.app.databinding.FragmentSavedBinding
import com.cookease.app.ui_components.recipe.RecipeAdapter
import com.cookease.app.utils.ConnectivityObserver
import com.cookease.app.utils.NetworkConnectivityObserver
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedViewModel by viewModels()
    private lateinit var adapter: RecipeAdapter
    private lateinit var connectivityObserver: NetworkConnectivityObserver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        connectivityObserver = NetworkConnectivityObserver(requireContext())
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupConnectivityObserver()
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                val action = SavedFragmentDirections.actionSavedToRecipeDetail(recipe.id)
                findNavController().navigate(action)
            },
            onAuthorClick = { userId ->
                val action = SavedFragmentDirections.actionSavedToProfile(userId)
                findNavController().navigate(action)
            },
            onRemoveClick = { recipe ->
                viewModel.removeFromSaved(recipe.id)
            },
            isSavedScreen = true
        )
        binding.rvSavedRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSavedRecipes.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnHideInfo.setOnClickListener {
            binding.cardInfo.isVisible = false
        }
        
        binding.btnExplore.setOnClickListener {
            findNavController().navigate(com.cookease.app.R.id.nav_home)
        }
    }

    private fun setupConnectivityObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                connectivityObserver.observe().collectLatest { status ->
                    when (status) {
                        ConnectivityObserver.Status.Lost, ConnectivityObserver.Status.Unavailable -> {
                            showBannerInfo("App is offline. Showing cached recipes.")
                        }
                        ConnectivityObserver.Status.Available -> {
                            if (binding.cardInfo.isVisible && binding.tvInfoMessage.text.toString().contains("offline", ignoreCase = true)) {
                                binding.cardInfo.isVisible = false
                                // Potentially refresh if needed
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.savedRecipes.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes)
            showEmptyState(recipes.isEmpty())
        }

        viewModel.syncState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> showSyncIndicator(true)
                is Resource.Success -> {
                    showSyncIndicator(false)
                    if (!binding.tvInfoMessage.text.toString().contains("offline", ignoreCase = true)) {
                        binding.cardInfo.isVisible = false
                    }
                }
                is Resource.Error -> {
                    showSyncIndicator(false)
                    if (result.message != "Not logged in" && !connectivityObserver.isCurrentlyConnected()) {
                        showOfflineBanner()
                    }
                }
            }
        }

        viewModel.removeResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    Snackbar.make(binding.root, "Recipe removed", Snackbar.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    showBannerInfo("Failed to remove: ${result.message}")
                }
                else -> {}
            }
        }
    }

    private fun showEmptyState(show: Boolean) {
        binding.layoutEmptyState.isVisible = show
        binding.rvSavedRecipes.isVisible = !show
    }

    private fun showSyncIndicator(show: Boolean) {
        binding.loadingProgress.isVisible = show
    }

    private fun showOfflineBanner() {
        showBannerInfo("App is offline. Showing cached recipes.")
    }

    private fun showBannerInfo(message: String) {
        binding.tvInfoMessage.text = message
        binding.cardInfo.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
