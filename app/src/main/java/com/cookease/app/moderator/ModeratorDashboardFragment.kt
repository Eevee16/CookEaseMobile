package com.cookease.app.moderator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cookease.app.R
import com.cookease.app.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModeratorDashboardFragment : Fragment(), ModeratorAdapter.RecipeActionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ModeratorAdapter
    private lateinit var loadingProgress: ProgressBar
    private lateinit var pendingCount: TextView
    private lateinit var approvedCount: TextView
    private lateinit var rejectedCount: TextView

    private var recipes: List<Recipe> = listOf()
    private var activeTab: String = "pending"
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var tabPending: Button
    private lateinit var tabApproved: Button
    private lateinit var tabRejected: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_moderator_dashboard, container, false)
        recyclerView = view.findViewById(R.id.recipesRecyclerView)
        loadingProgress = view.findViewById(R.id.loadingProgress)
        pendingCount = view.findViewById(R.id.pendingCount)
        approvedCount = view.findViewById(R.id.approvedCount)
        rejectedCount = view.findViewById(R.id.rejectedCount)
        tabPending = view.findViewById(R.id.tabPending)
        tabApproved = view.findViewById(R.id.tabApproved)
        tabRejected = view.findViewById(R.id.tabRejected)

        setupRecyclerView()
        setupTabs()
        fetchRecipes()

        return view
    }

    private fun setupRecyclerView() {
        adapter = ModeratorAdapter(listOf(), this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupTabs() {
        tabPending.setOnClickListener { activeTab = "pending"; filterRecipes() }
        tabApproved.setOnClickListener { activeTab = "approved"; filterRecipes() }
        tabRejected.setOnClickListener { activeTab = "rejected"; filterRecipes() }
    }

    private fun filterRecipes() {
        val filtered = recipes.filter {
            it.status == activeTab || (activeTab == "pending" && it.status.isNullOrEmpty())
        }
        adapter.updateRecipes(filtered)
    }

    private fun fetchRecipes() {
        loadingProgress.visibility = View.VISIBLE
        coroutineScope.launch {
            try {
                recipes = fetchRecipesFromSupabase()
                updateStats()
                filterRecipes()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loadingProgress.visibility = View.GONE
            }
        }
    }

    private fun updateStats() {
        pendingCount.text = recipes.count { it.status.isNullOrEmpty() || it.status == "pending" }.toString()
        approvedCount.text = recipes.count { it.status == "approved" }.toString()
        rejectedCount.text = recipes.count { it.status == "rejected" }.toString()
    }

    override fun onView(recipe: Recipe) { /* TODO: navigate to detail */ }

    override fun onApprove(recipe: Recipe) {
        val updated = recipe.copy(status = "approved")
        recipes = recipes.map { if (it.id == recipe.id) updated else it }
        updateStats()
        filterRecipes()
        coroutineScope.launch {
            // TODO: Supabase update call
        }
    }

    override fun onReject(recipe: Recipe) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Reject Recipe")
        val input = android.widget.EditText(requireContext())
        dialog.setView(input)
        dialog.setPositiveButton("Reject") { _, _ ->
            val updated = recipe.copy(
                status = "rejected",
                rejectionReason = input.text.toString()
            )
            recipes = recipes.map { if (it.id == recipe.id) updated else it }
            updateStats()
            filterRecipes()
            coroutineScope.launch {
                // TODO: Supabase update call
            }
        }
        dialog.setNegativeButton("Cancel", null)
        dialog.show()
    }

    override fun onDelete(recipe: Recipe) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete ${recipe.title}?")
            .setPositiveButton("Delete") { _, _ ->
                recipes = recipes.filter { it.id != recipe.id }
                updateStats()
                filterRecipes()
                coroutineScope.launch {
                    // TODO: Supabase delete call
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }

    private suspend fun fetchRecipesFromSupabase(): List<Recipe> = withContext(Dispatchers.IO) {
        listOf()
    }
}