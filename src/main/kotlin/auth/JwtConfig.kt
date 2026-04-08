package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import java.util.Date

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expiresInMs: Long = 3_600_000,
) {
    private val algorithm = Algorithm.HMAC256(secret)

    fun verifier(): JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(userId: Int, email: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + expiresInMs))
            .sign(algorithm)

    companion object {
        fun fromConfig(config: ApplicationConfig): JwtConfig {
            val jwtConfig = config.config("jwt")
            return JwtConfig(
                secret = jwtConfig.property("secret").getString(),
                issuer = jwtConfig.property("issuer").getString(),
                audience = jwtConfig.property("audience").getString(),
                realm = jwtConfig.property("realm").getString(),
            )
        }
    }
}
