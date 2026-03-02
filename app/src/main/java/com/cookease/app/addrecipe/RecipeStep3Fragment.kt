package com.cookease.app.addrecipe

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cookease.app.R
import com.cookease.app.SupabaseClientProvider
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class RecipeStep3Fragment : Fragment(R.layout.fragment_add_step3) {

    private val viewModel: AddRecipeViewModel by activityViewModels()

    private lateinit var searchAdapter: IngredientSearchAdapter
    private lateinit var selectedAdapter: SelectedIngredientAdapter
    private lateinit var instructionAdapter: InstructionStepAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSearch = view.findViewById<TextInputEditText>(R.id.etIngredientSearch)
        val rvResults = view.findViewById<RecyclerView>(R.id.rvIngredientResults)
        val rvSelected = view.findViewById<RecyclerView>(R.id.rvSelectedIngredients)
        val rvSteps = view.findViewById<RecyclerView>(R.id.rvInstructionSteps)
        val tvIngredientCount = view.findViewById<TextView>(R.id.tvIngredientCount)
        val tvStepCount = view.findViewById<TextView>(R.id.tvStepCount)
        val tvSelectedLabel = view.findViewById<TextView>(R.id.tvSelectedLabel)
        val tvIngredientsError = view.findViewById<TextView>(R.id.tvIngredientsError)
        val tvInstructionsError = view.findViewById<TextView>(R.id.tvInstructionsError)
        val btnAddStep = view.findViewById<Button>(R.id.btnAddStep)
        val btnBack = view.findViewById<Button>(R.id.btnBackStep3)
        val btnDone = view.findViewById<Button>(R.id.btnDone)

        val fm = requireParentFragment().childFragmentManager

        // ── Search Adapter ────────────────────────────────────────────
        searchAdapter = IngredientSearchAdapter(
            items = emptyList(),
            isAdded = { viewModel.isIngredientAdded(it.name) },
            onAddClick = { ingredient ->
                showIngredientPopup(ingredient)
            }
        )
        rvResults.layoutManager = LinearLayoutManager(requireContext())
        rvResults.adapter = searchAdapter

        // ── Selected Adapter ──────────────────────────────────────────
        selectedAdapter = SelectedIngredientAdapter(
            items = viewModel.selectedIngredients.value ?: mutableListOf(),
            onRemove = { idx ->
                viewModel.removeIngredient(idx)
            },
            onQtyChanged = { idx, qty -> viewModel.updateIngredient(idx, qty = qty) },
            onUnitChanged = { idx, unit -> viewModel.updateIngredient(idx, unit = unit) },
            onPrepChanged = { idx, prep -> viewModel.updateIngredient(idx, prep = prep) }
        )
        rvSelected.layoutManager = LinearLayoutManager(requireContext())
        rvSelected.adapter = selectedAdapter

        // ── Instruction Adapter ───────────────────────────────────────
        instructionAdapter = InstructionStepAdapter(
            steps = viewModel.instructionSteps.value ?: mutableListOf(""),
            onRemove = { idx -> viewModel.removeInstructionStep(idx) },
            onTextChanged = { idx, text -> viewModel.updateInstructionStep(idx, text) }
        )
        rvSteps.layoutManager = LinearLayoutManager(requireContext())
        rvSteps.adapter = instructionAdapter

        // ── Load ingredients from Supabase ────────────────────────────
        if (viewModel.allIngredients.value.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    val client = SupabaseClientProvider.client
                    val result = client.postgrest.from("ingredients")
                        .select()
                        .decodeList<IngredientItem>()
                    viewModel.setAllIngredients(result)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to load ingredients", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ── Observe all ingredients → update search results ───────────
        viewModel.allIngredients.observe(viewLifecycleOwner) { all ->
            val query = etSearch.text.toString().trim()
            val filtered = if (query.isEmpty()) all.take(8) else all.filter {
                it.name.contains(query, ignoreCase = true)
            }
            searchAdapter.updateList(filtered)
        }

        // ── Search filter ─────────────────────────────────────────────
        etSearch.addTextChangedListener { text ->
            val query = text.toString().trim()
            val all = viewModel.allIngredients.value ?: emptyList()
            val filtered = if (query.isEmpty()) all.take(8) else all.filter {
                it.name.contains(query, ignoreCase = true)
            }
            searchAdapter.updateList(filtered)
        }

        // ── Observe selected ingredients ──────────────────────────────
        viewModel.selectedIngredients.observe(viewLifecycleOwner) { list ->
            selectedAdapter.updateList(list)
            tvIngredientCount.text = "${list.size} selected"
            tvSelectedLabel.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            tvIngredientsError.visibility = View.GONE
            searchAdapter.notifyDataSetChanged() // refresh Added states
        }

        // ── Observe instruction steps ─────────────────────────────────
        viewModel.instructionSteps.observe(viewLifecycleOwner) { steps ->
            instructionAdapter.updateList(steps)
            val filled = steps.count { it.trim().isNotEmpty() }
            tvStepCount.text = "$filled steps"
            tvInstructionsError.visibility = View.GONE
        }

        // ── Add step ──────────────────────────────────────────────────
        btnAddStep.setOnClickListener { viewModel.addInstructionStep() }

        // ── Back ──────────────────────────────────────────────────────
        btnBack.setOnClickListener { fm.popBackStack() }

        // ── Submit state ──────────────────────────────────────────────
        viewModel.submitState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SubmitState.Loading -> {
                    btnDone.isEnabled = false
                    btnDone.text = "Submitting..."
                }
                is SubmitState.Success -> {
                    viewModel.resetAllFields()
                    fm.beginTransaction()
                        .replace(R.id.fragment_container, RecipeSuccessFragment())
                        .commit()
                }
                is SubmitState.Error -> {
                    btnDone.isEnabled = true
                    btnDone.text = getString(R.string.submit_recipe)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                is SubmitState.Idle -> {
                    btnDone.isEnabled = true
                    btnDone.text = getString(R.string.submit_recipe)
                }
            }
        }

        // ── Done / Submit ─────────────────────────────────────────────
        btnDone.setOnClickListener {
            var valid = true

            if (viewModel.selectedIngredients.value.isNullOrEmpty()) {
                tvIngredientsError.visibility = View.VISIBLE
                valid = false
            }

            val hasSteps = viewModel.instructionSteps.value?.any { it.trim().isNotEmpty() } == true
            if (!hasSteps) {
                tvInstructionsError.visibility = View.VISIBLE
                valid = false
            }

            if (!valid) return@setOnClickListener

            viewModel.submitRecipe()
        }
    }

    private fun showIngredientPopup(ingredient: IngredientItem) {
        val dialog = IngredientPopupDialog(
            ingredient = ingredient,
            onConfirm = { selected ->
                viewModel.addIngredient(selected)
            }
        )
        dialog.show(childFragmentManager, "IngredientPopup")
    }
}