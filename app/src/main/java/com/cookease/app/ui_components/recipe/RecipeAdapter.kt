package com.cookease.app.ui_components.recipe

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.Recipe
import com.cookease.app.databinding.ItemRecipeCardBinding

class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit = {},
    private val onRemoveClick: ((Recipe) -> Unit)? = null
) : ListAdapter<Recipe, RecipeAdapter.VH>(DiffCallback()) {

    inner class VH(val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val recipe = getItem(position)

        holder.binding.tvRecipeTitle.text = recipe.title
        holder.binding.tvCategory.text = recipe.category ?: ""
        holder.binding.tvCuisine.text = recipe.cuisine ?: ""

        // Difficulty
        val difficulty = recipe.difficulty ?: "Medium"
        holder.binding.tvDifficulty.text = difficulty
        val (bgColor, textColor) = when (difficulty.lowercase()) {
            "easy" -> Pair("#D1FAE5", "#065F46")
            "medium" -> Pair("#FEF3C7", "#92400E")
            "hard" -> Pair("#FEE2E2", "#991B1B")
            else -> Pair("#E5E7EB", "#374151")
        }
        holder.binding.tvDifficulty.setBackgroundColor(Color.parseColor(bgColor))
        holder.binding.tvDifficulty.setTextColor(Color.parseColor(textColor))

        // Rating
        if (recipe.rating != null && recipe.rating > 0) {
            holder.binding.tvRating.text =
                String.format("★ %.1f", recipe.rating.toFloat())
            holder.binding.tvRating.visibility = View.VISIBLE
        } else {
            holder.binding.tvRating.visibility = View.GONE
        }

        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.ic_food_placeholder)
            .error(R.drawable.ic_food_placeholder)
            .centerCrop()
            .into(holder.binding.imgRecipe)

        // Normal click
        holder.itemView.setOnClickListener {
            onRecipeClick(recipe)
        }

        // Optional long press to remove (used in Saved screen)
        if (onRemoveClick != null) {
            holder.itemView.setOnLongClickListener {
                onRemoveClick.invoke(recipe)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(old: Recipe, new: Recipe) =
            old.id == new.id

        override fun areContentsTheSame(old: Recipe, new: Recipe) =
            old == new
    }
}
