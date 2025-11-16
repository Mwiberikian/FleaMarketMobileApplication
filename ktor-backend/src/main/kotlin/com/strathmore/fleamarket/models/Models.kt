package com.strathmore.fleamarket.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

// Enums
enum class UserRole { BUYER, SELLER, ADMIN }
enum class ItemType { FIXED_PRICE, AUCTION }
enum class ItemStatus { PENDING, APPROVED, ACTIVE, SOLD }
enum class NotificationType { SYSTEM, BID, ORDER, INFO }

// Database Tables
object Users : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val firstName = varchar("firstName", 100)
    val lastName = varchar("lastName", 100)
    val password = varchar("password", 255)
    val phone = varchar("phone", 20).nullable()
    val profileImageUrl = varchar("profileImageUrl", 1024).nullable()
    val role = enumerationByName<UserRole>("role", 20)
    val rating = double("rating").default(0.0)
    val reviewCount = integer("reviewCount").default(0)
    val createdAt = long("createdAt").default(System.currentTimeMillis())
}

object Categories : LongIdTable("categories") {
    val name = varchar("name", 100).uniqueIndex()
    val description = text("description")
}

object Items : UUIDTable("items") {
    val sellerId = reference("sellerId", Users.id)
    val title = varchar("title", 255)
    val description = text("description")
    val price = double("price").nullable()
    val startingBid = double("startingBid").nullable()
    val currentBid = double("currentBid").nullable()
    val itemType = enumerationByName<ItemType>("itemType", 20)
    val status = enumerationByName<ItemStatus>("status", 20)
    val images = text("images") // JSON array as string
    val categoryId = reference("categoryId", Categories.id).nullable()
    val auctionEndTime = long("auctionEndTime").nullable()
    val pickupLocation = varchar("pickupLocation", 50).default("STC")
    val createdAt = long("createdAt").default(System.currentTimeMillis())
}

object Bids : UUIDTable("bids") {
    val itemId = reference("itemId", Items.id)
    val bidderId = reference("bidderId", Users.id)
    val amount = double("amount")
    val timestamp = long("timestamp").default(System.currentTimeMillis())
}

object Notifications : UUIDTable("notifications") {
    val userId = reference("userId", Users.id)
    val title = varchar("title", 255)
    val message = text("message")
    val type = enumerationByName<NotificationType>("type", 20)
    val isRead = bool("isRead").default(false)
    val itemId = reference("itemId", Items.id).nullable()
    val timestamp = long("timestamp").default(System.currentTimeMillis())
}

// Entity Classes
class User(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<User>(Users)
    
    var email by Users.email
    var firstName by Users.firstName
    var lastName by Users.lastName
    var password by Users.password
    var phone by Users.phone
    var profileImageUrl by Users.profileImageUrl
    var role by Users.role
    var rating by Users.rating
    var reviewCount by Users.reviewCount
    var createdAt by Users.createdAt
}

class Category(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Category>(Categories)
    
    var name by Categories.name
    var description by Categories.description
}

class Item(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Item>(Items)
    
    var sellerId by User referencedOn Items.sellerId
    var title by Items.title
    var description by Items.description
    var price by Items.price
    var startingBid by Items.startingBid
    var currentBid by Items.currentBid
    var itemType by Items.itemType
    var status by Items.status
    var images by Items.images
    var categoryId by Category optionalReferencedOn Items.categoryId
    var auctionEndTime by Items.auctionEndTime
    var pickupLocation by Items.pickupLocation
    var createdAt by Items.createdAt
}

class Bid(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Bid>(Bids)
    
    var itemId by Item referencedOn Bids.itemId
    var bidderId by User referencedOn Bids.bidderId
    var amount by Bids.amount
    var timestamp by Bids.timestamp
}

class Notification(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Notification>(Notifications)
    
    var userId by User referencedOn Notifications.userId
    var title by Notifications.title
    var message by Notifications.message
    var type by Notifications.type
    var isRead by Notifications.isRead
    var itemId by Item optionalReferencedOn Notifications.itemId
    var timestamp by Notifications.timestamp
}

// DTOs (Data Transfer Objects) for API
@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val role: String,
    val rating: Double = 0.0,
    val reviewCount: Int = 0
)

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val description: String
)

@Serializable
data class ItemDto(
    val id: String,
    val sellerId: String,
    val sellerName: String? = null,
    val title: String,
    val description: String,
    val price: Double? = null,
    val startingBid: Double? = null,
    val currentBid: Double? = null,
    val itemType: String,
    val status: String,
    val images: List<String> = emptyList(),
    val categoryId: String? = null,
    val categoryName: String? = null,
    val auctionEndTime: Long? = null,
    val pickupLocation: String = "STC",
    val createdAt: Long
)

@Serializable
data class BidDto(
    val id: String,
    val itemId: String,
    val bidderId: String,
    val bidderName: String? = null,
    val amount: Double,
    val timestamp: Long
)

@Serializable
data class NotificationDto(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val itemId: String? = null,
    val timestamp: Long
)

// Request DTOs
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val role: String = "BUYER"
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class CreateItemRequest(
    val title: String,
    val description: String,
    val price: Double? = null,
    val startingBid: Double? = null,
    val itemType: String,
    val images: List<String> = emptyList(),
    val categoryId: String? = null,
    val auctionEndTime: Long? = null,
    val pickupLocation: String = "STC"
)

@Serializable
data class PlaceBidRequest(
    val amount: Double
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

