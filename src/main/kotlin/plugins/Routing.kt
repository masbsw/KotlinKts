package com.example.plugins

import com.example.auth.AuthService
import com.example.auth.JwtConfig
import com.example.repository.CategoryRepository
import com.example.repository.TaskRepository
import com.example.repository.UserRepository
import com.example.routes.authRoutes
import com.example.routes.categoryRoutes
import com.example.routes.taskRoutes
import com.example.services.CategoryService
import com.example.services.TaskService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(jwtConfig: JwtConfig) {
    val userRepository = UserRepository()
    val categoryRepository = CategoryRepository()
    val taskRepository = TaskRepository()

    val authService = AuthService(userRepository, jwtConfig)
    val categoryService = CategoryService(categoryRepository)
    val taskService = TaskService(taskRepository, categoryRepository)

    routing {
        authRoutes(authService)
        categoryRoutes(categoryService, taskService)
        taskRoutes(taskService)
    }
}
