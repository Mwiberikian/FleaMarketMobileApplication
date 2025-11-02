package com.labs.fleamarketapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.labs.fleamarketapp.LoginActivity
import com.labs.fleamarketapp.databinding.FragmentProfileBinding
import com.labs.fleamarketapp.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: UserViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set initial values
        updateProfileDisplay(viewModel.currentUser.value)
        
        setupObservers()
        setupClickListeners()
    }
    
    private fun updateProfileDisplay(user: com.labs.fleamarketapp.data.User?) {
        if (_binding == null) return
        
        user?.let {
            binding.profileName.text = it.name
            binding.profileEmail.text = it.email
            
            if (it.profileImageUrl != null && it.profileImageUrl.isNotEmpty()) {
                try {
                    Glide.with(requireContext())
                        .load(it.profileImageUrl)
                        .circleCrop()
                        .into(binding.profileImage)
                } catch (e: Exception) {
                    // Handle error silently
                }
            } else {
                // Clear image if no URL
                binding.profileImage.setImageResource(0)
            }
        } ?: run {
            // No user logged in
            binding.profileName.text = "Not logged in"
            binding.profileEmail.text = ""
            binding.profileImage.setImageResource(0)
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    viewModel.currentUser.collect { user ->
                        if (_binding != null) { // Check binding is still valid
                            updateProfileDisplay(user)
                        }
                    }
                } catch (e: Exception) {
                    // Handle any errors in collection
                    if (_binding != null) {
                        binding.profileName.text = "Error loading profile"
                        binding.profileEmail.text = ""
                    }
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.logoutButton.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

