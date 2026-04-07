package com.example.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Tasks : IntIdTable() {
    val title = varchar("title", 255)
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.CASCADE)
}