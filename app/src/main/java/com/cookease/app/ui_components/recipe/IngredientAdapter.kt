package com.cookease.app.ui_components.recipe

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cookease.app.databinding.ItemIngredientBinding

class IngredientAdapter(private val ingredients: List<String>) :
    RecyclerView.Adapter<IngredientAdapter.VH>() {

    private val checkedPositions = mutableSetOf<Int>()

    inner class VH(val binding: ItemIngredientBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = ingredients.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ingredient = ingredients[position].trim()
        holder.binding.tvIngredient.text = ingredient

        val isChecked = checkedPositions.contains(position)

        // Remove previous listener first
        holder.binding.checkbox.setOnCheckedChangeListener(null)
        holder.binding.checkbox.isChecked = isChecked
        updateStrikeThrough(holder.binding.tvIngredient, isChecked)

        holder.binding.checkbox.setOnCheckedChangeListener { _, checked ->
            if (checked) checkedPositions.add(position) else checkedPositions.remove(position)
            updateStrikeThrough(holder.binding.tvIngredient, checked)
        }
    }

    private fun updateStrikeThrough(textView: android.widget.TextView, strike: Boolean) {
        textView.paintFlags = if (strike)
            textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else
            textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}