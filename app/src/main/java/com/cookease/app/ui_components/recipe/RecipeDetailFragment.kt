package com.cookease.app.ui_components.recipe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.Recipe
import com.cookease.app.databinding.FragmentRecipeDetailBinding
import com.google.android.material.snackbar.Snackbar

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private val args: RecipeDetailFragmentArgs by navArgs()
    private val viewModel: RecipeDetailViewModel by viewModels {
        RecipeDetailViewModelFactory(requireContext())
    }

    // Track if user just clicked save button (to trigger navigation)
    private var justSavedRecipe = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupObservers()
        setupListeners()
        viewModel.loadRecipe(args.recipeId)
    }

    private fun setupTabs() {
        binding.tabLayout.apply {
            addTab(newTab().setText("Ingredients"))
            addTab(newTab().setText("Directions"))
            addTab(newTab().setText("Notes"))
        }
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                binding.ingredientsContent.isVisible = tab.position == 0
                binding.directionsContent.isVisible = tab.position == 1
                binding.notesContent.isVisible = tab.position == 2
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RecipeDetailState.Loading -> showLoading()
                is RecipeDetailState.Success -> showRecipe(state.recipe)
                is RecipeDetailState.NotFound -> showNotFound()
                is RecipeDetailState.Error -> showError(state.message)
            }
        }

        viewModel.isSaved.observe(viewLifecycleOwner) { saved ->
            binding.btnSave.text = if (saved) "✓ Saved" else "🔖 Save Recipe"
            binding.btnDownload.isVisible = saved

            // Navigate to saved page if user just saved the recipe
            if (saved && justSavedRecipe) {
                justSavedRecipe = false // Reset flag
                Snackbar.make(binding.root, "Recipe saved!", Snackbar.LENGTH_SHORT).show()

                // Navigate to saved recipes page
                try {
                    findNavController().navigate(R.id.nav_saved)
                } catch (e: Exception) {
                    // Navigation failed, stay on current page
                    e.printStackTrace()
                }
            } else if (!saved && justSavedRecipe) {
                // Recipe was unsaved
                justSavedRecipe = false
                Snackbar.make(binding.root, "Recipe removed from favorites", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnSave.setOnClickListener {
            justSavedRecipe = true // Set flag before toggling
            viewModel.toggleSaved()
            // Message and navigation handled in isSaved observer
        }

        binding.btnDownload.setOnClickListener {
            Snackbar.make(binding.root, "Recipe downloaded for offline use", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnShare.setOnClickListener {
            val recipe = (viewModel.state.value as? RecipeDetailState.Success)?.recipe ?: return@setOnClickListener
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, recipe.title)
                putExtra(Intent.EXTRA_TEXT, "Check out this recipe: ${recipe.title}")
            }
            startActivity(Intent.createChooser(intent, "Share Recipe"))
        }
    }

    private fun showRecipe(recipe: Recipe) {
        binding.scrollView.isVisible = true
        binding.loadingContainer.isVisible = false
        binding.notFoundContainer.isVisible = false

        Glide.with(this)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.ic_food_placeholder)
            .error(R.drawable.ic_food_placeholder)
            .centerCrop()
            .into(binding.imgRecipe)

        // Rating safe display
        recipe.rating?.let {
            if (it > 0.0) {
                binding.ratingBadge.isVisible = true
                binding.tvRating.text = String.format("%.1f", it)
            }
        }

        binding.tvCategoryLabel.text = "${recipe.cuisine ?: "World"} • ${recipe.category ?: "Main Course"}"
        binding.tvTitle.text = recipe.title
        binding.tvAuthorAvatar.text = recipe.ownerName?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
        binding.tvAuthorName.text = recipe.ownerName ?: "Anonymous"
        binding.tvDescription.text = recipe.description ?: ""
        binding.tvServings.text = (recipe.servings ?: 1).toString()
        binding.tvPrepTime.text = viewModel.formatTime(recipe.prepTime)
        binding.tvCookTime.text = viewModel.formatTime(recipe.cookTime)
        binding.tvDifficulty.text = recipe.difficulty ?: "Medium"

        if (!recipe.notes.isNullOrBlank()) binding.tvNotes.text = recipe.notes
        binding.tabLayout.getTabAt(2)?.view?.isVisible = !recipe.notes.isNullOrBlank()
        binding.btnEdit.isVisible = false

        // Ingredients (List<String>)
        val ingredients = recipe.ingredients
        binding.rvIngredients.layoutManager = LinearLayoutManager(requireContext())
        binding.rvIngredients.adapter = IngredientAdapter(ingredients)

        // Instructions (List<String>)
        val instructions = recipe.instructions
        binding.rvInstructions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInstructions.adapter = InstructionAdapter(instructions)
    }

    private fun showLoading() {
        binding.scrollView.isVisible = false
        binding.loadingContainer.isVisible = true
        binding.notFoundContainer.isVisible = false
    }

    private fun showNotFound() {
        binding.scrollView.isVisible = false
        binding.loadingContainer.isVisible = false
        binding.notFoundContainer.isVisible = true
    }

    private fun showError(message: String) {
        showNotFound()
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
