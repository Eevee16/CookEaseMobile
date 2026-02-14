package com.cookease.app.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * User data class for profile information
 * TODO: Move this to a separate file in data/models when API is ready
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImage: String = "",
    val recipesCreated: Int = 0,
    val recipesCooked: Int = 0,
    val followers: Int = 0,
    val following: Int = 0
)

/**
 * ProfileViewModel - Manages user profile and their recipes
 *
 * TODO: API Integration
 * 1. Inject UserRepository and RecipeRepository
 * 2. Implement user authentication
 * 3. Load user profile from API
 * 4. Load created recipes from API
 * 5. Load cooked recipes from API
 * 6. Implement edit profile functionality
 * 7. Implement logout
 * 8. Add follow/unfollow functionality
 */
class ProfileViewModel : ViewModel() {

    // ==================== LIVE DATA ====================
    private val _userProfile = MutableLiveData<Resource<User>>()
    val userProfile: LiveData<Resource<User>> = _userProfile

    private val _createdRecipes = MutableLiveData<Resource<List<Recipe>>>()
    val createdRecipes: LiveData<Resource<List<Recipe>>> = _createdRecipes

    private val _cookedRecipes = MutableLiveData<Resource<List<Recipe>>>()
    val cookedRecipes: LiveData<Resource<List<Recipe>>> = _cookedRecipes

    private val _logoutResult = MutableLiveData<Resource<Boolean>>()
    val logoutResult: LiveData<Resource<Boolean>> = _logoutResult

    // TODO: Inject repositories when ready
    // private val userRepository: UserRepository
    // private val recipeRepository: RecipeRepository

    // ==================== USER PROFILE ====================
    /**
     * Load user profile data
     * TODO: Replace with actual API call
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _userProfile.value = Resource.Loading()

                // TODO: Replace with actual API call
                // val response = userRepository.getUserProfile()
                // _userProfile.value = Resource.Success(response)

                // TEMPORARY: Mock user profile
                delay(500)
                val mockUser = User(
                    id = "user123",
                    name = "Juan Dela Cruz",
                    email = "juan@cookease.com",
                    profileImage = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
                    recipesCreated = 12,
                    recipesCooked = 45,
                    followers = 128,
                    following = 45
                )
                _userProfile.value = Resource.Success(mockUser)

            } catch (e: Exception) {
                _userProfile.value = Resource.Error("Failed to load profile: ${e.message}")
            }
        }
    }

    /**
     * Update user profile
     * TODO: Replace with actual API call
     */
    fun updateProfile(name: String, email: String, profileImage: String?) {
        viewModelScope.launch {
            try {
                _userProfile.value = Resource.Loading()

                // TODO: Replace with actual API call
                // val response = userRepository.updateProfile(name, email, profileImage)
                // _userProfile.value = Resource.Success(response)

                // Reload profile after update
                loadUserProfile()

            } catch (e: Exception) {
                _userProfile.value = Resource.Error("Failed to update profile: ${e.message}")
            }
        }
    }

    // ==================== CREATED RECIPES ====================
    /**
     * Load recipes created by the user
     * TODO: Replace with actual API call
     */
    fun loadCreatedRecipes() {
        viewModelScope.launch {
            try {
                _createdRecipes.value = Resource.Loading()

                // TODO: Replace with actual API call
                // val response = recipeRepository.getUserCreatedRecipes()
                // _createdRecipes.value = Resource.Success(response)

                // TEMPORARY: Mock created recipes
                delay(500)
                val mockCreated = getMockCreatedRecipes()
                _createdRecipes.value = Resource.Success(mockCreated)

            } catch (e: Exception) {
                _createdRecipes.value = Resource.Error("Failed to load created recipes: ${e.message}")
            }
        }
    }

    // ==================== COOKED RECIPES ====================
    /**
     * Load recipes the user has cooked
     * TODO: Replace with actual API call
     */
    fun loadCookedRecipes() {
        viewModelScope.launch {
            try {
                _cookedRecipes.value = Resource.Loading()

                // TODO: Replace with actual API call
                // val response = recipeRepository.getUserCookedRecipes()
                // _cookedRecipes.value = Resource.Success(response)

                // TEMPORARY: Mock cooked recipes
                delay(500)
                val mockCooked = getMockCookedRecipes()
                _cookedRecipes.value = Resource.Success(mockCooked)

            } catch (e: Exception) {
                _cookedRecipes.value = Resource.Error("Failed to load cooked recipes: ${e.message}")
            }
        }
    }

    /**
     * Mark a recipe as cooked by the user
     * TODO: Replace with actual API call
     */
    fun markRecipeAsCooked(recipeId: String) {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual API call
                // recipeRepository.markAsCooked(recipeId)

                // Reload cooked recipes
                loadCookedRecipes()

            } catch (e: Exception) {
                _cookedRecipes.value = Resource.Error("Failed to mark as cooked: ${e.message}")
            }
        }
    }

    // ==================== AUTHENTICATION ====================
    /**
     * Logout user
     * TODO: Replace with actual logout logic (clear tokens, etc.)
     */
    fun logout() {
        viewModelScope.launch {
            try {
                _logoutResult.value = Resource.Loading()

                // TODO: Clear authentication tokens
                // userRepository.clearAuthToken()

                // TODO: Clear local user data
                // userRepository.clearUserData()

                _logoutResult.value = Resource.Success(true)

            } catch (e: Exception) {
                _logoutResult.value = Resource.Error("Logout failed: ${e.message}")
            }
        }
    }

    // TODO: Add follow/unfollow functionality
    // fun followUser(userId: String) {
    //     viewModelScope.launch {
    //         try {
    //             userRepository.followUser(userId)
    //             loadUserProfile() // Refresh to update following count
    //         } catch (e: Exception) {
    //             // Handle error
    //         }
    //     }
    // }

    // TODO: Add recipe creation
    // fun createRecipe(recipe: Recipe) {
    //     viewModelScope.launch {
    //         try {
    //             recipeRepository.createRecipe(recipe)
    //             loadCreatedRecipes() // Refresh created recipes
    //         } catch (e: Exception) {
    //             // Handle error
    //         }
    //     }
    // }

    // TODO: Add recipe deletion
    // fun deleteRecipe(recipeId: String) {
    //     viewModelScope.launch {
    //         try {
    //             recipeRepository.deleteRecipe(recipeId)
    //             loadCreatedRecipes() // Refresh created recipes
    //         } catch (e: Exception) {
    //             // Handle error
    //         }
    //     }
    // }

    // ==================== MOCK DATA (DELETE WHEN API IS READY) ====================
    /**
     * TEMPORARY: Mock created recipes
     * DELETE THIS METHOD when API is integrated
     */
    private fun getMockCreatedRecipes(): List<Recipe> {
        return listOf(
            Recipe(
                id = "101",
                title = "My Special Adobo",
                description = "My family's secret adobo recipe passed down for generations",
                image = "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400",
                rating = 4.9f,
                difficulty = "Easy",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "102",
                title = "Grandma's Sinigang",
                description = "Traditional sinigang recipe from my lola",
                image = "https://images.unsplash.com/photo-1591644571062-d43b471916f5?w=400",
                rating = 4.8f,
                difficulty = "Medium",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "103",
                title = "Spicy Bicol Express",
                description = "Extra spicy version of the classic Bicol Express",
                image = "https://images.unsplash.com/photo-1617196038278-1d8e9c0ed48f?w=400",
                rating = 4.7f,
                difficulty = "Hard",
                cuisine = "Filipino"
            )
        )
    }

    /**
     * TEMPORARY: Mock cooked recipes
     * DELETE THIS METHOD when API is integrated
     */
    private fun getMockCookedRecipes(): List<Recipe> {
        return listOf(
            Recipe(
                id = "1",
                title = "Chicken Adobo",
                description = "Classic Filipino chicken braised in soy sauce and vinegar",
                image = "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400",
                rating = 4.8f,
                difficulty = "Easy",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "7",
                title = "Bicol Express",
                description = "Spicy pork stew cooked in coconut milk with chili peppers",
                image = "https://images.unsplash.com/photo-1617196038278-1d8e9c0ed48f?w=400",
                rating = 4.7f,
                difficulty = "Hard",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "10",
                title = "Kare-Kare",
                description = "Oxtail stew with peanut sauce served with vegetables and bagoong",
                image = "https://images.unsplash.com/photo-1607076786357-1c0f35265f62?w=400",
                rating = 4.6f,
                difficulty = "Hard",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "9",
                title = "Lumpiang Shanghai",
                description = "Filipino spring rolls filled with ground pork and vegetables",
                image = "https://images.unsplash.com/photo-1617196982693-b6d12fa1c4e1?w=400",
                rating = 4.8f,
                difficulty = "Easy",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "5",
                title = "Halo-Halo",
                description = "Refreshing Filipino shaved ice dessert with sweet beans and fruits",
                image = "https://images.unsplash.com/photo-1563805042-7684a24f32e8?w=400",
                rating = 4.4f,
                difficulty = "Easy",
                cuisine = "Filipino"
            )
        )
    }
}