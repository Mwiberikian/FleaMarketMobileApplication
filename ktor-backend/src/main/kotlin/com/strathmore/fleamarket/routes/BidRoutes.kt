package com.strathmore.fleamarket.routes

import com.strathmore.fleamarket.models.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Route.bidRoutes() {
    route("/api/items/{itemId}/bids") {
        // Get all bids for an item
        get {
            try {
                val itemId = call.parameters["itemId"] ?: return@get call.respond(
                    ApiResponse<List<BidDto>>(success = false, message = "Item ID required")
                )
                
                val bids = transaction {
                    Bid.find { Bids.itemId eq UUID.fromString(itemId) }
                        .sortedByDescending { it.timestamp }
                        .map { bid ->
                            val bidder = User.findById(bid.bidderId.id)
                            BidDto(
                                id = bid.id.value.toString(),
                                itemId = bid.itemId.id.value.toString(),
                                bidderId = bid.bidderId.id.value.toString(),
                                bidderName = bidder?.let { "${it.firstName} ${it.lastName}" },
                                amount = bid.amount,
                                timestamp = bid.timestamp
                            )
                        }
                }
                
                call.respond(ApiResponse(success = true, data = bids))
            } catch (e: Exception) {
                call.respond(
                    ApiResponse<List<BidDto>>(
                        success = false,
                        message = "Failed to load bids: ${e.message}"
                    )
                )
            }
        }
        
        // Place a bid
        post {
            try {
                val itemId = call.parameters["itemId"] ?: return@post call.respond(
                    ApiResponse<BidDto>(success = false, message = "Item ID required")
                )
                
                val bidderId = call.request.header("X-User-Id") ?: return@post call.respond(
                    ApiResponse<BidDto>(success = false, message = "Authentication required")
                )
                
                val request = call.receive<PlaceBidRequest>()
                
                val bid = transaction {
                    val item = Item.findById(UUID.fromString(itemId))
                        ?: throw Exception("Item not found")
                    
                    if (item.status != ItemStatus.ACTIVE) {
                        throw Exception("Item is not active")
                    }
                    
                    if (item.itemType != ItemType.AUCTION) {
                        throw Exception("Item is not an auction")
                    }
                    
                    // Check if bid is higher than current bid or starting bid
                    val minBid = item.currentBid ?: item.startingBid ?: 0.0
                    if (request.amount <= minBid) {
                        throw Exception("Bid amount must be higher than current bid")
                    }
                    
                    // Create bid
                    val newBid = Bid.new {
                        this.itemId = item
                        this.bidderId = User.findById(UUID.fromString(bidderId))
                            ?: throw Exception("User not found")
                        amount = request.amount
                    }
                    
                    // Update item's current bid
                    item.currentBid = request.amount
                    
                    // Create notification for seller
                    val seller = User.findById(item.sellerId.id)
                    if (seller != null) {
                        Notification.new {
                            this.userId = seller
                            title = "New Bid on ${item.title}"
                            message = "Someone placed a bid of ${request.amount} on your item"
                            type = NotificationType.BID
                            this.itemId = item
                        }
                    }
                    
                    newBid
                }
                
                val bidder = transaction { User.findById(bid.bidderId.id) }
                
                call.respond(
                    ApiResponse(
                        success = true,
                        data = BidDto(
                            id = bid.id.value.toString(),
                            itemId = bid.itemId.id.value.toString(),
                            bidderId = bid.bidderId.id.value.toString(),
                            bidderName = bidder?.let { "${it.firstName} ${it.lastName}" },
                            amount = bid.amount,
                            timestamp = bid.timestamp
                        ),
                        message = "Bid placed successfully"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    ApiResponse<BidDto>(
                        success = false,
                        message = "Failed to place bid: ${e.message}"
                    )
                )
            }
        }
    }
}

