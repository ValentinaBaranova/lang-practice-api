package com.practice.controller

import com.practice.dto.ExerciseSetResponse
import com.practice.service.ExerciseSetService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/exercises")
class ExerciseController(
    private val exerciseSetService: ExerciseSetService
) {
    @GetMapping("/public")
    fun listPublicExercises(): List<ExerciseSetResponse> {
        return exerciseSetService.listPublicExerciseSets()
    }
}
