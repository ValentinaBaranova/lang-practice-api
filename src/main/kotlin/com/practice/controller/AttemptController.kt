package com.practice.controller

import com.practice.dto.AttemptCreateRequest
import com.practice.dto.AttemptQuestionResponse
import com.practice.dto.AttemptResponse
import com.practice.dto.QuestionAnswerRequest
import com.practice.service.AttemptService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/attempts")
class AttemptController(
    private val attemptService: AttemptService
) {

    @PostMapping
    fun createAttempt(@Valid @RequestBody request: AttemptCreateRequest): AttemptResponse {
        return attemptService.createAttempt(request)
    }

    @GetMapping("/{id}")
    fun getAttempt(@PathVariable id: UUID): AttemptResponse {
        return attemptService.getAttempt(id)
    }

    @PostMapping("/{id}/answers")
    fun submitAnswer(
        @PathVariable id: UUID,
        @Valid @RequestBody request: QuestionAnswerRequest
    ): AttemptQuestionResponse {
        return attemptService.submitAnswer(id, request)
    }

    @GetMapping("/{id}/questions")
    fun getAttemptQuestions(@PathVariable id: UUID): List<AttemptQuestionResponse> {
        return attemptService.getAttemptQuestions(id)
    }

    @GetMapping("/exercise-set/{exerciseSetId}")
    fun getAttemptsByExerciseSetId(@PathVariable exerciseSetId: UUID): List<AttemptResponse> {
        return attemptService.getAttemptsByExerciseSetId(exerciseSetId)
    }
}
