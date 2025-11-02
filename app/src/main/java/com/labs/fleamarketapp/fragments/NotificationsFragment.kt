package com.labs.fleamarketapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.databinding.FragmentNotificationsBinding
import com.labs.fleamarketapp.viewmodel.NotificationViewModel
import com.labs.fleamarketapp.viewmodel.UserViewModel

class NotificationsFragment : Fragment() {
    
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    
    private val notificationViewModel: NotificationViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        
        // Load notifications
        userViewModel.currentUser.value?.let { user ->
            notificationViewModel.loadNotifications(user.id)
        } ?: run {
            binding.emptyText.visibility = View.VISIBLE
        }
    }
    
    private fun setupRecyclerView() {
        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            // TODO: Create NotificationAdapter
        }
    }
    
    private fun setupObservers() {
        notificationViewModel.notificationsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.emptyText.visibility = View.GONE
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        binding.emptyText.visibility = View.VISIBLE
                    } else {
                        binding.emptyText.visibility = View.GONE
                        // TODO: Update adapter with notifications
                    }
                }
                is UiState.Error -> {
                    binding.emptyText.visibility = View.VISIBLE
                    binding.emptyText.text = state.message
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

