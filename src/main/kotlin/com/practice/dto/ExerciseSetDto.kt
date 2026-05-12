package com.practice.dto

import com.practice.domain.ExerciseType
import com.practice.domain.ExerciseVisibility
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.UUID

data class ExerciseSetResponse(
    val id: UUID,
    val teacherAccessCode: String,
    val teacherName: String,
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

    @field:NotBlank(message = "Correct answer is required")
    val correctAnswer: String,

    @field:NotBlank(message = "Source text is required")
    val sourceText: String,

    val options: List<String>? = null
)

data class ExerciseSetCreateRequest(
    @field:NotBlank(message = "Teacher access code is required")
    val teacherAccessCode: String,

    @field:NotBlank(message = "Title is required")
    val title: String,

    @field:NotNull(message = "Type is required")
    val type: ExerciseType,

    val visibility: ExerciseVisibility = ExerciseVisibility.PRIVATE,

    @field:NotBlank(message = "Bulk input is required")
    val bulkInput: String
)

data class ExerciseSetUpdateRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,

    val visibility: ExerciseVisibility = ExerciseVisibility.PRIVATE,

    @field:NotBlank(message = "Bulk input is required")
    val bulkInput: String
)
