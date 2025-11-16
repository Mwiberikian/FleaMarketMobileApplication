package com.strathmore.fleamarket.routes

import com.strathmore.fleamarket.models.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Route.notificationRoutes() {
    route("/api/notifications") {
        // Get user's notifications
        get {
            try {
                val userId = call.request.header("X-User-Id") ?: return@get call.respond(
                    ApiResponse<List<NotificationDto>>(success = false, message = "Authentication required")
                )
                
                val unreadOnly = call.parameters["unreadOnly"]?.toBoolean() ?: false
                
                val notifications = transaction {
                    Notification.find { Notifications.userId eq UUID.fromString(userId) }
                        .toList()
                        .asSequence()
                        .filter { if (unreadOnly) !it.isRead else true }
                        .sortedByDescending { it.timestamp }
                        .take(50)
                        .map { notification -> notification.toDto() }
                        .toList()
                }
                
                call.respond(ApiResponse(success = true, data = notifications))
            } catch (e: Exception) {
                call.respond(
                    ApiResponse<List<NotificationDto>>(
                        success = false,
                        message = "Failed to load notifications: ${e.message}"
                    )
                )
            }
        }
        
        // Mark notification as read
        put("/{id}/read") {
            try {
                val notificationId = call.parameters["id"] ?: return@put call.respond(
                    ApiResponse<Unit>(success = false, message = "Notification ID required")
                )
                
                transaction {
                    val notification = Notification.findById(UUID.fromString(notificationId))
                    if (notification != null) {
                        notification.isRead = true
                    }
                }
                
                call.respond(ApiResponse<Unit>(success = true, message = "Notification marked as read"))
            } catch (e: Exception) {
                call.respond(
                    ApiResponse<Unit>(
                        success = false,
                        message = "Failed to update notification: ${e.message}"
                    )
                )
            }
        }

        // Mark notification as unread
        put("/{id}/unread") {
            try {
                val notificationId = call.parameters["id"] ?: return@put call.respond(
                    ApiResponse<Unit>(success = false, message = "Notification ID required")
                )

                transaction {
                    val notification = Notification.findById(UUID.fromString(notificationId))
                    if (notification != null) {
                        notification.isRead = false
                    }
                }

                call.respond(ApiResponse<Unit>(success = true, message = "Notification marked as unread"))
            } catch (e: Exception) {
                call.respond(
                    ApiResponse<Unit>(
                        success = false,
                        message = "Failed to update notification: ${e.message}"
                    )
                )
            }
        }
    }
}

private fun Notification.toDto(): NotificationDto = NotificationDto(
    id = id.value.toString(),
    userId = userId.id.value.toString(),
    title = title,
    message = message,
    type = type.name,
    isRead = isRead,
    itemId = itemId?.id?.value?.toString(),
    timestamp = timestamp
)

