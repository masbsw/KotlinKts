package com.example.routes

import com.example.auth.AuthService
import com.example.auth.dto.LoginRequest
import com.example.auth.dto.RegisterRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/api/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = authService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authService.login(request)
            call.respond(HttpStatusCode.OK, response)
        }

        authenticate("auth-jwt") {
            get("/me") {
                call.respond(authService.getUser(call.userId()))
            }
        }
    }
}
