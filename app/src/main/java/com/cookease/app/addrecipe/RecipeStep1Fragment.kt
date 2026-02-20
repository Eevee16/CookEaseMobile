package com.cookease.app.addrecipe

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cookease.app.R
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecipeStep1Fragment : Fragment(R.layout.fragment_add_step1) {

    private val viewModel: AddRecipeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivBack = view.findViewById<ImageButton>(R.id.ivBack)
        val etName = view.findViewById<EditText>(R.id.etName)
        val spCuisine = view.findViewById<Spinner>(R.id.spCuisine)
        val spCategory = view.findViewById<Spinner>(R.id.spCategory)
        val toggleDifficulty = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleDifficulty)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnNext = view.findViewById<Button>(R.id.btnNextStep1)

        val cuisineList = listOf("Filipino", "Italian", "American", "Japanese", "Chinese")
        spCuisine.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            cuisineList
        )

        val categoryList = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Dessert")
        spCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categoryList
        )

        etName.setText(viewModel.recipeName.value)
        etDescription.setText(viewModel.description.value)

        viewModel.cuisine.value?.let { saved ->
            val pos = cuisineList.indexOf(saved)
            if (pos >= 0) spCuisine.setSelection(pos)
        }
        viewModel.category.value?.let { saved ->
            val pos = categoryList.indexOf(saved)
            if (pos >= 0) spCategory.setSelection(pos)
        }
        viewModel.difficulty.value?.let { saved ->
            when (saved) {
                "Easy" -> toggleDifficulty.check(R.id.btnEasy)
                "Medium" -> toggleDifficulty.check(R.id.btnMedium)
                "Hard" -> toggleDifficulty.check(R.id.btnHard)
            }
        }

        // Back — exit the add recipe flow entirely via the bottom nav
        ivBack.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.selectedItemId = R.id.nav_home
            requireParentFragment().childFragmentManager.popBackStack(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        val fm = requireParentFragment().childFragmentManager

        btnNext.setOnClickListener {
            val name = etName.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val selectedDifficultyId = toggleDifficulty.checkedButtonId
            val difficulty = when (selectedDifficultyId) {
                R.id.btnEasy -> "Easy"
                R.id.btnMedium -> "Medium"
                R.id.btnHard -> "Hard"
                else -> ""
            }

            var hasError = false

            if (name.isEmpty()) {
                etName.error = "Recipe name is required"
                hasError = true
            } else if (name.length < 3) {
                etName.error = "Recipe name must be at least 3 characters"
                hasError = true
            } else {
                etName.error = null
            }

            if (description.isEmpty()) {
                etDescription.error = "Description is required"
                hasError = true
            } else {
                etDescription.error = null
            }

            if (difficulty.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a difficulty level", Toast.LENGTH_SHORT).show()
                hasError = true
            }

            if (hasError) return@setOnClickListener

            viewModel.recipeName.value = name
            viewModel.cuisine.value = spCuisine.selectedItem.toString()
            viewModel.category.value = spCategory.selectedItem.toString()
            viewModel.difficulty.value = difficulty
            viewModel.description.value = description

            fm.beginTransaction()
                .replace(R.id.fragment_container, RecipeStep2Fragment())
                .addToBackStack("Step1")
                .commit()
        }
    }
}