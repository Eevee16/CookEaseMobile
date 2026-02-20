package com.cookease.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.SupabaseClientProvider
import com.cookease.app.User
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

class ProfileViewModel : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    private val _userRecipes = MutableLiveData<List<Recipe>>()
    val userRecipes: LiveData<List<Recipe>> get() = _userRecipes

    private val _recipeStats = MutableLiveData<RecipeStats>()
    val recipeStats: LiveData<RecipeStats> get() = _recipeStats

    private val _logoutResult = MutableLiveData<Boolean>()
    val logoutResult: LiveData<Boolean> = _logoutResult

    fun logout() {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signOut()
                _logoutResult.value = true
            } catch (e: Exception) {
                _logoutResult.value = false
            }
        }
    }

    fun fetchUserData() {
        viewModelScope.launch {
            try {
                val client = SupabaseClientProvider.client
                val currentUser = client.auth.currentUserOrNull() ?: return@launch

                // Get name from metadata
                val meta = currentUser.userMetadata
                val firstName = meta?.get("first_name")?.jsonPrimitive?.content ?: ""
                val lastName = meta?.get("last_name")?.jsonPrimitive?.content ?: ""
                val fullName = "$firstName $lastName".trim().ifBlank { "Anonymous" }

                // Get profile photo from profiles table
                val profiles = client.postgrest.from("profiles")
                    .select { filter { eq("id", currentUser.id) } }
                    .decodeList<Map<String, String>>()
                val photoUrl = profiles.firstOrNull()?.get("photo_url")

                _user.value = User(
                    name = fullName,
                    email = currentUser.email ?: "",
                    photoUrl = photoUrl
                )

                // Fetch user's recipes
                val recipes = client.postgrest.from("recipes")
                    .select { filter { eq("user_id", currentUser.id) } }
                    .decodeList<Recipe>()

                _userRecipes.value = recipes
                _recipeStats.value = RecipeStats(
                    total = recipes.size,
                    approved = recipes.count { it.status == "approved" },
                    pending = recipes.count { it.status == "pending" },
                    rejected = recipes.count { it.status == "rejected" }
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class RecipeStats(val total: Int, val approved: Int, val pending: Int, val rejected: Int)