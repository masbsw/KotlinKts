package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable("users") {
    val fullName = varchar("full_name", 120)
    val email = varchar("email", 150).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
}

data class UserRecord(
    val id: Int,
    val fullName: String,
    val email: String,
    val passwordHash: String,
)

@Serializable
data class UserResponse(
    val id: Int,
    val fullName: String,
    val email: String,
)
