package com.practice

import com.practice.domain.ExerciseType
import com.practice.dto.AiGenerateRequest
import com.practice.dto.AiPromptResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class AiControllerTest : IntegrationTestBase() {

    @Test
    fun `should accept generate request for authenticated teacher`() {
        val request = AiGenerateRequest(
            type = ExerciseType.FILL_GAP_TEXT,
            topic = "test",
            amount = 5,
        )

        val entity = HttpEntity(request, authHeaders())
        val response = restTemplate.postForEntity(url("/api/ai/generate"), entity, Map::class.java)

        // In test environment, AI may be mocked to return a simple payload or error string; just assert 200 OK
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.get("content")).isNotNull()
    }

    @Test
    fun `should build exercise prompt`() {
        val entity = HttpEntity<Nothing>(authHeaders())
        val response = restTemplate.exchange(
            url("/api/ai/build-exercise-prompt?type=MULTIPLE_CHOICE&topic=test&amount=5"),
            HttpMethod.GET,
            entity,
            AiPromptResponse::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.prompt).contains("Generate 5 sentences")
        assertThat(response.body?.prompt).contains("multiple choice practice")
    }
}
