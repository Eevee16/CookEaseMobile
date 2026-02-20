package com.cookease.app.ui_components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.cookease.app.Recipe
import com.cookease.app.databinding.FragmentPopularBinding
import com.cookease.app.ui_components.adapter.PopularAdapter

class PopularFragment : Fragment() {

    private var _binding: FragmentPopularBinding? = null
    private val binding get() = _binding!!
    private lateinit var popularAdapter: PopularAdapter
    private val popularRecipes = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPopularBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        popularAdapter = PopularAdapter(popularRecipes)
        binding.rvPopularRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvPopularRecipes.adapter = popularAdapter

        fetchPopularRecipes()
    }

    private fun fetchPopularRecipes() {
        // TODO: Replace with real API call
        popularRecipes.addAll(listOf(
            Recipe(id = "1", title = "Recipe 1", viewCount = 100),
            Recipe(id = "2", title = "Recipe 2", viewCount = 150)
        ))
        popularAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}