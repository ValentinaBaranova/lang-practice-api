package com.practice

import com.practice.dto.AiGenerateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class AiControllerTest : IntegrationTestBase() {

    @Test
    fun `should return 400 if teacher access code is incorrect`() {
        val request = AiGenerateRequest(
            type = "FILL_GAP",
            topic = "test",
            amount = 5,
            teacherAccessCode = "INVALID_CODE"
        )

        val response = restTemplate.postForEntity(url("/api/ai/generate"), request, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.get("message")).isEqualTo("Teacher not found with accessCode: INVALID_CODE")
    }
}
