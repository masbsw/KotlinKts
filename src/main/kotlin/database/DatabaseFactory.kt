package com.example.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.example.models.Categories
import com.example.models.Tasks
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

object DatabaseFactory {

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

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        transaction {

            SchemaUtils.create(Categories, Tasks)

            if (Categories.selectAll().empty()) {

                val work = Categories.insertAndGetId {
                    it[name] = "Work"
                }

                val home = Categories.insertAndGetId {
                    it[name] = "Home"
                }

                val study = Categories.insertAndGetId {
                    it[name] = "Study"
                }

                repeat(5) {
                    Tasks.insert {
                        it[title] = "Task $it"
                        it[categoryId] = work
                    }
                }
            }
        }
    }
}