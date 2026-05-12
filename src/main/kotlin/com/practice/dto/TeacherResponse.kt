package com.practice.dto

import java.time.OffsetDateTime

data class TeacherResponse(
    val accessCode: String,
    val name: String,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
)
