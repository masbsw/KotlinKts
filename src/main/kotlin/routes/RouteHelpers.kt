package com.example.routes

import com.example.exceptions.UnauthorizedException
import com.example.exceptions.ValidationException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun ApplicationCall.requireIdParameter(name: String): Int =
    parameters[name]?.toIntOrNull() ?: throw ValidationException("Path parameter '$name' must be a number")

fun ApplicationCall.userId(): Int =
    principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("userId")
        ?.asInt()
        ?: throw UnauthorizedException("JWT token is invalid")
