package com.practice.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
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
