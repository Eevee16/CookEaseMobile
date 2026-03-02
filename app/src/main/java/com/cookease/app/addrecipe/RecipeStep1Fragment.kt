package com.cookease.app.addrecipe

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cookease.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RecipeStep1Fragment : Fragment(R.layout.fragment_add_step1) {

    private val viewModel: AddRecipeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivBack = view.findViewById<ImageButton>(R.id.ivBack)
        val tilName = view.findViewById<TextInputLayout>(R.id.tilName)
        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val actvCuisine = view.findViewById<AutoCompleteTextView>(R.id.actvCuisine)
        val actvCategory = view.findViewById<AutoCompleteTextView>(R.id.actvCategory)
        val toggleDifficulty = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleDifficulty)
        val tilServings = view.findViewById<TextInputLayout>(R.id.tilServings)
        val etServings = view.findViewById<TextInputEditText>(R.id.etServings)
        val tilDescription = view.findViewById<TextInputLayout>(R.id.tilDescription)
        val etDescription = view.findViewById<TextInputEditText>(R.id.etDescription)
        val btnNext = view.findViewById<MaterialButton>(R.id.btnNextStep1)

        // ── Dropdowns ─────────────────────────────────────────────────
        actvCuisine.setAdapter(ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            AddRecipeViewModel.CUISINE_OPTIONS
        ))
        actvCategory.setAdapter(ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            AddRecipeViewModel.CATEGORY_OPTIONS
        ))

        // ── Restore state ─────────────────────────────────────────────
        etName.setText(viewModel.recipeName.value)
        etDescription.setText(viewModel.description.value)
        etServings.setText(viewModel.servings.value)
        viewModel.cuisine.value?.takeIf { it.isNotBlank() }?.let { actvCuisine.setText(it, false) }
        viewModel.category.value?.takeIf { it.isNotBlank() }?.let { actvCategory.setText(it, false) }
        viewModel.difficulty.value?.let { saved ->
            when (saved) {
                "Easy" -> toggleDifficulty.check(R.id.btnEasy)
                "Medium" -> toggleDifficulty.check(R.id.btnMedium)
                "Hard" -> toggleDifficulty.check(R.id.btnHard)
            }
        }

        // ── Back ──────────────────────────────────────────────────────
        ivBack.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.selectedItemId = R.id.nav_home
            requireParentFragment().childFragmentManager.popBackStack(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        // ── Next ──────────────────────────────────────────────────────
        btnNext.setOnClickListener {
            val name = etName.text.toString().trim()
            val cuisineSelected = actvCuisine.text.toString().trim()
            val categorySelected = actvCategory.text.toString().trim()
            val servingsText = etServings.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val difficulty = when (toggleDifficulty.checkedButtonId) {
                R.id.btnEasy -> "Easy"
                R.id.btnMedium -> "Medium"
                R.id.btnHard -> "Hard"
                else -> ""
            }

            var hasError = false

            if (name.isEmpty()) {
                tilName.error = "Recipe title is required"
                hasError = true
            } else if (name.length < 3) {
                tilName.error = "Must be at least 3 characters"
                hasError = true
            } else {
                tilName.error = null
            }

            if (!AddRecipeViewModel.CUISINE_OPTIONS.contains(cuisineSelected)) {
                Toast.makeText(requireContext(), "Please select a cuisine", Toast.LENGTH_SHORT).show()
                hasError = true
            }

            if (!AddRecipeViewModel.CATEGORY_OPTIONS.contains(categorySelected)) {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
                hasError = true
            }

            if (difficulty.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a difficulty level", Toast.LENGTH_SHORT).show()
                hasError = true
            }

            if (servingsText.isEmpty() || servingsText.toIntOrNull() == null) {
                tilServings.error = "Please enter a valid number of servings"
                hasError = true
            } else {
                tilServings.error = null
            }

            if (description.isEmpty()) {
                tilDescription.error = "Description is required"
                hasError = true
            } else {
                tilDescription.error = null
            }

            if (hasError) return@setOnClickListener

            viewModel.recipeName.value = name
            viewModel.cuisine.value = cuisineSelected
            viewModel.category.value = categorySelected
            viewModel.difficulty.value = difficulty
            viewModel.servings.value = servingsText
            viewModel.description.value = description

            requireParentFragment().childFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, RecipeStep2Fragment())
                .addToBackStack("Step1")
                .commit()
        }
    }
}