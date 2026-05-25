package com.practice

import com.practice.domain.ExerciseType
import com.practice.domain.ExerciseVisibility
import com.practice.dto.ExerciseSetCreateRequest
import com.practice.dto.ExerciseSetResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.util.UUID

class ExerciseVisibilityIntegrationTest : IntegrationTestBase() {

    @Test
    fun `should only return public exercises in public endpoint`() {
        // 1. Create a private exercise
        val privateRequest = ExerciseSetCreateRequest(
            title = "Private Exercise",
            type = ExerciseType.FILL_GAP_TEXT,
            visibility = ExerciseVisibility.PRIVATE,
            bulkInput = "This is [private]."
        )
        val privateResponse = restTemplate.postForEntity(url("/api/exercise-sets"), HttpEntity(privateRequest, authHeaders()), ExerciseSetResponse::class.java)
        assertThat(privateResponse.statusCode).isEqualTo(HttpStatus.OK)
        val privateExercise = privateResponse.body!!

        // 2. Create a public exercise
        val publicRequest = ExerciseSetCreateRequest(
            title = "Public Exercise",
            type = ExerciseType.FILL_GAP_TEXT,
            visibility = ExerciseVisibility.PUBLIC,
            bulkInput = "This is [public]."
        )
        val publicResponse = restTemplate.postForEntity(url("/api/exercise-sets"), HttpEntity(publicRequest, authHeaders()), ExerciseSetResponse::class.java)
        assertThat(publicResponse.statusCode).isEqualTo(HttpStatus.OK)
        val publicExercise = publicResponse.body!!

        // 3. Get public exercises
        val publicListResponse = restTemplate.getForEntity(url("/api/exercise-sets/public"), Map::class.java)
        assertThat(publicListResponse.statusCode).isEqualTo(HttpStatus.OK)
        val publicBody = publicListResponse.body!!
        val publicContent = publicBody["content"] as List<Map<String, Any>>
        
        // 4. Verify list
        val publicItem = publicContent.find { it["id"] == publicExercise.id.toString() }
        assertThat(publicItem).isNotNull
        assertThat(publicItem!!["teacherName"]).isNull()
        assertThat(publicContent.any { it["id"] == privateExercise.id.toString() }).isFalse()
        
        // 5. Verify teacher list still shows both (or at least the teacher's ones)
        val teacherListResponse = restTemplate.exchange(
            url("/api/exercise-sets"),
            HttpMethod.GET,
            HttpEntity<Nothing>(authHeaders()),
            Map::class.java
        )
        val teacherBody = teacherListResponse.body!!
        val teacherContent = teacherBody["content"] as List<Map<String, Any>>
        assertThat(teacherContent.any { it["id"] == publicExercise.id.toString() }).isTrue()
        assertThat(teacherContent.any { it["id"] == privateExercise.id.toString() }).isTrue()
    }
}
