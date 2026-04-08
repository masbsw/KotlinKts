package com.example.routes

import com.example.models.CreateCategoryRequest
import com.example.models.UpdateCategoryRequest
import com.example.services.CategoryService
import com.example.services.TaskService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRoutes(
    categoryService: CategoryService,
    taskService: TaskService,
) {
    authenticate("auth-jwt") {
        route("/api/categories") {
            get {
                call.respond(categoryService.getAll())
            }

            post {
                val request = call.receive<CreateCategoryRequest>()
                call.respond(HttpStatusCode.Created, categoryService.create(request))
            }

            get("/{id}") {
                call.respond(categoryService.getById(call.requireIdParameter("id")))
            }

            get("/{id}/tasks") {
                val categoryId = call.requireIdParameter("id")
                call.respond(taskService.getByCategory(categoryId, call.userId()))
            }

            put("/{id}") {
                val request = call.receive<UpdateCategoryRequest>()
                call.respond(categoryService.update(call.requireIdParameter("id"), request))
            }

            delete("/{id}") {
                categoryService.delete(call.requireIdParameter("id"))
                call.respond(HttpStatusCode.OK, mapOf("message" to "Category deleted"))
            }
        }
    }
}
