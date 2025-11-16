package com.labs.fleamarketapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.adapter.CategoryAdapter
import com.labs.fleamarketapp.adapter.FeaturedListingAdapter
import com.labs.fleamarketapp.data.HomeCategory
import com.labs.fleamarketapp.data.Item
import com.labs.fleamarketapp.data.ItemStatus
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.databinding.FragmentHomeBinding
import com.labs.fleamarketapp.LoginActivity
import com.labs.fleamarketapp.viewmodel.ItemViewModel
import com.labs.fleamarketapp.viewmodel.UserViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import java.util.UUID

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ItemViewModel by viewModels {
        androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private val featuredAdapter = FeaturedListingAdapter(::openItemDetails)

    private var featuredItems: List<Item> = emptyList()
    private var selectedCategory = "All"

    private val categories = listOf(
        HomeCategory("All", "Everything new on campus"),
        HomeCategory("Books", "Texts & notes"),
        HomeCategory("Electronics", "Phones & laptops"),
        HomeCategory("Jewellery", "Watches & accessories"),
        HomeCategory("Furniture", "Dorm essentials"),
        HomeCategory("Food", "Snacks & meals"),
        HomeCategory("Fashion", "Fits & accessories"),
        HomeCategory("Services", "Tutors & gigs")
    )

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
        setupBanner()
        setupSearch()
        setupRecyclerViews()
        bindCategories()
        setupObservers()
        setupLoginCta()
        viewModel.loadFeaturedItems()
    }

    private fun setupBanner() {
        binding.createListingButton.setOnClickListener {
            handleRestrictedAction {
                findNavController().navigate(R.id.nav_create_listing)
            }
        }

        binding.shopButton.setOnClickListener {
            handleRestrictedAction {
                findNavController().navigate(R.id.nav_listings)
            }
        }

        // Load banner and why-use images from assets, matching view IDs to asset filenames
        loadAssetImage(binding.bannerImage, "bannerImage.png")
        loadAssetImage(binding.whyImageTop, "whyImageTop.png")
        loadAssetImage(binding.whyImageBottom, "whyImageBottom.png")
    }

    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged {
            applyFilters()
        }
        binding.searchGoButton.setOnClickListener {
            applyFilters()
        }
    }

    private fun setupRecyclerViews() {
        binding.categoriesRecycler.apply {
            layoutManager = GridLayoutManager(context, 2)
            isNestedScrollingEnabled = false
        }
        binding.featuredRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredAdapter
        }
    }

    private fun bindCategories() {
        categoryAdapter = CategoryAdapter(categories) { category ->
            selectedCategory = category.title
            applyFilters()
        }
        binding.categoriesRecycler.adapter = categoryAdapter
    }

    private fun setupObservers() {
        viewModel.featuredItemsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.errorText.isVisible = false
                }
                is UiState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.errorText.isVisible = false
                    updateListings(state.data)
                }
                is UiState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.errorText.isVisible = true
                    binding.errorText.text = state.message
                }
            }
        }
    }

    private fun setupLoginCta() {
        binding.loginButton.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                userViewModel.currentUser.collect { user ->
                    binding.loginButton.isVisible = user == null
                }
            }
        }
    }

    // Removed sample data population to avoid showing mock listings

    private fun updateListings(items: List<Item>) {
        featuredItems = items.sortedByDescending { it.createdAt }.take(6)
        applyFilters()
    }

    private fun applyFilters() {
        val query = binding.searchEditText.text?.toString()?.trim().orEmpty()
        val filtered = featuredItems.filter { item ->
            val matchesCategory = when {
                selectedCategory == "All" -> true
                selectedCategory.equals("Fashion", true) -> item.category.equals("Clothing", true)
                else -> item.category.equals(selectedCategory, true)
            }
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, true) ||
                    item.description.contains(query, true)
            matchesCategory && matchesQuery
        }
        featuredAdapter.submitList(filtered)
        binding.emptyStateText.isVisible = filtered.isEmpty()
    }

    private fun openItemDetails(item: Item) {
        handleRestrictedAction {
            val bundle = Bundle().apply { putString("itemId", item.id) }
            findNavController().navigate(R.id.nav_item_detail, bundle)
        }
    }

    private fun loadAssetImage(view: android.widget.ImageView, fileName: String) {
        val ctx = view.context
        val assetToUse = when {
            assetExists(ctx, fileName) -> fileName
            assetExists(ctx, fileName.lowercase()) -> fileName.lowercase()
            else -> null
        }
        if (assetToUse == null) {
            // Show a subtle placeholder if asset isn't found
            Glide.with(ctx)
                .load(R.drawable.ic_launcher_foreground)
                .into(view)
            return
        }
        Glide.with(ctx)
            .load("file:///android_asset/$assetToUse")
            .error(R.drawable.ic_launcher_foreground)
            .into(view)
    }

    private fun assetExists(context: android.content.Context, name: String): Boolean {
        return try {
            context.assets.open(name).close()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun handleRestrictedAction(action: () -> Unit) {
        val user = userViewModel.currentUser.value
        if (user == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        } else {
            action()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val SAMPLE_IMAGES = listOf(
            "https://images.unsplash.com/photo-1517336714731-489689fd1ca8",
            "https://images.unsplash.com/photo-1523475472560-d2df97ec485c",
            "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab",
            "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f"
        )
    }
}
