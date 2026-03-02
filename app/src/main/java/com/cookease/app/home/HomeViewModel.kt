package com.cookease.app.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.Resource
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = HomeRepository()

    private val _recipes = MutableLiveData<Resource<List<Recipe>>>()
    val recipes: LiveData<Resource<List<Recipe>>> = _recipes

    // All loaded recipes — used for client-side filtering
    private var allRecipes: List<Recipe> = emptyList()

    // Active filters
    private var activeCategory = "all"
    private var activeCuisine = "all"
    private var activeDifficulty = "all"

    fun fetchApprovedRecipes() {
        viewModelScope.launch {
            _recipes.value = Resource.Loading()
            try {
                val results = repository.fetchApprovedRecipes()
                allRecipes = results
                applyFilters()
            } catch (e: Exception) {
                _recipes.value = Resource.Error(e.message ?: "Failed to load recipes")
            }
        }
    }

    fun searchRecipes(query: String) {
        viewModelScope.launch {
            _recipes.value = Resource.Loading()
            try {
                val results = repository.searchRecipes(query)
                allRecipes = results
                applyFilters()
            } catch (e: Exception) {
                _recipes.value = Resource.Error(e.message ?: "Search failed")
            }
        }
    }

    fun setFilter(category: String? = null, cuisine: String? = null, difficulty: String? = null) {
        category?.let { activeCategory = it }
        cuisine?.let { activeCuisine = it }
        difficulty?.let { activeDifficulty = it }
        applyFilters()
    }

    fun clearFilters() {
        activeCategory = "all"
        activeCuisine = "all"
        activeDifficulty = "all"
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = allRecipes
        if (activeCategory != "all") filtered = filtered.filter { it.category == activeCategory }
        if (activeCuisine != "all") filtered = filtered.filter { it.cuisine == activeCuisine }
        if (activeDifficulty != "all") filtered = filtered.filter { it.difficulty == activeDifficulty }
        _recipes.value = Resource.Success(filtered)
    }

    fun getCategories(): List<String> =
        listOf("all") + allRecipes.mapNotNull { it.category }.distinct().sorted()

    fun getCuisines(): List<String> =
        listOf("all") + allRecipes.mapNotNull { it.cuisine }.distinct().sorted()
}