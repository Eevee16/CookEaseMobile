package com.cookease.app.addrecipe

import kotlinx.serialization.Serializable

@Serializable
data class IngredientItem(
    val id: String = "", // Changed to String to handle UUIDs or numeric IDs safely
    val name: String = "",
    val category: String? = null,
    val image_url: String? = null
)

data class SelectedIngredient(
    val id: String,
    val name: String,
    val image_url: String?,
    val qty: String = "",
    val unit: String = "pcs",
    val prep: String = "",
    val raw: String = ""
)

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    data class Success(val recipeId: String) : SubmitState()
    data class Error(val message: String) : SubmitState()
}
