package com.cookease.app

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

@Parcelize
@Serializable
data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val image: String? = null,
    val rating: Double? = 0.0,
    val difficulty: String? = "Medium",
    val cuisine: String? = null,
    val category: String? = null,
    val servings: Int? = 1,
    @SerialName("prepTime")
    val prepTime: Int? = 0,
    @SerialName("cookTime")
    val cookTime: Int? = 0,
    val notes: String? = null,
    val slug: String? = "",

    @Serializable(with = FlexibleStringListSerializer::class)
    val ingredients: List<String> = emptyList(),
    @Serializable(with = FlexibleStringListSerializer::class)
    val instructions: List<String> = emptyList(),

    @SerialName("views")
    val views: Int? = 0,
    @SerialName("view_count")
    val viewCount: Int? = 0,
    @SerialName("owner_id")
    val ownerId: String? = null,
    @SerialName("owner_name")
    val ownerName: String? = null,
    val status: String? = "pending",
    @SerialName("rejection_reason")
    val rejectionReason: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
) : Parcelable

object FlexibleStringListSerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FlexibleStringList")

    override fun deserialize(decoder: Decoder): List<String> {
        val input = (decoder as? JsonDecoder)?.decodeJsonElement() ?: return emptyList()
        
        return when (input) {
            is JsonArray -> {
                input.map { (it as? JsonPrimitive)?.content ?: it.toString() }
            }
            is JsonPrimitive -> {
                val content = input.content
                if (content.startsWith("[") && content.endsWith("]")) {
                    try {
                        Json.decodeFromString<List<String>>(content)
                    } catch (e: Exception) {
                        listOf(content)
                    }
                } else if (content.isBlank() || content == "null") {
                    emptyList()
                } else {
                    listOf(content)
                }
            }
            else -> emptyList()
        }
    }

    override fun serialize(encoder: Encoder, value: List<String>) {
        encoder.encodeSerializableValue(JsonArray.serializer(), JsonArray(value.map { JsonPrimitive(it) }))
    }
}
