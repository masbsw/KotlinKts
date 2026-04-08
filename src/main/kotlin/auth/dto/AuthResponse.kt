package com.example.auth.dto

import com.example.models.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse,
)
