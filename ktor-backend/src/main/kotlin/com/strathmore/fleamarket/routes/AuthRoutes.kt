package com.strathmore.fleamarket.routes

import com.strathmore.fleamarket.models.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Route.authRoutes() {
    route("/api/auth") {
        // Register new user
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                
                // Validate email
                if (!request.email.endsWith("@strathmore.edu", ignoreCase = true)) {
                    call.respond(
                        ApiResponse<UserDto>(
                            success = false,
                            message = "Email must be a Strathmore email (@strathmore.edu)"
                        )
                    )
                    return@post
                }
                
                // Check if user exists
                val existingUser = transaction {
                    User.find { Users.email eq request.email }.firstOrNull()
                }
                
                if (existingUser != null) {
                    call.respond(
                        ApiResponse<UserDto>(
                            success = false,
                            message = "User with this email already exists"
                        )
                    )
                    return@post
                }
                
                // Create user
                val user = transaction {
                    User.new {
                        email = request.email
                        firstName = request.firstName
                        lastName = request.lastName
                        password = request.password // In production, hash this
                        phone = request.phone
                        profileImageUrl = request.profileImageUrl
                        role = UserRole.valueOf(request.role.uppercase())
                        status = UserStatus.PENDING // Requires admin approval
                    }
                }
                
                call.respond(
                    ApiResponse(
                        success = true,
                        data = UserDto(
                            id = user.id.value.toString(),
                            email = user.email,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            phone = user.phone,
                            profileImageUrl = user.profileImageUrl,
                            role = user.role.name,
                            status = user.status.name,
                            rating = user.rating,
                            reviewCount = user.reviewCount
                        ),
                        message = "Registration successful. Account pending approval."
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    ApiResponse<UserDto>(
                        success = false,
                        message = "Registration failed: ${e.message}"
                    )
                )
            }
        }
        
        // Login user
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                
                val user = transaction {
                    User.find { Users.email eq request.email }.firstOrNull()
                }
                
                if (user == null || user.password != request.password) {
                    call.respond(
                        ApiResponse<UserDto>(
                            success = false,
                            message = "Invalid email or password"
                        )
                    )
                    return@post
                }
                
                if (user.status != UserStatus.APPROVED) {
                    call.respond(
                        ApiResponse<UserDto>(
                            success = false,
                            message = "Account is pending approval"
                        )
                    )
                    return@post
                }
                
                call.respond(
                    ApiResponse(
                        success = true,
                        data = UserDto(
                            id = user.id.value.toString(),
                            email = user.email,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            phone = user.phone,
                            profileImageUrl = user.profileImageUrl,
                            role = user.role.name,
                            status = user.status.name,
                            rating = user.rating,
                            reviewCount = user.reviewCount
                        ),
                        message = "Login successful"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    ApiResponse<UserDto>(
                        success = false,
                        message = "Login failed: ${e.message}"
                    )
                )
            }
        }
    }
}

