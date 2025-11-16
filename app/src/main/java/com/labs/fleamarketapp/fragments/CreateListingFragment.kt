package com.labs.fleamarketapp.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButtonToggleGroup
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.adapter.SelectedImageAdapter
import com.labs.fleamarketapp.data.PickupLocations
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.databinding.FragmentCreateListingBinding
import com.labs.fleamarketapp.util.FormValidator
import com.labs.fleamarketapp.util.ImagePicker.registerMultiImagePicker
import com.labs.fleamarketapp.api.ApiClient
import com.labs.fleamarketapp.viewmodel.ItemViewModel
import com.labs.fleamarketapp.viewmodel.UserViewModel
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class CreateListingFragment : Fragment() {

    private var _binding: FragmentCreateListingBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private val selectedImages = mutableListOf<Uri>()
    private val imageAdapter = SelectedImageAdapter(::removeImage)
    private var editingItemId: String? = null
    private var originalImages: List<String> = emptyList()

    private val galleryPicker = registerMultiImagePicker { uris ->
        if (uris.isEmpty()) return@registerMultiImagePicker
        val remaining = MAX_IMAGES - selectedImages.size
        if (remaining <= 0) {
            Toast.makeText(requireContext(), "Maximum of $MAX_IMAGES images", Toast.LENGTH_SHORT).show()
            return@registerMultiImagePicker
        }
        selectedImages.addAll(uris.take(remaining))
        imageAdapter.submitList(selectedImages.toList())
    }

    private val categories = listOf(
        "Electronics" to 1L,
        "Books" to 2L,
        "Clothing" to 3L,
        "Jewellery" to 4L,
        "Furniture" to 5L,
        "Food" to null,
        "Services" to null
    )
    // Removed conditions

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
        setupDropdowns()
        setupImagesRecycler()
        setupItemTypeToggle()
        setupObservers()
        setupButtons()

        // Check if we are editing an existing item
        editingItemId = arguments?.getString("itemId")
        editingItemId?.let { id ->
            itemViewModel.loadItemForEdit(id)
            binding.postListingButton.text = getString(R.string.edit) // reuse "Edit" text for brevity
        }
    }

    private fun setupDropdowns() {
        binding.categoryAutoComplete.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories.map { it.first })
        )
        binding.pickupLocationAutoComplete.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, PickupLocations.options)
        )
        binding.pickupLocationAutoComplete.setText(PickupLocations.options.first(), false)
    }

    private fun setupImagesRecycler() {
        binding.selectedImagesRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    private fun setupItemTypeToggle() {
        binding.itemTypeToggle.check(R.id.fixedPriceButton)
        binding.itemTypeToggle.addOnButtonCheckedListener { _: MaterialButtonToggleGroup, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.fixedPriceButton -> {
                    binding.priceInput.visibility = View.VISIBLE
                    binding.startingBidInput.visibility = View.GONE
                    binding.auctionDurationInput.visibility = View.GONE
                    binding.startingBidEditText.text?.clear()
                    binding.auctionDurationEditText.text?.clear()
                }
                R.id.auctionButton -> {
                    binding.priceInput.visibility = View.GONE
                    binding.priceEditText.text?.clear()
                    binding.startingBidInput.visibility = View.VISIBLE
                    binding.auctionDurationInput.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupObservers() {
        itemViewModel.createItemState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.postListingButton.isEnabled = false
                is UiState.Success -> {
                    binding.postListingButton.isEnabled = true
                    Toast.makeText(context, "Listing Successfully Posted", Toast.LENGTH_SHORT).show()
                    // Navigate to My Listings
                    findNavController().navigate(R.id.nav_listings)
                }
                is UiState.Error -> {
                    binding.postListingButton.isEnabled = true
                    Toast.makeText(context, state.message ?: "Failed to create listing", Toast.LENGTH_SHORT).show()
                }
            }
        }

        itemViewModel.editItemState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> prefillForm(state.data)
                else -> { /* no-op */ }
            }
        }

        itemViewModel.updateItemState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.postListingButton.isEnabled = false
                is UiState.Success -> {
                    binding.postListingButton.isEnabled = true
                    Toast.makeText(context, "Listing updated", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.nav_listings)
                }
                is UiState.Error -> {
                    binding.postListingButton.isEnabled = true
                    Toast.makeText(context, state.message ?: "Failed to update listing", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
        binding.addImagesButton.setOnClickListener { galleryPicker.launch("image/*") }
        binding.postListingButton.setOnClickListener { submitForm() }
    }

    private fun submitForm() {
        val user = userViewModel.currentUser.value ?: run {
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validateInput()) return

        val isAuction = binding.itemTypeToggle.checkedButtonId == R.id.auctionButton
        val priceValue = if (!isAuction) binding.priceEditText.text.toString().toDoubleOrNull() else null
        val startingBid = if (isAuction) binding.startingBidEditText.text.toString().toDoubleOrNull() else null
        val durationHours = if (isAuction) binding.auctionDurationEditText.text.toString().toIntOrNull() ?: 0 else 0
        val auctionEndTime = if (isAuction && durationHours > 0) {
            System.currentTimeMillis() + durationHours * 60 * 60 * 1000L
        } else null

        val categoryName = binding.categoryAutoComplete.text.toString()
        val categoryId = categories.firstOrNull { it.first == categoryName }?.second
        val pickupLocation = binding.pickupLocationAutoComplete.text.toString()

        // Upload images first (if any), then create or update item with public URLs
        viewLifecycleOwner.lifecycleScope.launch {
            binding.postListingButton.isEnabled = false
            try {
                val uploaded = withContext(Dispatchers.IO) { uploadSelectedImages() }
                val finalImages = if (uploaded.isNotEmpty()) uploaded else originalImages

                val itemTypeValue = if (isAuction) "AUCTION" else "FIXED_PRICE"
                val editingId = editingItemId
                if (editingId != null) {
                    itemViewModel.updateItem(
                        itemId = editingId,
                        sellerId = user.id,
                        title = binding.titleEditText.text.toString().trim(),
                        description = binding.descriptionEditText.text.toString().trim(),
                        price = priceValue,
                        startingBid = startingBid,
                        itemType = itemTypeValue,
                        images = finalImages,
                        categoryId = categoryId,
                        auctionEndTime = auctionEndTime,
                        pickupLocation = pickupLocation
                    )
                } else {
                    itemViewModel.createItem(
                        sellerId = user.id,
                        title = binding.titleEditText.text.toString().trim(),
                        description = binding.descriptionEditText.text.toString().trim(),
                        price = priceValue,
                        startingBid = startingBid,
                        itemType = itemTypeValue,
                        images = finalImages,
                        categoryId = categoryId,
                        auctionEndTime = auctionEndTime,
                        pickupLocation = pickupLocation
                    )
                }
            } catch (e: Exception) {
                binding.postListingButton.isEnabled = true
                Toast.makeText(requireContext(), e.message ?: "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun uploadSelectedImages(): List<String> {
        if (selectedImages.isEmpty()) return emptyList()
        val resolver = requireContext().contentResolver
        val parts = selectedImages.mapNotNull { uri ->
            try {
                val tmp = File.createTempFile("upl_", null, requireContext().cacheDir)
                resolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tmp).use { output -> input.copyTo(output) }
                }
                val mime = resolver.getType(uri) ?: "image/jpeg"
                val body = tmp.asRequestBody(mime.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", tmp.name, body)
            } catch (_: Exception) {
                null
            }
        }
        if (parts.isEmpty()) return emptyList()
        val response = ApiClient.api.uploadImages(parts)
        if (response.isSuccessful) {
            val urls = response.body()?.data ?: emptyList()
            return urls
        } else {
            throw IllegalStateException(response.errorBody()?.string() ?: "Upload failed")
        }
    }
    private fun validateInput(): Boolean {
        var valid = true
        valid = FormValidator.requiredText(binding.titleInput, binding.titleEditText.text) && valid
        valid = FormValidator.requiredText(binding.descriptionInput, binding.descriptionEditText.text) && valid
        valid = FormValidator.dropdownSelection(binding.categoryInput, binding.categoryAutoComplete.text?.toString()) && valid
        valid = FormValidator.dropdownSelection(binding.pickupLocationInput, binding.pickupLocationAutoComplete.text?.toString()) && valid

        when (binding.itemTypeToggle.checkedButtonId) {
            R.id.fixedPriceButton -> {
                val price = binding.priceEditText.text.toString().toDoubleOrNull()
                valid = FormValidator.positiveNumber(binding.priceInput, price) && valid
            }
            R.id.auctionButton -> {
                val bid = binding.startingBidEditText.text.toString().toDoubleOrNull()
                val duration = binding.auctionDurationEditText.text.toString().toIntOrNull()
                valid = FormValidator.positiveNumber(binding.startingBidInput, bid) && valid
                valid = FormValidator.positiveInteger(binding.auctionDurationInput, duration) && valid
            }
            else -> {
                Toast.makeText(context, "Select a pricing option", Toast.LENGTH_SHORT).show()
                valid = false
            }
        }
        return valid
    }

    private fun removeImage(uri: Uri) {
        selectedImages.remove(uri)
        imageAdapter.submitList(selectedImages.toList())
    }

    private fun prefillForm(item: com.labs.fleamarketapp.data.Item) {
        binding.titleEditText.setText(item.title)
        binding.descriptionEditText.setText(item.description)
        // Category (by name)
        binding.categoryAutoComplete.setText(item.category, false)
        // Pickup
        binding.pickupLocationAutoComplete.setText(item.pickupLocation, false)
        // Pricing mode
        if (item.isAuction) {
            binding.itemTypeToggle.check(R.id.auctionButton)
            binding.priceInput.visibility = View.GONE
            binding.startingBidInput.visibility = View.VISIBLE
            binding.auctionDurationInput.visibility = View.VISIBLE
            binding.startingBidEditText.setText(item.currentBid?.toString() ?: "")
        } else {
            binding.itemTypeToggle.check(R.id.fixedPriceButton)
            binding.priceInput.visibility = View.VISIBLE
            binding.startingBidInput.visibility = View.GONE
            binding.auctionDurationInput.visibility = View.GONE
            binding.priceEditText.setText(item.price.toString())
        }
        // Keep original images to reuse if user doesn't add new ones
        originalImages = item.images
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
                private const val MAX_IMAGES = 1
    }
}

