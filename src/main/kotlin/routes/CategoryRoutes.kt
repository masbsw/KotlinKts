package com.example.routes

import com.example.repository.CategoryRepository
import com.example.repository.TaskRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRoutes(categoryRepository: CategoryRepository, taskRepository: TaskRepository) {

    route("/api/categories") {

        get {
            val categories = categoryRepository.getAll()
            call.respond(categories)
        }

        post {
            val body = call.receive<Map<String, String>>()
            val name = body["name"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            categoryRepository.create(name)
            call.respond(HttpStatusCode.Created)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val category = categoryRepository.getById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(category)
        }

        get("/{id}/tasks") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val tasks = taskRepository.getByCategory(id)
            call.respond(tasks)
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val body = call.receive<Map<String, String>>()
            val name = body["name"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            categoryRepository.update(id, name)
            call.respond(HttpStatusCode.OK)
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            categoryRepository.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}