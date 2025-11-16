package com.strathmore.fleamarket.routes

import com.strathmore.fleamarket.models.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.categoryRoutes() {
    route("/api/categories") {
        // Get all categories
        get {
            try {
                val categories = transaction {
                    Category.all().map { category ->
                        CategoryDto(
                            id = category.id.value.toString(),
                            name = category.name,
                            description = category.description
                        )
                    }
                }
                
                call.respond(ApiResponse(success = true, data = categories))
            } catch (e: Exception) {
                call.respond(
                    ApiResponse<List<CategoryDto>>(
                        success = false,
                        message = "Failed to load categories: ${e.message}"
                    )
                )
            }
        }
    }
}

