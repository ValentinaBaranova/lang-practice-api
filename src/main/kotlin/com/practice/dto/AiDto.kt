package com.practice.dto

data class AiGenerateRequest(
    val type: String,
    val topic: String? = null,
    val amount: Int? = 10
)

data class AiGenerateResponse(
    val content: String
)

data class AiPromptResponse(
    val prompt: String
)
