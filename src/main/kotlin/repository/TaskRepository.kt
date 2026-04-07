package com.example.repository

import com.example.database.dbQuery
import com.example.models.Tasks
import com.example.models.TaskDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class TaskRepository {

    suspend fun getAll(): List<TaskDto> = dbQuery {
        Tasks.selectAll().map {
            TaskDto(
                id = it[Tasks.id].value,
                title = it[Tasks.title],
                categoryId = it[Tasks.categoryId].value
            )
        }
    }

    suspend fun getByCategory(categoryId: Int): List<TaskDto> = dbQuery {
        Tasks.select { Tasks.categoryId eq categoryId }
            .map {
                TaskDto(
                    id = it[Tasks.id].value,
                    title = it[Tasks.title],
                    categoryId = it[Tasks.categoryId].value
                )
            }
    }

    suspend fun create(title: String, categoryId: Int) = dbQuery {
        Tasks.insert {
            it[Tasks.title] = title
            it[Tasks.categoryId] = categoryId
        }
    }

    suspend fun update(id: Int, title: String) = dbQuery {
        Tasks.update({ Tasks.id eq id }) {
            it[Tasks.title] = title
        }
    }

    suspend fun delete(id: Int) = dbQuery {
        Tasks.deleteWhere { Tasks.id eq id }
    }
}