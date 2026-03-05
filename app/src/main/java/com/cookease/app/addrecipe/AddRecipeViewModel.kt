package com.cookease.app.addrecipe

import android.app.Application
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
import kotlinx.coroutines.launch
import java.util.*

class AddRecipeViewModel(application: Application) : AndroidViewModel(application) {

    // Step 1
    val recipeName = MutableLiveData<String>("")
    val description = MutableLiveData<String>("")
    val cuisine = MutableLiveData<String>("")
    val category = MutableLiveData<String>("")
    val difficulty = MutableLiveData<String>("")
    val servings = MutableLiveData<String>("1") // ✅ synced: default "1" (was "")

    // Step 2
    val prepTime = MutableLiveData<String>("")
    val cookTime = MutableLiveData<String>("")
    val imageUri = MutableLiveData<String?>(null)

    // Step 3
    private val _allIngredients = MutableLiveData<List<IngredientItem>>(emptyList())
    val allIngredients: LiveData<List<IngredientItem>> = _allIngredients

    private val _selectedIngredients = MutableLiveData<MutableList<SelectedIngredient>>(mutableListOf())
    val selectedIngredients: LiveData<MutableList<SelectedIngredient>> = _selectedIngredients

    private val _instructionSteps = MutableLiveData<MutableList<String>>(mutableListOf(""))
    val instructionSteps: LiveData<MutableList<String>> = _instructionSteps

    private val _submitState = MutableLiveData<SubmitState>(SubmitState.Idle)
    val submitState: LiveData<SubmitState> = _submitState

    private val db = AppDatabase.getInstance(application)

    fun setAllIngredients(list: List<IngredientItem>) {
        _allIngredients.value = list
    }

    fun isIngredientAdded(name: String): Boolean {
        return _selectedIngredients.value?.any { it.name.equals(name, ignoreCase = true) } ?: false
    }

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

    fun updateIngredient(index: Int, qty: String? = null, unit: String? = null, prep: String? = null) {
        val list = _selectedIngredients.value ?: return
        if (index in list.indices) {
            val item = list[index]
            list[index] = item.copy(
                qty = qty ?: item.qty,
                unit = unit ?: item.unit,
                prep = prep ?: item.prep
            )
            _selectedIngredients.value = list
        }
    }

    fun addInstructionStep() {
        val list = _instructionSteps.value ?: mutableListOf()
        list.add("")
        _instructionSteps.value = list
    }

    fun removeInstructionStep(index: Int) {
        val list = _instructionSteps.value ?: return
        if (index in list.indices && list.size > 1) {
            list.removeAt(index)
            _instructionSteps.value = list
        }
    }

    fun updateInstructionStep(index: Int, text: String) {
        val list = _instructionSteps.value ?: return
        if (index in list.indices) {
            list[index] = text
            _instructionSteps.value = list
        }
    }

    fun submitRecipe() {
        viewModelScope.launch {
            try {
                _submitState.value = SubmitState.Loading

                val name = recipeName.value?.trim() ?: ""
                val desc = description.value?.trim() ?: ""
                val cuis = cuisine.value ?: ""
                val cat = category.value ?: ""
                val diff = difficulty.value ?: ""
                val serv = servings.value?.toIntOrNull() ?: 0
                val prep = prepTime.value?.toIntOrNull() ?: 0
                val cook = cookTime.value?.toIntOrNull() ?: 0

                val ings = _selectedIngredients.value?.map {
                    "${it.qty} ${it.unit} ${it.name}${if (it.prep.isNotBlank()) " (${it.prep})" else ""}"
                } ?: emptyList()

                val insts = _instructionSteps.value?.filter { it.isNotBlank() } ?: emptyList()

                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id

                val recipe = Recipe(
                    id = UUID.randomUUID().toString(),
                    title = name,
                    description = desc,
                    category = cat,
                    cuisine = cuis,
                    difficulty = diff,
                    servings = serv,
                    prepTime = prep,
                    cookTime = cook,
                    imageUrl = imageUri.value,
                    rating = 0.0,
                    status = "pending",
                    ownerId = userId,
                    createdAt = System.currentTimeMillis().toString(),
                    ingredients = ings,
                    instructions = insts
                )

                SupabaseClientProvider.client.postgrest["recipes"].insert(recipe)
                db.savedRecipeDao().insertRecipe(recipe.toEntity())

                _submitState.value = SubmitState.Success(recipe.id)
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "Failed to save recipe")
            }
        }
    }

    fun resetState() {
        _submitState.value = SubmitState.Idle
    }

    fun resetAllFields() {
        recipeName.value = ""
        description.value = ""
        cuisine.value = ""
        category.value = ""
        difficulty.value = ""
        servings.value = "1" // ✅ synced: reset to "1"
        prepTime.value = ""
        cookTime.value = ""
        imageUri.value = null
        _selectedIngredients.value = mutableListOf()
        _instructionSteps.value = mutableListOf("")
        _submitState.value = SubmitState.Idle
    }

    companion object {
        // ✅ synced with web: full 18-item units list
        val UNITS = listOf(
            "pcs", "cups", "tbsp", "tsp", "g", "kg", "ml", "L",
            "oz", "lb", "cloves", "slices", "strips", "bunches",
            "stalks", "pinch", "handful", "to taste"
        )

        // ✅ synced with web: 10 categories, "Snacks" (not "Snack"), added Soup/Salad/Side Dish
        val CATEGORY_OPTIONS = listOf(
            "Breakfast", "Lunch", "Dinner", "Dessert", "Snacks",
            "Appetizer", "Soup", "Salad", "Beverage", "Side Dish"
        )

        // ✅ synced with web: 16 cuisines, added Filipino/Vietnamese/Spanish/Greek/Middle Eastern/African/Fusion
        val CUISINE_OPTIONS = listOf(
            "Filipino", "Chinese", "Japanese", "Korean", "Thai",
            "Vietnamese", "Indian", "Italian", "French", "American",
            "Mexican", "Spanish", "Greek", "Middle Eastern", "African", "Fusion"
        )

        // ✅ synced with web: full 17-item prep suggestions
        val PREP_SUGGESTIONS = listOf(
            "minced", "chopped", "sliced", "diced", "thinly sliced",
            "roughly chopped", "finely chopped", "beaten", "room temperature",
            "melted", "softened", "peeled", "grated", "julienned",
            "halved", "quartered", "crushed"
        )
    }
}