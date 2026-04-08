package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Tasks : IntIdTable("tasks") {
    val title = varchar("title", 150)
    val description = varchar("description", 500)
    val status = varchar("status", 30)
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.CASCADE)
    val createdBy = reference("created_by", Users, onDelete = ReferenceOption.CASCADE)
}

@Serializable
data class TaskResponse(
    val id: Int,
    val title: String,
    val description: String,
    val status: String,
    val categoryId: Int,
    val createdBy: Int,
)

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String,
    val status: String,
    val categoryId: Int,
)

@Serializable
data class UpdateTaskRequest(
    val title: String,
    val description: String,
    val status: String,
    val categoryId: Int,
)
