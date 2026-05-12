package com.practice.service

import com.practice.domain.Teacher
import com.practice.dto.TeacherResponse
import com.practice.repository.TeacherRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeacherService(
    private val teacherRepository: TeacherRepository
) {
    @Transactional(readOnly = true)
    fun getTeacherByAccessCode(accessCode: String): TeacherResponse {
        val teacher = teacherRepository.findByAccessCode(accessCode)
            .let { it ?: throw NoSuchElementException("Teacher not found with accessCode: $accessCode") }
        return teacher.toResponse()
    }

    private fun Teacher.toResponse() = TeacherResponse(
        accessCode = this.accessCode,
        name = this.name,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
