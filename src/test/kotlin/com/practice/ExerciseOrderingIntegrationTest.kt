package com.practice

import com.practice.domain.ExerciseType
import com.practice.dto.ExerciseSetCreateRequest
import com.practice.dto.ExerciseSetResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

class ExerciseOrderingIntegrationTest : IntegrationTestBase() {

    private val defaultTeacherId = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @Test
    fun `exercise list should be ordered by createdAt descending`() {
        // 1. Create first exercise
        val request1 = ExerciseSetCreateRequest(
            teacherId = defaultTeacherId,
            title = "First Exercise",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = "The [first] exercise."
        )
        val response1 = restTemplate.postForEntity(url("/api/exercise-sets"), request1, ExerciseSetResponse::class.java)
        assertThat(response1.statusCode).isEqualTo(HttpStatus.OK)
        val exercise1 = response1.body!!

        // Sleep a bit to ensure different createdAt if necessary, 
        // though @CreationTimestamp should handle it even if fast, 
        // but some databases might have low precision.
        // Actually, in most tests we use H2 or similar, it should be fine.
        Thread.sleep(100)

        // 2. Create second exercise
        val request2 = ExerciseSetCreateRequest(
            teacherId = defaultTeacherId,
            title = "Second Exercise",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = "The [second] exercise."
        )
        val response2 = restTemplate.postForEntity(url("/api/exercise-sets"), request2, ExerciseSetResponse::class.java)
        assertThat(response2.statusCode).isEqualTo(HttpStatus.OK)
        val exercise2 = response2.body!!

        // 3. Get list and verify order
        val listResponse = restTemplate.getForEntity(url("/api/exercise-sets?teacherId=$defaultTeacherId"), Array<ExerciseSetResponse>::class.java)
        assertThat(listResponse.statusCode).isEqualTo(HttpStatus.OK)
        val list = listResponse.body!!

        assertThat(list.size).isGreaterThanOrEqualTo(2)
        
        // Find indices of our created exercises in the list
        val index1 = list.indexOfFirst { it.id == exercise1.id }
        val index2 = list.indexOfFirst { it.id == exercise2.id }

        assertThat(index1).isGreaterThan(-1)
        assertThat(index2).isGreaterThan(-1)
        
        // Second exercise should be BEFORE first exercise (new on top)
        assertThat(index2).isLessThan(index1)
    }
}
