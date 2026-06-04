package com.practice.domain

import java.util.UUID

data class ExerciseQuestion(
    val id: UUID,
    val prompt: String,
    val sourceText: String,
    // TODO: should be inside gaps
    val options: List<String> = emptyList(),
    val gaps: List<Gap> = emptyList()
)

data class Gap(
    val index: Int,
    val correctAnswer: String
)
