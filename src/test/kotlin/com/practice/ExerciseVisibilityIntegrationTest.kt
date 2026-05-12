package com.practice

import com.practice.domain.ExerciseType
import com.practice.domain.ExerciseVisibility
import com.practice.dto.ExerciseSetCreateRequest
import com.practice.dto.ExerciseSetResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

class ExerciseVisibilityIntegrationTest : IntegrationTestBase() {

    private val defaultTeacherId = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @Test
    fun `should only return public exercises in public endpoint`() {
        // 1. Create a private exercise
        val privateRequest = ExerciseSetCreateRequest(
            teacherId = defaultTeacherId,
            title = "Private Exercise",
            type = ExerciseType.FILL_GAP_TEXT,
            visibility = ExerciseVisibility.PRIVATE,
            bulkInput = "This is [private]."
        )
        val privateResponse = restTemplate.postForEntity(url("/api/exercise-sets"), privateRequest, ExerciseSetResponse::class.java)
        assertThat(privateResponse.statusCode).isEqualTo(HttpStatus.OK)
        val privateExercise = privateResponse.body!!

        // 2. Create a public exercise
        val publicRequest = ExerciseSetCreateRequest(
            teacherId = defaultTeacherId,
            title = "Public Exercise",
            type = ExerciseType.FILL_GAP_TEXT,
            visibility = ExerciseVisibility.PUBLIC,
            bulkInput = "This is [public]."
        )
        val publicResponse = restTemplate.postForEntity(url("/api/exercise-sets"), publicRequest, ExerciseSetResponse::class.java)
        assertThat(publicResponse.statusCode).isEqualTo(HttpStatus.OK)
        val publicExercise = publicResponse.body!!

        // 3. Get public exercises
        val publicListResponse = restTemplate.getForEntity(url("/api/exercise-sets/public"), Array<ExerciseSetResponse>::class.java)
        assertThat(publicListResponse.statusCode).isEqualTo(HttpStatus.OK)
        val publicList = publicListResponse.body!!

        // 4. Verify list
        assertThat(publicList.any { it.id == publicExercise.id }).isTrue()
        assertThat(publicList.any { it.id == privateExercise.id }).isFalse()
        
        // 5. Verify teacher list still shows both (or at least the teacher's ones)
        val teacherListResponse = restTemplate.getForEntity(url("/api/exercise-sets?teacherId=$defaultTeacherId"), Array<ExerciseSetResponse>::class.java)
        val teacherList = teacherListResponse.body!!
        assertThat(teacherList.any { it.id == publicExercise.id }).isTrue()
        assertThat(teacherList.any { it.id == privateExercise.id }).isTrue()
    }
}
