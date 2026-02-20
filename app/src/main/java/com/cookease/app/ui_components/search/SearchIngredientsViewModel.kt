package com.cookease.app.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import android.content.Context

class SearchIngredientsViewModel(private val supabase: io.github.jan.supabase.SupabaseClient) : ViewModel() {

    private val _allRecipes = MutableLiveData<List<Recipe>>(emptyList())

    private val _allIngredients = MutableLiveData<List<String>>(emptyList())
    val allIngredients: LiveData<List<String>> = _allIngredients

    private val _filteredIngredients = MutableLiveData<List<String>>(emptyList())
    val filteredIngredients: LiveData<List<String>> = _filteredIngredients

    private val _selectedIngredients = MutableLiveData<Set<String>>(emptySet())
    val selectedIngredients: LiveData<Set<String>> = _selectedIngredients

    private val _filteredRecipes = MutableLiveData<List<Recipe>>(emptyList())
    val filteredRecipes: LiveData<List<Recipe>> = _filteredRecipes

    private val _loading = MutableLiveData(true)
    val loading: LiveData<Boolean> = _loading

    fun fetchRecipes() {
        _loading.value = true
        viewModelScope.launch {
            runCatching {
                supabase.postgrest["recipes"]
                    .select {
                        filter { eq("status", "approved") }
                    }
                    .decodeList<Recipe>()
            }.onSuccess { data ->
                _allRecipes.value = data
                extractIngredients(data)
                applyIngredientFilter()
            }.onFailure {
                _allRecipes.value = emptyList()
            }
            _loading.value = false
        }
    }

    private fun extractIngredients(recipes: List<Recipe>) {
        val set = mutableSetOf<String>()
        val measurementWords = setOf("cup", "tablespoon", "teaspoon", "pound", "ounce",
            "gram", "kg", "lb", "oz", "tbsp", "tsp")
        val prepWords = setOf("minced", "chopped", "diced", "sliced", "crushed",
            "ground", "fresh", "dried", "cooked", "peeled", "trimmed")

        recipes.forEach { recipe ->
            recipe.ingredients?.split(",", "\n")?.forEach { ing ->
                val cleaned = ing.lowercase()
                    .replace(Regex("[0-9½¼¾⅓⅔⅛⅜⅝⅞]"), "")
                    .split(" ")
                    .filter { word -> word !in measurementWords && word !in prepWords }
                    .joinToString(" ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                if (cleaned.length > 2) set.add(cleaned)
            }
        }
        val sorted = set.sorted()
        _allIngredients.value = sorted
        _filteredIngredients.value = sorted
    }

    fun filterIngredients(query: String) {
        _filteredIngredients.value = if (query.isBlank()) _allIngredients.value ?: emptyList()
        else _allIngredients.value?.filter { it.contains(query, ignoreCase = true) } ?: emptyList()
    }

    fun toggleIngredient(ingredient: String) {
        val current = _selectedIngredients.value?.toMutableSet() ?: mutableSetOf()
        if (current.contains(ingredient)) current.remove(ingredient) else current.add(ingredient)
        _selectedIngredients.value = current
        applyIngredientFilter()
    }

    fun clearAll() {
        _selectedIngredients.value = emptySet()
        _filteredRecipes.value = emptyList()
    }

    private fun applyIngredientFilter() {
        val selected = _selectedIngredients.value ?: return
        if (selected.isEmpty()) {
            _filteredRecipes.value = emptyList()
            return
        }
        _filteredRecipes.value = _allRecipes.value?.filter { recipe ->
            selected.all { sel ->
                recipe.ingredients?.contains(sel, ignoreCase = true) == true
            }
        } ?: emptyList()
    }
}

class SearchIngredientsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SearchIngredientsViewModel(SupabaseClientProvider.client) as T
    }
}