package com.practice

import com.practice.dto.AiGenerateRequest
import com.practice.dto.AiPromptResponse
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

    @Test
    fun `should build exercise prompt`() {
        val response = restTemplate.getForEntity(
            url("/api/ai/build-exercise-prompt?type=MULTIPLE_CHOICE&topic=test&amount=5"),
            AiPromptResponse::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.prompt).contains("You are a helpful assistant")
        assertThat(response.body?.prompt).contains("Generate 5 sentences")
        assertThat(response.body?.prompt).contains("multiple choice practice")
    }
}
