package com.cookease.app.saved

import com.cookease.app.data.local.AppDatabase
import com.cookease.app.data.local.RecipeEntity
import com.cookease.app.Recipe
import com.cookease.app.Resource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedRecipeRepository(
    private val db: AppDatabase,
    private val supabase: SupabaseClient
) {

    fun getSavedRecipes(): Flow<List<Recipe>> {
        return db.savedRecipeDao().getAllSavedRecipes().map { entities ->
            entities.map { it.toRecipe() }
        }
    }

    suspend fun syncFromSupabase(): Resource<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Resource.Error("Not logged in")

            val savedRows = supabase.postgrest["saved_recipes"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<SavedRecipeRow>()

            val ids = savedRows.map { it.recipe_id }
            if (ids.isEmpty()) return Resource.Success(Unit)

            val recipes = supabase.postgrest["recipes"]
                .select { filter { isIn("id", ids) } }
                .decodeList<Recipe>()

            recipes.forEach { recipe ->
                db.savedRecipeDao().insertRecipe(recipe.toEntity())
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sync failed")
        }
    }

    suspend fun removeFromSaved(recipeId: String): Resource<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Resource.Error("Not logged in")

            supabase.postgrest["saved_recipes"]
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("recipe_id", recipeId)
                    }
                }

            db.savedRecipeDao().deleteRecipe(recipeId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            // If Supabase fails, still remove locally (offline support)
            db.savedRecipeDao().deleteRecipe(recipeId)
            Resource.Success(Unit)
        }
    }
}

data class SavedRecipeRow(val recipe_id: String)

/* ===========================
   MAPPERS (TYPE SAFE)
   =========================== */

fun RecipeEntity.toRecipe() = Recipe(
    id = id,
    title = title,
    category = category,
    cuisine = cuisine,
    imageUrl = imageUrl,
    rating = rating.toDouble(),
    status = status,
    createdAt = createdAt,
    description = description,
    ingredients = ingredients?.split("||") ?: emptyList(),   // String → List<String>
    instructions = instructions?.split("||") ?: emptyList()
)

fun Recipe.toEntity() = RecipeEntity(
    id = id,
    title = title,
    category = category,
    cuisine = cuisine,
    imageUrl = imageUrl,
    rating = rating?.toFloat() ?: 0f,
    status = status,
    createdAt = createdAt,
    description = description,
    ingredients = ingredients.joinToString("||"),   // List<String> → String
    instructions = instructions.joinToString("||")
)
