package com.cookease.app.home

import com.cookease.app.Recipe
import com.cookease.app.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class HomeRepository {

    private val client = SupabaseClientProvider.client

    suspend fun fetchApprovedRecipes(): List<Recipe> {
        return client.postgrest.from("recipes")
            .select {
                filter { isIn("status", listOf("approved", "done")) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Recipe>()
    }

    suspend fun searchRecipes(query: String): List<Recipe> {
        return client.postgrest.from("recipes")
            .select {
                filter {
                    isIn("status", listOf("approved", "done"))
                    ilike("title", "%$query%")
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Recipe>()
    }

    suspend fun fetchByCategory(category: String): List<Recipe> {
        return client.postgrest.from("recipes")
            .select {
                filter {
                    isIn("status", listOf("approved", "done"))
                    eq("category", category)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Recipe>()
    }
}