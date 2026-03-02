package com.cookease.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String?,
    val cuisine: String?,
    val imageUrl: String?,
    val rating: Float,
    val status: String?,
    val createdAt: String?,
    val description: String?,
    val ingredients: String?,
    val instructions: String?,
    val savedAt: Long = System.currentTimeMillis()
)