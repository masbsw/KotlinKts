package com.example.repository

import com.example.database.dbQuery
import com.example.models.Categories
import com.example.models.CategoryResponse
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class CategoryRepository {
    suspend fun getAll(): List<CategoryResponse> = dbQuery {
        Categories.selectAll().map(::toCategoryResponse)
    }

    suspend fun getById(id: Int): CategoryResponse? = dbQuery {
        Categories.selectAll()
            .where { Categories.id eq id }
            .map(::toCategoryResponse)
            .singleOrNull()
    }

    suspend fun create(name: String, description: String): CategoryResponse = dbQuery {
        val id = Categories.insertAndGetId {
            it[Categories.name] = name
            it[Categories.description] = description
        }
        CategoryResponse(id.value, name, description)
    }

    suspend fun update(id: Int, name: String, description: String): Boolean = dbQuery {
        Categories.update({ Categories.id eq id }) {
            it[Categories.name] = name
            it[Categories.description] = description
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Categories.deleteWhere { Categories.id eq id } > 0
    }

    private fun toCategoryResponse(row: ResultRow) = CategoryResponse(
        id = row[Categories.id].value,
        name = row[Categories.name],
        description = row[Categories.description],
    )
}
