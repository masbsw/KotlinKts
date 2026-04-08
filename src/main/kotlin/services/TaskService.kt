package com.example.services

import com.example.exceptions.NotFoundException
import com.example.exceptions.ValidationException
import com.example.models.CreateTaskRequest
import com.example.models.TaskResponse
import com.example.models.UpdateTaskRequest
import com.example.repository.CategoryRepository
import com.example.repository.TaskRepository

class TaskService(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
) {
    suspend fun getAll(userId: Int, categoryId: Int?): List<TaskResponse> {
        if (categoryId != null) {
            ensureCategoryExists(categoryId)
        }
        return taskRepository.getAll(userId, categoryId)
    }

    suspend fun getById(id: Int, userId: Int): TaskResponse =
        taskRepository.getById(id, userId) ?: throw NotFoundException("Task with id=$id not found")

    suspend fun getByCategory(categoryId: Int, userId: Int): List<TaskResponse> {
        ensureCategoryExists(categoryId)
        return taskRepository.getByCategory(categoryId, userId)
    }

    suspend fun create(request: CreateTaskRequest, userId: Int): TaskResponse {
        validate(request.title, request.description, request.status)
        ensureCategoryExists(request.categoryId)
        return taskRepository.create(
            title = request.title.trim(),
            description = request.description.trim(),
            status = request.status.trim().uppercase(),
            categoryId = request.categoryId,
            userId = userId,
        )
    }

    suspend fun update(id: Int, request: UpdateTaskRequest, userId: Int): TaskResponse {
        validate(request.title, request.description, request.status)
        ensureCategoryExists(request.categoryId)
        val updated = taskRepository.update(
            id = id,
            userId = userId,
            title = request.title.trim(),
            description = request.description.trim(),
            status = request.status.trim().uppercase(),
            categoryId = request.categoryId,
        )
        if (!updated) {
            throw NotFoundException("Task with id=$id not found")
        }
        return getById(id, userId)
    }

    suspend fun delete(id: Int, userId: Int) {
        if (!taskRepository.delete(id, userId)) {
            throw NotFoundException("Task with id=$id not found")
        }
    }

    private suspend fun ensureCategoryExists(categoryId: Int) {
        if (categoryRepository.getById(categoryId) == null) {
            throw NotFoundException("Category with id=$categoryId not found")
        }
    }

    private fun validate(title: String, description: String, status: String) {
        if (title.isBlank()) {
            throw ValidationException("Task title is required")
        }
        if (description.isBlank()) {
            throw ValidationException("Task description is required")
        }
        val normalizedStatus = status.trim().uppercase()
        if (normalizedStatus !in setOf("OPEN", "IN_PROGRESS", "DONE")) {
            throw ValidationException("Task status must be one of OPEN, IN_PROGRESS, DONE")
        }
    }
}
