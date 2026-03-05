package com.cookease.app.moderator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cookease.app.R
import com.cookease.app.Recipe
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ModeratorDashboardFragment : Fragment() {

    private val viewModel: ModeratorViewModel by viewModels()
    private lateinit var adapter: ModeratorAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var tvEmptyMessage: TextView
    private lateinit var tvEmptyHint: TextView
    private lateinit var pendingCount: TextView
    private lateinit var approvedCount: TextView
    private lateinit var rejectedCount: TextView
    private lateinit var tabPending: MaterialButton
    private lateinit var tabApproved: MaterialButton
    private lateinit var tabRejected: MaterialButton

    private var activeTab = "pending"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_moderator_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recipesRecyclerView)
        loadingProgress = view.findViewById(R.id.loadingProgress)
        emptyState = view.findViewById(R.id.emptyState)
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage)
        tvEmptyHint = view.findViewById(R.id.tvEmptyHint)
        pendingCount = view.findViewById(R.id.pendingCount)
        approvedCount = view.findViewById(R.id.approvedCount)
        rejectedCount = view.findViewById(R.id.rejectedCount)
        tabPending = view.findViewById(R.id.tabPending)
        tabApproved = view.findViewById(R.id.tabApproved)
        tabRejected = view.findViewById(R.id.tabRejected)

        setupRecyclerView()
        setupTabs()
        setupObservers()

        viewModel.fetchRecipes()
    }

    private fun setupRecyclerView() {
        adapter = ModeratorAdapter(emptyList(), object : ModeratorAdapter.RecipeActionListener {
            override fun onView(recipe: Recipe) {
                // Navigate to Recipe Detail from Moderator Dashboard
                val action = ModeratorDashboardFragmentDirections.actionModeratorDashboardFragmentToRecipeDetailFragment(recipe.id)
                findNavController().navigate(action)
            }

            override fun onApprove(recipe: Recipe) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Approve Recipe")
                    .setMessage("Approve \"${recipe.title}\"?")
                    .setPositiveButton("Approve") { _, _ ->
                        viewModel.approveRecipe(recipe.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            override fun onReject(recipe: Recipe) {
                showRejectDialog(recipe)
            }

            override fun onDelete(recipe: Recipe) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Recipe")
                    .setMessage("Permanently delete \"${recipe.title}\"? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteRecipe(recipe.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }, activeTab)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupTabs() {
        tabPending.setOnClickListener { switchTab("pending") }
        tabApproved.setOnClickListener { switchTab("approved") }
        tabRejected.setOnClickListener { switchTab("rejected") }
    }

    private fun switchTab(tab: String) {
        activeTab = tab
        updateTabStyles()
        updateListForTab()
    }

    private fun updateTabStyles() {
        val activeColor = resources.getColor(R.color.purple_primary, null)
        val inactiveColor = resources.getColor(R.color.gray_400, null)
        tabPending.setTextColor(if (activeTab == "pending") activeColor else inactiveColor)
        tabApproved.setTextColor(if (activeTab == "approved") activeColor else inactiveColor)
        tabRejected.setTextColor(if (activeTab == "rejected") activeColor else inactiveColor)
    }

    private fun updateListForTab() {
        val list = when (activeTab) {
            "approved" -> viewModel.approvedRecipes.value ?: emptyList()
            "rejected" -> viewModel.rejectedRecipes.value ?: emptyList()
            else -> viewModel.pendingRecipes.value ?: emptyList()
        }
        showList(list)
    }

    private fun showList(list: List<Recipe>) {
        adapter.updateRecipes(list, activeTab)
        if (list.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            tvEmptyMessage.text = "No $activeTab recipes"
            tvEmptyHint.visibility = if (activeTab == "pending") View.VISIBLE else View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ModeratorState.Loading -> {
                    loadingProgress.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    emptyState.visibility = View.GONE
                }
                is ModeratorState.Success -> {
                    loadingProgress.visibility = View.GONE
                    updateStats()
                    updateListForTab()
                }
                is ModeratorState.Error -> {
                    loadingProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ModeratorState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetActionState()
                }
                else -> {}
            }
        }
    }

    private fun updateStats() {
        pendingCount.text = (viewModel.pendingRecipes.value?.size ?: 0).toString()
        approvedCount.text = (viewModel.approvedRecipes.value?.size ?: 0).toString()
        rejectedCount.text = (viewModel.rejectedRecipes.value?.size ?: 0).toString()
    }

    private fun showRejectDialog(recipe: Recipe) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_reject_recipe, null)
        val etReason = dialogView.findViewById<EditText>(R.id.etRejectionReason)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reject Recipe")
            .setMessage("Provide a reason for rejecting \"${recipe.title}\"")
            .setView(dialogView)
            .setPositiveButton("Confirm Rejection") { _, _ ->
                val reason = etReason.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(requireContext(), "Please provide a rejection reason", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.rejectRecipe(recipe.id, reason)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
