package com.practice.controller

import com.practice.dto.ExerciseSetCreateRequest
import com.practice.dto.ExerciseSetResponse
import com.practice.dto.ExerciseSetUpdateRequest
import com.practice.service.ExerciseSetService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/exercise-sets")
class ExerciseSetController(
    private val exerciseSetService: ExerciseSetService
) {

    @PostMapping
    fun createExerciseSet(@Valid @RequestBody request: ExerciseSetCreateRequest): ExerciseSetResponse {
        return exerciseSetService.createExerciseSet(request)
    }

    @GetMapping("/{id}")
    fun getExerciseSetById(@PathVariable id: UUID): ExerciseSetResponse {
        return exerciseSetService.getExerciseSetById(id)
    }

    @GetMapping("/share/{shareSlug}")
    fun getExerciseSetByShareSlug(@PathVariable shareSlug: String): ExerciseSetResponse {
        return exerciseSetService.getExerciseSetByShareSlug(shareSlug)
    }

    @GetMapping("/public")
    fun listPublicExerciseSets(): List<ExerciseSetResponse> {
        return exerciseSetService.listPublicExerciseSets()
    }

    @GetMapping
    fun listExerciseSets(@RequestParam(required = false) teacherId: UUID?): List<ExerciseSetResponse> {
        return exerciseSetService.listExerciseSets(teacherId)
    }

    @PutMapping("/{id}")
    fun updateExerciseSet(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ExerciseSetUpdateRequest
    ): ExerciseSetResponse {
        return exerciseSetService.updateExerciseSet(id, request)
    }
}
