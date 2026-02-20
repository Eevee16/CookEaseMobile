package com.cookease.app.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.Resource
import com.cookease.app.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _recipes = MutableLiveData<Resource<List<Recipe>>>()
    val recipes: LiveData<Resource<List<Recipe>>> = _recipes

    fun fetchApprovedRecipes() {
        viewModelScope.launch {
            _recipes.value = Resource.Loading()
            try {
                val results = SupabaseClientProvider.client.postgrest
                    .from("recipes")
                    .select {
                        filter { eq("status", "approved") }
                    }
                    .decodeList<Recipe>()
                _recipes.value = Resource.Success(results)
            } catch (e: Exception) {
                _recipes.value = Resource.Error(e.message ?: "Failed to load recipes")
            }
        }
    }

    fun searchRecipes(query: String) {
        viewModelScope.launch {
            _recipes.value = Resource.Loading()
            try {
                val results = SupabaseClientProvider.client.postgrest
                    .from("recipes")
                    .select {
                        filter {
                            eq("status", "approved")
                            ilike("title", "%$query%")
                        }
                    }
                    .decodeList<Recipe>()
                _recipes.value = Resource.Success(results)
            } catch (e: Exception) {
                _recipes.value = Resource.Error(e.message ?: "Search failed")
            }
        }
    }
}