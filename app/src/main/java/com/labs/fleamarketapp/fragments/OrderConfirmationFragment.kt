package com.labs.fleamarketapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.databinding.FragmentOrderConfirmationBinding

class OrderConfirmationFragment : Fragment() {

    private var _binding: FragmentOrderConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sellerLocation = arguments?.getString("sellerLocation") ?: "STC"
        val buyerLocation = arguments?.getString("buyerLocation") ?: sellerLocation

        binding.sellerLocationSummary.text = getString(R.string.label_seller_location, sellerLocation)
        binding.buyerLocationSummary.text = getString(R.string.label_buyer_pickup, buyerLocation)

        binding.doneButton.setOnClickListener {
            findNavController().popBackStack(R.id.nav_home, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

