package com.cookease.app.saved

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.Resource
import kotlinx.coroutines.launch

class SavedViewModel : ViewModel() {

    private val _savedRecipes = MutableLiveData<Resource<List<Recipe>>>()
    val savedRecipes: LiveData<Resource<List<Recipe>>> = _savedRecipes

    private val _removeRecipeResult = MutableLiveData<Resource<Unit>>()
    val removeRecipeResult: LiveData<Resource<Unit>> = _removeRecipeResult

    fun loadSavedRecipes() {
        _savedRecipes.value = Resource.Loading()
        viewModelScope.launch {
            try {
                // TODO: Replace with actual Supabase fetch
                val recipes = listOf<Recipe>()
                _savedRecipes.value = Resource.Success(recipes)
            } catch (e: Exception) {
                _savedRecipes.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun removeFromSaved(recipeId: String) {
        _removeRecipeResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                // TODO: Replace with actual Supabase delete
                _removeRecipeResult.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _removeRecipeResult.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
}