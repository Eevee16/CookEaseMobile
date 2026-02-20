package com.cookease.app.ui_components.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.Recipe
import com.cookease.app.databinding.ItemPopularRecipeBinding

class PopularAdapter(private val recipes: List<Recipe>) :
    RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val binding = ItemPopularRecipeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PopularViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount() = recipes.size

    inner class PopularViewHolder(private val binding: ItemPopularRecipeBinding) :
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
    }
}