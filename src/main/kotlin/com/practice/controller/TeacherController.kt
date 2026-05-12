package com.practice.controller

import com.practice.dto.TeacherResponse
import com.practice.service.TeacherService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/teachers")
class TeacherController(
    private val teacherService: TeacherService
) {
    @GetMapping("/{accessCode}")
    fun getTeacherByAccessCode(@PathVariable accessCode: String): TeacherResponse {
        return teacherService.getTeacherByAccessCode(accessCode)
    }
}
