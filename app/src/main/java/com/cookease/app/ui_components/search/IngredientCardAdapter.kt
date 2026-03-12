package com.cookease.app.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.cookease.app.R
import com.cookease.app.addrecipe.IngredientItem
import com.cookease.app.databinding.ItemIngredientCardBinding

class IngredientCardAdapter(
    private var ingredients: List<IngredientItem>,
    private var selected: Set<String>,
    private val onToggle: (String) -> Unit
) : RecyclerView.Adapter<IngredientCardAdapter.VH>() {

    inner class VH(val binding: ItemIngredientCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemIngredientCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = ingredients.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ingredient = ingredients[position]
        val isSelected = selected.contains(ingredient.name)

        holder.binding.tvIngredientName.text = ingredient.name
        holder.binding.selectedOverlay.isVisible = isSelected

        // Use Coil for better consistency with other parts of the app
        holder.binding.imgIngredient.load(ingredient.image_url?.takeIf { it.isNotEmpty() }) {
            placeholder(R.drawable.ic_ingredient_placeholder)
            error(R.drawable.ic_ingredient_placeholder)
            transformations(RoundedCornersTransformation(8f))
        }

        holder.itemView.setOnClickListener { onToggle(ingredient.name) }
    }

    fun updateData(newIngredients: List<IngredientItem>, newSelected: Set<String>) {
        ingredients = newIngredients
        selected = newSelected
        notifyDataSetChanged()
    }
}