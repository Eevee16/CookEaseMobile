package com.cookease.app

data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val rating: Float = 0f,
    val difficulty: String = "Medium",
    val cuisine: String = "Filipino"
)
