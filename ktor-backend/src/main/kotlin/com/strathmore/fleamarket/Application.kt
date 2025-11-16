package com.strathmore.fleamarket

import com.strathmore.fleamarket.database.DatabaseFactory
import com.strathmore.fleamarket.routes.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Initialize database
    DatabaseFactory.init()
    
    // Configure CORS for Android app
    install(CORS) {
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        allowHeader("Authorization")
        anyHost() // Allow all hosts for development
    }
    
    // JSON serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    // Routes
    routing {
        authRoutes()
        itemRoutes()
        bidRoutes()
        notificationRoutes()
        categoryRoutes()
        adminRoutes()
                uploadRoutes()

        // Serve uploaded images from local disk
        staticFiles("/uploads", File("uploads"))
        
        // Health check
        get("/health") {
            call.respond(io.ktor.http.HttpStatusCode.OK, mapOf("status" to "ok"))
        }
    }
}

