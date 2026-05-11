package com.practice.dto

import com.practice.domain.ExerciseType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.UUID

data class ExerciseSetResponse(
    val id: UUID,
    val teacherId: UUID,
    val teacherName: String,
    val title: String,
    val type: ExerciseType,
    val questions: List<ExerciseQuestion>,
    val shareSlug: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
)

data class ExerciseQuestion(
    @field:NotBlank(message = "Prompt is required")
    val prompt: String,

    @field:NotBlank(message = "Correct answer is required")
    val correctAnswer: String
)

data class ExerciseSetCreateRequest(
    @field:NotNull(message = "Teacher ID is required")
    val teacherId: UUID,

    @field:NotBlank(message = "Title is required")
    val title: String,

    @field:NotNull(message = "Type is required")
    val type: ExerciseType,

    @field:NotEmpty(message = "Questions must contain at least one question")
    val questions: List<ExerciseQuestion>
)

data class ExerciseSetUpdateRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,

    @field:NotEmpty(message = "Questions must contain at least one question")
    val questions: List<ExerciseQuestion>
)
