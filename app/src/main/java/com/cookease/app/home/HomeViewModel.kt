package com.cookease.app.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * HomeViewModel - Manages data for HomeFragment
 *
 * TODO: API Integration
 * 1. Inject RecipeRepository through constructor
 * 2. Replace mock data with actual API calls
 * 3. Add error handling from API
 * 4. Implement pagination
 * 5. Add pull-to-refresh logic
 */
class HomeViewModel : ViewModel() {

    // ==================== LIVE DATA ====================
    private val _recipes = MutableLiveData<Resource<List<Recipe>>>()
    val recipes: LiveData<Resource<List<Recipe>>> = _recipes

    private val _searchResults = MutableLiveData<Resource<List<Recipe>>>()
    val searchResults: LiveData<Resource<List<Recipe>>> = _searchResults

    // TODO: Inject repository when ready
    // private val repository: RecipeRepository = RecipeRepository()

    // ==================== DATA LOADING ====================
    /**
     * Fetch all recipes from API
     * TODO: Replace with actual API call
     */
    fun fetchRecipes() {
        viewModelScope.launch {
            try {
                _recipes.value = Resource.Loading()

                // TODO: Replace with actual API call
                // val response = repository.getAllRecipes()
                // _recipes.value = Resource.Success(response)

                // TEMPORARY: Mock API delay
                delay(1000)
                val mockRecipes = getMockRecipes()
                _recipes.value = Resource.Success(mockRecipes)

            } catch (e: Exception) {
                _recipes.value = Resource.Error("Failed to load recipes: ${e.message}")
            }
        }
    }

    /**
     * Search recipes by query
     * TODO: Replace with actual API search endpoint
     */
    fun searchRecipes(query: String) {
        viewModelScope.launch {
            try {
                _searchResults.value = Resource.Loading()

                // TODO: Replace with actual API call
                // val response = repository.searchRecipes(query)
                // _searchResults.value = Resource.Success(response)

                // TEMPORARY: Mock search with delay
                delay(500)
                val allRecipes = getMockRecipes()
                val filtered = allRecipes.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true) ||
                            it.cuisine.contains(query, ignoreCase = true)
                }
                _searchResults.value = Resource.Success(filtered)

            } catch (e: Exception) {
                _searchResults.value = Resource.Error("Search failed: ${e.message}")
            }
        }
    }

    /**
     * Refresh recipes (for pull-to-refresh)
     */
    fun refreshRecipes() {
        fetchRecipes()
    }

    // TODO: Add pagination
    // fun loadMoreRecipes(page: Int) {
    //     viewModelScope.launch {
    //         try {
    //             val response = repository.getRecipes(page)
    //             // Append to existing list
    //         } catch (e: Exception) {
    //             // Handle error
    //         }
    //     }
    // }

    // ==================== MOCK DATA (DELETE WHEN API IS READY) ====================
    /**
     * TEMPORARY: Mock recipe data
     * DELETE THIS METHOD when API is integrated
     */
    private fun getMockRecipes(): List<Recipe> {
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
                id = "2",
                title = "Sinigang na Baboy",
                description = "Sour tamarind soup with pork and vegetables",
                image = "https://images.unsplash.com/photo-1591644571062-d43b471916f5?w=400",
                rating = 4.6f,
                difficulty = "Medium",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "3",
                title = "Lechon Kawali",
                description = "Crispy pan-fried pork belly with dipping sauce",
                image = "https://images.unsplash.com/photo-1571091655789-9d57f1f689fc?w=400",
                rating = 4.7f,
                difficulty = "Medium",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "4",
                title = "Pancit Canton",
                description = "Stir-fried egg noodles with veggies and protein",
                image = "https://images.unsplash.com/photo-1511689985-952d1c90e4a8?w=400",
                rating = 4.5f,
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
            ),
            Recipe(
                id = "6",
                title = "Pininyahang Manok",
                description = "Sweet and savory pineapple chicken stew",
                image = "https://images.unsplash.com/photo-1548946526-fc7a2a1e8b3a?w=400",
                rating = 4.6f,
                difficulty = "Medium",
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
                id = "8",
                title = "Laing",
                description = "Taro leaves cooked in coconut milk with chili and shrimp paste",
                image = "https://images.unsplash.com/photo-1619478252306-8b4f13f45e72?w=400",
                rating = 4.5f,
                difficulty = "Medium",
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
                id = "10",
                title = "Kare-Kare",
                description = "Oxtail stew with peanut sauce served with vegetables and bagoong",
                image = "https://images.unsplash.com/photo-1607076786357-1c0f35265f62?w=400",
                rating = 4.6f,
                difficulty = "Hard",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "11",
                title = "Arroz Caldo",
                description = "Filipino rice porridge with chicken, ginger, and garlic",
                image = "https://images.unsplash.com/photo-1623958056082-cd90b0c43c03?w=400",
                rating = 4.5f,
                difficulty = "Easy",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "12",
                title = "Beef Mechado",
                description = "Filipino beef stew with tomato sauce and vegetables",
                image = "https://images.unsplash.com/photo-1586201375761-83865001c2b9?w=400",
                rating = 4.6f,
                difficulty = "Medium",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "13",
                title = "Chicken Inasal",
                description = "Grilled marinated chicken from Bacolod",
                image = "https://images.unsplash.com/photo-1617196038278-1d8e9c0ed48f?w=400",
                rating = 4.7f,
                difficulty = "Medium",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "14",
                title = "Taho",
                description = "Sweet soft tofu dessert with syrup and tapioca pearls",
                image = "https://images.unsplash.com/photo-1606256598592-3cb1d5b2ed3d?w=400",
                rating = 4.4f,
                difficulty = "Easy",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "15",
                title = "Bibingka",
                description = "Filipino rice cake topped with salted egg and cheese",
                image = "https://images.unsplash.com/photo-1617196982693-b6d12fa1c4e1?w=400",
                rating = 4.5f,
                difficulty = "Easy",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "16",
                title = "Puto",
                description = "Steamed Filipino rice cakes often served with cheese or salted egg",
                image = "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400",
                rating = 4.4f,
                difficulty = "Easy",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "17",
                title = "Crispy Pata",
                description = "Deep-fried pork leg served with soy-vinegar dipping sauce",
                image = "https://images.unsplash.com/photo-1617196038278-1d8e9c0ed48f?w=400",
                rating = 4.7f,
                difficulty = "Hard",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "18",
                title = "Buko Pandan",
                description = "Refreshing dessert with young coconut, pandan jelly, and cream",
                image = "https://images.unsplash.com/photo-1619478252306-8b4f13f45e72?w=400",
                rating = 4.6f,
                difficulty = "Easy",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "19",
                title = "Dinuguan",
                description = "Savory Filipino pork blood stew with vinegar and spices",
                image = "https://images.unsplash.com/photo-1598866535622-1a78bfad0fa1?w=400",
                rating = 4.5f,
                difficulty = "Medium",
                cuisine = "Filipino"
            ),
            Recipe(
                id = "20",
                title = "Menudo",
                description = "Filipino pork and liver stew with tomato sauce and vegetables",
                image = "https://images.unsplash.com/photo-1586201375761-83865001c2b9?w=400",
                rating = 4.6f,
                difficulty = "Medium",
                cuisine = "Filipino"
            )
        )
    }
}