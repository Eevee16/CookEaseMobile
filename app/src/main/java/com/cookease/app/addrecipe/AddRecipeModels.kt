package com.cookease.app.addrecipe

import kotlinx.serialization.Serializable

@Serializable
data class IngredientItem(
    val id: Int = 0,
    val name: String = "",
    val category: String = "",
    val image_url: String = ""
)

data class SelectedIngredient(
    val id: Int,
    val name: String,
    val image_url: String,
    val qty: String = "",
    val unit: String = "pcs",
    val prep: String = ""
)

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    data class Success(val recipeId: String) : SubmitState()
    data class Error(val message: String) : SubmitState()
}
