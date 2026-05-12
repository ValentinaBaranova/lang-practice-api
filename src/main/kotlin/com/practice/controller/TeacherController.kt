package com.practice.controller

import com.practice.dto.TeacherCreateRequest
import com.practice.dto.TeacherResponse
import com.practice.service.TeacherService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/teachers")
class TeacherController(
    private val teacherService: TeacherService
) {
    @PostMapping
    fun createTeacher(@Valid @RequestBody request: TeacherCreateRequest): TeacherResponse {
        return teacherService.createTeacher(request)
    }

    @GetMapping("/{accessCode}")
    fun getTeacherByAccessCode(@PathVariable accessCode: String): TeacherResponse {
        return teacherService.getTeacherByAccessCode(accessCode)
    }
}
