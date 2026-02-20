package com.cookease.app.addrecipe

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cookease.app.R
import android.util.Log

class RecipeStep3Fragment : Fragment(R.layout.fragment_add_step3) {

    private val viewModel: AddRecipeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etIngredients = view.findViewById<EditText>(R.id.etIngredients)
        val etInstructions = view.findViewById<EditText>(R.id.etInstructions)
        val btnBack = view.findViewById<Button>(R.id.btnBackStep3)
        val btnDone = view.findViewById<Button>(R.id.btnDone)

        Log.d("RecipeStep3", "onViewCreated called")

        // Restore State
        etIngredients.setText(viewModel.ingredients.value)
        etInstructions.setText(viewModel.instructions.value)

        val fm = requireParentFragment().childFragmentManager

        // Back Button
        btnBack.setOnClickListener {
            fm.popBackStack()
        }

        // Observe submit state — navigate only on confirmed success
        viewModel.submitState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SubmitState.Loading -> {
                    btnDone.isEnabled = false
                    btnDone.text = "Submitting..."
                }
                is SubmitState.Success -> {
                    Log.d("RecipeStep3", "Submit successful, navigating to success screen")
                    viewModel.resetAllFields() // clears all form data for next time
                    fm.beginTransaction()
                        .replace(R.id.fragment_container, RecipeSuccessFragment())
                        .commit()
                }
                is SubmitState.Error -> {
                    btnDone.isEnabled = true
                    btnDone.text = requireContext().getString(R.string.submit_recipe)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    Log.e("RecipeStep3", "Submit error: ${state.message}")
                    viewModel.resetState()
                }
                is SubmitState.Idle -> {
                    btnDone.isEnabled = true
                    btnDone.text = requireContext().getString(R.string.submit_recipe)
                }
            }
        }

        // Done Button
        btnDone.setOnClickListener {
            Log.d("RecipeStep3", "Done button clicked")

            // Save BEFORE validating
            viewModel.ingredients.value = etIngredients.text.toString()
            viewModel.instructions.value = etInstructions.text.toString()

            if (!viewModel.validateStep3()) {
                Log.e("RecipeStep3", "Validation failed")
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.submitRecipe()
            // Navigation is handled by the submitState observer above
        }
    }
}