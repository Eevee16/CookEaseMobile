package com.cookease.app.addrecipe

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cookease.app.R
import com.google.android.material.textfield.TextInputEditText

class InstructionStepAdapter(
    private var steps: MutableList<String>,
    private val onRemove: (Int) -> Unit,
    private val onTextChanged: (Int, String) -> Unit
) : RecyclerView.Adapter<InstructionStepAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvStepNumber: TextView = view.findViewById(R.id.tvStepNumber)
        val etStep: TextInputEditText = view.findViewById(R.id.tvInstruction)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemoveStep)
        var textWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_instruction_step, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val stepText = steps[position]
        holder.tvStepNumber.text = "Step ${position + 1}"
        
        // Remove old watcher to prevent recursive updates when setting text
        holder.textWatcher?.let { holder.etStep.removeTextChangedListener(it) }
        
        if (holder.etStep.text.toString() != stepText) {
            holder.etStep.setText(stepText)
        }
        
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val newText = s?.toString() ?: ""
                    // Only update if text actually changed to avoid circular updates
                    if (steps[pos] != newText) {
                        onTextChanged(pos, newText)
                    }
                }
            }
        }
        holder.etStep.addTextChangedListener(watcher)
        holder.textWatcher = watcher

        holder.btnRemove.isEnabled = steps.size > 1
        holder.btnRemove.alpha = if (steps.size > 1) 1f else 0.3f
        holder.btnRemove.setOnClickListener { 
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION && steps.size > 1) {
                onRemove(pos) 
            }
        }
    }

    override fun getItemCount() = steps.size

    fun updateList(newSteps: List<String>) {
        // Optimization: notify only if size changed. 
        // Text changes are handled by the EditText itself.
        val sizeChanged = steps.size != newSteps.size
        steps = newSteps.toMutableList()
        if (sizeChanged) {
            notifyDataSetChanged()
        }
    }
}