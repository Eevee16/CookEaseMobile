package com.cookease.app.moderator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import kotlinx.coroutines.launch

sealed class ModeratorState {
    object Idle : ModeratorState()
    object Loading : ModeratorState()
    object Success : ModeratorState()
    data class Error(val message: String) : ModeratorState()
}

class ModeratorViewModel : ViewModel() {

    private val repository = ModeratorRepository()

    private val _allRecipes = MutableLiveData<List<Recipe>>(emptyList())

    val pendingRecipes: LiveData<List<Recipe>> get() = _pending
    val approvedRecipes: LiveData<List<Recipe>> get() = _approved
    val rejectedRecipes: LiveData<List<Recipe>> get() = _rejected

    private val _pending = MutableLiveData<List<Recipe>>(emptyList())
    private val _approved = MutableLiveData<List<Recipe>>(emptyList())
    private val _rejected = MutableLiveData<List<Recipe>>(emptyList())

    private val _state = MutableLiveData<ModeratorState>(ModeratorState.Idle)
    val state: LiveData<ModeratorState> = _state

    private val _actionState = MutableLiveData<ModeratorState>(ModeratorState.Idle)
    val actionState: LiveData<ModeratorState> = _actionState

    // ── Fetch ─────────────────────────────────────────────────────────

    fun fetchRecipes() {
        _state.value = ModeratorState.Loading
        viewModelScope.launch {
            try {
                val all = repository.fetchAllRecipes()
                _allRecipes.value = all
                splitRecipes(all)
                _state.value = ModeratorState.Success
            } catch (e: Exception) {
                _state.value = ModeratorState.Error(e.message ?: "Failed to load recipes")
            }
        }
    }

    private fun splitRecipes(all: List<Recipe>) {
        _pending.value = all.filter { it.status.isNullOrEmpty() || it.status == "pending" }
        _approved.value = all.filter { it.status == "approved" }
        _rejected.value = all.filter { it.status == "rejected" }
    }

    // ── Approve ───────────────────────────────────────────────────────

    fun approveRecipe(recipeId: String) {
        _actionState.value = ModeratorState.Loading
        viewModelScope.launch {
            try {
                repository.approveRecipe(recipeId)
                fetchRecipes()
                _actionState.value = ModeratorState.Success
            } catch (e: Exception) {
                _actionState.value = ModeratorState.Error(e.message ?: "Failed to approve recipe")
            }
        }
    }

    // ── Reject ────────────────────────────────────────────────────────

    fun rejectRecipe(recipeId: String, reason: String) {
        _actionState.value = ModeratorState.Loading
        viewModelScope.launch {
            try {
                repository.rejectRecipe(recipeId, reason)
                fetchRecipes()
                _actionState.value = ModeratorState.Success
            } catch (e: Exception) {
                _actionState.value = ModeratorState.Error(e.message ?: "Failed to reject recipe")
            }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────

    fun deleteRecipe(recipeId: String) {
        _actionState.value = ModeratorState.Loading
        viewModelScope.launch {
            try {
                repository.deleteRecipe(recipeId)
                fetchRecipes()
                _actionState.value = ModeratorState.Success
            } catch (e: Exception) {
                _actionState.value = ModeratorState.Error(e.message ?: "Failed to delete recipe")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ModeratorState.Idle
    }
}