package com.example.repository

import com.example.database.dbQuery
import com.example.models.TaskResponse
import com.example.models.Tasks
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class TaskRepository {
    suspend fun getAll(userId: Int, categoryId: Int?): List<TaskResponse> = dbQuery {
        val condition = if (categoryId != null) {
            (Tasks.createdBy eq userId) and (Tasks.categoryId eq categoryId)
        } else {
            Tasks.createdBy eq userId
        }

        Tasks.selectAll()
            .where { condition }
            .map(::toTaskResponse)
    }

    suspend fun getById(id: Int, userId: Int): TaskResponse? = dbQuery {
        Tasks.selectAll()
            .where { (Tasks.id eq id) and (Tasks.createdBy eq userId) }
            .map(::toTaskResponse)
            .singleOrNull()
    }

    suspend fun getByCategory(categoryId: Int, userId: Int): List<TaskResponse> = dbQuery {
        Tasks.selectAll()
            .where { (Tasks.categoryId eq categoryId) and (Tasks.createdBy eq userId) }
            .map(::toTaskResponse)
    }

    suspend fun create(
        title: String,
        description: String,
        status: String,
        categoryId: Int,
        userId: Int,
    ): TaskResponse = dbQuery {
        val id = Tasks.insertAndGetId {
            it[Tasks.title] = title
            it[Tasks.description] = description
            it[Tasks.status] = status
            it[Tasks.categoryId] = categoryId
            it[Tasks.createdBy] = userId
        }
        TaskResponse(id.value, title, description, status, categoryId, userId)
    }

    suspend fun update(
        id: Int,
        userId: Int,
        title: String,
        description: String,
        status: String,
        categoryId: Int,
    ): Boolean = dbQuery {
        Tasks.update({ (Tasks.id eq id) and (Tasks.createdBy eq userId) }) {
            it[Tasks.title] = title
            it[Tasks.description] = description
            it[Tasks.status] = status
            it[Tasks.categoryId] = categoryId
        } > 0
    }

    suspend fun delete(id: Int, userId: Int): Boolean = dbQuery {
        Tasks.deleteWhere { (Tasks.id eq id) and (Tasks.createdBy eq userId) } > 0
    }

    private fun toTaskResponse(row: ResultRow) = TaskResponse(
        id = row[Tasks.id].value,
        title = row[Tasks.title],
        description = row[Tasks.description],
        status = row[Tasks.status],
        categoryId = row[Tasks.categoryId].value,
        createdBy = row[Tasks.createdBy].value,
    )
}
