package com.example

import com.example.database.DatabaseFactory
import com.example.plugins.configureLogging
import com.example.repository.CategoryRepository
import com.example.repository.TaskRepository
import com.example.routes.categoryRoutes
import com.example.routes.taskRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    install(ContentNegotiation) {
        json()
    }

    DatabaseFactory.init(environment.config)
    configureLogging()

    val categoryRepository = CategoryRepository()
    val taskRepository = TaskRepository()

    routing {
        categoryRoutes(categoryRepository, taskRepository)
        taskRoutes(taskRepository)
    }

    install(StatusPages) {
        exception<Exception> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to cause.localizedMessage)
            )
        }
    }
}