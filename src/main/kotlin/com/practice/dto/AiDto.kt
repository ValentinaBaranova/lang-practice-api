package com.practice.dto

import com.practice.domain.ExerciseType

data class AiGenerateRequest(
    val type: ExerciseType,
    val topic: String? = null,
    val amount: Int? = 10
)

data class AiGenerateResponse(
    val content: String,
    val questions: List<ExerciseQuestion>? = null
)

data class AiPromptResponse(
    val prompt: String
)
