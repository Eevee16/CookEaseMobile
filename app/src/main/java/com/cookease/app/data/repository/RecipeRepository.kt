package com.cookease.app.data.repository

import com.cookease.app.Recipe
import com.cookease.app.data.local.AppDatabase
import com.cookease.app.saved.toRecipe
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

class RecipeRepository(
    private val supabase: SupabaseClient,
    private val db: AppDatabase? = null
) {

    suspend fun getRecipeById(id: String): Result<Recipe> = runCatching {
        try {
            // First try fetching from Supabase (online)
            supabase.postgrest["recipes"]
                .select(Columns.ALL) { filter { eq("id", id) } }
                .decodeSingle<Recipe>()
        } catch (e: Exception) {
            // If offline or error, try fetching from local Room database
            val localRecipe = db?.savedRecipeDao()?.getRecipeById(id)
            localRecipe?.toRecipe() ?: throw e
        }
    }

    suspend fun incrementViewCount(id: String): Result<Unit> = runCatching {
        val current = supabase.postgrest["recipes"]
            .select(Columns.raw("view_count")) { filter { eq("id", id) } }
            .decodeSingle<JsonObject>()
        val currentCount = current["view_count"]?.jsonPrimitive?.int ?: 0
        supabase.postgrest["recipes"]
            .update({ set("view_count", currentCount + 1) }) { filter { eq("id", id) } }
    }

    fun parseIngredients(raw: Any?): List<String> {
        if (raw == null) return emptyList()
        return when (raw) {
            is List<*> -> raw.filterIsInstance<String>().filter { it.isNotBlank() }
            is String -> {
                runCatching { Json.decodeFromString<List<String>>(raw) }.getOrElse {
                    raw.split("\n", "\\n")
                        .map { it.trim().replace(Regex("^\\d+\\.\\s*"), "") }
                        .filter { it.isNotBlank() }
                }
            }
            else -> emptyList()
        }
    }
}
