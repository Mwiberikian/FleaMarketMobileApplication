package com.labs.fleamarketapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.adapter.ItemAdapter
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.databinding.FragmentListingsBinding
import com.labs.fleamarketapp.viewmodel.ItemViewModel
import com.labs.fleamarketapp.viewmodel.UserViewModel

class ListingsFragment : Fragment() {
    
    private var _binding: FragmentListingsBinding? = null
    private val binding get() = _binding!!
    
    private val itemViewModel: ItemViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var itemAdapter: ItemAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        // Load user listings
        userViewModel.currentUser.value?.let { user ->
            itemViewModel.loadUserListings(user.id)
        } ?: run {
            // User not logged in, show empty state
            binding.emptyText.visibility = View.VISIBLE
        }
    }
    
    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter(emptyList()) { item ->
            val bundle = Bundle().apply {
                putString("itemId", item.id)
            }
            findNavController().navigate(R.id.nav_item_detail, bundle)
        }
        binding.listingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = itemAdapter
        }
    }
    
    private fun setupObservers() {
        itemViewModel.userListingsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyText.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.data.isEmpty()) {
                        binding.emptyText.visibility = View.VISIBLE
                    } else {
                        binding.emptyText.visibility = View.GONE
                        itemAdapter = ItemAdapter(state.data) { item ->
                            val bundle = Bundle().apply {
                                putString("itemId", item.id)
                            }
                            findNavController().navigate(R.id.nav_item_detail, bundle)
                        }
                        binding.listingsRecyclerView.adapter = itemAdapter
                    }
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyText.visibility = View.VISIBLE
                    binding.emptyText.text = state.message
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.createListingFab.setOnClickListener {
            findNavController().navigate(R.id.nav_create_listing)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

