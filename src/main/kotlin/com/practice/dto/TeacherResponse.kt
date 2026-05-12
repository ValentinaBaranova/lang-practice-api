package com.practice.dto

import java.time.OffsetDateTime
import java.util.UUID

data class TeacherResponse(
    val id: UUID,
    val name: String,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
)
