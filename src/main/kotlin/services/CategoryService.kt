package com.example.services

import com.example.exceptions.ConflictException
import com.example.exceptions.NotFoundException
import com.example.exceptions.ValidationException
import com.example.models.CategoryResponse
import com.example.models.CreateCategoryRequest
import com.example.models.UpdateCategoryRequest
import com.example.repository.CategoryRepository
import org.jetbrains.exposed.exceptions.ExposedSQLException

class CategoryService(
    private val categoryRepository: CategoryRepository,
) {
    suspend fun getAll(): List<CategoryResponse> = categoryRepository.getAll()

    suspend fun getById(id: Int): CategoryResponse =
        categoryRepository.getById(id) ?: throw NotFoundException("Category with id=$id not found")

    suspend fun create(request: CreateCategoryRequest): CategoryResponse {
        validate(request.name, request.description)
        return try {
            categoryRepository.create(request.name.trim(), request.description.trim())
        } catch (_: ExposedSQLException) {
            throw ConflictException("Category with name ${request.name} already exists")
        }
    }

    suspend fun update(id: Int, request: UpdateCategoryRequest): CategoryResponse {
        validate(request.name, request.description)
        val updated = try {
            categoryRepository.update(id, request.name.trim(), request.description.trim())
        } catch (_: ExposedSQLException) {
            throw ConflictException("Category with name ${request.name} already exists")
        }
        if (!updated) {
            throw NotFoundException("Category with id=$id not found")
        }
        return getById(id)
    }

    suspend fun delete(id: Int) {
        if (!categoryRepository.delete(id)) {
            throw NotFoundException("Category with id=$id not found")
        }
    }

    private fun validate(name: String, description: String) {
        if (name.isBlank()) {
            throw ValidationException("Category name is required")
        }
        if (description.isBlank()) {
            throw ValidationException("Category description is required")
        }
    }
}
