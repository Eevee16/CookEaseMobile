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
                    val customName = profile["name"]
                    
                    val firstLast = "$firstName $lastName".trim()
                    val fullName = if (!customName.isNullOrBlank()) customName else firstLast
                    
                    recipe.copy(
                        ownerName = if (fullName.isNotBlank()) fullName else (profile["email"]?.substringBefore("@") ?: "Chef"),
                        ownerPhotoUrl = profile["photo_url"]
                    )
                } else {
                    // Fallback: handle cases where owner_name might be an email
                    val cleanedName = if (recipe.ownerName?.contains("@") == true) "Chef" else recipe.ownerName
                    recipe.copy(ownerName = cleanedName ?: "Chef")
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
