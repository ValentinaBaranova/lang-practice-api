package com.practice.service

import com.practice.domain.Teacher
import com.practice.dto.TeacherResponse
import com.practice.repository.TeacherRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TeacherService(
    private val teacherRepository: TeacherRepository
) {
    @Transactional(readOnly = true)
    fun getTeacherById(id: UUID): TeacherResponse {
        val teacher = teacherRepository.findById(id)
            .orElseThrow { NoSuchElementException("Teacher not found with id: $id") }
        return teacher.toResponse()
    }

    private fun Teacher.toResponse() = TeacherResponse(
        id = this.id!!,
        name = this.name,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
