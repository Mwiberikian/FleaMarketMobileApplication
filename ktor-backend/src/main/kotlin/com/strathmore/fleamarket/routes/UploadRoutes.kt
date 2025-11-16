package com.strathmore.fleamarket.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.*
import com.strathmore.fleamarket.models.ApiResponse

fun Route.uploadRoutes() {
    route("/api/upload") {
        post {
            try {
                val multipart = call.receiveMultipart()
                val baseDir = File("uploads").apply { mkdirs() }
                val savedUrls = mutableListOf<String>()

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val ext = File(part.originalFileName ?: "image.jpg").extension.ifBlank { "jpg" }
                            val fileName = "${UUID.randomUUID()}.$ext"
                            val file = File(baseDir, fileName)
                            part.streamProvider().use { input ->
                                file.outputStream().use { output -> input.copyTo(output) }
                            }
                            val host = call.request.host()
                            val port = call.request.port()
                            val url = "http://$host:$port/uploads/$fileName"
                            savedUrls += url
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (savedUrls.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse<List<String>>(success = false, message = "No files uploaded"))
                } else {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = savedUrls))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse<List<String>>(success = false, message = (e.message ?: "Upload failed")))
            }
        }
    }
}

