package com.cookease.app

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImage: String = "",
    val recipesCreated: Int = 0,
    val recipesCooked: Int = 0,
    val followers: Int = 0,
    val following: Int = 0
)