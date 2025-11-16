package com.labs.fleamarketapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.data.PickupLocations
import com.labs.fleamarketapp.databinding.FragmentCheckoutBinding
import java.text.NumberFormat
import java.util.Locale

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemTitle = arguments?.getString("itemTitle").orEmpty()
        val itemPrice = arguments?.getString("itemPrice")?.toDoubleOrNull() ?: 0.0
        val sellerLocation = arguments?.getString("sellerLocation") ?: PickupLocations.options.first()

        binding.itemNameText.text = itemTitle
        binding.itemPriceText.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(itemPrice)
        binding.sellerLocationText.text = getString(R.string.label_seller_location, sellerLocation)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PickupLocations.options
        )
        binding.buyerPickupAutoComplete.setAdapter(adapter)
        binding.buyerPickupAutoComplete.setText(sellerLocation, false)

        binding.confirmPickupButton.setOnClickListener {
            val buyerLocation = binding.buyerPickupAutoComplete.text?.toString()
                ?.takeIf { it.isNotBlank() } ?: sellerLocation

            val bundle = bundleOf(
                "sellerLocation" to sellerLocation,
                "buyerLocation" to buyerLocation
            )
            findNavController().navigate(R.id.nav_order_confirmation, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

