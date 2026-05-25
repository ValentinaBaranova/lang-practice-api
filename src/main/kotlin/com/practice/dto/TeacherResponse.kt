package com.practice.dto

import java.time.OffsetDateTime

data class TeacherResponse(
    val name: String,
    val email: String? = null,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
)
