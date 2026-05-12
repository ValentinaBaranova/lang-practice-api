package com.practice.dto

import jakarta.validation.constraints.NotBlank

data class TeacherCreateRequest(
    @field:NotBlank
    val name: String
)
