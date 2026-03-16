package com.cookease.app.ui_components.recipe

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.SupabaseClientProvider
import com.cookease.app.data.local.AppDatabase
import com.cookease.app.data.repository.RecipeRepository
import com.cookease.app.saved.SavedRecipeRepository
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

sealed class RecipeDetailState {
    object Loading : RecipeDetailState()
    data class Success(val recipe: Recipe) : RecipeDetailState()
    object NotFound : RecipeDetailState()
    data class Error(val message: String) : RecipeDetailState()
}

class RecipeDetailViewModel(
    private val repository: RecipeRepository,
    private val savedRepository: SavedRecipeRepository
) : ViewModel() {

    private val _state = MutableLiveData<RecipeDetailState>()
    val state: LiveData<RecipeDetailState> = _state

    private val _isSaved = MutableLiveData(false)
    val isSaved: LiveData<Boolean> = _isSaved

    private val _ownerPhotoUrl = MutableLiveData<String?>(null)
    val ownerPhotoUrl: LiveData<String?> = _ownerPhotoUrl

    private val _servingsMultiplier = MutableLiveData(1.0f)
    val servingsMultiplier: LiveData<Float> = _servingsMultiplier

    private val viewedIds = mutableSetOf<String>()

    fun loadRecipe(id: String) {
        _state.value = RecipeDetailState.Loading
        viewModelScope.launch {
            repository.getRecipeById(id)
                .onSuccess { recipe ->
                    _state.value = RecipeDetailState.Success(recipe)
                    checkIfSaved(id)
                    trackView(id)
                    fetchOwnerProfile(recipe.ownerId)
                }
                .onFailure {
                    _state.value = RecipeDetailState.NotFound
                }
        }
    }

    private fun fetchOwnerProfile(ownerId: String?) {
        if (ownerId.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val client = SupabaseClientProvider.client
                val profile = client.postgrest.from("profiles")
                    .select { filter { eq("id", ownerId) } }
                    .decodeSingleOrNull<Map<String, kotlinx.serialization.json.JsonElement>>()
                
                val photoUrl = profile?.get("photo_url")?.jsonPrimitive?.content
                _ownerPhotoUrl.postValue(photoUrl)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkIfSaved(id: String) {
        viewModelScope.launch {
            savedRepository.getSavedRecipes().collect { savedList ->
                _isSaved.postValue(savedList.any { it.id == id })
            }
        }
    }

    private fun trackView(id: String) {
        if (viewedIds.contains(id)) return
        viewedIds.add(id)
        viewModelScope.launch { repository.incrementViewCount(id) }
    }

    fun toggleSaved() {
        val recipe = (state.value as? RecipeDetailState.Success)?.recipe ?: return
        val currentlySaved = _isSaved.value ?: false
        
        viewModelScope.launch {
            if (currentlySaved) {
                savedRepository.removeFromSaved(recipe.id)
                _isSaved.postValue(false)
            } else {
                savedRepository.addToSaved(recipe)
                _isSaved.postValue(true)
            }
        }
    }

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
        val db = AppDatabase.getInstance(context)
        val supabase = SupabaseClientProvider.client
        @Suppress("UNCHECKED_CAST")
        return RecipeDetailViewModel(
            RecipeRepository(supabase, db), // Passed the db instance here
            SavedRecipeRepository(db, supabase)
        ) as T
    }
}
