package com.cookease.app.addrecipe

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.util.UUID

@Serializable
data class IngredientItem(
    val id: String,
    val name: String,
    val image_url: String = ""
)

data class SelectedIngredient(
    val id: String,
    val name: String,
    val image_url: String = "",
    var qty: String = "",
    var unit: String = "pcs",
    var prep: String = ""
) {
    fun toDisplayString(): String {
        val parts = listOf(
            qty,
            if (unit != "pcs" || qty.isNotEmpty()) unit else "",
            name,
            if (prep.isNotEmpty()) "($prep)" else ""
        ).filter { it.isNotEmpty() }
        return parts.joinToString(" ").trim()
    }
}

class AddRecipeViewModel : ViewModel() {

    companion object {
        val CUISINE_OPTIONS = listOf(
            "Filipino", "Chinese", "Japanese", "Korean", "Thai",
            "Vietnamese", "Indian", "Italian", "French", "American",
            "Mexican", "Spanish", "Greek", "Middle Eastern", "African", "Fusion"
        )
        val CATEGORY_OPTIONS = listOf(
            "Breakfast", "Lunch", "Dinner", "Dessert", "Snacks",
            "Appetizer", "Soup", "Salad", "Beverage", "Side Dish"
        )
        val DIFFICULTY_OPTIONS = listOf("Easy", "Medium", "Hard")
        val UNITS = listOf(
            "pcs", "cups", "tbsp", "tsp", "g", "kg", "ml", "L",
            "oz", "lb", "cloves", "slices", "strips", "bunches",
            "stalks", "pinch", "handful", "to taste"
        )
        val PREP_SUGGESTIONS = listOf(
            "minced", "chopped", "sliced", "diced", "thinly sliced",
            "roughly chopped", "finely chopped", "beaten", "room temperature",
            "melted", "softened", "peeled", "grated", "julienned",
            "halved", "quartered", "crushed"
        )
    }

    // Step 1
    val recipeName = MutableLiveData("")
    val cuisine = MutableLiveData("")
    val category = MutableLiveData("")
    val difficulty = MutableLiveData("")
    val description = MutableLiveData("")
    val servings = MutableLiveData("1")

    // Step 2
    val prepTime = MutableLiveData("")
    val cookTime = MutableLiveData("")
    val imageUri = MutableLiveData<String?>()

    // Step 3
    val selectedIngredients = MutableLiveData<MutableList<SelectedIngredient>>(mutableListOf())
    val instructionSteps = MutableLiveData<MutableList<String>>(mutableListOf(""))
    val allIngredients = MutableLiveData<List<IngredientItem>>(emptyList())
    val ingredientsLoading = MutableLiveData(false)

    val submitState = MutableLiveData<SubmitState>(SubmitState.Idle)

    // ── Ingredient helpers ────────────────────────────────────────────

    fun setAllIngredients(list: List<IngredientItem>) {
        allIngredients.value = list
    }

    fun isIngredientAdded(name: String): Boolean =
        selectedIngredients.value?.any { it.name.lowercase() == name.lowercase() } == true

    fun addIngredient(ingredient: SelectedIngredient) {
        val current = selectedIngredients.value ?: mutableListOf()
        if (!isIngredientAdded(ingredient.name)) {
            current.add(ingredient)
            selectedIngredients.value = current
        }
    }

    fun removeIngredient(index: Int) {
        val current = selectedIngredients.value ?: return
        if (index in current.indices) {
            current.removeAt(index)
            selectedIngredients.value = current
        }
    }

    fun updateIngredient(index: Int, qty: String? = null, unit: String? = null, prep: String? = null) {
        val current = selectedIngredients.value ?: return
        if (index in current.indices) {
            qty?.let { current[index].qty = it }
            unit?.let { current[index].unit = it }
            prep?.let { current[index].prep = it }
            selectedIngredients.value = current
        }
    }

    // ── Instruction helpers ───────────────────────────────────────────

    fun addInstructionStep() {
        val current = instructionSteps.value ?: mutableListOf()
        current.add("")
        instructionSteps.value = current
    }

    fun removeInstructionStep(index: Int) {
        val current = instructionSteps.value ?: return
        if (current.size > 1 && index in current.indices) {
            current.removeAt(index)
            instructionSteps.value = current
        }
    }

    fun updateInstructionStep(index: Int, text: String) {
        val current = instructionSteps.value ?: return
        if (index in current.indices) {
            current[index] = text
            instructionSteps.value = current
        }
    }

    // ── Validation ────────────────────────────────────────────────────

    fun validateStep1(): Boolean {
        val name = recipeName.value?.trim() ?: ""
        return name.length >= 3 &&
                category.value?.isNotBlank() == true &&
                cuisine.value?.isNotBlank() == true &&
                difficulty.value?.isNotBlank() == true
    }

    fun validateStep2(): Boolean =
        prepTime.value?.isNotBlank() == true &&
                cookTime.value?.isNotBlank() == true &&
                servings.value?.isNotBlank() == true

    fun validateStep3(): Boolean =
        selectedIngredients.value?.isNotEmpty() == true &&
                instructionSteps.value?.any { it.trim().isNotEmpty() } == true

    // ── Submit ────────────────────────────────────────────────────────

    fun submitRecipe() {
        if (!validateStep1() || !validateStep2() || !validateStep3()) {
            submitState.value = SubmitState.Error("Please complete all steps before submitting.")
            return
        }
        submitState.value = SubmitState.Loading
        viewModelScope.launch {
            try {
                val client = SupabaseClientProvider.client
                val user = client.auth.currentUserOrNull()
                    ?: run {
                        submitState.value = SubmitState.Error("You must be logged in to submit a recipe.")
                        return@launch
                    }

                val ingredientStrings = selectedIngredients.value?.map { it.toDisplayString() } ?: emptyList()
                val instructionList = instructionSteps.value?.filter { it.trim().isNotEmpty() } ?: emptyList()
                val slug = slugify(recipeName.value ?: "") + "-" + UUID.randomUUID().toString().take(6)

                client.postgrest.from("recipes").insert(
                    buildJsonObject {
                        put("title", recipeName.value)
                        put("slug", slug)
                        put("cuisine", cuisine.value)
                        put("category", category.value)
                        put("difficulty", difficulty.value)
                        put("description", description.value)
                        put("prepTime", prepTime.value?.toIntOrNull() ?: 0)
                        put("cookTime", cookTime.value?.toIntOrNull() ?: 0)
                        put("servings", servings.value?.toIntOrNull() ?: 1)
                        put("image_url", imageUri.value ?: "")
                        put("owner_id", user.id)
                        put("owner_name", user.email ?: "")
                        put("rating", 0)
                        put("status", "pending")
                        putJsonArray("ingredients") { ingredientStrings.forEach { add(it) } }
                        putJsonArray("instructions") { instructionList.forEach { add(it) } }
                    }
                )
                submitState.value = SubmitState.Success
            } catch (e: Exception) {
                submitState.value = SubmitState.Error(e.message ?: "Failed to submit recipe.")
            }
        }
    }

    private fun slugify(text: String): String =
        text.lowercase().trim()
            .replace(Regex("[^\\w\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .replace(Regex("-+"), "-")

    fun resetState() { submitState.value = SubmitState.Idle }

    fun resetAllFields() {
        recipeName.value = ""
        cuisine.value = ""
        category.value = ""
        difficulty.value = ""
        description.value = ""
        servings.value = "1"
        prepTime.value = ""
        cookTime.value = ""
        imageUri.value = null
        selectedIngredients.value = mutableListOf()
        instructionSteps.value = mutableListOf("")
        submitState.value = SubmitState.Idle
    }
}

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    object Success : SubmitState()
    data class Error(val message: String) : SubmitState()
}