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

    private val _allRecipes = MutableLiveData<List<Recipe>>(emptyList())

    private val _userRecipes = MutableLiveData<List<Recipe>>()
    val userRecipes: LiveData<List<Recipe>> get() = _userRecipes

    private val _recipeStats = MutableLiveData<RecipeStats>()
    val recipeStats: LiveData<RecipeStats> get() = _recipeStats

    private val _logoutResult = MutableLiveData<Boolean>()
    val logoutResult: LiveData<Boolean> = _logoutResult

    private val _role = MutableLiveData<String?>(null)
    val role: LiveData<String?> = _role

    private var activeTab = "all"

    // ── Logout ────────────────────────────────────────────────────────

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

    // ── Fetch ─────────────────────────────────────────────────────────

    fun fetchUserData() {
        viewModelScope.launch {
            try {
                val client = SupabaseClientProvider.client
                val currentUser = client.auth.currentUserOrNull() ?: return@launch

                // Fetch profile
                val profile = try {
                    client.postgrest.from("profiles")
                        .select {
                            filter { eq("id", currentUser.id) }
                        }.decodeSingle<Map<String, String>>()
                } catch (e: Exception) {
                    null
                }

                val meta = currentUser.userMetadata

                val firstName = profile?.get("first_name")
                    ?: meta?.get("first_name")?.jsonPrimitive?.content ?: ""
                val lastName = profile?.get("last_name")
                    ?: meta?.get("last_name")?.jsonPrimitive?.content ?: ""
                val fullName = "$firstName $lastName".trim().ifBlank {
                    currentUser.email?.substringBefore("@") ?: "User"
                }
                val photoUrl = profile?.get("photo_url")
                val role = profile?.get("role")

                _role.value = role
                _user.value = User(
                    name = fullName,
                    email = currentUser.email ?: "",
                    photoUrl = photoUrl
                )

                // ── Fix: use owner_id not user_id ─────────────────────
                val recipes = client.postgrest.from("recipes")
                    .select {
                        filter { eq("owner_id", currentUser.id) }
                    }
                    .decodeList<Recipe>()

                _allRecipes.value = recipes
                _recipeStats.value = RecipeStats(
                    total = recipes.size,
                    approved = recipes.count { it.status == "approved" },
                    pending = recipes.count { it.status == "pending" || it.status.isNullOrEmpty() },
                    rejected = recipes.count { it.status == "rejected" }
                )

                applyTab()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ── Tab filtering ─────────────────────────────────────────────────

    fun setTab(tab: String) {
        activeTab = tab
        applyTab()
    }

    private fun applyTab() {
        val all = _allRecipes.value ?: return
        _userRecipes.value = when (activeTab) {
            "approved" -> all.filter { it.status == "approved" }
            "pending" -> all.filter { it.status == "pending" || it.status.isNullOrEmpty() }
            "rejected" -> all.filter { it.status == "rejected" }
            else -> all
        }
    }
}

data class RecipeStats(val total: Int, val approved: Int, val pending: Int, val rejected: Int)