package com.cookease.app.filter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cookease.app.Recipe

class FilterViewModel : ViewModel() {
    val selectedCategory = MutableLiveData<String>("")
    val selectedCuisine = MutableLiveData<String>("")
    val filteredRecipes = MutableLiveData<List<Recipe>>()

    fun filterRecipes(recipes: List<Recipe>) {
        var filtered = recipes

        selectedCategory.value?.let { category ->
            if (category.isNotEmpty() && category != "All") {
                filtered = filtered.filter { recipe -> recipe.category == category }
            }
        }

        selectedCuisine.value?.let { cuisine ->
            if (cuisine.isNotEmpty() && cuisine != "All") {
                filtered = filtered.filter { recipe -> recipe.cuisine == cuisine }
            }
        }

        filteredRecipes.value = filtered
    }
}