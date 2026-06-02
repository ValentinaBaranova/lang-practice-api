package com.practice.controller

import com.practice.IntegrationTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class TopicsControllerTest : IntegrationTestBase() {

    @Test
    fun `test getTopics returns topics from configuration`() {
        val response = restTemplate.getForEntity(url("/api/topics"), List::class.java)
        
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val topics = response.body as List<*>
        assertThat(topics).isNotEmpty
        assertThat(topics).contains("Presente", "Pretérito Indefinido")
    }
}
