package com.cookease.app.ui_components.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.Recipe
import com.cookease.app.databinding.ItemPopularRecipeBinding

class PopularItemViewHolder(private val binding: ItemPopularRecipeBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(recipe: Recipe) {
        binding.recipeTitle.text = recipe.title
        binding.viewCount.text = recipe.viewCount.toString()
        Glide.with(binding.root.context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.ic_food_placeholder)
            .centerCrop()
            .into(binding.recipeImage)
    }

    companion object {
        fun from(parent: ViewGroup): PopularItemViewHolder {
            val binding = ItemPopularRecipeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return PopularItemViewHolder(binding)
        }
    }
}