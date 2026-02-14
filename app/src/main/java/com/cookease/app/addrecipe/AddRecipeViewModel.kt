package com.cookease.app.addrecipe

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddRecipeViewModel : ViewModel() {

    // --- STEP 1 DATA ---
    val recipeName = MutableLiveData<String>("")
    val cuisine = MutableLiveData<String>("") // e.g., "Filipino"
    val category = MutableLiveData<String>("") // e.g., "Breakfast"
    val difficulty = MutableLiveData<String>("") // e.g., "Medium"
    val description = MutableLiveData<String>("")

    // --- STEP 2 DATA ---
    val prepTime = MutableLiveData<String>("")
    val cookTime = MutableLiveData<String>("")
    val servings = MutableLiveData<String>("")
    val imageUri = MutableLiveData<String?>() // To store the photo path

    // --- STEP 3 DATA ---
    val ingredients = MutableLiveData<String>("")
    val instructions = MutableLiveData<String>("")

    // --- LOGIC ---
    fun submitRecipe() {
        //Supabase later
        val finalData = mapOf(
            "name" to recipeName.value,
            "cuisine" to cuisine.value,
            // ... add others
        )
        // TODO: Call Supabase Insert Here
    }
}