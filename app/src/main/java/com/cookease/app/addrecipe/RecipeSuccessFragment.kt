package com.cookease.app.addrecipe

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
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.selectedItemId = R.id.nav_home
            parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }
}
