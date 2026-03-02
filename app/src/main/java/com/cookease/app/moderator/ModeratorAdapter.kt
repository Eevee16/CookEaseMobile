package com.cookease.app.moderator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.cookease.app.R
import com.cookease.app.Recipe
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

class ModeratorAdapter(
    private var recipes: List<Recipe>,
    private val listener: RecipeActionListener,
    private var activeTab: String = "pending"
) : RecyclerView.Adapter<ModeratorAdapter.VH>() {

    interface RecipeActionListener {
        fun onView(recipe: Recipe)
        fun onApprove(recipe: Recipe)
        fun onReject(recipe: Recipe)
        fun onDelete(recipe: Recipe)
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivRecipeThumbnail)
        val tvTitle: TextView = view.findViewById(R.id.tvRecipeTitle)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvOwner: TextView = view.findViewById(R.id.tvOwner)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvRejectionReason: TextView = view.findViewById(R.id.tvRejectionReason)
        val btnApprove: MaterialButton = view.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = view.findViewById(R.id.btnReject)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_moderator, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val recipe = recipes[position]

        holder.tvTitle.text = recipe.title
        holder.tvCategory.text = recipe.category ?: "N/A"
        holder.tvOwner.text = "By ${recipe.ownerName ?: "Unknown"}"
        holder.tvDate.text = formatDate(recipe.createdAt)

        holder.ivThumbnail.load(recipe.imageUrl?.ifEmpty { null }) {
            placeholder(R.drawable.ic_camera)
            error(R.drawable.ic_camera)
            transformations(RoundedCornersTransformation(8f))
        }

        // Rejection reason — only show on rejected tab
        if (activeTab == "rejected" && !recipe.rejectionReason.isNullOrEmpty()) {
            holder.tvRejectionReason.text = "Reason: ${recipe.rejectionReason}"
            holder.tvRejectionReason.visibility = View.VISIBLE
        } else {
            holder.tvRejectionReason.visibility = View.GONE
        }

        // Show/hide approve button (hide on approved tab)
        holder.btnApprove.visibility = if (activeTab == "approved") View.GONE else View.VISIBLE

        // Show/hide reject button (hide on rejected tab)
        holder.btnReject.visibility = if (activeTab == "rejected") View.GONE else View.VISIBLE

        holder.btnApprove.setOnClickListener { listener.onApprove(recipe) }
        holder.btnReject.setOnClickListener { listener.onReject(recipe) }
        holder.btnDelete.setOnClickListener { listener.onDelete(recipe) }
        holder.itemView.setOnClickListener { listener.onView(recipe) }
    }

    override fun getItemCount() = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>, tab: String) {
        recipes = newRecipes
        activeTab = tab
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
