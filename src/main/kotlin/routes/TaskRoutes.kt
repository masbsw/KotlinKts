package com.example.routes

import com.example.repository.TaskRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.taskRoutes(taskRepository: TaskRepository) {

    route("/api/tasks") {

        get {
            val categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull()
            val tasks = if (categoryId != null) {
                taskRepository.getByCategory(categoryId)
            } else {
                taskRepository.getAll()
            }
            call.respond(tasks)
        }

        post {
            val body = call.receive<Map<String, String>>()
            val title = body["title"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val categoryId = body["categoryId"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)
            taskRepository.create(title, categoryId)
            call.respond(HttpStatusCode.Created)
        }
    }
}