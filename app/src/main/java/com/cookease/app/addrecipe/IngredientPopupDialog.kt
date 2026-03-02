package com.cookease.app.addrecipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.cookease.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class IngredientPopupDialog(
    private val ingredient: IngredientItem,
    private val onConfirm: (SelectedIngredient) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_ingredient_popup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvPopupIngredientName)
        val etQty = view.findViewById<TextInputEditText>(R.id.etPopupQty)
        val spUnit = view.findViewById<Spinner>(R.id.spPopupUnit)
        val etPrep = view.findViewById<TextInputEditText>(R.id.etPopupPrep)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupPrepSuggestions)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnPopupCancel)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnPopupConfirm)

        tvTitle.text = ingredient.name
        btnConfirm.text = "Add ${ingredient.name}"

        // Unit spinner
        val unitAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            AddRecipeViewModel.UNITS
        )
        spUnit.adapter = unitAdapter

        // Prep suggestion chips
        AddRecipeViewModel.PREP_SUGGESTIONS.forEach { suggestion ->
            val chip = Chip(requireContext()).apply {
                text = suggestion
                isCheckable = true
                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        etPrep.setText(suggestion)
                        // uncheck others
                        for (i in 0 until chipGroup.childCount) {
                            val c = chipGroup.getChildAt(i) as? Chip
                            if (c != this) c?.isChecked = false
                        }
                    } else {
                        if (etPrep.text.toString() == suggestion) etPrep.setText("")
                    }
                }
            }
            chipGroup.addView(chip)
        }

        btnCancel.setOnClickListener { dismiss() }

        btnConfirm.setOnClickListener {
            val selected = SelectedIngredient(
                id = ingredient.id,
                name = ingredient.name,
                image_url = ingredient.image_url,
                qty = etQty.text.toString().trim(),
                unit = AddRecipeViewModel.UNITS[spUnit.selectedItemPosition],
                prep = etPrep.text.toString().trim()
            )
            onConfirm(selected)
            dismiss()
        }
    }
}