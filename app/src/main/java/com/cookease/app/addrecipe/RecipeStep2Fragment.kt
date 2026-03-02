package com.cookease.app.addrecipe

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cookease.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RecipeStep2Fragment : Fragment(R.layout.fragment_add_step2) {

    private val viewModel: AddRecipeViewModel by activityViewModels()
    private lateinit var ivRecipeImage: ImageView

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                ivRecipeImage.setImageURI(it)
                ivRecipeImage.setPadding(0, 0, 0, 0)
                viewModel.imageUri.value = it.toString()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tilPrepTime = view.findViewById<TextInputLayout>(R.id.tilPrepTime)
        val etPrepTime = view.findViewById<TextInputEditText>(R.id.etPrepTime)
        val tilCookTime = view.findViewById<TextInputLayout>(R.id.tilCookTime)
        val etCookTime = view.findViewById<TextInputEditText>(R.id.etCookTime)
        ivRecipeImage = view.findViewById(R.id.ivRecipeImage)
        val btnBack = view.findViewById<MaterialButton>(R.id.btnBackStep2)
        val btnNext = view.findViewById<MaterialButton>(R.id.btnNextStep2)

        // ── Restore state ─────────────────────────────────────────────
        etPrepTime.setText(viewModel.prepTime.value)
        etCookTime.setText(viewModel.cookTime.value)
        viewModel.imageUri.value?.let {
            ivRecipeImage.setImageURI(Uri.parse(it))
            ivRecipeImage.setPadding(0, 0, 0, 0)
        }

        // ── Image picker ──────────────────────────────────────────────
        ivRecipeImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val fm = requireParentFragment().childFragmentManager

        // ── Back ──────────────────────────────────────────────────────
        btnBack.setOnClickListener { fm.popBackStack() }

        // ── Next ──────────────────────────────────────────────────────
        btnNext.setOnClickListener {
            val prepTime = etPrepTime.text.toString().trim()
            val cookTime = etCookTime.text.toString().trim()

            var hasError = false

            if (prepTime.isEmpty() || prepTime.toIntOrNull() == null) {
                tilPrepTime.error = "Please enter a valid prep time"
                hasError = true
            } else {
                tilPrepTime.error = null
            }

            if (cookTime.isEmpty() || cookTime.toIntOrNull() == null) {
                tilCookTime.error = "Please enter a valid cook time"
                hasError = true
            } else {
                tilCookTime.error = null
            }

            if (hasError) return@setOnClickListener

            viewModel.prepTime.value = prepTime
            viewModel.cookTime.value = cookTime

            fm.beginTransaction()
                .replace(R.id.fragment_container, RecipeStep3Fragment())
                .addToBackStack("Step2")
                .commit()
        }
    }
}