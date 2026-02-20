package com.cookease.app.filter

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cookease.app.R
import com.cookease.app.Recipe

class CourseCuisineFilterFragment : Fragment(R.layout.fragment_filter) {

    private val filterViewModel: FilterViewModel by activityViewModels()

    private lateinit var categorySpinner: Spinner
    private lateinit var cuisineSpinner: Spinner
    private lateinit var resultTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categorySpinner = view.findViewById(R.id.spCategory)
        cuisineSpinner = view.findViewById(R.id.spCuisine)
        resultTextView = view.findViewById(R.id.tvResultCount)

        val categories = listOf("All", "Breakfast", "Lunch", "Dinner", "Snack", "Dessert")
        val cuisines = listOf("All", "Filipino", "Italian", "American", "Japanese", "Chinese")

        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = categoryAdapter

        val cuisineAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, cuisines)
        cuisineSpinner.adapter = cuisineAdapter

        filterViewModel.filteredRecipes.observe(viewLifecycleOwner) { filteredRecipes ->
            resultTextView.text = "${filteredRecipes.size} recipes available"
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterViewModel.selectedCategory.value = categorySpinner.selectedItem.toString()
                filterViewModel.filterRecipes(getRecipes())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        cuisineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterViewModel.selectedCuisine.value = cuisineSpinner.selectedItem.toString()
                filterViewModel.filterRecipes(getRecipes())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun getRecipes(): List<Recipe> {
        return listOf(
            Recipe(id = "1", title = "Pancakes", category = "Breakfast", cuisine = "American"),
            Recipe(id = "2", title = "Sushi", category = "Dinner", cuisine = "Japanese"),
            Recipe(id = "3", title = "Pasta", category = "Lunch", cuisine = "Italian")
        )
    }
}