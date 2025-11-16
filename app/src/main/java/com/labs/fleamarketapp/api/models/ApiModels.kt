package com.labs.fleamarketapp.api.models

import com.google.gson.annotations.SerializedName

/**
 * API Data Models matching Ktor backend DTOs
 */

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("role") val role: String = "BUYER"
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("role") val role: String,
    @SerializedName("status") val status: String? = null,
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("reviewCount") val reviewCount: Int = 0
)

data class ServerItem(
    @SerializedName("id") val id: String,
    @SerializedName("sellerId") val sellerId: String,
    @SerializedName("sellerName") val sellerName: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("startingBid") val startingBid: Double? = null,
    @SerializedName("currentBid") val currentBid: Double? = null,
    @SerializedName("itemType") val itemType: String,
    @SerializedName("status") val status: String,
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("categoryId") val categoryId: String? = null,
    @SerializedName("categoryName") val categoryName: String? = null,
    @SerializedName("auctionEndTime") val auctionEndTime: Long? = null,
    @SerializedName("pickupLocation") val pickupLocation: String = "STC",
    @SerializedName("createdAt") val createdAt: Long
)

data class ServerBid(
    @SerializedName("id") val id: String,
    @SerializedName("itemId") val itemId: String,
    @SerializedName("bidderId") val bidderId: String,
    @SerializedName("bidderName") val bidderName: String? = null,
    @SerializedName("amount") val amount: Double,
    @SerializedName("timestamp") val timestamp: Long
)

data class CategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String
)

data class NotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String,
    @SerializedName("isRead") val isRead: Boolean,
    @SerializedName("itemId") val itemId: String? = null,
    @SerializedName("timestamp") val timestamp: Long
)

data class CreateItemRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("startingBid") val startingBid: Double? = null,
    @SerializedName("itemType") val itemType: String,
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("categoryId") val categoryId: String? = null,
    @SerializedName("auctionEndTime") val auctionEndTime: Long? = null,
    @SerializedName("pickupLocation") val pickupLocation: String = "STC"
)

data class PlaceBidRequest(
    @SerializedName("amount") val amount: Double
)

data class ServerUser(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("role") val role: String,
    @SerializedName("status") val status: String,
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("reviewCount") val reviewCount: Int = 0
)

data class UpdateStatusRequest(
    @SerializedName("status") val status: String
)

