package com.cookease.app.addrecipe

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import com.cookease.app.SupabaseClientProvider
import com.cookease.app.data.local.AppDatabase
import com.cookease.app.saved.toEntity
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.*

class AddRecipeViewModel(application: Application) : AndroidViewModel(application) {

    var isEditing = false
    var existingRecipeId: String? = null
    private var originalCreatedAt: String? = null

    // LiveData for UI binding
    val recipeName = MutableLiveData("")
    val description = MutableLiveData("")
    val cuisine = MutableLiveData("")
    val category = MutableLiveData("")
    val difficulty = MutableLiveData("")
    val servings = MutableLiveData("1")
    val prepTime = MutableLiveData("")
    val cookTime = MutableLiveData("")
    val imageUri = MutableLiveData<String?>(null)

    private val _selectedIngredients = MutableLiveData<MutableList<SelectedIngredient>>(mutableListOf())
    val selectedIngredients: LiveData<MutableList<SelectedIngredient>> = _selectedIngredients

    private val _instructionSteps = MutableLiveData<MutableList<String>>(mutableListOf(""))
    val instructionSteps: LiveData<MutableList<String>> = _instructionSteps

    private val _allIngredients = MutableLiveData<List<IngredientItem>>(emptyList())
    val allIngredients: LiveData<List<IngredientItem>> = _allIngredients

    private val _submitState = MutableLiveData<SubmitState>(SubmitState.Idle)
    val submitState: LiveData<SubmitState> = _submitState

    private val db = AppDatabase.getInstance(application)

    fun setAllIngredients(ingredients: List<IngredientItem>) {
        _allIngredients.value = ingredients
    }

    fun isIngredientAdded(name: String): Boolean {
        return _selectedIngredients.value?.any { it.name.equals(name, ignoreCase = true) } ?: false
    }

    fun updateIngredient(index: Int, qty: String? = null, unit: String? = null, prep: String? = null) {
        val list = _selectedIngredients.value ?: return
        if (index in list.indices) {
            val current = list[index]
            list[index] = current.copy(
                qty = qty ?: current.qty,
                unit = unit ?: current.unit,
                prep = prep ?: current.prep
            )
            _selectedIngredients.value = list
        }
    }

    fun removeInstructionStep(index: Int) {
        val list = _instructionSteps.value ?: return
        if (index in list.indices) {
            list.removeAt(index)
            _instructionSteps.value = list
        }
    }

    fun resetState() {
        _submitState.value = SubmitState.Idle
    }

    private suspend fun uploadImage(uriString: String, recipeId: String): String? {
        val uri = Uri.parse(uriString)
        if (uri.scheme != "content") return uriString // Already a URL or non-content URI

        return try {
            val bytes = getApplication<Application>().contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null

            val fileName = "recipe_$recipeId.jpg"
            val filePath = "recipe_images/$fileName"

            val client = SupabaseClientProvider.client
            client.storage.from("recipes").upload(filePath, bytes, upsert = true)
            client.storage.from("recipes").publicUrl(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun submitRecipe() {
        val currentUser = SupabaseClientProvider.client.auth.currentUserOrNull()
        val userId = currentUser?.id

        if (userId == null) {
            _submitState.value = SubmitState.Error("You must be logged in to add a recipe")
            return
        }

        viewModelScope.launch {
            try {
                _submitState.value = SubmitState.Loading

                val recipeId = existingRecipeId ?: UUID.randomUUID().toString()

                // Upload image if it's a local URI
                val finalImageUrl = imageUri.value?.let { uploadImage(it, recipeId) } ?: imageUri.value

                // Fetch current user's display name or email for 'owner_name'
                val ownerName = try {
                    val profile = SupabaseClientProvider.client.postgrest.from("profiles")
                        .select { filter { eq("id", userId) } }
                        .decodeSingleOrNull<Map<String, String>>()

                    val firstName = profile?.get("first_name") ?: currentUser.userMetadata?.get("first_name")?.jsonPrimitive?.content ?: ""
                    val lastName = profile?.get("last_name") ?: currentUser.userMetadata?.get("last_name")?.jsonPrimitive?.content ?: ""
                    val fullName = "$firstName $lastName".trim()

                    fullName.ifBlank { currentUser.email?.substringBefore("@") ?: "Unknown" }
                } catch (e: Exception) {
                    currentUser.email?.substringBefore("@") ?: "Unknown"
                }

                // 1. Prepare formatted strings
                val ings = _selectedIngredients.value?.map {
                    if (it.raw.isNotBlank()) it.raw
                    else "${it.qty} ${it.unit} ${it.name}${if (it.prep.isNotBlank()) " (${it.prep})" else ""}".trim()
                } ?: emptyList()

                val insts = _instructionSteps.value?.filter { it.isNotBlank() } ?: emptyList()

                // 2. Error-proof Timestamp
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val currentTime = sdf.format(Date())

                // 3. Create Recipe Object
                val recipe = Recipe(
                    id = recipeId,
                    title = recipeName.value?.trim() ?: "Untitled Recipe",
                    description = description.value?.trim(),
                    category = category.value,
                    cuisine = cuisine.value,
                    difficulty = difficulty.value,
                    servings = servings.value?.toIntOrNull() ?: 1,
                    prepTime = prepTime.value?.toIntOrNull() ?: 0,
                    cookTime = cookTime.value?.toIntOrNull() ?: 0,
                    imageUrl = finalImageUrl,
                    rating = 0.0,
                    status = "pending",
                    ownerId = userId,
                    ownerName = ownerName,
                    createdAt = originalCreatedAt ?: currentTime,
                    ingredients = ings,
                    instructions = insts,
                    viewCount = 0
                )

                // 4. Upsert to Supabase
                if (isEditing) {
                    SupabaseClientProvider.client.postgrest["recipes"].update(recipe) {
                        filter { eq("id", recipe.id) }
                    }
                } else {
                    SupabaseClientProvider.client.postgrest["recipes"].insert(recipe)
                }

                // 5. Save locally for offline access
                db.savedRecipeDao().insertRecipe(recipe.toEntity())

                _submitState.value = SubmitState.Success(recipe.id)
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    // Helper functions for UI
    fun addIngredient(ingredient: SelectedIngredient) {
        val list = _selectedIngredients.value ?: mutableListOf()
        list.add(ingredient)
        _selectedIngredients.value = list
    }

    fun removeIngredient(index: Int) {
        val list = _selectedIngredients.value ?: return
        if (index in list.indices) {
            list.removeAt(index)
            _selectedIngredients.value = list
        }
    }

    fun addInstructionStep() {
        val list = _instructionSteps.value ?: mutableListOf()
        list.add("")
        _instructionSteps.value = list
    }

    fun updateInstructionStep(index: Int, text: String) {
        val list = _instructionSteps.value ?: return
        if (index in list.indices) {
            list[index] = text
            _instructionSteps.value = list
        }
    }

    fun resetAllFields() {
        isEditing = false
        existingRecipeId = null
        recipeName.value = ""
        description.value = ""
        imageUri.value = null
        _selectedIngredients.value = mutableListOf()
        _instructionSteps.value = mutableListOf("")
        _submitState.value = SubmitState.Idle
    }

    companion object {
        val UNITS = listOf("pcs", "cups", "tbsp", "tsp", "g", "kg", "ml", "L", "oz", "lb", "to taste")
        val PREP_SUGGESTIONS = listOf("minced", "chopped", "sliced", "diced", "peeled", "grated")

        val CUISINE_OPTIONS = listOf(
            "Filipino", "Chinese", "Japanese", "Korean", "Thai", "Vietnamese",
            "Indian", "Italian", "French", "American", "Mexican", "Spanish",
            "Greek", "Middle Eastern", "African", "Fusion"
        )

        val CATEGORY_OPTIONS = listOf(
            "Breakfast", "Lunch", "Dinner", "Dessert", "Snacks",
            "Appetizer", "Soup", "Salad", "Beverage", "Side Dish"
        )
    }
}