package com.practice.controller

import com.practice.domain.Teacher
import com.practice.dto.AiGenerateRequest
import com.practice.dto.AiGenerateResponse
import com.practice.dto.AiPromptResponse
import com.practice.service.AiService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai")
class AiController(
    private val aiService: AiService
) {
    @PostMapping("/generate")
    fun generate(
        @RequestBody request: AiGenerateRequest,
        @AuthenticationPrincipal teacher: Teacher?
    ): AiGenerateResponse {
        return aiService.generateExercise(
            type = request.type,
            topic = request.topic,
            amount = request.amount ?: 10,
            teacher = teacher
        )
    }

    @GetMapping("/build-exercise-prompt")
    fun buildExercisePrompt(
        @RequestParam type: String,
        @RequestParam(required = false) topic: String?,
        @RequestParam(defaultValue = "10") amount: Int
    ): AiPromptResponse {
        return AiPromptResponse(aiService.buildExercisePrompt(type, topic, amount))
    }
}
