package com.practice

import com.practice.domain.ExerciseType
import com.practice.dto.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.util.UUID

class ExerciseFlowIntegrationTest : IntegrationTestBase() {

    @Test
    fun `teacher creates exercise and student submits correct answer`() {
        // 1. Create ExerciseSet
        val createSetRequest = ExerciseSetCreateRequest(
            title = "Test Exercise Set",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = """
                The [quick] brown fox.
                The [lazy] dog.
            """.trimIndent()
        )
        val createSetResponse = restTemplate.postForEntity(url("/api/exercise-sets"), HttpEntity(createSetRequest, authHeaders()), ExerciseSetResponse::class.java)
        assertThat(createSetResponse.statusCode).isEqualTo(HttpStatus.OK)
        val exerciseSet = createSetResponse.body!!
        assertThat(exerciseSet.title).isEqualTo("Test Exercise Set")
        assertThat(exerciseSet.questions).hasSize(2)

        // 2. Get ExerciseSet by ID
        val getSetResponse = restTemplate.exchange(url("/api/exercise-sets/${exerciseSet.id}"), HttpMethod.GET, HttpEntity<Nothing>(authHeaders()), ExerciseSetResponse::class.java)
        assertThat(getSetResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getSetResponse.body?.id).isEqualTo(exerciseSet.id)

        // 3. Create Attempt
        val createAttemptRequest = AttemptCreateRequest(
            exerciseSetId = exerciseSet.id,
            studentName = "John Doe"
        )
        val createAttemptResponse = restTemplate.postForEntity(url("/api/attempts"), createAttemptRequest, AttemptResponse::class.java)
        assertThat(createAttemptResponse.statusCode).isEqualTo(HttpStatus.OK)
        val attempt = createAttemptResponse.body!!
        assertThat(attempt.exerciseSetId).isEqualTo(exerciseSet.id)
        assertThat(attempt.studentName).isEqualTo("John Doe")

        // 4. Submit an answer
        val questionId = exerciseSet.questions[0].id!!
        val answerRequest = QuestionAnswerRequest(
            questionId = questionId,
            answer = "The quick brown fox."
        )
        val submitAnswerResponse = restTemplate.postForEntity(url("/api/attempts/${attempt.id}/answers"), answerRequest, AttemptQuestionResponse::class.java)
        assertThat(submitAnswerResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(submitAnswerResponse.body?.isCorrect).isTrue()

        // 5. Verify attempt progress
        val updatedAttemptResponse = restTemplate.getForEntity(url("/api/attempts/${attempt.id}"), AttemptResponse::class.java)
        assertThat(updatedAttemptResponse.statusCode).isEqualTo(HttpStatus.OK)
        val updatedAttempt = updatedAttemptResponse.body!!
        assertThat(updatedAttempt.answeredQuestions).isEqualTo(1)
        assertThat(updatedAttempt.correctAnswers).isEqualTo(1)
    }

    @Test
    fun `teacher creates exercise and student submits incorrect answer`() {
        // 1. Create ExerciseSet
        val createSetRequest = ExerciseSetCreateRequest(
            title = "Test Exercise Set",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = "The [quick] brown fox."
        )
        val createSetResponse = restTemplate.postForEntity(url("/api/exercise-sets"), HttpEntity(createSetRequest, authHeaders()), ExerciseSetResponse::class.java)
        assertThat(createSetResponse.statusCode).isEqualTo(HttpStatus.OK)
        val exerciseSet = createSetResponse.body!!

        // 2. Create Attempt
        val createAttemptRequest = AttemptCreateRequest(
            exerciseSetId = exerciseSet.id,
            studentName = "Jane Doe"
        )
        val createAttemptResponse = restTemplate.postForEntity(url("/api/attempts"), createAttemptRequest, AttemptResponse::class.java)
        assertThat(createAttemptResponse.statusCode).isEqualTo(HttpStatus.OK)
        val attempt = createAttemptResponse.body!!

        // 3. Submit an incorrect answer
        val questionId = exerciseSet.questions[0].id!!
        val answerRequest = QuestionAnswerRequest(
            questionId = questionId,
            answer = "The slow brown fox."
        )
        val submitAnswerResponse = restTemplate.postForEntity(url("/api/attempts/${attempt.id}/answers"), answerRequest, AttemptQuestionResponse::class.java)
        assertThat(submitAnswerResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(submitAnswerResponse.body?.isCorrect).isFalse()

        // 4. Verify attempt progress
        val updatedAttemptResponse = restTemplate.getForEntity(url("/api/attempts/${attempt.id}"), AttemptResponse::class.java)
        assertThat(updatedAttemptResponse.statusCode).isEqualTo(HttpStatus.OK)
        val updatedAttempt = updatedAttemptResponse.body!!
        assertThat(updatedAttempt.answeredQuestions).isEqualTo(1)
        assertThat(updatedAttempt.correctAnswers).isEqualTo(0)
    }

    @Test
    fun `student submits answer with leading and trailing spaces`() {
        // 1. Create ExerciseSet
        val createSetRequest = ExerciseSetCreateRequest(
            title = "Test Exercise Set",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = "The [quick] brown fox."
        )
        val createSetResponse = restTemplate.postForEntity(url("/api/exercise-sets"), HttpEntity(createSetRequest, authHeaders()), ExerciseSetResponse::class.java)
        val exerciseSet = createSetResponse.body!!

        // 2. Create Attempt
        val createAttemptRequest = AttemptCreateRequest(
            exerciseSetId = exerciseSet.id,
            studentName = "Space Student"
        )
        val createAttemptResponse = restTemplate.postForEntity(url("/api/attempts"), createAttemptRequest, AttemptResponse::class.java)
        val attempt = createAttemptResponse.body!!

        // 3. Submit answer with spaces for the gap "quick"
        val questionId = exerciseSet.questions[0].id!!
        val answerRequest1 = QuestionAnswerRequest(
            questionId = questionId,
            answer = " quick "
        )
        val response1 = restTemplate.postForEntity(url("/api/attempts/${attempt.id}/answers"), answerRequest1, AttemptQuestionResponse::class.java)
        assertThat(response1.body?.isCorrect).isTrue()

        // 4. Submit answer with spaces for the full sentence
        val answerRequest2 = QuestionAnswerRequest(
            questionId = questionId,
            answer = "  The  quick  brown  fox.  "
        )
        val response2 = restTemplate.postForEntity(url("/api/attempts/${attempt.id}/answers"), answerRequest2, AttemptQuestionResponse::class.java)
        assertThat(response2.body?.isCorrect).isTrue()
    }

    @Test
    fun `add should return bad request when bulk input is empty`() {
        val createSetRequest = ExerciseSetCreateRequest(
            title = "Test Exercise Set",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = ""
        )
        val createSetResponse = restTemplate.postForEntity(url("/api/exercise-sets"), HttpEntity(createSetRequest, authHeaders()), Map::class.java)
        assertThat(createSetResponse.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }
}
