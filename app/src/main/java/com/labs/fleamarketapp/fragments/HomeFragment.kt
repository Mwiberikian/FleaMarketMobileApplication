package com.labs.fleamarketapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.adapter.CategoryAdapter
import com.labs.fleamarketapp.adapter.ItemAdapter
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.databinding.FragmentHomeBinding
import com.labs.fleamarketapp.viewmodel.ItemViewModel

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ItemViewModel by viewModels {
        androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    
    private val categories = listOf("All", "Electronics", "Books", "Clothing", "Jewellery", "Other")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupObservers()
        viewModel.loadFeaturedItems()
    }
    
    private fun setupRecyclerViews() {
        // Categories RecyclerView
        categoryAdapter = CategoryAdapter(categories) { category ->
            // TODO: Filter items by category
            context?.let {
                Toast.makeText(it, "Selected: $category", Toast.LENGTH_SHORT).show()
            }
        }
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
        
        // Items RecyclerView
        itemAdapter = ItemAdapter(emptyList()) { item ->
            val bundle = Bundle().apply {
                putString("itemId", item.id)
            }
            findNavController().navigate(R.id.nav_item_detail, bundle)
        }
        binding.itemsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = itemAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.featuredItemsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorText.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.GONE
                    itemAdapter = ItemAdapter(state.data) { item ->
                        val bundle = Bundle().apply {
                            putString("itemId", item.id)
                        }
                        findNavController().navigate(R.id.nav_item_detail, bundle)
                    }
                    binding.itemsRecyclerView.adapter = itemAdapter
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = state.message
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

