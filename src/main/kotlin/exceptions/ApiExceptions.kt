package com.example.exceptions

import io.ktor.http.HttpStatusCode

open class ApiException(
    val statusCode: HttpStatusCode,
    override val message: String,
) : RuntimeException(message)

class ValidationException(message: String) : ApiException(HttpStatusCode.BadRequest, message)

class NotFoundException(message: String) : ApiException(HttpStatusCode.NotFound, message)

class ConflictException(message: String) : ApiException(HttpStatusCode.Conflict, message)

class UnauthorizedException(message: String) : ApiException(HttpStatusCode.Unauthorized, message)
