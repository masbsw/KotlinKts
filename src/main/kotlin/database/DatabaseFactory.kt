package com.example.database

import com.example.models.Categories
import com.example.models.Tasks
import com.example.models.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object DatabaseFactory {
    var database: Database? = null
        private set
    private var dataSource: HikariDataSource? = null

    fun init(config: ApplicationConfig) {
        val db = config.config("database")

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = db.property("url").getString()
            driverClassName = db.property("driver").getString()
            username = db.property("user").getString()
            password = db.property("password").getString()
            maximumPoolSize = 3
            isAutoCommit = false
        }

        close()
        val dataSource = HikariDataSource(hikariConfig)
        this.dataSource = dataSource
        val database = Database.connect(dataSource)
        this.database = database

        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Users, Categories, Tasks)
            seedData()
        }
    }

    fun close() {
        database = null
        dataSource?.close()
        dataSource = null
    }

    private fun seedData() {
        if (!Users.selectAll().empty()) return

        val adminId = Users.insertAndGetId {
            it[fullName] = "Demo Admin"
            it[email] = "admin@example.com"
            it[passwordHash] = BCrypt.hashpw("password123", BCrypt.gensalt())
        }

        val workId = Categories.insertAndGetId {
            it[name] = "Work"
            it[description] = "Tasks related to job and deadlines"
        }
        val studyId = Categories.insertAndGetId {
            it[name] = "Study"
            it[description] = "Learning plans and homework"
        }
        val homeId = Categories.insertAndGetId {
            it[name] = "Home"
            it[description] = "Household chores and maintenance"
        }

        val seedTasks = listOf(
            Triple("Prepare sprint report", workId, "OPEN"),
            Triple("Review pull request", workId, "IN_PROGRESS"),
            Triple("Read Ktor docs", studyId, "OPEN"),
            Triple("Finish homework", studyId, "DONE"),
            Triple("Clean kitchen", homeId, "DONE"),
            Triple("Buy groceries", homeId, "OPEN"),
        )

        seedTasks.forEachIndexed { index, (title, categoryId, status) ->
            Tasks.insertAndGetId {
                it[Tasks.title] = title
                it[Tasks.description] = "Seed task ${index + 1}: $title"
                it[Tasks.status] = status
                it[Tasks.categoryId] = categoryId
                it[Tasks.createdBy] = adminId
            }
        }
    }
}
