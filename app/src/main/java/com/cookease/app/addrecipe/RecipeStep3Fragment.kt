package com.cookease.app.addrecipe

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cookease.app.R

class RecipeStep3Fragment : Fragment(R.layout.fragment_add_step3) {

    private val viewModel: AddRecipeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etIngredients = view.findViewById<EditText>(R.id.etIngredients)
        val etInstructions = view.findViewById<EditText>(R.id.etInstructions)
        val btnBack = view.findViewById<Button>(R.id.btnBackStep3)
        val btnDone = view.findViewById<Button>(R.id.btnDone)

        // Restore State
        etIngredients.setText(viewModel.ingredients.value)
        etInstructions.setText(viewModel.instructions.value)

        // Back Button
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Done Button
        btnDone.setOnClickListener {
            viewModel.ingredients.value = etIngredients.text.toString()
            viewModel.instructions.value = etInstructions.text.toString()

            viewModel.submitRecipe()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecipeSuccessFragment())
                .commit()
        }
    }
}