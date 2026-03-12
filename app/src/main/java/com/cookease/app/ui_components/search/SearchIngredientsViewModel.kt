package com.cookease.app.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.SupabaseClientProvider
import com.cookease.app.addrecipe.IngredientItem
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import android.content.Context

class SearchIngredientsViewModel(private val supabase: io.github.jan.supabase.SupabaseClient) : ViewModel() {

    private val _allRecipes = MutableLiveData<List<Recipe>>(emptyList())

    private val _allIngredients = MutableLiveData<List<IngredientItem>>(emptyList())
    val allIngredients: LiveData<List<IngredientItem>> = _allIngredients

    private val _filteredIngredients = MutableLiveData<List<IngredientItem>>(emptyList())
    val filteredIngredients: LiveData<List<IngredientItem>> = _filteredIngredients

    private val _selectedIngredients = MutableLiveData<Set<String>>(emptySet())
    val selectedIngredients: LiveData<Set<String>> = _selectedIngredients

    private val _filteredRecipes = MutableLiveData<List<Recipe>>(emptyList())
    val filteredRecipes: LiveData<List<Recipe>> = _filteredRecipes

    private val _loading = MutableLiveData(true)
    val loading: LiveData<Boolean> = _loading

    private var currentSearchQuery = ""

    init {
        fetchData()
    }

    fun fetchData() {
        _loading.value = true
        viewModelScope.launch {
            try {
                // 1. Fetch Ingredients
                val ingredients = supabase.postgrest["ingredients"]
                    .select()
                    .decodeList<IngredientItem>()
                    .sortedBy { it.name }
                
                _allIngredients.value = ingredients
                // Immediately populate filtered list so they show up before searching
                filterIngredients(currentSearchQuery)

                // 2. Fetch Approved Recipes
                val recipes = supabase.postgrest["recipes"]
                    .select {
                        filter { 
                            or {
                                eq("status", "approved")
                                eq("status", "done")
                            }
                        }
                    }
                    .decodeList<Recipe>()
                
                _allRecipes.value = recipes
                applyIngredientFilter()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun filterIngredients(query: String) {
        currentSearchQuery = query
        val all = _allIngredients.value ?: emptyList()
        val selected = _selectedIngredients.value ?: emptySet()
        
        // Show all unselected ingredients if query is blank, otherwise filter
        _filteredIngredients.value = if (query.isBlank()) {
            all.filter { it.name !in selected }
        } else {
            all.filter { it.name !in selected && it.name.contains(query, ignoreCase = true) }
        }
    }

    fun toggleIngredient(ingredientName: String) {
        val current = _selectedIngredients.value?.toMutableSet() ?: mutableSetOf()
        if (current.contains(ingredientName)) {
            current.remove(ingredientName)
        } else {
            current.add(ingredientName)
        }
        _selectedIngredients.value = current
        filterIngredients(currentSearchQuery)
        applyIngredientFilter()
    }

    fun clearAll() {
        _selectedIngredients.value = emptySet()
        filterIngredients("")
        applyIngredientFilter()
    }

    private fun applyIngredientFilter() {
        val selected = _selectedIngredients.value ?: emptySet()
        val recipes = _allRecipes.value ?: emptyList()

        if (selected.isEmpty()) {
            _filteredRecipes.value = recipes
            return
        }

        _filteredRecipes.value = recipes.filter { recipe ->
            selected.all { sel ->
                recipe.ingredients.any { ing -> 
                    ing.contains(sel, ignoreCase = true)
                }
            }
        }
    }
}

class SearchIngredientsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SearchIngredientsViewModel(SupabaseClientProvider.client) as T
    }
}
