package com.cookease.app.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.cookease.app.Recipe
import com.cookease.app.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class SearchCourseCuisineViewModel(private val supabase: io.github.jan.supabase.SupabaseClient) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _allRecipes = MutableLiveData<List<Recipe>>(emptyList())

    private val _filteredRecipes = MutableLiveData<List<Recipe>>(emptyList())
    val filteredRecipes: LiveData<List<Recipe>> = _filteredRecipes

    private val _categories = MutableLiveData<List<String>>(emptyList())
    val categories: LiveData<List<String>> = _categories

    private val _cuisines = MutableLiveData<List<String>>(emptyList())
    val cuisines: LiveData<List<String>> = _cuisines

    var selectedCategory: String = ""
    var selectedCuisine: String = ""

    fun fetchRecipes() {
        _loading.value = true
        viewModelScope.launch {
            runCatching {
                supabase.postgrest["recipes"]
                    .select { filter { eq("status", "approved") } }
                    .decodeList<Recipe>()
            }.onSuccess { data ->
                _allRecipes.value = data
                _categories.value = data.mapNotNull { it.category }.distinct().sorted()
                _cuisines.value = data.mapNotNull { it.cuisine }.distinct().sorted()
                _filteredRecipes.value = data
            }.onFailure {
                _allRecipes.value = emptyList()
            }
            _loading.value = false
        }
    }

    fun applyFilters() {
        val all = _allRecipes.value ?: return
        _filteredRecipes.value = all.filter { recipe ->
            (selectedCategory.isEmpty() || recipe.category == selectedCategory) &&
                    (selectedCuisine.isEmpty() || recipe.cuisine == selectedCuisine)
        }
    }

    fun clearFilters() {
        selectedCategory = ""
        selectedCuisine = ""
        _filteredRecipes.value = _allRecipes.value ?: emptyList()
    }
}

class SearchCourseCuisineViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SearchCourseCuisineViewModel(SupabaseClientProvider.client) as T
    }
}