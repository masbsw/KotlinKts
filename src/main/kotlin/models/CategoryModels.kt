package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

object Categories : IntIdTable("categories") {
    val name = varchar("name", 100).uniqueIndex()
    val description = varchar("description", 255)
}

@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val description: String,
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val description: String,
)

@Serializable
data class UpdateCategoryRequest(
    val name: String,
    val description: String,
)
