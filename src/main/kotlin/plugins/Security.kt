package com.example.plugins

import com.example.auth.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity(jwtConfig: JwtConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(jwtConfig.verifier())
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asInt()
                val email = credential.payload.getClaim("email").asString()
                if (userId != null && !email.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
