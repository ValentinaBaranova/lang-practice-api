package com.practice

import com.practice.domain.ExerciseType
import com.practice.dto.ExerciseSetCreateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

class ExerciseCreationErrorTest : IntegrationTestBase() {

    private val defaultTeacherId = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @Test
    fun `creating exercise with invalid bulk input should return clear error message`() {
        val createSetRequest = ExerciseSetCreateRequest(
            teacherId = defaultTeacherId,
            title = "Invalid Exercise",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = """
                Correct [line].
                Invalid line without brackets.
            """.trimIndent()
        )
        
        val response = restTemplate.postForEntity(url("/api/exercise-sets"), createSetRequest, Map::class.java)
        
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        val body = response.body as Map<*, *>
        assertThat(body["message"]?.toString()).contains("Line 2: Each line must contain at least one answer in []")
        assertThat(body["message"]?.toString()).contains("Invalid line without brackets.")
        
        val errors = body["errors"] as List<*>
        assertThat(errors).hasSize(1)
        assertThat(errors[0].toString()).contains("Line 2: Each line must contain at least one answer in []")
    }
}
