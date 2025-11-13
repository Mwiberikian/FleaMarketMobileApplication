package com.labs.fleamarketapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.databinding.FragmentItemDetailBinding
import com.labs.fleamarketapp.viewmodel.AuctionViewModel
import com.labs.fleamarketapp.viewmodel.UserViewModel
import java.text.NumberFormat
import java.util.Locale

class ItemDetailFragment : Fragment() {
    
    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!
    
    private val auctionViewModel: AuctionViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val itemId = arguments?.getString("itemId") ?: ""
        
        setupObservers()
        setupClickListeners()
        
        if (itemId.isNotEmpty()) {
            auctionViewModel.loadItem(itemId)
            auctionViewModel.loadBids(itemId)
        }
    }
    
    private fun setupObservers() {
        auctionViewModel.itemState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show loading
                }
                is UiState.Success -> {
                    val item = state.data
                    displayItem(item)
                }
                is UiState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        auctionViewModel.placeBidState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.placeBidButton.isEnabled = false
                }
                is UiState.Success -> {
                    binding.placeBidButton.isEnabled = true
                    Toast.makeText(context, "Bid placed successfully!", Toast.LENGTH_SHORT).show()
                    // Reload bids
                    arguments?.getString("itemId")?.let {
                        auctionViewModel.loadBids(it)
                    }
                }
                is UiState.Error -> {
                    binding.placeBidButton.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun displayItem(item: com.labs.fleamarketapp.data.Item) {
        binding.itemTitle.text = item.title
        binding.itemDescription.text = item.description
        binding.itemPrice.text = formatPrice(item.price)
        binding.itemCategory.text = item.category
        binding.sellerName.text = "Seller: ${item.sellerName}"
        
        if (item.imageUrl != null && item.imageUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(item.imageUrl)
                .into(binding.itemImage)
        }
        
        // Show auction UI if it's an auction
        if (item.isAuction) {
            binding.currentBidLabel.visibility = View.VISIBLE
            binding.currentBidAmount.visibility = View.VISIBLE
            binding.bidInputLayout.visibility = View.VISIBLE
            binding.placeBidButton.visibility = View.VISIBLE
            binding.buyButton.visibility = View.GONE
            
            item.currentBid?.let {
                binding.currentBidAmount.text = formatPrice(it)
            }
        } else {
            binding.currentBidLabel.visibility = View.GONE
            binding.currentBidAmount.visibility = View.GONE
            binding.bidInputLayout.visibility = View.GONE
            binding.placeBidButton.visibility = View.GONE
            binding.buyButton.visibility = View.VISIBLE
        }
    }
    
    private fun setupClickListeners() {
        binding.placeBidButton.setOnClickListener {
            val bidAmount = binding.bidAmountEditText.text.toString().toDoubleOrNull()
            if (bidAmount != null && bidAmount > 0) {
                val itemId = arguments?.getString("itemId") ?: return@setOnClickListener
                val user = userViewModel.currentUser.value ?: return@setOnClickListener
                val token = userViewModel.authToken.value
                if (token.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Authentication required. Please log in again.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                auctionViewModel.placeBid(
                    token = token,
                    itemId = itemId,
                    bidderId = user.id,
                    amount = bidAmount
                )
            } else {
                Toast.makeText(context, "Please enter a valid bid amount", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.buyButton.setOnClickListener {
            Toast.makeText(context, "Purchase functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun formatPrice(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        return format.format(price)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

