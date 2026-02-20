package com.cookease.app.addrecipe

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AddRecipeViewModel : ViewModel() {

    val recipeName = MutableLiveData("")
    val cuisine = MutableLiveData("")
    val category = MutableLiveData("")
    val difficulty = MutableLiveData("")
    val description = MutableLiveData("")
    val prepTime = MutableLiveData("")
    val cookTime = MutableLiveData("")
    val servings = MutableLiveData("")
    val imageUri = MutableLiveData<String?>()
    val ingredients = MutableLiveData("")
    val instructions = MutableLiveData("")

    val submitState = MutableLiveData<SubmitState>(SubmitState.Idle)

    fun validateStep1(): Boolean {
        return recipeName.value?.isNotBlank() == true
    }

    fun validateStep2(): Boolean {
        return prepTime.value?.isNotBlank() == true &&
                cookTime.value?.isNotBlank() == true &&
                servings.value?.isNotBlank() == true
    }

    fun validateStep3(): Boolean {
        return ingredients.value?.isNotBlank() == true &&
                instructions.value?.isNotBlank() == true
    }

    fun submitRecipe() {
        if (!validateStep1() || !validateStep2() || !validateStep3()) {
            submitState.value = SubmitState.Error("Please complete all steps before submitting.")
            return
        }

        submitState.value = SubmitState.Loading

        viewModelScope.launch {
            try {
                val client = SupabaseClientProvider.client
                val userId = client.auth.currentUserOrNull()?.id
                    ?: run {
                        submitState.value = SubmitState.Error("You must be logged in to submit a recipe.")
                        return@launch
                    }

                client.postgrest.from("recipes").insert(
                    buildJsonObject {
                        put("title", recipeName.value)
                        put("cuisine", cuisine.value)
                        put("category", category.value)
                        put("difficulty", difficulty.value)
                        put("prepTime", prepTime.value?.toIntOrNull() ?: 0)
                        put("cookTime", cookTime.value?.toIntOrNull() ?: 0)
                        put("servings", servings.value?.toIntOrNull() ?: 1)
                        put("image_url", imageUri.value ?: "")
                        put("ingredients", ingredients.value)
                        put("instructions", instructions.value)
                        put("owner_id", userId)
                        put("status", "pending")
                    }
                )

                submitState.value = SubmitState.Success

            } catch (e: Exception) {
                submitState.value = SubmitState.Error(e.message ?: "Failed to submit recipe.")
            }
        }
    }

    fun resetState() {
        submitState.value = SubmitState.Idle
    }

    fun resetAllFields() {
        recipeName.value = ""
        cuisine.value = ""
        category.value = ""
        difficulty.value = ""
        description.value = ""
        prepTime.value = ""
        cookTime.value = ""
        servings.value = ""
        imageUri.value = null
        ingredients.value = ""
        instructions.value = ""
        submitState.value = SubmitState.Idle
    }
}

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    object Success : SubmitState()
    data class Error(val message: String) : SubmitState()
}