package com.practice

import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class IntegrationTestBase {
    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected  lateinit var restTemplate: TestRestTemplate

    protected  fun url(path: String) = "http://localhost:$port$path"

    companion object {
        @Container
        @JvmStatic
        @ServiceConnection
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("lang_practice")
            .withUsername("postgres")
            .withPassword("postgres")

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            // Add Hikari/Flyway tuning while letting @ServiceConnection wire the DataSource
            registry.add("spring.datasource.hikari.maximum-pool-size") { 5 }
            registry.add("spring.datasource.hikari.minimum-idle") { 1 }
            registry.add("spring.datasource.hikari.max-lifetime") { 1800000 } // 30 min
            registry.add("spring.datasource.hikari.keepalive-time") { 300000 } // 5 min

            registry.add("spring.flyway.enabled") { true }
            registry.add("spring.flyway.locations") { "classpath:db/migration" }
            
            // Mock AI
            registry.add("spring.ai.openai.api-key") { "mock" }
            registry.add("spring.ai.openai.chat.options.model") { "gpt-4o-mini" }
        }
    }
}
