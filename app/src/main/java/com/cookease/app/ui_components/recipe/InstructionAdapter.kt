package com.cookease.app.ui.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cookease.app.databinding.ItemInstructionStepBinding

class InstructionAdapter(private val steps: List<String>) :
    RecyclerView.Adapter<InstructionAdapter.VH>() {

    inner class VH(val binding: ItemInstructionStepBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemInstructionStepBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = steps.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.tvStepNumber.text = (position + 1).toString()
        holder.binding.tvInstruction.text = steps[position]
    }
}