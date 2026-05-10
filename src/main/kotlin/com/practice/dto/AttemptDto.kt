package com.practice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class AttemptResponse(
    val id: UUID,
    val exerciseSetId: UUID,
    val studentName: String?,
    val totalQuestions: Int?,
    val answeredQuestions: Int?,
    val correctAnswers: Int?
)

data class AttemptCreateRequest(
    @field:NotNull(message = "Exercise set ID is required")
    val exerciseSetId: UUID,

    @field:NotBlank(message = "Student name is required")
    val studentName: String
)

data class QuestionAnswerRequest(
    @field:NotBlank(message = "Question ID is required")
    val questionId: String,

    @field:NotBlank(message = "Answer is required")
    val answer: String
)

data class AttemptQuestionResponse(
    val id: UUID,
    val attemptId: UUID,
    val questionId: String,
    val answer: String,
    val isCorrect: Boolean
)
