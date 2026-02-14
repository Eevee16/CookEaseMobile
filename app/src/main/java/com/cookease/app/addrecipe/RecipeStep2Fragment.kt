package com.cookease.app.addrecipe

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cookease.app.R

class RecipeStep2Fragment : Fragment(R.layout.fragment_add_step2) {

    private val viewModel: AddRecipeViewModel by activityViewModels()
    private lateinit var ivRecipeImage: ImageView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            ivRecipeImage.setImageURI(it)
            ivRecipeImage.setPadding(0,0,0,0)
            viewModel.imageUri.value = it.toString()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etPrepTime = view.findViewById<EditText>(R.id.etPrepTime)
        val etCookTime = view.findViewById<EditText>(R.id.etCookTime)
        val etServings = view.findViewById<EditText>(R.id.etServings)
        ivRecipeImage = view.findViewById(R.id.ivRecipeImage)
        val btnBack = view.findViewById<Button>(R.id.btnBackStep2)
        val btnNext = view.findViewById<Button>(R.id.btnNextStep2)

        // Restore Data
        etPrepTime.setText(viewModel.prepTime.value)
        etCookTime.setText(viewModel.cookTime.value)
        etServings.setText(viewModel.servings.value)
        if (viewModel.imageUri.value != null) {
            ivRecipeImage.setImageURI(Uri.parse(viewModel.imageUri.value))
            ivRecipeImage.setPadding(0,0,0,0)
        }

        // Image Click
        ivRecipeImage.setOnClickListener { pickImageLauncher.launch("image/*") }

        // Back Button (Bottom only)
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Next Button
        btnNext.setOnClickListener {
            viewModel.prepTime.value = etPrepTime.text.toString()
            viewModel.cookTime.value = etCookTime.text.toString()
            viewModel.servings.value = etServings.text.toString()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecipeStep3Fragment())
                .addToBackStack("Step2")
                .commit()
        }
    }
}