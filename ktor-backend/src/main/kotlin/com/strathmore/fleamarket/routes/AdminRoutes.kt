package com.strathmore.fleamarket.routes

import com.strathmore.fleamarket.models.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun Route.adminRoutes() {
    
    route("/api/admin") {
        get("/users") {
            if (!requireAdmin(call)) return@get
            val users = transaction {
                User.all().map { it.toDto() }
            }
            call.respond(ApiResponse(success = true, data = users))
        }
        
        put("/users/{id}/status") {
            if (!requireAdmin(call)) return@put
            val userId = call.parameters["id"] ?: return@put call.respond(
                ApiResponse<Unit>(success = false, message = "User ID required")
            )
            val request = call.receive<UpdateStatusRequest>()
            transaction {
                val user = User.findById(UUID.fromString(userId)) ?: throw IllegalArgumentException("User not found")
                user.status = UserStatus.valueOf(request.status.uppercase())
            }
            call.respond(ApiResponse<Unit>(success = true, message = "User status updated"))
        }
        
        get("/items") {
            if (!requireAdmin(call)) return@get
            val items = transaction {
                Item.all().sortedByDescending { it.createdAt }.map { it.toDtoAdmin() }
            }
            call.respond(ApiResponse(success = true, data = items))
        }
        
        put("/items/{id}/status") {
            if (!requireAdmin(call)) return@put
            val itemId = call.parameters["id"] ?: return@put call.respond(
                ApiResponse<Unit>(success = false, message = "Item ID required")
            )
            val request = call.receive<UpdateStatusRequest>()
            transaction {
                val item = Item.findById(UUID.fromString(itemId)) ?: throw IllegalArgumentException("Item not found")
                item.status = ItemStatus.valueOf(request.status.uppercase())
            }
            call.respond(ApiResponse<Unit>(success = true, message = "Item status updated"))
        }
        
        delete("/items/{id}") {
            if (!requireAdmin(call)) return@delete
            val itemId = call.parameters["id"] ?: return@delete call.respond(
                ApiResponse<Unit>(success = false, message = "Item ID required")
            )
            transaction {
                Item.findById(UUID.fromString(itemId))?.delete()
            }
            call.respond(ApiResponse<Unit>(success = true, message = "Item deleted"))
        }
    }
}

@kotlinx.serialization.Serializable
private data class UpdateStatusRequest(val status: String)

private suspend fun requireAdmin(call: ApplicationCall): Boolean {
    return try {
        val adminId = call.request.header("X-Admin-Id") ?: throw IllegalStateException("Admin ID header missing")
        val admin = transaction { User.findById(UUID.fromString(adminId)) }
            ?: throw IllegalStateException("Admin not found")
        if (admin.role != UserRole.ADMIN) {
            throw IllegalStateException("Admin privileges required")
        }
        true
    } catch (e: Exception) {
        call.respond(
            ApiResponse<Unit>(
                success = false,
                message = e.message ?: "Admin authentication failed"
            )
        )
        false
    }
}

private fun Item.toDtoAdmin(): ItemDto {
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
        condition = condition.name,
        itemType = itemType.name,
        status = status.name,
        images = imagesList,
        categoryId = categoryId?.id?.value?.toString(),
        categoryName = category?.name,
        auctionEndTime = auctionEndTime,
        createdAt = createdAt
    )
}

private fun User.toDto(): UserDto = UserDto(
    id = id.value.toString(),
    email = email,
    firstName = firstName,
    lastName = lastName,
    phone = phone,
    role = role.name,
    status = status.name,
    rating = rating,
    reviewCount = reviewCount
)

