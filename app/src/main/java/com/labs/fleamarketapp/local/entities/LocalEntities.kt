package com.labs.fleamarketapp.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local entities for user-specific data stored in Room
 * These are NOT synced to server - they're private to each user
 */

@Entity(tableName = "local_items")
data class LocalItem(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val price: Double?,
    val categoryId: Long?,
    val images: List<String>,
    val itemType: ItemType,
    val condition: ItemCondition,
    val isDraft: Boolean = true, // true = draft, false = pending sync
    val createdAt: Long,
    val lastModified: Long
)

@Entity(tableName = "draft_bids")
data class DraftBid(
    @PrimaryKey val id: String,
    val itemId: String,
    val userId: String,
    val amount: Double,
    val createdAt: Long,
    val synced: Boolean = false // true when successfully synced to server
)

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey val userId: String,
    val favoriteCategories: List<Long> = emptyList(), // category IDs
    val notificationSettings: String, // JSON string for notification preferences
    val lastSyncTimestamp: Long = 0
)

@Entity(tableName = "shopping_cart")
data class CartItem(
    @PrimaryKey val id: String,
    val userId: String,
    val itemId: String,
    val quantity: Int = 1,
    val addedAt: Long
)

