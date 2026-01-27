package com.cookease.app.ui.theme

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cookease.app.R
import com.google.android.material.tabs.TabLayout

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayoutProfile)
        val rvProfile = view.findViewById<RecyclerView>(R.id.rvProfileRecipes)

        // 1. Setup Grid
        rvProfile.layoutManager = GridLayoutManager(requireContext(), 2)
        // TODO: Set your Adapter here (e.g., ProfileRecipeAdapter)

        // 2. Handle Tab Clicks
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // User clicked "CREATED"
                        // rvProfile.adapter = AdapterWithCreatedRecipes
                    }
                    1 -> {
                        // User clicked "COOKED"
                        // rvProfile.adapter = AdapterWithCookedRecipes
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}