package com.practice

import com.practice.service.AiService
import com.practice.service.GoogleTokenService
import com.practice.service.GoogleUserInfo
import com.practice.service.TelegramBotService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.`when`
import org.springframework.ai.chat.model.ChatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.HttpHeaders
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.testcontainers.containers.PostgreSQLContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
abstract class IntegrationTestBase {
    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var restTemplate: TestRestTemplate

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @MockitoBean
    protected lateinit var googleTokenService: GoogleTokenService

    @MockitoBean
    protected lateinit var chatModel: ChatModel

    @MockitoSpyBean
    protected lateinit var aiService: AiService

    @MockitoSpyBean
    protected lateinit var telegramBotService: TelegramBotService

    protected fun url(path: String) = "http://localhost:$port$path"

    @BeforeEach
    fun setup() {
        cleanupDatabase()
        setupMockAuth()
    }

    private fun cleanupDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE attempt_question, attempt, exercise_set, telegram_user, teacher CASCADE")
    }

    fun setupMockAuth() {
        `when`(googleTokenService.verify("valid-token")).thenReturn(
            GoogleUserInfo("test@example.com", "Test Teacher")
        )
    }

    protected fun authHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.setBearerAuth("valid-token")
        return headers
    }

    companion object {
        @ServiceConnection
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("lang_practice")
            .withUsername("postgres")
            .withPassword("postgres")
            .apply { start() }
    }
}
