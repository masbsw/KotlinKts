package com.example.plugins

import com.example.exceptions.ApiException
import com.example.models.ErrorResponse
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.statusCode, ErrorResponse(cause.message))
        }
        exception<ContentConvertException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
        }
        exception<ExposedSQLException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.localizedMessage ?: "Database error"))
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(cause.localizedMessage ?: "Internal server error"),
            )
        }
    }
}
