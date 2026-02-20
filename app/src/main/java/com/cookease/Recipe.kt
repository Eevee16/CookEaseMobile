package com.cookease.app

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Recipe(
    val id: String = "",  // UUID
    val title: String = "",
    val description: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,  // image_url (text)

    val image: String? = null,  // Optional image

    val rating: Int? = null,  // Rating can be an int based on your table definition

    val difficulty: String? = "Medium",  // Default value set to "Medium"
    val cuisine: String? = null,
    val category: String? = null,

    @SerialName("views")  // The views column from the database
    val views: Int? = null,  // Integer views

    @SerialName("view_count")
    val viewCount: Int? = null,  // view_count (int4)

    val ingredients: String? = null,
    val instructions: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,  // created_at (timestamp)

    val status: String? = null,  // status (text)

    @SerialName("owner_name")
    val ownerName: String? = null,  // owner_name (text)

    @SerialName("owner_id")
    val ownerId: String? = null,  // owner_id (UUID)

    @SerialName("rejection_reason")
    val rejectionReason: String? = null,  // rejection_reason (text)

    val servings: Int? = null,  // servings (int2)

    @SerialName("prepTime")
    val prepTime: Int? = null,  // prepTime (int2)

    @SerialName("cookTime")
    val cookTime: Int? = null,  // cookTime (int2)

    val notes: String? = null,  // notes (text)

    @SerialName("slug")
    val slug: String = ""  // slug (text)
) : Parcelable
