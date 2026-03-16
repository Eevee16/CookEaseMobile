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
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import io.github.jan.supabase.gotrue.SessionStatus

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

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    private var activeTab = "all"

    init {
        // Automatically fetch data when session becomes available
        viewModelScope.launch {
            SupabaseClientProvider.client.auth.sessionStatus.collectLatest { status ->
                if (status is SessionStatus.Authenticated) {
                    fetchUserData()
                }
            }
        }
    }

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

                // Fetch profile safely
                val profile = try {
                    client.postgrest.from("profiles")
                        .select {
                            filter { eq("id", currentUser.id) }
                        }.decodeSingleOrNull<Map<String, kotlinx.serialization.json.JsonElement>>()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }

                val meta = currentUser.userMetadata

                val firstName = profile?.get("first_name")?.jsonPrimitive?.content
                    ?: meta?.get("first_name")?.jsonPrimitive?.content ?: ""
                val lastName = profile?.get("last_name")?.jsonPrimitive?.content
                    ?: meta?.get("last_name")?.jsonPrimitive?.content ?: ""
                val customDisplayName = profile?.get("name")?.jsonPrimitive?.content
                
                val fullName = if (!customDisplayName.isNullOrBlank()) {
                    customDisplayName
                } else {
                    "$firstName $lastName".trim().ifBlank {
                        currentUser.email?.substringBefore("@") ?: "User"
                    }
                }
                
                val photoUrl = profile?.get("photo_url")?.jsonPrimitive?.content
                val role = profile?.get("role")?.jsonPrimitive?.content

                _role.value = role
                _user.value = User(
                    id = currentUser.id,
                    name = fullName,
                    firstName = firstName,
                    lastName = lastName,
                    email = currentUser.email ?: "",
                    photoUrl = photoUrl
                )

                val recipes = try {
                    client.postgrest.from("recipes")
                        .select {
                            filter { eq("owner_id", currentUser.id) }
                        }
                        .decodeList<Recipe>()
                } catch (e: Exception) {
                    emptyList()
                }

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

    fun updateProfile(firstName: String, lastName: String, displayName: String, imageBytes: ByteArray?, mimeType: String?) {
        viewModelScope.launch {
            try {
                val client = SupabaseClientProvider.client
                val userId = client.auth.currentUserOrNull()?.id ?: return@launch

                var photoUrl = _user.value?.photoUrl

                // 1. Upload photo if changed
                if (imageBytes != null && mimeType != null) {
                    val ext = mimeType.substringAfter("/")
                    val filePath = "profile-pictures/${userId}-${System.currentTimeMillis()}.$ext"
                    client.storage.from("profile-images").upload(filePath, imageBytes, upsert = true)
                    photoUrl = client.storage.from("profile-images").publicUrl(filePath)
                }

                // 2. Update profiles table
                client.postgrest.from("profiles").update(
                    buildJsonObject {
                        put("first_name", firstName)
                        put("last_name", lastName)
                        put("name", displayName)
                        put("photo_url", photoUrl)
                    }
                ) {
                    filter { eq("id", userId) }
                }

                // 3. Update local state
                val finalName = if (displayName.isNotBlank()) displayName else "$firstName $lastName".trim()
                _user.value = _user.value?.copy(
                    name = finalName.ifBlank { _user.value?.name ?: "User" },
                    firstName = firstName,
                    lastName = lastName,
                    photoUrl = photoUrl
                )
                _updateResult.value = Result.success(Unit)

            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
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
