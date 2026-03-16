package com.cookease.app.moderator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cookease.app.R
import com.cookease.app.Recipe
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
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

    private lateinit var cardError: MaterialCardView
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnHideError: ImageButton
    private lateinit var cardSuccess: MaterialCardView
    private lateinit var tvSuccessMessage: TextView
    private lateinit var btnHideSuccess: ImageButton

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

        cardError = view.findViewById(R.id.cardError)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        btnHideError = view.findViewById(R.id.btnHideError)
        cardSuccess = view.findViewById(R.id.cardSuccess)
        tvSuccessMessage = view.findViewById(R.id.tvSuccessMessage)
        btnHideSuccess = view.findViewById(R.id.btnHideSuccess)

        setupRecyclerView()
        setupTabs()
        setupObservers()
        setupListeners()

        viewModel.fetchRecipes()
    }

    private fun setupRecyclerView() {
        adapter = ModeratorAdapter(emptyList(), object : ModeratorAdapter.RecipeActionListener {
            override fun onView(recipe: Recipe) {
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

    private fun setupListeners() {
        btnHideError.setOnClickListener { cardError.isVisible = false }
        btnHideSuccess.setOnClickListener { cardSuccess.isVisible = false }
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
            recyclerView.isVisible = false
            emptyState.isVisible = true
            tvEmptyMessage.text = "No $activeTab recipes"
            tvEmptyHint.isVisible = activeTab == "pending"
        } else {
            recyclerView.isVisible = true
            emptyState.isVisible = false
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ModeratorState.Loading -> {
                    loadingProgress.isVisible = true
                    recyclerView.isVisible = false
                    emptyState.isVisible = false
                    cardError.isVisible = false
                }
                is ModeratorState.Success -> {
                    loadingProgress.isVisible = false
                    updateStats()
                    updateListForTab()
                }
                is ModeratorState.Error -> {
                    loadingProgress.isVisible = false
                    showBannerError(state.message)
                }
                else -> {}
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ModeratorState.Success -> {
                    showBannerSuccess("Action successful")
                    viewModel.resetActionState()
                }
                is ModeratorState.Error -> {
                    showBannerError(state.message)
                    viewModel.resetActionState()
                }
                else -> {}
            }
        }
    }

    private fun showBannerError(message: String) {
        tvErrorMessage.text = message
        cardError.isVisible = true
        cardSuccess.isVisible = false
    }

    private fun showBannerSuccess(message: String) {
        tvSuccessMessage.text = message
        cardSuccess.isVisible = true
        cardError.isVisible = false
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
                    showBannerError("Please provide a rejection reason")
                } else {
                    viewModel.rejectRecipe(recipe.id, reason)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
