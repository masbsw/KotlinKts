package com.example.auth

import com.example.auth.dto.AuthResponse
import com.example.auth.dto.LoginRequest
import com.example.auth.dto.RegisterRequest
import com.example.exceptions.ConflictException
import com.example.exceptions.UnauthorizedException
import com.example.exceptions.ValidationException
import com.example.models.UserResponse
import com.example.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt

class AuthService(
    private val userRepository: UserRepository,
    private val jwtConfig: JwtConfig,
) {
    suspend fun register(request: RegisterRequest): AuthResponse {
        validateRegisterRequest(request)
        val normalizedEmail = request.email.trim().lowercase()

        if (userRepository.findByEmail(normalizedEmail) != null) {
            throw ConflictException("User with email $normalizedEmail already exists")
        }

        val user = userRepository.create(
            fullName = request.fullName.trim(),
            email = normalizedEmail,
            passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt()),
        )

        return AuthResponse(
            token = jwtConfig.generateToken(user.id, user.email),
            user = user,
        )
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        if (request.email.isBlank() || request.password.isBlank()) {
            throw ValidationException("Email and password are required")
        }

        val normalizedEmail = request.email.trim().lowercase()
        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw UnauthorizedException("Invalid email or password")

        if (!BCrypt.checkpw(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid email or password")
        }

        val responseUser = UserResponse(
            id = user.id,
            fullName = user.fullName,
            email = user.email,
        )

        return AuthResponse(
            token = jwtConfig.generateToken(responseUser.id, responseUser.email),
            user = responseUser,
        )
    }

    suspend fun getUser(userId: Int): UserResponse =
        userRepository.getById(userId) ?: throw UnauthorizedException("User from token was not found")

    private fun validateRegisterRequest(request: RegisterRequest) {
        if (request.fullName.isBlank()) {
            throw ValidationException("Full name is required")
        }
        if (request.email.isBlank() || !request.email.contains("@")) {
            throw ValidationException("A valid email is required")
        }
        if (request.password.length < 6) {
            throw ValidationException("Password must contain at least 6 characters")
        }
    }
}
