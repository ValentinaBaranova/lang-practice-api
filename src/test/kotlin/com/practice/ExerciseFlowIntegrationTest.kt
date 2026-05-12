package com.practice

import com.practice.domain.ExerciseType
import com.practice.dto.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

class ExerciseFlowIntegrationTest : IntegrationTestBase() {

    private val defaultTeacherId = UUID.fromString("00000000-0000-0000-0000-000000000000")
    private val defaultAccessCode = "DEFAULT001"

    @Test
    fun `teacher creates exercise and student submits correct answer`() {
        // 1. Get default teacher
        val teacherResponse = restTemplate.getForEntity(url("/api/teachers/$defaultAccessCode"), TeacherResponse::class.java)
        assertThat(teacherResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(teacherResponse.body?.accessCode).isEqualTo(defaultAccessCode)

        // 2. Create ExerciseSet
        val createSetRequest = ExerciseSetCreateRequest(
            teacherAccessCode = defaultAccessCode,
            title = "Test Exercise Set",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = """
                The [quick] brown fox.
                The [lazy] dog.
            """.trimIndent()
        )
        val createSetResponse = restTemplate.postForEntity(url("/api/exercise-sets"), createSetRequest, ExerciseSetResponse::class.java)
        assertThat(createSetResponse.statusCode).isEqualTo(HttpStatus.OK)
        val exerciseSet = createSetResponse.body!!
        assertThat(exerciseSet.title).isEqualTo("Test Exercise Set")
        assertThat(exerciseSet.questions).hasSize(2)

        // 3. Get ExerciseSet by ID
        val getSetResponse = restTemplate.getForEntity(url("/api/exercise-sets/${exerciseSet.id}"), ExerciseSetResponse::class.java)
        assertThat(getSetResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getSetResponse.body?.id).isEqualTo(exerciseSet.id)

        // 4. Create Attempt
        val createAttemptRequest = AttemptCreateRequest(
            exerciseSetId = exerciseSet.id,
            studentName = "John Doe"
        )
        val createAttemptResponse = restTemplate.postForEntity(url("/api/attempts"), createAttemptRequest, AttemptResponse::class.java)
        assertThat(createAttemptResponse.statusCode).isEqualTo(HttpStatus.OK)
        val attempt = createAttemptResponse.body!!
        assertThat(attempt.exerciseSetId).isEqualTo(exerciseSet.id)
        assertThat(attempt.studentName).isEqualTo("John Doe")

        // 5. Submit an answer
        val questionId = exerciseSet.questions[0].id!!
        val answerRequest = QuestionAnswerRequest(
            questionId = questionId,
            answer = "The quick brown fox."
        )
        val submitAnswerResponse = restTemplate.postForEntity(url("/api/attempts/${attempt.id}/answers"), answerRequest, AttemptQuestionResponse::class.java)
        assertThat(submitAnswerResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(submitAnswerResponse.body?.isCorrect).isTrue()

        // 6. Verify attempt progress
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
            teacherAccessCode = defaultAccessCode,
            title = "Test Exercise Set",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = "The [quick] brown fox."
        )
        val createSetResponse = restTemplate.postForEntity(url("/api/exercise-sets"), createSetRequest, ExerciseSetResponse::class.java)
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
    fun `add should return bad request when bulk input is empty`() {
        val createSetRequest = ExerciseSetCreateRequest(
            teacherAccessCode = defaultAccessCode,
            title = "Test Exercise Set",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = ""
        )
        val createSetResponse = restTemplate.postForEntity(url("/api/exercise-sets"), createSetRequest, Map::class.java)
        assertThat(createSetResponse.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }
}
