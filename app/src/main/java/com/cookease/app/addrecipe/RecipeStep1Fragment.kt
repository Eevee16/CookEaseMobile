package com.cookease.app.addrecipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cookease.app.R
import com.cookease.app.databinding.FragmentAddStep1Binding
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecipeStep1Fragment : Fragment() {

    private var _binding: FragmentAddStep1Binding? = null
    private val binding get() = _binding!!

    private val viewModel: AddRecipeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        restoreState()
        setupListeners()
    }

    private fun setupDropdowns() {
        binding.actvCuisine.setAdapter(ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            AddRecipeViewModel.CUISINE_OPTIONS
        ))
        binding.actvCategory.setAdapter(ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            AddRecipeViewModel.CATEGORY_OPTIONS
        ))
    }

    private fun restoreState() {
        binding.etName.setText(viewModel.recipeName.value)
        binding.etDescription.setText(viewModel.description.value)
        // ✅ synced: servings defaults to "1" so this will show "1" on first load
        binding.etServings.setText(viewModel.servings.value)

        viewModel.cuisine.value?.takeIf { it.isNotBlank() }?.let {
            binding.actvCuisine.setText(it, false)
        }
        viewModel.category.value?.takeIf { it.isNotBlank() }?.let {
            binding.actvCategory.setText(it, false)
        }

        viewModel.difficulty.value?.let { saved ->
            when (saved) {
                "Easy" -> binding.toggleDifficulty.check(R.id.btnEasy)
                "Medium" -> binding.toggleDifficulty.check(R.id.btnMedium)
                "Hard" -> binding.toggleDifficulty.check(R.id.btnHard)
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.selectedItemId = R.id.nav_home
            parentFragmentManager.popBackStack()
        }

        binding.btnNextStep1.setOnClickListener {
            validateAndProceed()
        }
    }

    private fun validateAndProceed() {
        val name = binding.etName.text.toString().trim()
        val cuisineSelected = binding.actvCuisine.text.toString().trim()
        val categorySelected = binding.actvCategory.text.toString().trim()
        val servingsText = binding.etServings.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val difficulty = when (binding.toggleDifficulty.checkedButtonId) {
            R.id.btnEasy -> "Easy"
            R.id.btnMedium -> "Medium"
            R.id.btnHard -> "Hard"
            else -> ""
        }

        var hasError = false

        if (name.isEmpty()) {
            binding.tilName.error = "Recipe title is required"
            hasError = true
        } else if (name.length < 3) {
            binding.tilName.error = "Must be at least 3 characters"
            hasError = true
        } else {
            binding.tilName.error = null
        }

        // ✅ synced: validate against full 16-item CUISINE_OPTIONS
        if (!AddRecipeViewModel.CUISINE_OPTIONS.contains(cuisineSelected)) {
            Toast.makeText(requireContext(), "Please select a cuisine", Toast.LENGTH_SHORT).show()
            hasError = true
        }

        // ✅ synced: validate against full 10-item CATEGORY_OPTIONS
        if (!AddRecipeViewModel.CATEGORY_OPTIONS.contains(categorySelected)) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            hasError = true
        }

        if (difficulty.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a difficulty level", Toast.LENGTH_SHORT).show()
            hasError = true
        }

        if (servingsText.isEmpty() || servingsText.toIntOrNull() == null) {
            binding.tilServings.error = "Please enter a valid number of servings"
            hasError = true
        } else {
            binding.tilServings.error = null
        }

        if (description.isEmpty()) {
            binding.tilDescription.error = "Description is required"
            hasError = true
        } else {
            binding.tilDescription.error = null
        }

        if (hasError) return

        viewModel.recipeName.value = name
        viewModel.cuisine.value = cuisineSelected
        viewModel.category.value = categorySelected
        viewModel.difficulty.value = difficulty
        viewModel.servings.value = servingsText
        viewModel.description.value = description

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, RecipeStep2Fragment())
            .addToBackStack("Step1")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
