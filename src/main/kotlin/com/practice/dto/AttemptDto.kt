package com.practice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.UUID

data class AttemptResponse(
    val id: UUID,
    val exerciseSetId: UUID,
    val studentName: String?,
    val totalQuestions: Int?,
    val answeredQuestions: Int?,
    val correctAnswers: Int?,
    val createdAt: OffsetDateTime? = null
)

data class AttemptCreateRequest(
    @field:NotNull(message = "Exercise set ID is required")
    val exerciseSetId: UUID,

    @field:NotBlank(message = "Student name is required")
    val studentName: String
)

data class QuestionAnswerRequest(
    @field:NotNull(message = "Question ID is required")
    val questionId: UUID,
    val answers: List<GapAnswerRequest>
)

data class GapAnswerRequest(
    val index: Int,
    val answer: String
)

data class AttemptQuestionResponse(
    val id: UUID,
    val attemptId: UUID,
    val questionId: UUID,
    val answers: List<GapAnswerResponse>
)

data class GapAnswerResponse(
    val index: Int,
    val answer: String,
    val isCorrect: Boolean,
    val expectedAnswer: String? = null
)
