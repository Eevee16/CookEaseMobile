package com.cookease.app.moderator

import com.cookease.app.Recipe
import com.cookease.app.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ModeratorRepository {

    private val client = SupabaseClientProvider.client

    suspend fun fetchAllRecipes(): List<Recipe> {
        return client.postgrest.from("recipes")
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Recipe>()
    }

    suspend fun approveRecipe(recipeId: String) {
        client.postgrest.from("recipes")
            .update(buildJsonObject {
                put("status", "approved")
                put("rejection_reason", null as String?)
            }) {
                filter { eq("id", recipeId) }
            }
    }

    suspend fun rejectRecipe(recipeId: String, reason: String) {
        client.postgrest.from("recipes")
            .update(buildJsonObject {
                put("status", "rejected")
                put("rejection_reason", reason)
            }) {
                filter { eq("id", recipeId) }
            }
    }

    suspend fun deleteRecipe(recipeId: String) {
        client.postgrest.from("recipes")
            .delete {
                filter { eq("id", recipeId) }
            }
    }
}