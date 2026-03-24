package com.cookease.app.ui_components.recipe

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.Recipe
import com.cookease.app.databinding.ItemRecipeCardBinding

class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit = { _ -> },
    private val onAuthorClick: ((String) -> Unit)? = null,
    private val onRemoveClick: ((Recipe) -> Unit)? = null,
    private val isSavedScreen: Boolean = false
) : ListAdapter<Recipe, RecipeAdapter.VH>(DiffCallback()) {

    inner class VH(val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val recipe = getItem(position)

        holder.binding.tvRecipeTitle.text = recipe.title
        holder.binding.tvCategory.text = recipe.category?.uppercase() ?: ""
        holder.binding.tvCuisine.text = recipe.cuisine ?: ""

        // Offline Indication - Only show if isSavedScreen AND isDownloaded is true
        if (isSavedScreen && recipe.isDownloaded) {
            holder.binding.offlineBadge.visibility = View.VISIBLE
            holder.binding.tvOfflineStatus.text = "OFFLINE READY"
        } else {
            holder.binding.offlineBadge.visibility = View.GONE
        }

        // Difficulty Tag
        val difficulty = recipe.difficulty ?: "Medium"
        holder.binding.tvDifficulty.text = difficulty.uppercase()
        val (bgColor, textColor) = when (difficulty.lowercase()) {
            "easy" -> Pair("#D1FAE5", "#065F46")
            "medium" -> Pair("#FEF3C7", "#92400E")
            "hard" -> Pair("#EF4444", "#FFFFFF") 
            else -> Pair("#E5E7EB", "#374151")
        }
        
        holder.binding.tvDifficulty.backgroundTintList = ColorStateList.valueOf(Color.parseColor(bgColor))
        holder.binding.tvDifficulty.setTextColor(Color.parseColor(textColor))

        holder.binding.tvCategory.visibility = if (recipe.category.isNullOrBlank()) View.GONE else View.VISIBLE

        // Rating
        if (recipe.rating != null && recipe.rating > 0) {
            holder.binding.tvRating.text = String.format("★ %.1f", recipe.rating.toFloat())
            holder.binding.tvRating.visibility = View.VISIBLE
        } else {
            holder.binding.tvRating.visibility = View.GONE
        }

        // Author Info
        val authorName = recipe.ownerName ?: "Anonymous"
        holder.binding.tvAuthorName.text = authorName
        
        if (!recipe.ownerPhotoUrl.isNullOrBlank()) {
            holder.binding.ivAuthorImage.isVisible = true
            holder.binding.tvAuthorAvatar.isVisible = false
            Glide.with(holder.itemView.context)
                .load(recipe.ownerPhotoUrl)
                .circleCrop()
                .placeholder(R.drawable.bg_circle_orange)
                .into(holder.binding.ivAuthorImage)
        } else {
            holder.binding.ivAuthorImage.isVisible = false
            holder.binding.tvAuthorAvatar.isVisible = true
            holder.binding.tvAuthorAvatar.text = authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
        }
        
        holder.binding.authorContainer.setOnClickListener {
            recipe.ownerId?.let { id -> onAuthorClick?.invoke(id) }
        }

        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.ic_food_placeholder)
            .error(R.drawable.ic_food_placeholder)
            .centerCrop()
            .into(holder.binding.imgRecipe)

        holder.itemView.setOnClickListener {
            onRecipeClick(recipe)
        }

        if (onRemoveClick != null) {
            holder.itemView.setOnLongClickListener {
                onRemoveClick.invoke(recipe)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(old: Recipe, new: Recipe) = old.id == new.id
        override fun areContentsTheSame(old: Recipe, new: Recipe) = old == new
    }
}
