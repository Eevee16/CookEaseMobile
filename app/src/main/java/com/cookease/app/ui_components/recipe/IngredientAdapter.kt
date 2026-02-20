package com.cookease.app.ui.recipe

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cookease.app.databinding.ItemIngredientBinding

class IngredientAdapter(private val ingredients: List<String>) :
    RecyclerView.Adapter<IngredientAdapter.VH>() {

    private val checked = mutableSetOf<Int>()

    inner class VH(val binding: ItemIngredientBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = ingredients.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ingredient = ingredients[position]
        holder.binding.tvIngredient.text = ingredient
        holder.binding.checkbox.isChecked = checked.contains(position)

        // Strike-through when checked
        val flags = if (checked.contains(position))
            holder.binding.tvIngredient.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else
            holder.binding.tvIngredient.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        holder.binding.tvIngredient.paintFlags = flags

        holder.binding.checkbox.setOnCheckedChangeListener(null)
        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checked.add(position) else checked.remove(position)
            val newFlags = if (isChecked)
                holder.binding.tvIngredient.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else
                holder.binding.tvIngredient.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.binding.tvIngredient.paintFlags = newFlags
        }
    }
}