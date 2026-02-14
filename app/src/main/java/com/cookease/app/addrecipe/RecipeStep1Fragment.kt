package com.cookease.app.addrecipe

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton // Added this import
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cookease.app.R
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecipeStep1Fragment : Fragment(R.layout.fragment_add_step1) {

    private val viewModel: AddRecipeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Bind Views
        val ivBack = view.findViewById<ImageButton>(R.id.ivBack) // Updated to ImageButton
        val etName = view.findViewById<EditText>(R.id.etName)
        val spCuisine = view.findViewById<Spinner>(R.id.spCuisine)
        val spCategory = view.findViewById<Spinner>(R.id.spCategory)
        val toggleDifficulty = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleDifficulty)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnNext = view.findViewById<Button>(R.id.btnNextStep1)

        // 2. Setup Dropdowns (Spinners)
        val cuisineList = listOf("Filipino", "Italian", "American", "Japanese", "Chinese")
        val cuisineAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, cuisineList)
        spCuisine.adapter = cuisineAdapter

        val categoryList = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Dessert")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryList)
        spCategory.adapter = categoryAdapter

        // 3. Restore Data (If coming back from Step 2)
        etName.setText(viewModel.recipeName.value)
        etDescription.setText(viewModel.description.value)

        // Restore Spinner Selections
        viewModel.cuisine.value?.let {
            val pos = cuisineList.indexOf(it)
            if (pos >= 0) spCuisine.setSelection(pos)
        }
        viewModel.category.value?.let {
            val pos = categoryList.indexOf(it)
            if (pos >= 0) spCategory.setSelection(pos)
        }

        // 4. Handle Back Button

                ivBack.setOnClickListener {
                    val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                    bottomNav.selectedItemId = R.id.nav_home
                    parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }

        // 5. Handle Next Button
        btnNext.setOnClickListener {
            val selectedDifficultyId = toggleDifficulty.checkedButtonId
            val difficulty = when(selectedDifficultyId) {
                R.id.btnEasy -> "Easy"
                R.id.btnMedium -> "Medium"
                R.id.btnHard -> "Hard"
                else -> "Medium"
            }

            // Save to ViewModel
            viewModel.recipeName.value = etName.text.toString()
            viewModel.cuisine.value = spCuisine.selectedItem.toString()
            viewModel.category.value = spCategory.selectedItem.toString()
            viewModel.difficulty.value = difficulty
            viewModel.description.value = etDescription.text.toString()

            // Navigate to Step 2
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecipeStep2Fragment())
                .addToBackStack("Step1")
                .commit()
        }
    }
}