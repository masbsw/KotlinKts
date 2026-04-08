package com.example.routes

import com.example.models.CreateTaskRequest
import com.example.models.UpdateTaskRequest
import com.example.services.TaskService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.taskRoutes(taskService: TaskService) {
    authenticate("auth-jwt") {
        route("/api/tasks") {
            get {
                val categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull()
                call.respond(taskService.getAll(call.userId(), categoryId))
            }

            post {
                val request = call.receive<CreateTaskRequest>()
                call.respond(HttpStatusCode.Created, taskService.create(request, call.userId()))
            }

            get("/{id}") {
                call.respond(taskService.getById(call.requireIdParameter("id"), call.userId()))
            }

            put("/{id}") {
                val request = call.receive<UpdateTaskRequest>()
                call.respond(taskService.update(call.requireIdParameter("id"), request, call.userId()))
            }

            delete("/{id}") {
                taskService.delete(call.requireIdParameter("id"), call.userId())
                call.respond(HttpStatusCode.OK, mapOf("message" to "Task deleted"))
            }
        }
    }
}
