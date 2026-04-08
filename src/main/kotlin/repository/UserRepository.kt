package com.example.repository

import com.example.database.dbQuery
import com.example.models.UserRecord
import com.example.models.UserResponse
import com.example.models.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

class UserRepository {
    suspend fun findByEmail(email: String): UserRecord? = dbQuery {
        Users.selectAll()
            .where { Users.email eq email }
            .map(::toUserRecord)
            .singleOrNull()
    }

    suspend fun getById(id: Int): UserResponse? = dbQuery {
        Users.selectAll()
            .where { Users.id eq id }
            .map {
                UserResponse(
                    id = it[Users.id].value,
                    fullName = it[Users.fullName],
                    email = it[Users.email],
                )
            }
            .singleOrNull()
    }

    suspend fun create(fullName: String, email: String, passwordHash: String): UserResponse = dbQuery {
        val id = Users.insertAndGetId {
            it[Users.fullName] = fullName
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
        }
        UserResponse(id.value, fullName, email)
    }

    private fun toUserRecord(row: ResultRow) = UserRecord(
        id = row[Users.id].value,
        fullName = row[Users.fullName],
        email = row[Users.email],
        passwordHash = row[Users.passwordHash],
    )
}
