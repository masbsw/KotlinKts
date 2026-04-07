package com.example.repository

import com.example.database.dbQuery
import com.example.models.Categories
import com.example.models.CategoryDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class CategoryRepository {

    suspend fun getAll(): List<CategoryDto> = dbQuery {
        Categories.selectAll().map {
            CategoryDto(
                id = it[Categories.id].value,
                name = it[Categories.name]
            )
        }
    }

    suspend fun getById(id: Int): CategoryDto? = dbQuery {
        Categories.select { Categories.id eq id }
            .map {
                CategoryDto(
                    id = it[Categories.id].value,
                    name = it[Categories.name]
                )
            }
            .singleOrNull()
    }

    suspend fun create(name: String) = dbQuery {
        Categories.insert {
            it[Categories.name] = name
        }
    }

    suspend fun update(id: Int, name: String) = dbQuery {
        Categories.update({ Categories.id eq id }) {
            it[Categories.name] = name
        }
    }

    suspend fun delete(id: Int) = dbQuery {
        Categories.deleteWhere { Categories.id eq id }
    }
}