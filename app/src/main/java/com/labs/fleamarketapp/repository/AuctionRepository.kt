package com.labs.fleamarketapp.repository

import com.labs.fleamarketapp.local.dao.BidDao
import com.labs.fleamarketapp.local.entities.BidEntity
import kotlinx.coroutines.flow.Flow

class AuctionRepository(private val bidDao: BidDao) {
    suspend fun placeBid(bid: BidEntity) = bidDao.placeBid(bid)
    fun getBidsForItem(itemId: String): Flow<List<BidEntity>> = bidDao.getBidsForItem(itemId)
    suspend fun getHighestBid(itemId: String): BidEntity? = bidDao.getHighestBid(itemId)
}
