package com.labs.fleamarketapp.api

import com.labs.fleamarketapp.api.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for Ktor backend
 */
interface MarketplaceApi {
    
    // Authentication
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<LoginResponse>>
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>
    
    // Items
    @GET("api/items")
    suspend fun getItems(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("sellerId") sellerId: String? = null
    ): Response<ApiResponse<List<ServerItem>>>
    
    @GET("api/items/{id}")
    suspend fun getItemById(@Path("id") id: String): Response<ApiResponse<ServerItem>>
    
    @POST("api/items")
    suspend fun createItem(
        @Header("X-User-Id") userId: String,
        @Body request: CreateItemRequest
    ): Response<ApiResponse<ServerItem>>
    
    // Bids
    @GET("api/items/{itemId}/bids")
    suspend fun getBidsForItem(@Path("itemId") itemId: String): Response<ApiResponse<List<ServerBid>>>
    
    @POST("api/items/{itemId}/bids")
    suspend fun placeBid(
        @Header("X-User-Id") userId: String,
        @Path("itemId") itemId: String,
        @Body request: PlaceBidRequest
    ): Response<ApiResponse<ServerBid>>
    
    // Categories
    @GET("api/categories")
    suspend fun getCategories(): Response<ApiResponse<List<CategoryDto>>>
    
    // Notifications
    @GET("api/notifications")
    suspend fun getNotifications(
        @Header("X-User-Id") userId: String,
        @Query("unreadOnly") unreadOnly: Boolean = false
    ): Response<ApiResponse<List<NotificationDto>>>
    
    @PUT("api/notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    @PUT("api/notifications/{id}/unread")
    suspend fun markNotificationAsUnread(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>
    
    // Admin
    @GET("api/admin/users")
    suspend fun getAllUsers(
        @Header("X-Admin-Id") adminId: String
    ): Response<ApiResponse<List<ServerUser>>>
    
    @PUT("api/admin/users/{id}/status")
    suspend fun updateUserStatus(
        @Header("X-Admin-Id") adminId: String,
        @Path("id") userId: String,
        @Body request: UpdateStatusRequest
    ): Response<ApiResponse<Unit>>
    
    @GET("api/admin/items")
    suspend fun getAllItemsAdmin(
        @Header("X-Admin-Id") adminId: String
    ): Response<ApiResponse<List<ServerItem>>>
    
    @PUT("api/admin/items/{id}/status")
    suspend fun updateItemStatus(
        @Header("X-Admin-Id") adminId: String,
        @Path("id") itemId: String,
        @Body request: UpdateStatusRequest
    ): Response<ApiResponse<Unit>>
    
    @DELETE("api/admin/items/{id}")
    suspend fun deleteItem(
        @Header("X-Admin-Id") adminId: String,
        @Path("id") itemId: String
    ): Response<ApiResponse<Unit>>
}

