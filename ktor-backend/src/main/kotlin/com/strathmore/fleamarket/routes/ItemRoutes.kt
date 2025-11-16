package com.strathmore.fleamarket.routes

import com.strathmore.fleamarket.models.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Route.itemRoutes() {
    // Use generic encode/decode for image lists
    val allowedLocations = setOf("STC", "Phase1 Gazebos", "Phase2 Gazebos", "Parking Lot")
    
    route("/api/items") {
        // Get all approved/active items
        get {
            try {
                val categoryId = call.parameters["category"]
                val search = call.parameters["search"]
                val sellerFilter = call.parameters["sellerId"]
                
                val items = transaction {
                    val base = Item.find { Items.status eq ItemStatus.ACTIVE }
                        .toList()
                    val filtered = base
                        .asSequence()
                        .filter { sellerFilter == null || it.sellerId.id.value.toString() == sellerFilter }
                        .filter {
                            if (categoryId != null) {
                                val category = categoryId.toLongOrNull()?.let { Category.findById(it) }
                                if (category != null) {
                                    it.categoryId?.id == category.id
                                } else true
                            } else true
                        }
                        .filter {
                            if (search.isNullOrBlank()) true
                            else {
                                it.title.contains(search, ignoreCase = true) ||
                                it.description.contains(search, ignoreCase = true)
                            }
                        }
                        .sortedByDescending { it.createdAt }
                        .take(100)
                        .map { it.toDto() }
                        .toList()
                    
                    filtered
                }
                
                call.respond(ApiResponse(success = true, data = items))
            } catch (e: Exception) {
                call.respond(
                    ApiResponse<List<ItemDto>>(
                        success = false,
                        message = "Failed to load items: ${e.message}"
                    )
                )
            }
        }
        
        // Get single item by ID
        get("/{id}") {
            try {
                val itemId = call.parameters["id"] ?: return@get call.respond(
                    ApiResponse<ItemDto>(success = false, message = "Item ID required")
                )
                
                val item = transaction {
                    Item.findById(UUID.fromString(itemId))
                }
                
                if (item == null) {
                    call.respond(
                        ApiResponse<ItemDto>(success = false, message = "Item not found")
                    )
                    return@get
                }
                
                call.respond(
                    ApiResponse(
                        success = true,
                        data = item.toDto()
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    ApiResponse<ItemDto>(
                        success = false,
                        message = "Failed to load item: ${e.message}"
                    )
                )
            }
        }
        
        // Create new item (requires sellerId in header for now - in production use JWT)
        post {
            try {
                val sellerId = call.request.header("X-User-Id") ?: return@post call.respond(
                    ApiResponse<ItemDto>(success = false, message = "Authentication required")
                )
                
                val request = call.receive<CreateItemRequest>()

                // Reject client-side content URIs to avoid storing non-loadable image paths
                if (request.images.any { it.startsWith("content://") }) {
                    return@post call.respond(
                        ApiResponse<ItemDto>(success = false, message = "Invalid image paths. Please upload images first.")
                    )
                }
                
                val itemDto = transaction {
                    val created = Item.new {
                        this.sellerId = User.findById(UUID.fromString(sellerId))
                            ?: throw Exception("User not found")
                        title = request.title
                        description = request.description
                        price = request.price
                        startingBid = request.startingBid
                        currentBid = request.startingBid ?: request.price
                        itemType = ItemType.valueOf(request.itemType)
                        status = ItemStatus.ACTIVE
                        images = Json.encodeToString(request.images)
                        categoryId = request.categoryId
                            ?.toLongOrNull()
                            ?.let { Category.findById(it) }
                        auctionEndTime = request.auctionEndTime
                        pickupLocation = request.pickupLocation.takeIf { allowedLocations.contains(it) } ?: "STC"
                        createdAt = System.currentTimeMillis()
                    }
                    created.toDto()
                }
                
                call.respond(
                    ApiResponse<ItemDto>(
                        success = true,
                        data = itemDto,
                        message = "Item created successfully."
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    ApiResponse<ItemDto>(
                        success = false,
                        message = "Failed to create item: ${e.message}"
                    )
                )
            }
        }
        
        // Update item (owner only)
        put("/{id}") {
            try {
                val sellerId = call.request.header("X-User-Id") ?: return@put call.respond(
                    ApiResponse<ItemDto>(success = false, message = "Authentication required")
                )
                val itemId = call.parameters["id"] ?: return@put call.respond(
                    ApiResponse<ItemDto>(success = false, message = "Item ID required")
                )
                val request = call.receive<CreateItemRequest>()
                
                // Reject client-side content URIs
                if (request.images.any { it.startsWith("content://") }) {
                    return@put call.respond(
                        ApiResponse<ItemDto>(success = false, message = "Invalid image paths. Please upload images first.")
                    )
                }
                
                val updatedDto = transaction {
                    val item = Item.findById(UUID.fromString(itemId))
                        ?: throw IllegalArgumentException("Item not found")
                    if (item.sellerId.id.value.toString() != sellerId) {
                        throw IllegalStateException("Not authorized to update this item")
                    }
                    item.apply {
                        title = request.title
                        description = request.description
                        price = request.price
                        startingBid = request.startingBid
                        currentBid = request.startingBid ?: request.price
                        itemType = ItemType.valueOf(request.itemType)
                        images = Json.encodeToString(request.images)
                        categoryId = request.categoryId?.toLongOrNull()?.let { Category.findById(it) }
                        auctionEndTime = request.auctionEndTime
                        pickupLocation = request.pickupLocation.takeIf { allowedLocations.contains(it) } ?: "STC"
                    }
                    item.toDto()
                }
                
                call.respond(ApiResponse(success = true, data = updatedDto, message = "Item updated"))
            } catch (e: Exception) {
                call.respond(ApiResponse<ItemDto>(success = false, message = "Failed to update item: ${e.message}"))
            }
        }
        
        // Delete item (owner only)
        delete("/{id}") {
            try {
                val sellerId = call.request.header("X-User-Id") ?: return@delete call.respond(
                    ApiResponse<Unit>(success = false, message = "Authentication required")
                )
                val itemId = call.parameters["id"] ?: return@delete call.respond(
                    ApiResponse<Unit>(success = false, message = "Item ID required")
                )
                
                transaction {
                    val item = Item.findById(UUID.fromString(itemId))
                        ?: throw IllegalArgumentException("Item not found")
                    if (item.sellerId.id.value.toString() != sellerId) {
                        throw IllegalStateException("Not authorized to delete this item")
                    }
                    item.delete()
                }
                
                call.respond(ApiResponse<Unit>(success = true, message = "Item deleted"))
            } catch (e: Exception) {
                call.respond(ApiResponse<Unit>(success = false, message = "Failed to delete item: ${e.message}"))
            }
        }
    }
}

private fun Item.toDto(): ItemDto {
    val seller = User.findById(sellerId.id)
    val category = categoryId?.let { Category.findById(it.id) }
    val imagesList = try {
        if (images.isBlank()) emptyList()
        else Json.decodeFromString<List<String>>(images)
    } catch (_: Exception) {
        emptyList()
    }
    
    return ItemDto(
        id = id.value.toString(),
        sellerId = sellerId.id.value.toString(),
        sellerName = seller?.let { "${it.firstName} ${it.lastName}" },
        title = title,
        description = description,
        price = price,
        startingBid = startingBid,
        currentBid = currentBid,
        itemType = itemType.name,
        status = status.name,
        images = imagesList,
        categoryId = categoryId?.id?.value?.toString(),
        categoryName = category?.name,
        auctionEndTime = auctionEndTime,
        pickupLocation = pickupLocation,
        createdAt = createdAt
    )
}

