package com.practice.controller

import com.practice.dto.ExerciseSetResponse
import com.practice.service.ExerciseSetService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/exercises")
class ExerciseController(
    private val exerciseSetService: ExerciseSetService
) {
    @GetMapping("/public")
    fun listPublicExercises(
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<ExerciseSetResponse> {
        return exerciseSetService.listPublicExerciseSets(pageable)
    }
}
