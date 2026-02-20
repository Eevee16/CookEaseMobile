package com.cookease.app.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.databinding.ItemIngredientCardBinding

class IngredientCardAdapter(
    private var ingredients: List<String>,
    private var selected: Set<String>,
    private val onToggle: (String) -> Unit
) : RecyclerView.Adapter<IngredientCardAdapter.VH>() {

    inner class VH(val binding: ItemIngredientCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemIngredientCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = ingredients.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ingredient = ingredients[position]
        val isSelected = selected.contains(ingredient)

        holder.binding.tvIngredientName.text = ingredient
        holder.binding.selectedOverlay.isVisible = isSelected

        Glide.with(holder.itemView.context)
            .load(R.drawable.ic_food_placeholder)
            .centerCrop()
            .into(holder.binding.imgIngredient)

        holder.itemView.setOnClickListener { onToggle(ingredient) }
    }

    fun updateData(newIngredients: List<String>, newSelected: Set<String>) {
        ingredients = newIngredients
        selected = newSelected
        notifyDataSetChanged()
    }
}