package com.cookease.app.ui.recipe

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.Recipe
import com.cookease.app.databinding.ItemRecipeCardBinding

class RecipeAdapter(
    private val onClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.VH>(DiffCallback()) {

    inner class VH(val binding: ItemRecipeCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val recipe = getItem(position)
        holder.binding.tvRecipeTitle.text = recipe.title
        holder.binding.tvCategory.text = recipe.category ?: ""
        holder.binding.tvCuisine.text = recipe.cuisine ?: ""

        // Difficulty tag with color
        val difficulty = recipe.difficulty ?: "Medium"
        holder.binding.tvDifficulty.text = difficulty
        val (bgColor, textColor) = when (difficulty.lowercase()) {
            "easy" -> Pair("#D1FAE5", "#065F46")      // green
            "medium" -> Pair("#FEF3C7", "#92400E")    // yellow
            "hard" -> Pair("#FEE2E2", "#991B1B")      // red
            else -> Pair("#E5E7EB", "#374151")         // gray
        }
        holder.binding.tvDifficulty.setBackgroundColor(Color.parseColor(bgColor))
        holder.binding.tvDifficulty.setTextColor(Color.parseColor(textColor))

        // Safe handling of nullable rating
        if (recipe.rating?.compareTo(0f) == 1) {  // safe check for rating > 0
            holder.binding.tvRating.text = String.format("★ %.1f", recipe.rating ?: 0f)
            holder.binding.tvRating.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.tvRating.visibility = android.view.View.GONE
        }

        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.ic_food_placeholder)
            .error(R.drawable.ic_food_placeholder)
            .centerCrop()
            .into(holder.binding.imgRecipe)

        holder.itemView.setOnClickListener { onClick(recipe) }
    }

    class DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(old: Recipe, new: Recipe) = old.id == new.id
        override fun areContentsTheSame(old: Recipe, new: Recipe) = old == new
    }
}
