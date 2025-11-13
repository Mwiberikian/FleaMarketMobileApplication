package com.labs.fleamarketapp.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.databinding.FragmentCreateListingBinding
import com.labs.fleamarketapp.viewmodel.ItemViewModel
import com.labs.fleamarketapp.viewmodel.UserViewModel

class CreateListingFragment : Fragment() {
    
    private var _binding: FragmentCreateListingBinding? = null
    private val binding get() = _binding!!
    
    private val itemViewModel: ItemViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    
    private val categories = arrayOf("Electronics", "Books", "Clothing", "Jewellery", "Other")
    private val categoryNameToId = mapOf(
        "Electronics" to 1L,
        "Books" to 2L,
        "Clothing" to 3L,
        "Jewellery" to 4L,
        "Other" to null
    )
    
    private var selectedImageUri: Uri? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(requireContext())
                .load(it)
                .into(binding.itemImageView)
            binding.itemImageView.visibility = View.VISIBLE
            binding.addImageButton.visibility = View.GONE
        }
    }
    
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
        setupCheckboxListeners()
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
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        
        binding.addImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        
        binding.itemImageView.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        
        binding.createButton.setOnClickListener {
            if (validateInput()) {
                if (userViewModel.currentUser.value == null) {
                    Toast.makeText(requireContext(), "Authentication required. Please log in again.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val isAuction = binding.auctionCheckBox.isChecked
                val isFixedPrice = binding.fixedPriceCheckBox.isChecked
                
                val priceValue = if (isFixedPrice) {
                    binding.priceEditText.text.toString().toDoubleOrNull()
                } else {
                    null
                }
                
                val startingBid = if (isAuction) {
                    binding.startingBidEditText.text.toString().toDoubleOrNull()
                } else {
                    null
                }
                
                val timeLimitHours = if (isAuction) {
                    binding.timeLimitEditText.text.toString().toIntOrNull() ?: 0
                } else {
                    0
                }
                
                val auctionEndTime = if (isAuction && timeLimitHours > 0) {
                    System.currentTimeMillis() + (timeLimitHours * 60 * 60 * 1000L)
                } else {
                    null
                }
                
                val token = userViewModel.authToken.value
                if (token.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Authentication required. Please log in again.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val imageList = selectedImageUri?.let { listOf(it.toString()) } ?: emptyList()
                val categoryName = binding.categoryAutoComplete.text.toString().trim()
                val categoryId = categoryNameToId[categoryName]
                val itemType = if (isAuction) "AUCTION" else "FIXED_PRICE"
                val condition = "GOOD" // TODO: capture from UI when available
                
                itemViewModel.createItem(
                    token = token,
                    title = binding.titleEditText.text.toString().trim(),
                    description = binding.descriptionEditText.text.toString().trim(),
                    price = priceValue,
                    startingBid = startingBid,
                    condition = condition,
                    itemType = itemType,
                    images = imageList,
                    categoryId = categoryId,
                    auctionEndTime = auctionEndTime
                )
            }
        }
    }
    
    private fun setupCheckboxListeners() {
        binding.fixedPriceCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.priceInputLayout.visibility = View.VISIBLE
            } else {
                binding.priceInputLayout.visibility = View.GONE
                binding.priceEditText.text?.clear()
            }
        }
        
        binding.auctionCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.startingBidInputLayout.visibility = View.VISIBLE
                binding.timeLimitInputLayout.visibility = View.VISIBLE
            } else {
                binding.startingBidInputLayout.visibility = View.GONE
                binding.timeLimitInputLayout.visibility = View.GONE
                binding.startingBidEditText.text?.clear()
                binding.timeLimitEditText.text?.clear()
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
        
        if (binding.categoryAutoComplete.text.toString().trim().isEmpty()) {
            binding.categoryInputLayout.error = "Category is required"
            isValid = false
        } else {
            binding.categoryInputLayout.error = null
        }
        
        val isFixedPrice = binding.fixedPriceCheckBox.isChecked
        val isAuction = binding.auctionCheckBox.isChecked
        
        if (!isFixedPrice && !isAuction) {
            Toast.makeText(context, "Please select either Fixed Price or Enable Auction", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        if (isFixedPrice) {
            val price = binding.priceEditText.text.toString().toDoubleOrNull()
            if (price == null || price <= 0) {
                binding.priceInputLayout.error = "Valid price is required"
                isValid = false
            } else {
                binding.priceInputLayout.error = null
            }
        }
        
        if (isAuction) {
            val startingBid = binding.startingBidEditText.text.toString().toDoubleOrNull()
            if (startingBid == null || startingBid <= 0) {
                binding.startingBidInputLayout.error = "Valid starting bid is required"
                isValid = false
            } else {
                binding.startingBidInputLayout.error = null
            }
            
            val timeLimit = binding.timeLimitEditText.text.toString().toIntOrNull()
            if (timeLimit == null || timeLimit <= 0) {
                binding.timeLimitInputLayout.error = "Valid time limit is required"
                isValid = false
            } else {
                binding.timeLimitInputLayout.error = null
            }
        }
        
        return isValid
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

