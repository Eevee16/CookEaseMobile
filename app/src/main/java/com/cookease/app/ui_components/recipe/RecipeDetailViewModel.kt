package com.cookease.app.ui_components.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.SupabaseClientProvider
import com.cookease.app.data.repository.RecipeRepository
import kotlinx.coroutines.launch
import android.content.Context

sealed class RecipeDetailState {
    object Loading : RecipeDetailState()
    data class Success(val recipe: Recipe) : RecipeDetailState()
    object NotFound : RecipeDetailState()
    data class Error(val message: String) : RecipeDetailState()
}

class RecipeDetailViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _state = MutableLiveData<RecipeDetailState>()
    val state: LiveData<RecipeDetailState> = _state

    private val _isSaved = MutableLiveData(false)
    val isSaved: LiveData<Boolean> = _isSaved

    private val _servingsMultiplier = MutableLiveData(1.0f)
    val servingsMultiplier: LiveData<Float> = _servingsMultiplier

    private val viewedIds = mutableSetOf<String>()

    fun loadRecipe(id: String) {
        _state.value = RecipeDetailState.Loading
        viewModelScope.launch {
            repository.getRecipeById(id)
                .onSuccess { recipe ->
                    _state.value = RecipeDetailState.Success(recipe)
                    trackView(id)
                }
                .onFailure {
                    _state.value = RecipeDetailState.NotFound
                }
        }
    }

    private fun trackView(id: String) {
        if (viewedIds.contains(id)) return
        viewedIds.add(id)
        viewModelScope.launch { repository.incrementViewCount(id) }
    }

    fun toggleSaved() { _isSaved.value = !(_isSaved.value ?: false) }

    fun adjustServings(increment: Float) {
        val new = (_servingsMultiplier.value ?: 1.0f) + increment
        if (new in 0.5f..10.0f) _servingsMultiplier.value = new
    }

    fun resetServings() { _servingsMultiplier.value = 1.0f }

    fun formatTime(minutes: Int?): String {
        if (minutes == null || minutes <= 0) return "N/A"
        return if (minutes < 60) "$minutes min"
        else {
            val h = minutes / 60; val m = minutes % 60
            if (m > 0) "${h}h ${m}m" else "${h}h"
        }
    }
}

class RecipeDetailViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RecipeDetailViewModel(
            RecipeRepository(SupabaseClientProvider.client)
        ) as T
    }
}