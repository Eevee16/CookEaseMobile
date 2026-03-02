package com.cookease.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRecipeDao {

    @Query("SELECT * FROM saved_recipes ORDER BY savedAt DESC")
    fun getAllSavedRecipes(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM saved_recipes WHERE id = :recipeId")
    suspend fun deleteRecipe(recipeId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_recipes WHERE id = :recipeId)")
    suspend fun isRecipeSaved(recipeId: String): Boolean
}