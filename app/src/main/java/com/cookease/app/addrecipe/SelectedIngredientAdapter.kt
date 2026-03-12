package com.cookease.app.addrecipe

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
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
        var qtyWatcher: TextWatcher? = null
        var prepWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_ingredient, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvName.text = item.name
        holder.ivImage.load(item.image_url?.takeIf { it.isNotEmpty() }) {
            placeholder(R.drawable.ic_ingredient_placeholder)
            error(R.drawable.ic_ingredient_placeholder)
            transformations(RoundedCornersTransformation(8f))
        }

        // --- Qty ---
        holder.qtyWatcher?.let { holder.etQty.removeTextChangedListener(it) }
        if (holder.etQty.text.toString() != item.qty) {
            holder.etQty.setText(item.qty)
        }
        val qWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val newText = s?.toString() ?: ""
                    if (items[pos].qty != newText) onQtyChanged(pos, newText)
                }
            }
        }
        holder.etQty.addTextChangedListener(qWatcher)
        holder.qtyWatcher = qWatcher

        // --- Unit spinner ---
        val unitAdapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_dropdown_item,
            AddRecipeViewModel.UNITS
        )
        holder.spUnit.adapter = unitAdapter
        val unitPos = AddRecipeViewModel.UNITS.indexOf(item.unit)
        if (unitPos >= 0) holder.spUnit.setSelection(unitPos, false)
        
        holder.spUnit.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val bPos = holder.bindingAdapterPosition
                if (bPos != RecyclerView.NO_POSITION) {
                    val newUnit = AddRecipeViewModel.UNITS[pos]
                    if (items[bPos].unit != newUnit) onUnitChanged(bPos, newUnit)
                }
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }

        // --- Prep ---
        holder.prepWatcher?.let { holder.etPrep.removeTextChangedListener(it) }
        if (holder.etPrep.text.toString() != item.prep) {
            holder.etPrep.setText(item.prep)
        }
        val pWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val newText = s?.toString() ?: ""
                    if (items[pos].prep != newText) onPrepChanged(pos, newText)
                }
            }
        }
        holder.etPrep.addTextChangedListener(pWatcher)
        holder.prepWatcher = pWatcher

        // Remove
        holder.btnRemove.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onRemove(pos)
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<SelectedIngredient>) {
        val sizeChanged = items.size != newItems.size
        items = newItems.toMutableList()
        if (sizeChanged) {
            notifyDataSetChanged()
        }
    }
}