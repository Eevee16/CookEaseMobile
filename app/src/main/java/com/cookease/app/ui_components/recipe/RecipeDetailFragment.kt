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
            binding.btnSave.text = if (saved) "Saved" else "Save Recipe"
            
            // Hide download button if not bookmarked
            binding.btnDownload.isVisible = saved

            // If it was just saved, we show a message.
            if (saved && justSavedRecipe) {
                justSavedRecipe = false
                Snackbar.make(binding.root, "Recipe bookmarked!", Snackbar.LENGTH_SHORT).show()
            } else if (!saved && justSavedRecipe) {
                justSavedRecipe = false
                Snackbar.make(binding.root, "Removed from bookmarks", Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.ownerPhotoUrl.observe(viewLifecycleOwner) { photoUrl ->
            if (!photoUrl.isNullOrBlank()) {
                binding.ivAuthorImage.isVisible = true
                binding.tvAuthorAvatar.isVisible = false
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.bg_circle_orange)
                    .circleCrop()
                    .into(binding.ivAuthorImage)
            } else {
                binding.ivAuthorImage.isVisible = false
                binding.tvAuthorAvatar.isVisible = true
            }
        }

        viewModel.ownerName.observe(viewLifecycleOwner) { name ->
            if (!name.isNullOrBlank()) {
                binding.tvAuthorName.text = name
                binding.tvAuthorAvatar.text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnSave.setOnClickListener {
            justSavedRecipe = true
            viewModel.toggleSaved()
        }

        binding.btnDownload.setOnClickListener {
            val currentState = viewModel.state.value
            if (currentState is RecipeDetailState.Success) {
                viewModel.markAsDownloaded(currentState.recipe.id)
                Snackbar.make(binding.root, "Recipe available offline!", Snackbar.LENGTH_SHORT).show()
                binding.btnDownload.setImageResource(android.R.drawable.checkbox_on_background)
                binding.btnDownload.isEnabled = false
            }
        }

        binding.btnShare.setOnClickListener {
            val recipe = (viewModel.state.value as? RecipeDetailState.Success)?.recipe ?: return@setOnClickListener
            
            val shareText = """
                Check out this delicious recipe on CookEase!
                
                ${recipe.title}
                ${recipe.description ?: ""}
                
                Ingredients:
                ${recipe.ingredients.joinToString("\n• ", prefix = "• ")}
                
                Instructions:
                ${recipe.instructions.mapIndexed { index, s -> "${index + 1}. $s" }.joinToString("\n")}
                
                Download CookEase to discover more recipes!
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Recipe: ${recipe.title}")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Share Recipe via"))
        }

        binding.authorSection.setOnClickListener {
            val recipe = (viewModel.state.value as? RecipeDetailState.Success)?.recipe
            if (recipe?.ownerId != null) {
                val action = RecipeDetailFragmentDirections.actionRecipeDetailToProfile(recipe.ownerId)
                findNavController().navigate(action)
            }
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

        recipe.rating?.let {
            if (it > 0.0) {
                binding.ratingBadge.isVisible = true
                binding.tvRating.text = String.format("%.1f", it)
            }
        }

        binding.tvCategoryLabel.text = "${recipe.cuisine ?: "World"} • ${recipe.category ?: "Main Course"}"
        binding.tvTitle.text = recipe.title
        
        // Initial author data from recipe object
        if (viewModel.ownerName.value.isNullOrBlank()) {
            val cleanedName = if (recipe.ownerName?.contains("@") == true) "Chef" else recipe.ownerName
            binding.tvAuthorName.text = cleanedName ?: "Chef"
            binding.tvAuthorAvatar.text = (cleanedName ?: "Chef").firstOrNull()?.uppercaseChar()?.toString() ?: "C"
        }

        binding.tvDescription.text = recipe.description ?: ""
        binding.tvServings.text = (recipe.servings ?: 1).toString()
        binding.tvPrepTime.text = viewModel.formatTime(recipe.prepTime)
        binding.tvCookTime.text = viewModel.formatTime(recipe.cookTime)
        binding.tvDifficulty.text = recipe.difficulty ?: "Medium"

        if (!recipe.notes.isNullOrBlank()) binding.tvNotes.text = recipe.notes
        binding.tabLayout.getTabAt(2)?.view?.isVisible = !recipe.notes.isNullOrBlank()
        binding.btnEdit.isVisible = false

        binding.rvIngredients.layoutManager = LinearLayoutManager(requireContext())
        binding.rvIngredients.adapter = IngredientAdapter(recipe.ingredients)

        binding.rvInstructions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInstructions.adapter = InstructionAdapter(recipe.instructions)
        
        // Setup download button state based on recipe status
        if (recipe.isDownloaded) {
            binding.btnDownload.setImageResource(android.R.drawable.checkbox_on_background)
            binding.btnDownload.isEnabled = false
        } else {
            binding.btnDownload.setImageResource(R.drawable.ic_download)
            binding.btnDownload.isEnabled = true
        }
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
