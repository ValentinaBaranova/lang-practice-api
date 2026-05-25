package com.practice

import com.practice.service.GoogleTokenService
import com.practice.service.GoogleUserInfo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.HttpHeaders
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
abstract class IntegrationTestBase {
    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var restTemplate: TestRestTemplate

    @MockitoBean
    protected lateinit var googleTokenService: GoogleTokenService

    protected fun url(path: String) = "http://localhost:$port$path"

    @BeforeEach
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
        @Container
        @JvmStatic
        @ServiceConnection
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("lang_practice")
            .withUsername("postgres")
            .withPassword("postgres")
    }
}
