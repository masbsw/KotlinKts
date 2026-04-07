package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: Int,
    val title: String,
    val categoryId: Int? = null
)