package com.practice.dto

import com.practice.domain.ExerciseType
import com.practice.domain.ExerciseVisibility
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.UUID

data class ExerciseSetResponse(
    val id: UUID,
    val title: String,
    val type: ExerciseType,
    val visibility: ExerciseVisibility,
    val questions: List<ExerciseQuestion>,
    val shareSlug: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
)

data class ExerciseQuestion(
    val id: UUID? = null,

    @field:NotBlank(message = "Prompt is required")
    val prompt: String,

    @field:NotBlank(message = "Source text is required")
    val sourceText: String,

    val options: List<String>? = null,
    val gaps: List<GapDto>? = null
)

data class GapDto(
    val index: Int,
    val correctAnswer: String
)

data class ExerciseSetCreateRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,

    @field:NotNull(message = "Type is required")
    val type: ExerciseType,

    @field:NotBlank(message = "Bulk input is required")
    val bulkInput: String
)

data class ExerciseSetUpdateRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,

    @field:NotBlank(message = "Bulk input is required")
    val bulkInput: String
)


data class ValidateAnswerRequest(
    @field:NotNull(message = "Question is required")
    val question: ExerciseQuestion,

    @field:NotNull(message = "Answers are required")
    @field:NotEmpty(message = "Answers cannot be empty")
    val answers: List<GapAnswerRequest>
)

data class ValidateAnswerResponse(
    val isCorrect: Boolean,
    val gapResults: List<GapAnswerResponse>? = null
)
