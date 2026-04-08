package com.example

import com.example.auth.JwtConfig
import com.example.database.DatabaseFactory
import com.example.plugins.configureCors
import com.example.plugins.configureLogging
import io.ktor.server.application.*
import com.example.plugins.configureRouting
import com.example.plugins.configureSecurity
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val jwtConfig = JwtConfig.fromConfig(environment.config)
    DatabaseFactory.init(environment.config)
    configureLogging()
    configureSerialization()
    configureCors()
    configureSecurity(jwtConfig)
    configureStatusPages()
    configureRouting(jwtConfig)
}
