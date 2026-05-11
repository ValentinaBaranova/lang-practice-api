package com.practice.domain

import java.util.UUID

data class ExerciseQuestion(
    val id: UUID,
    val prompt: String,
    val correctAnswer: String
)
