package com.cookease.app.home

import com.cookease.app.Recipe
import com.cookease.app.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class HomeRepository {

    private val client = SupabaseClientProvider.client

    suspend fun fetchApprovedRecipes(): List<Recipe> {
        val recipes = client.postgrest.from("recipes")
            .select {
                filter { isIn("status", listOf("approved", "done")) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Recipe>()

        return enrichRecipesWithProfiles(recipes)
    }

    suspend fun searchRecipes(query: String): List<Recipe> {
        val recipes = client.postgrest.from("recipes")
            .select {
                filter {
                    isIn("status", listOf("approved", "done"))
                    ilike("title", "%$query%")
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Recipe>()

        return enrichRecipesWithProfiles(recipes)
    }

    private suspend fun enrichRecipesWithProfiles(recipes: List<Recipe>): List<Recipe> {
        val ownerIds = recipes.mapNotNull { it.ownerId }.distinct()
        if (ownerIds.isEmpty()) return recipes

        return try {
            // Fetch profile data for all owners in one request
            val profiles = client.postgrest.from("profiles")
                .select {
                    filter { isIn("id", ownerIds) }
                }
                .decodeList<Map<String, String>>()

            val ownerMap = profiles.associateBy({ it["id"] }, { it })

            recipes.map { recipe ->
                val profile = ownerMap[recipe.ownerId]
                if (profile != null) {
                    val firstName = profile["first_name"] ?: ""
                    val lastName = profile["last_name"] ?: ""
                    val fullName = "$firstName $lastName".trim()
                    
                    recipe.copy(
                        ownerName = fullName.ifBlank { profile["email"]?.substringBefore("@") } ?: recipe.ownerName
                    )
                } else {
                    recipe
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            recipes
        }
    }

    suspend fun fetchByCategory(category: String): List<Recipe> {
        val recipes = client.postgrest.from("recipes")
            .select {
                filter {
                    isIn("status", listOf("approved", "done"))
                    eq("category", category)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Recipe>()
            
        return enrichRecipesWithProfiles(recipes)
    }
}
