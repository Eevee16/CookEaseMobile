package com.cookease.app.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _filteredRecipes = MutableLiveData<List<Recipe>>()
    val filteredRecipes: LiveData<List<Recipe>> = _filteredRecipes

    private var allRecipes: List<Recipe> = emptyList()

    fun fetchAllRecipes() {
        viewModelScope.launch {
            try {
                val recipes = SupabaseClientProvider.client.postgrest
                    .from("recipes")
                    .select {
                        filter { eq("status", "approved") }
                    }
                    .decodeList<Recipe>()
                allRecipes = recipes
                _filteredRecipes.value = recipes
            } catch (e: Exception) {
                e.printStackTrace()
                _filteredRecipes.value = emptyList()
            }
        }
    }

    fun filterRecipes(category: String, cuisine: String, searchTerm: String) {
        val filtered = allRecipes.filter { recipe ->
            (category.isBlank() || recipe.category.equals(category, ignoreCase = true)) &&
                    (cuisine.isBlank() || recipe.cuisine.equals(cuisine, ignoreCase = true)) &&
                    (searchTerm.isBlank() ||
                            recipe.title.contains(searchTerm, ignoreCase = true) ||
                            recipe.ingredients?.contains(searchTerm, ignoreCase = true) == true)
        }
        _filteredRecipes.value = filtered
    }

    fun searchRecipes(query: String) {
        filterRecipes("", "", query)
    }
}