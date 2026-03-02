package com.cookease.app.addrecipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_instruction_step, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tvStepNumber.text = "Step ${position + 1}"
        holder.etStep.setText(steps[position])
        holder.etStep.addTextChangedListener { onTextChanged(position, it.toString()) }
        holder.btnRemove.isEnabled = steps.size > 1
        holder.btnRemove.alpha = if (steps.size > 1) 1f else 0.3f
        holder.btnRemove.setOnClickListener { if (steps.size > 1) onRemove(position) }
    }

    override fun getItemCount() = steps.size

    fun updateList(newSteps: MutableList<String>) {
        steps = newSteps
        notifyDataSetChanged()
    }
}