package com.practice

import com.practice.domain.ExerciseType
import com.practice.dto.ExerciseSetCreateRequest
import com.practice.dto.ExerciseSetResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

class MultipleChoiceExerciseTest : IntegrationTestBase() {

    private val defaultTeacherId = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @Test
    fun `should create and retrieve multiple choice exercise`() {
        val createSetRequest = ExerciseSetCreateRequest(
            teacherId = defaultTeacherId,
            title = "Multiple Choice Test",
            type = ExerciseType.MULTIPLE_CHOICE,
            bulkInput = "Mañana ____ al cine. [iremos] {vamos|iremos|fuimos|íbamos}"
        )

        val createResponse = restTemplate.postForEntity(url("/api/exercise-sets"), createSetRequest, ExerciseSetResponse::class.java)

        assertThat(createResponse.statusCode).isEqualTo(HttpStatus.OK)
        val exerciseSet = createResponse.body!!
        assertThat(exerciseSet.type).isEqualTo(ExerciseType.MULTIPLE_CHOICE)
        assertThat(exerciseSet.questions).hasSize(1)
        
        val question = exerciseSet.questions[0]
        assertThat(question.prompt).isEqualTo("Mañana ____ al cine.")
        assertThat(question.correctAnswer).isEqualTo("iremos")
        assertThat(question.options).containsExactly("vamos", "iremos", "fuimos", "íbamos")

        // Retrieve it
        val getResponse = restTemplate.getForEntity(url("/api/exercise-sets/${exerciseSet.id}"), ExerciseSetResponse::class.java)
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body?.questions?.get(0)?.options).containsExactly("vamos", "iremos", "fuimos", "íbamos")
    }

    @Test
    fun `should support multiple choice without explicit placeholder`() {
        val createSetRequest = ExerciseSetCreateRequest(
            teacherId = defaultTeacherId,
            title = "Multiple Choice Test 2",
            type = ExerciseType.MULTIPLE_CHOICE,
            bulkInput = "How are you? [Fine] {Fine|Bad|Okay}"
        )

        val createResponse = restTemplate.postForEntity(url("/api/exercise-sets"), createSetRequest, ExerciseSetResponse::class.java)

        assertThat(createResponse.statusCode).isEqualTo(HttpStatus.OK)
        val exerciseSet = createResponse.body!!
        val question = exerciseSet.questions[0]
        assertThat(question.prompt).isEqualTo("How are you? ___")
        assertThat(question.correctAnswer).isEqualTo("Fine")
        assertThat(question.options).containsExactly("Fine", "Bad", "Okay")
    }

    @Test
    fun `should fail if multiple choice exercise has less than 2 options`() {
        val createSetRequest = ExerciseSetCreateRequest(
            teacherId = defaultTeacherId,
            title = "Invalid MC",
            type = ExerciseType.MULTIPLE_CHOICE,
            bulkInput = "Invalid [answer] {only-one}"
        )

        val response = restTemplate.postForEntity(url("/api/exercise-sets"), createSetRequest, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.get("message").toString()).contains("Multiple choice exercise must have at least 2 options")
    }

    @Test
    fun `should fail if multiple choice exercise options do not contain correct answer`() {
        val createSetRequest = ExerciseSetCreateRequest(
            teacherId = defaultTeacherId,
            title = "Invalid MC",
            type = ExerciseType.MULTIPLE_CHOICE,
            bulkInput = "Invalid [answer] {option1|option2}"
        )

        val response = restTemplate.postForEntity(url("/api/exercise-sets"), createSetRequest, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.get("message").toString()).contains("Options must contain the correct answer")
    }

    @Test
    fun `should fail if multiple choice exercise has multiple answers in one line`() {
        val createSetRequest = ExerciseSetCreateRequest(
            teacherId = defaultTeacherId,
            title = "Invalid MC",
            type = ExerciseType.MULTIPLE_CHOICE,
            bulkInput = "Too many [answers] [here] {answers|here|none}"
        )

        val response = restTemplate.postForEntity(url("/api/exercise-sets"), createSetRequest, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.get("message").toString()).contains("Multiple choice exercise must have exactly one answer")
    }
}
