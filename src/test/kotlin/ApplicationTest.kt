package com.example

import com.example.auth.dto.AuthResponse
import com.example.auth.dto.LoginRequest
import com.example.auth.dto.RegisterRequest
import com.example.database.DatabaseFactory
import com.example.models.CategoryResponse
import com.example.models.CreateCategoryRequest
import com.example.models.CreateTaskRequest
import com.example.models.ErrorResponse
import com.example.models.TaskResponse
import com.example.models.UpdateTaskRequest
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class ApplicationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @AfterTest
    fun tearDown() {
        DatabaseFactory.close()
    }

    @Test
    fun registerAndLoginReturnJwtAndAllowProtectedEndpoint() = testApplication {
        configureTestApplication()
        val client = jsonClient()
        val email = uniqueEmail()

        val registerResponse = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("Test User", email, "secret123"))
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)
        val registered = registerResponse.body<AuthResponse>()
        assertTrue(registered.token.isNotBlank())

        val loginResponse = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, "secret123"))
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val token = loginResponse.body<AuthResponse>().token

        val meResponse = client.get("/api/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, meResponse.status)
    }

    @Test
    fun protectedEndpointWithoutTokenReturns401() = testApplication {
        configureTestApplication()
        val response = client.get("/api/tasks")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun invalidLoginReturns401() = testApplication {
        configureTestApplication()
        val client = jsonClient()

        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("missing@example.com", "wrongpass"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun categoryCrudAndTaskFilterWork() = testApplication {
        configureTestApplication()
        val client = jsonClient()
        val token = registerAndLogin(client)

        val categoryCreate = client.post("/api/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(CreateCategoryRequest("Backend", "Server-side tasks"))
        }
        assertEquals(HttpStatusCode.Created, categoryCreate.status)
        val category = categoryCreate.body<CategoryResponse>()

        val taskCreate = client.post("/api/tasks") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(CreateTaskRequest("Write docs", "Document JWT flow", "OPEN", category.id))
        }
        assertEquals(HttpStatusCode.Created, taskCreate.status)

        val filtered = client.get("/api/tasks?categoryId=${category.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, filtered.status)
        val tasks = filtered.body<List<TaskResponse>>()
        assertEquals(1, tasks.size)

        val categoryTasks = client.get("/api/categories/${category.id}/tasks") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, categoryTasks.status)
    }

    @Test
    fun updateAndDeleteTaskWork() = testApplication {
        configureTestApplication()
        val client = jsonClient()
        val token = registerAndLogin(client)
        val categoryId = createCategory(client, token, "Mobile", "Android and iOS")

        val created = client.post("/api/tasks") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(CreateTaskRequest("Build screen", "Implement login screen", "OPEN", categoryId))
        }.body<TaskResponse>()

        val updatedResponse = client.put("/api/tasks/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                UpdateTaskRequest(
                    title = "Build onboarding screen",
                    description = "Implement onboarding flow",
                    status = "DONE",
                    categoryId = categoryId,
                )
            )
        }
        assertEquals(HttpStatusCode.OK, updatedResponse.status)
        assertEquals("DONE", updatedResponse.body<TaskResponse>().status)

        val deleteResponse = client.delete("/api/tasks/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        val afterDelete = client.get("/api/tasks/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, afterDelete.status)
    }

    @Test
    fun validationAndNotFoundCasesReturnExpectedCodes() = testApplication {
        configureTestApplication()
        val client = jsonClient()
        val token = registerAndLogin(client)

        val invalidCreate = client.post("/api/tasks") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(CreateTaskRequest("", "", "WRONG", 999))
        }
        assertEquals(HttpStatusCode.BadRequest, invalidCreate.status)

        val missingTask = client.get("/api/tasks/9999") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, missingTask.status)
        assertTrue(missingTask.body<ErrorResponse>().error.contains("not found"))
    }

    private fun ApplicationTestBuilder.configureTestApplication() {
        environment {
            config = MapApplicationConfig(
                "database.url" to "jdbc:h2:mem:test_${Random.nextInt()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "database.driver" to "org.h2.Driver",
                "database.user" to "sa",
                "database.password" to "",
                "jwt.secret" to "test-secret-12345678901234567890",
                "jwt.issuer" to "test-issuer",
                "jwt.audience" to "test-audience",
                "jwt.realm" to "test-realm",
            )
        }
        application {
            module()
        }
    }

    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private suspend fun registerAndLogin(
        client: io.ktor.client.HttpClient,
    ): String {
        val email = uniqueEmail()
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("Authed User", email, "secret123"))
        }

        val loginResponse = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, "secret123"))
        }

        return loginResponse.body<AuthResponse>().token
    }

    private suspend fun createCategory(
        client: io.ktor.client.HttpClient,
        token: String,
        name: String,
        description: String,
    ): Int {
        val response = client.post("/api/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(CreateCategoryRequest(name, description))
        }
        return response.body<CategoryResponse>().id
    }

    private fun uniqueEmail(): String = "user${Random.nextInt(100000)}@example.com"
}
