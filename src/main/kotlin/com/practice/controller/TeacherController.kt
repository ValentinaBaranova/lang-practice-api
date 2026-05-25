package com.practice.controller

import com.practice.domain.Teacher
import com.practice.dto.TeacherCreateRequest
import com.practice.dto.TeacherResponse
import com.practice.service.TeacherService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/teachers")
class TeacherController(
    private val teacherService: TeacherService
) {
    @GetMapping("/me")
    fun getCurrentTeacher(@AuthenticationPrincipal teacher: Teacher): TeacherResponse {
        return teacherService.getTeacherResponse(teacher)
    }

    @PostMapping
    fun createTeacher(@Valid @RequestBody request: TeacherCreateRequest): TeacherResponse {
        return teacherService.createTeacher(request)
    }
}
