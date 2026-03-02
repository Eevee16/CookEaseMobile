package com.cookease.app.saved

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cookease.app.Resource
import com.cookease.app.SupabaseClientProvider
import com.cookease.app.data.local.AppDatabase
import kotlinx.coroutines.launch

class SavedViewModel(application: Application) : AndroidViewModel(application) {

    // Repository with initialized Supabase client
    private val repo = SavedRecipeRepository(
        db = AppDatabase.getInstance(application),
        supabase = SupabaseClientProvider.client
    )

    // LiveData for observing saved recipes
    val savedRecipes = repo.getSavedRecipes().asLiveData()

    // Sync state LiveData
    private val _syncState = MutableLiveData<Resource<Unit>>()
    val syncState: LiveData<Resource<Unit>> get() = _syncState

    // Remove operation result LiveData
    private val _removeResult = MutableLiveData<Resource<Unit>>()
    val removeResult: LiveData<Resource<Unit>> get() = _removeResult

    init {
        syncFromRemote()
    }

    /** Sync saved recipes from Supabase */
    fun syncFromRemote() {
        viewModelScope.launch {
            _syncState.value = Resource.Loading()
            val result = repo.syncFromSupabase()
            _syncState.value = result
        }
    }

    /** Remove a recipe from saved recipes */
    fun removeFromSaved(recipeId: String) {
        viewModelScope.launch {
            _removeResult.value = Resource.Loading()
            val result = repo.removeFromSaved(recipeId)
            _removeResult.value = result
        }
    }
}
