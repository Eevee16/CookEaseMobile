package com.cookease.app.addrecipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.cookease.app.R
import com.google.android.material.textfield.TextInputEditText

class SelectedIngredientAdapter(
    private var items: MutableList<SelectedIngredient>,
    private val onRemove: (Int) -> Unit,
    private val onQtyChanged: (Int, String) -> Unit,
    private val onUnitChanged: (Int, String) -> Unit,
    private val onPrepChanged: (Int, String) -> Unit
) : RecyclerView.Adapter<SelectedIngredientAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivSelectedIngredientImage)
        val tvName: TextView = view.findViewById(R.id.tvSelectedIngredientName)
        val etQty: TextInputEditText = view.findViewById(R.id.etIngredientQty)
        val spUnit: Spinner = view.findViewById(R.id.spIngredientUnit)
        val etPrep: TextInputEditText = view.findViewById(R.id.etIngredientPrep)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemoveIngredient)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_ingredient, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvName.text = item.name
        holder.ivImage.load(item.image_url.ifEmpty { null }) {
            placeholder(R.drawable.ic_ingredient_placeholder)
            error(R.drawable.ic_ingredient_placeholder)
            transformations(RoundedCornersTransformation(8f))
        }

        // Qty
        holder.etQty.setText(item.qty)
        holder.etQty.addTextChangedListener { onQtyChanged(position, it.toString()) }

        // Unit spinner
        val unitAdapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_dropdown_item,
            AddRecipeViewModel.UNITS
        )
        holder.spUnit.adapter = unitAdapter
        val unitPos = AddRecipeViewModel.UNITS.indexOf(item.unit)
        if (unitPos >= 0) holder.spUnit.setSelection(unitPos)
        holder.spUnit.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                onUnitChanged(position, AddRecipeViewModel.UNITS[pos])
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }

        // Prep
        holder.etPrep.setText(item.prep)
        holder.etPrep.addTextChangedListener { onPrepChanged(position, it.toString()) }

        // Remove
        holder.btnRemove.setOnClickListener { onRemove(position) }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: MutableList<SelectedIngredient>) {
        items = newItems
        notifyDataSetChanged()
    }
}