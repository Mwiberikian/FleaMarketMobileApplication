package com.labs.fleamarketapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.labs.fleamarketapp.data.Bid
import com.labs.fleamarketapp.data.Item
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.local.db.MarketplaceDatabase
import com.labs.fleamarketapp.local.entities.BidEntity
import com.labs.fleamarketapp.repository.HybridBidRepository
import com.labs.fleamarketapp.repository.HybridItemRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AuctionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = MarketplaceDatabase.getInstance(application)
    private val itemRepository = HybridItemRepository(
        itemDao = database.itemDao(),
        context = application
    )
    private val bidRepository = HybridBidRepository(
        bidDao = database.bidDao(),
        draftBidDao = database.draftBidDao(),
        context = application
    )
    
    private val _itemState = MutableLiveData<UiState<Item>>()
    val itemState: LiveData<UiState<Item>> = _itemState
    
    private val _bidsState = MutableLiveData<UiState<List<Bid>>>()
    val bidsState: LiveData<UiState<List<Bid>>> = _bidsState
    
    private val _placeBidState = MutableLiveData<UiState<Bid>>()
    val placeBidState: LiveData<UiState<Bid>> = _placeBidState
    
    /**
     * Load item details using hybrid repository
     */
    fun loadItem(itemId: String) {
        viewModelScope.launch {
            _itemState.value = UiState.Loading
            itemRepository.getItemById(itemId)
                .catch { e ->
                    _itemState.value = UiState.Error(e.message ?: "Failed to load item")
                }
                .collect { entity ->
                    if (entity != null) {
                        _itemState.value = UiState.Success(entity.toItem())
                    } else {
                        _itemState.value = UiState.Error("Item not found")
                    }
                }
        }
    }
    
    /**
     * Load bids for an item (from server, cached locally)
     */
    fun loadBids(itemId: String) {
        viewModelScope.launch {
            _bidsState.value = UiState.Loading
            bidRepository.getBidsForItem(itemId)
                .catch { e ->
                    _bidsState.value = UiState.Error(e.message ?: "Failed to load bids")
                }
                .collect { entities ->
                    val bids = entities.map { it.toBid() }
                    _bidsState.value = UiState.Success(bids)
                }
        }
    }
    
    /**
     * Place a bid with conflict resolution
     */
    fun placeBid(token: String, itemId: String, bidderId: String, amount: Double) {
        viewModelScope.launch {
            _placeBidState.value = UiState.Loading
            val result = bidRepository.placeBid(
                token = token,
                itemId = itemId,
                amount = amount,
                userId = bidderId
            )
            
            result.fold(
                onSuccess = { entity ->
                    _placeBidState.value = UiState.Success(entity.toBid())
                    loadBids(itemId) // Refresh bids list
                },
                onFailure = { e ->
                    _placeBidState.value = UiState.Error(e.message ?: "Failed to place bid")
                }
            )
        }
    }
    
    // Helper to convert BidEntity to Bid
    private fun BidEntity.toBid(): Bid {
        return Bid(
            id = id,
            itemId = itemId,
            bidderId = bidderId,
            bidderName = "", // Will be populated from server
            amount = amount,
            timestamp = timestamp
        )
    }
    
    // Helper to convert ItemEntity to Item (duplicate from ItemViewModel - consider moving to a mapper)
    private fun com.labs.fleamarketapp.local.entities.ItemEntity.toItem(): Item {
        return Item(
            id = id,
            title = title,
            description = description,
            price = price ?: 0.0,
            imageUrl = images.firstOrNull(),
            category = "",
            sellerId = sellerId,
            sellerName = "",
            createdAt = createdAt,
            status = when (status) {
                com.labs.fleamarketapp.local.entities.Status.ACTIVE -> com.labs.fleamarketapp.data.ItemStatus.AVAILABLE
                com.labs.fleamarketapp.local.entities.Status.SOLD -> com.labs.fleamarketapp.data.ItemStatus.SOLD
                else -> com.labs.fleamarketapp.data.ItemStatus.RESERVED
            },
            isAuction = itemType == com.labs.fleamarketapp.local.entities.ItemType.AUCTION,
            auctionEndTime = null,
            currentBid = null
        )
    }
}

