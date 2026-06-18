package com.practice.controller

import com.practice.domain.Teacher
import com.practice.dto.ExerciseSetCreateRequest
import com.practice.dto.ExerciseSetResponse
import com.practice.dto.ExerciseSetUpdateRequest
import com.practice.dto.ValidateAnswerRequest
import com.practice.dto.ValidateAnswerResponse
import com.practice.service.ExerciseSetService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/exercise-sets")
class ExerciseSetController(
    private val exerciseSetService: ExerciseSetService
) {

    @PostMapping
    fun createExerciseSet(
        @Valid @RequestBody request: ExerciseSetCreateRequest,
        @AuthenticationPrincipal teacher: Teacher
    ): ExerciseSetResponse {
        return exerciseSetService.createExerciseSet(teacher, request)
    }

    @PostMapping("/validate-answer")
    fun validateAnswer(@Valid @RequestBody request: ValidateAnswerRequest): ValidateAnswerResponse {
        return exerciseSetService.validateAnswer(request)
    }

    @GetMapping("/{id}")
    fun getExerciseSetById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal teacher: Teacher
    ): ExerciseSetResponse {
        return exerciseSetService.getExerciseSetById(id, teacher)
    }

    @GetMapping("/share/{shareSlug}")
    fun getExerciseSetByShareSlug(@PathVariable shareSlug: String): ExerciseSetResponse {
        return exerciseSetService.getExerciseSetByShareSlug(shareSlug)
    }

    @GetMapping("/public")
    fun listPublicExerciseSets(
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<ExerciseSetResponse> {
        return exerciseSetService.listPublicExerciseSets(pageable)
    }

    @GetMapping
    fun listExerciseSets(
        @AuthenticationPrincipal teacher: Teacher,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<ExerciseSetResponse> {
        return exerciseSetService.listExerciseSets(teacher, pageable)
    }

    @PutMapping("/{id}")
    fun updateExerciseSet(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ExerciseSetUpdateRequest,
        @AuthenticationPrincipal teacher: Teacher
    ): ExerciseSetResponse {
        return exerciseSetService.updateExerciseSet(id, teacher, request)
    }
}
