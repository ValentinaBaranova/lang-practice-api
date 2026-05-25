package com.practice

import com.practice.domain.ExerciseType
import com.practice.dto.ExerciseSetCreateRequest
import com.practice.dto.ExerciseSetResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.util.UUID

class ExerciseOrderingIntegrationTest : IntegrationTestBase() {

    @Test
    fun `exercise list should be ordered by createdAt descending`() {
        // 1. Create first exercise
        val request1 = ExerciseSetCreateRequest(
            title = "First Exercise",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = "The [first] exercise."
        )
        val response1 = restTemplate.postForEntity(url("/api/exercise-sets"), HttpEntity(request1, authHeaders()), ExerciseSetResponse::class.java)
        assertThat(response1.statusCode).isEqualTo(HttpStatus.OK)
        val exercise1 = response1.body!!

        // Sleep a bit to ensure different createdAt if necessary, 
        // though @CreationTimestamp should handle it even if fast, 
        // but some databases might have low precision.
        // Actually, in most tests we use H2 or similar, it should be fine.
        Thread.sleep(100)

        // 2. Create second exercise
        val request2 = ExerciseSetCreateRequest(
            title = "Second Exercise",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = "The [second] exercise."
        )
        val response2 = restTemplate.postForEntity(url("/api/exercise-sets"), HttpEntity(request2, authHeaders()), ExerciseSetResponse::class.java)
        assertThat(response2.statusCode).isEqualTo(HttpStatus.OK)
        val exercise2 = response2.body!!

        // 3. Get list and verify order
        val listResponse = restTemplate.exchange(
            url("/api/exercise-sets"),
            HttpMethod.GET,
            HttpEntity<Nothing>(authHeaders()),
            Map::class.java
        )
        assertThat(listResponse.statusCode).isEqualTo(HttpStatus.OK)
        val body = listResponse.body!!
        val content = body["content"] as List<Map<String, Any>>
        
        assertThat(content.size).isGreaterThanOrEqualTo(2)
        
        // Find indices of our created exercises in the list
        val index1 = content.indexOfFirst { it["id"] == exercise1.id.toString() }
        val index2 = content.indexOfFirst { it["id"] == exercise2.id.toString() }

        assertThat(index1).isGreaterThan(-1)
        assertThat(index2).isGreaterThan(-1)
        
        // Second exercise should be BEFORE first exercise (new on top)
        assertThat(index2).isLessThan(index1)
    }
}
