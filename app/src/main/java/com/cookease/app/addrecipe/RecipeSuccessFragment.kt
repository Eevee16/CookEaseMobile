package com.cookease.app.ui.addrecipe
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.cookease.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecipeSuccessFragment : Fragment(R.layout.fragment_add_success) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnHome = view.findViewById<Button>(R.id.btnBackToHome)

        btnHome.setOnClickListener {
            // 1. Find the Bottom Navigation View in the Parent Activity
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)

            // 2. Programmatically "click" the Home icon
            // (Make sure R.id.home matches the ID in your bottom_menu.xml)
            bottomNav.selectedItemId = R.id.nav_home

            // Optional: Reset the AddFragment so if they come back, it starts at Step 1
            // parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }
}