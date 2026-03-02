package com.cookease.app.addrecipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.cookease.app.R
import com.google.android.material.button.MaterialButton

class IngredientSearchAdapter(
    private var items: List<IngredientItem>,
    private val isAdded: (IngredientItem) -> Boolean,
    private val onAddClick: (IngredientItem) -> Unit
) : RecyclerView.Adapter<IngredientSearchAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivIngredientImage)
        val tvName: TextView = view.findViewById(R.id.tvIngredientName)
        val btnAdd: MaterialButton = view.findViewById(R.id.btnAddIngredient)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient_search_result, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val added = isAdded(item)

        holder.tvName.text = item.name
        holder.ivImage.load(item.image_url.ifEmpty { null }) {
            placeholder(R.drawable.ic_ingredient_placeholder)
            error(R.drawable.ic_ingredient_placeholder)
            transformations(RoundedCornersTransformation(8f))
        }

        if (added) {
            holder.btnAdd.text = "✓ Added"
            holder.btnAdd.isEnabled = false
            holder.btnAdd.alpha = 0.5f
        } else {
            holder.btnAdd.text = "+ Add"
            holder.btnAdd.isEnabled = true
            holder.btnAdd.alpha = 1f
            holder.btnAdd.setOnClickListener { onAddClick(item) }
        }
        holder.itemView.setOnClickListener { if (!added) onAddClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<IngredientItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}