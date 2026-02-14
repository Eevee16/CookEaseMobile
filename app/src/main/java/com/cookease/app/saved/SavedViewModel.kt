package com.cookease.app.saved

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SavedViewModel - Manages saved/favorited recipes
 *
 * TODO: API Integration
 * 1. Inject RecipeRepository and Room Database DAO
 * 2. Load saved recipes from local database
 * 3. Sync with backend API
 * 4. Implement add/remove from saved
 * 5. Handle offline mode
 */
class SavedViewModel : ViewModel() {

    // ==================== LIVE DATA ====================
    private val _savedRecipes = MutableLiveData<Resource<List<Recipe>>>()
    val savedRecipes: LiveData<Resource<List<Recipe>>> = _savedRecipes

    private val _removeRecipeResult = MutableLiveData<Resource<Boolean>>()
    val removeRecipeResult: LiveData<Resource<Boolean>> = _removeRecipeResult

    // TODO: Inject database DAO and repository when ready
    // private val recipeDao: RecipeDao
    // private val repository: RecipeRepository

    // ==================== DATA LOADING ====================
    /**
     * Load saved recipes from local database
     * TODO: Replace with actual Room database query
     */
    fun loadSavedRecipes() {
        viewModelScope.launch {
            try {
                _savedRecipes.value = Resource.Loading()

                // TODO: Replace with actual database query
                // val localRecipes = recipeDao.getAllSavedRecipes()
                // _savedRecipes.value = Resource.Success(localRecipes)

                // TEMPORARY: Mock database delay
                delay(500)
                val mockSaved = getMockSavedRecipes()
                _savedRecipes.value = Resource.Success(mockSaved)

            } catch (e: Exception) {
                _savedRecipes.value = Resource.Error("Failed to load saved recipes: ${e.message}")
            }
        }
    }

    /**
     * Add recipe to saved/favorites
     * TODO: Replace with actual database insert and API sync
     */
    fun addToSaved(recipe: Recipe) {
        viewModelScope.launch {
            try {
                // TODO: Save to local database first (for offline support)
                // recipeDao.insertRecipe(recipe)

                // TODO: Then sync with backend API
                // repository.addToFavorites(recipe.id)

                // Reload saved recipes
                loadSavedRecipes()

            } catch (e: Exception) {
                _savedRecipes.value = Resource.Error("Failed to save recipe: ${e.message}")
            }
        }
    }

    /**
     * Remove recipe from saved/favorites
     * TODO: Replace with actual database delete and API sync
     */
    fun removeFromSaved(recipeId: String) {
        viewModelScope.launch {
            try {
                _removeRecipeResult.value = Resource.Loading()

                // TODO: Remove from local database
                // recipeDao.deleteRecipeById(recipeId)

                // TODO: Sync with backend API
                // repository.removeFromFavorites(recipeId)

                _removeRecipeResult.value = Resource.Success(true)

                // Reload saved recipes
                loadSavedRecipes()

            } catch (e: Exception) {
                _removeRecipeResult.value = Resource.Error("Failed to remove recipe: ${e.message}")
            }
        }
    }

    /**
     * Check if a recipe is saved
     * TODO: Replace with actual database query
     */
    fun isRecipeSaved(recipeId: String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            try {
                // TODO: Check in database
                // val exists = recipeDao.isRecipeSaved(recipeId)
                // result.value = exists

                // TEMPORARY: Mock check
                result.value = getMockSavedRecipes().any { it.id == recipeId }

            } catch (e: Exception) {
                result.value = false
            }
        }
        return result
    }

    /**
     * Sync saved recipes with backend
     * TODO: Implement when backend API is ready
     */
    fun syncSavedRecipes() {
        viewModelScope.launch {
            try {
                // TODO: Get local saved recipes
                // val localRecipes = recipeDao.getAllSavedRecipes()

                // TODO: Sync with backend
                // repository.syncFavorites(localRecipes.map { it.id })

                // TODO: Update local database with backend response

            } catch (e: Exception) {
                _savedRecipes.value = Resource.Error("Sync failed: ${e.message}")
            }
        }
    }

    // ==================== MOCK DATA (DELETE WHEN API IS READY) ====================
    /**
     * TEMPORARY: Mock saved recipes
     * DELETE THIS METHOD when API/Database is integrated
     */
    private fun getMockSavedRecipes(): List<Recipe> {
        return listOf(
            Recipe(
                id = "1",
                title = "Chicken Adobo",
                description = "Classic Filipino chicken braised in soy sauce and vinegar",
                image = "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400",
                rating = 4.8f,
                difficulty = "Easy",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "10",
                title = "Kare-Kare",
                description = "Oxtail stew with peanut sauce served with vegetables and bagoong",
                image = "https://images.unsplash.com/photo-1607076786357-1c0f35265f62?w=400",
                rating = 4.6f,
                difficulty = "Hard",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "5",
                title = "Halo-Halo",
                description = "Refreshing Filipino shaved ice dessert with sweet beans and fruits",
                image = "https://images.unsplash.com/photo-1563805042-7684a24f32e8?w=400",
                rating = 4.4f,
                difficulty = "Easy",
                cuisine = "Filipino"
            )
        )
    }
}