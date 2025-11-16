package com.strathmore.fleamarket.database

import com.strathmore.fleamarket.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {
    fun init() {
        val dataDir = File("data").apply { mkdirs() }
        val driverClassName = "org.sqlite.JDBC"
        val jdbcURL = "jdbc:sqlite:${File(dataDir, "marketplace.db").absolutePath}"
        
        Database.connect(jdbcURL, driverClassName)
        
        transaction {
            // Create tables
            SchemaUtils.create(Users, Categories, Items, Bids, Notifications)
            
            // Preload sample data
            preloadData()
        }
    }
    
    private fun preloadData() {
        // Preload categories
        val defaultCategories = listOf(
            "Electronics" to "Electronics and gadgets",
            "Books" to "Books and stationery",
            "Clothing" to "Clothing and accessories",
            "Jewellery" to "Jewellery and watches",
            "Other" to "Other items"
        )
        defaultCategories.forEach { (name, descriptionText) ->
            Category.find { Categories.name eq name }.firstOrNull() ?: Category.new {
                this.name = name
                description = descriptionText
            }
        }
        
        // Preload admin user plus demo buyer/seller for shared testing
        val admin = User.find { Users.email eq "admin@strathmore.edu" }.firstOrNull()
            ?: User.new {
                email = "admin@strathmore.edu"
                firstName = "Admin"
                lastName = "User"
                password = "admin123" // In production, hash this
                role = UserRole.ADMIN
                status = UserStatus.APPROVED
            }
        
        val seller = User.find { Users.email eq "seller@strathmore.edu" }.firstOrNull()
            ?: User.new {
                email = "seller@strathmore.edu"
                firstName = "Demo"
                lastName = "Seller"
                password = "password"
                role = UserRole.SELLER
                status = UserStatus.APPROVED
            }
        
        User.find { Users.email eq "buyer@strathmore.edu" }.firstOrNull()
            ?: User.new {
                email = "buyer@strathmore.edu"
                firstName = "Demo"
                lastName = "Buyer"
                password = "password"
                role = UserRole.BUYER
                status = UserStatus.APPROVED
            }
        
        // Shared sample item so every user sees content immediately
        if (Item.all().empty()) {
            val electronics = Category.find { Categories.name eq "Electronics" }.first()
            Item.new {
                sellerId = seller
                title = "Lenovo ThinkPad X1 Carbon"
                description = "Lightly used ThinkPad with 16GB RAM and 512GB SSD. Perfect for coursework."
                price = 450.0
                startingBid = 300.0
                currentBid = 0.0
                condition = ItemCondition.GOOD
                itemType = ItemType.FIXED_PRICE
                status = ItemStatus.ACTIVE
                images = """["https://images.unsplash.com/photo-1517336714731-489689fd1ca8"]"""
                categoryId = electronics
                pickupLocation = "STC"
                createdAt = System.currentTimeMillis()
            }
        }
    }
}

