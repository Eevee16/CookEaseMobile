package com.cookease.app.moderator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookease.app.R
import com.cookease.app.Recipe

class ModeratorAdapter(
    private var recipes: List<Recipe>,
    private val listener: RecipeActionListener
) : RecyclerView.Adapter<ModeratorAdapter.RecipeViewHolder>() {

    interface RecipeActionListener {
        fun onView(recipe: Recipe)
        fun onApprove(recipe: Recipe)
        fun onReject(recipe: Recipe)
        fun onDelete(recipe: Recipe)
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.recipeImage)
        val title: TextView = itemView.findViewById(R.id.recipeTitle)
        val category: TextView = itemView.findViewById(R.id.recipeCategory)
        val owner: TextView = itemView.findViewById(R.id.recipeOwner)
        val date: TextView = itemView.findViewById(R.id.recipeDate)
        val rejection: TextView = itemView.findViewById(R.id.rejectionReason)

        val btnView: Button = itemView.findViewById(R.id.btnView)
        val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_moderator, parent, false)
        return RecipeViewHolder(view)
    }

    override fun getItemCount() = recipes.size

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.title.text = recipe.title
        holder.category.text = recipe.category ?: "N/A"
        holder.owner.text = recipe.ownerName ?: "Unknown"
        holder.date.text = recipe.createdAt ?: "N/A"

        if (recipe.status == "rejected") {
            holder.rejection.visibility = View.VISIBLE
            holder.rejection.text = recipe.rejectionReason ?: "No reason provided"
        } else {
            holder.rejection.visibility = View.GONE
        }

        Glide.with(holder.image.context)
            .load(recipe.imageUrl)
            .placeholder(R.color.gray)
            .into(holder.image)

        holder.btnView.setOnClickListener { listener.onView(recipe) }
        holder.btnApprove.setOnClickListener { listener.onApprove(recipe) }
        holder.btnReject.setOnClickListener { listener.onReject(recipe) }
        holder.btnDelete.setOnClickListener { listener.onDelete(recipe) }
    }

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}