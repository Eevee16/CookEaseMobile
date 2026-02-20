package com.cookease.app.addrecipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cookease.app.R

class AddFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load Step 1 into the fragment_container when AddFragment first loads
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecipeStep1Fragment())
                .commit()
        }
    }
}