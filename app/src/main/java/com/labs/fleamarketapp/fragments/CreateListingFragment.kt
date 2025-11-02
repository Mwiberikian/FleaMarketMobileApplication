package com.labs.fleamarketapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.data.Item
import com.labs.fleamarketapp.data.ItemStatus
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.databinding.FragmentCreateListingBinding
import com.labs.fleamarketapp.viewmodel.ItemViewModel
import com.labs.fleamarketapp.viewmodel.UserViewModel

class CreateListingFragment : Fragment() {
    
    private var _binding: FragmentCreateListingBinding? = null
    private val binding get() = _binding!!
    
    private val itemViewModel: ItemViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    
    private val categories = arrayOf("Electronics", "Books", "Clothing", "Jewellery", "Other")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateListingBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCategoryDropdown()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.categoryAutoComplete.setAdapter(adapter)
    }
    
    private fun setupObservers() {
        itemViewModel.createItemState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.createButton.isEnabled = false
                }
                is UiState.Success -> {
                    binding.createButton.isEnabled = true
                    Toast.makeText(context, "Listing created successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is UiState.Error -> {
                    binding.createButton.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.createButton.setOnClickListener {
            if (validateInput()) {
                val user = userViewModel.currentUser.value ?: return@setOnClickListener
                
                val item = Item(
                    id = "",
                    title = binding.titleEditText.text.toString().trim(),
                    description = binding.descriptionEditText.text.toString().trim(),
                    price = binding.priceEditText.text.toString().toDoubleOrNull() ?: 0.0,
                    category = binding.categoryAutoComplete.text.toString().trim(),
                    sellerId = user.id,
                    sellerName = user.name,
                    createdAt = System.currentTimeMillis(),
                    status = ItemStatus.AVAILABLE,
                    isAuction = binding.auctionCheckBox.isChecked
                )
                
                itemViewModel.createItem(item)
            }
        }
    }
    
    private fun validateInput(): Boolean {
        var isValid = true
        
        if (binding.titleEditText.text.toString().trim().isEmpty()) {
            binding.titleInputLayout.error = "Title is required"
            isValid = false
        } else {
            binding.titleInputLayout.error = null
        }
        
        if (binding.descriptionEditText.text.toString().trim().isEmpty()) {
            binding.descriptionInputLayout.error = "Description is required"
            isValid = false
        } else {
            binding.descriptionInputLayout.error = null
        }
        
        val price = binding.priceEditText.text.toString().toDoubleOrNull()
        if (price == null || price <= 0) {
            binding.priceInputLayout.error = "Valid price is required"
            isValid = false
        } else {
            binding.priceInputLayout.error = null
        }
        
        if (binding.categoryAutoComplete.text.toString().trim().isEmpty()) {
            binding.categoryInputLayout.error = "Category is required"
            isValid = false
        } else {
            binding.categoryInputLayout.error = null
        }
        
        return isValid
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

