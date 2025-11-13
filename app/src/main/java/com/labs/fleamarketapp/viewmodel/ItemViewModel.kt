package com.labs.fleamarketapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.labs.fleamarketapp.data.Item
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.local.db.MarketplaceDatabase
import com.labs.fleamarketapp.local.entities.ItemEntity
import com.labs.fleamarketapp.repository.HybridItemRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = MarketplaceDatabase.getInstance(application)
    private val itemRepository = HybridItemRepository(
        itemDao = database.itemDao(),
        context = application
    )
    
    private val _itemsState = MutableLiveData<UiState<List<Item>>>()
    val itemsState: LiveData<UiState<List<Item>>> = _itemsState
    
    private val _featuredItemsState = MutableLiveData<UiState<List<Item>>>()
    val featuredItemsState: LiveData<UiState<List<Item>>> = _featuredItemsState
    
    private val _userListingsState = MutableLiveData<UiState<List<Item>>>()
    val userListingsState: LiveData<UiState<List<Item>>> = _userListingsState
    
    private val _createItemState = MutableLiveData<UiState<Item>>()
    val createItemState: LiveData<UiState<Item>> = _createItemState
    
    private val _refreshState = MutableLiveData<UiState<Unit>>()
    val refreshState: LiveData<UiState<Unit>> = _refreshState
    
    /**
     * Load featured items using hybrid repository
     * Automatically syncs from server if online, uses cache if offline
     */
    fun loadFeaturedItems() {
        viewModelScope.launch {
            _featuredItemsState.value = UiState.Loading
            itemRepository.getFeaturedItems()
                .catch { e ->
                    _featuredItemsState.value = UiState.Error(e.message ?: "Failed to load items")
                }
                .collect { entities ->
                    val items = entities.map { it.toItem() }
                    _featuredItemsState.value = UiState.Success(items)
                }
        }
    }
    
    /**
     * Search items with server sync
     */
    fun searchItems(query: String) {
        if (query.isBlank()) {
            loadFeaturedItems()
            return
        }
        
        viewModelScope.launch {
            _itemsState.value = UiState.Loading
            itemRepository.searchItems(query)
                .catch { e ->
                    _itemsState.value = UiState.Error(e.message ?: "Search failed")
                }
                .collect { entities ->
                    val items = entities.map { it.toItem() }
                    _itemsState.value = UiState.Success(items)
                }
        }
    }
    
    /**
     * Get items by category
     */
    fun getItemsByCategory(categoryId: Long) {
        viewModelScope.launch {
            _itemsState.value = UiState.Loading
            itemRepository.getItemsByCategory(categoryId)
                .catch { e ->
                    _itemsState.value = UiState.Error(e.message ?: "Failed to load items")
                }
                .collect { entities ->
                    val items = entities.map { it.toItem() }
                    _itemsState.value = UiState.Success(items)
                }
        }
    }
    
    /**
     * Create item listing (POST to server)
     */
    fun createItem(
        token: String,
        title: String,
        description: String,
        price: Double?,
        startingBid: Double?,
        condition: String,
        itemType: String,
        images: List<String>,
        categoryId: Long?,
        auctionEndTime: Long?
    ) {
        viewModelScope.launch {
            _createItemState.value = UiState.Loading
            val result = itemRepository.createItem(
                token = token,
                title = title,
                description = description,
                price = price,
                startingBid = startingBid,
                condition = condition,
                itemType = itemType,
                images = images,
                categoryId = categoryId,
                auctionEndTime = auctionEndTime
            )
            
            result.fold(
                onSuccess = { entity ->
                    _createItemState.value = UiState.Success(entity.toItem())
                },
                onFailure = { e ->
                    _createItemState.value = UiState.Error(e.message ?: "Failed to create item")
                }
            )
        }
    }
    
    /**
     * Pull-to-refresh: Force sync from server
     */
    fun refreshItems() {
        viewModelScope.launch {
            _refreshState.value = UiState.Loading
            val result = itemRepository.refreshItems()
            
            result.fold(
                onSuccess = {
                    _refreshState.value = UiState.Success(Unit)
                    loadFeaturedItems() // Reload after refresh
                },
                onFailure = { e ->
                    _refreshState.value = UiState.Error(e.message ?: "Refresh failed")
                }
            )
        }
    }
    
    /**
     * Load user's own listings (from server)
     */
    fun loadUserListings(userId: String) {
        viewModelScope.launch {
            _userListingsState.value = UiState.Loading
            // TODO: Implement user listings endpoint in API
            // For now, filter by sellerId from local cache
            database.itemDao().getByStatus(com.labs.fleamarketapp.local.entities.Status.ACTIVE)
                .catch { e ->
                    _userListingsState.value = UiState.Error(e.message ?: "Failed to load listings")
                }
                .collect { entities ->
                    val userItems = entities.filter { it.sellerId == userId }
                    val items = userItems.map { it.toItem() }
                    _userListingsState.value = UiState.Success(items)
                }
        }
    }
    
    // Helper to convert ItemEntity to Item (UI model)
    private fun ItemEntity.toItem(): Item {
        return Item(
            id = id,
            title = title,
            description = description,
            price = price ?: 0.0,
            imageUrl = images.firstOrNull(),
            category = "", // Will be populated from categoryId if needed
            sellerId = sellerId,
            sellerName = "", // Will be populated from server
            createdAt = createdAt,
            status = when (status) {
                com.labs.fleamarketapp.local.entities.Status.ACTIVE -> com.labs.fleamarketapp.data.ItemStatus.AVAILABLE
                com.labs.fleamarketapp.local.entities.Status.SOLD -> com.labs.fleamarketapp.data.ItemStatus.SOLD
                else -> com.labs.fleamarketapp.data.ItemStatus.RESERVED
            },
            isAuction = itemType == com.labs.fleamarketapp.local.entities.ItemType.AUCTION,
            auctionEndTime = null, // Will be populated from server
            currentBid = null // Will be populated from server
        )
    }
}

