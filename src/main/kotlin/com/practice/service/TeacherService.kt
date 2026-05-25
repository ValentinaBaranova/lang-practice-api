package com.practice.service

import com.practice.domain.Teacher
import com.practice.dto.TeacherCreateRequest
import com.practice.dto.TeacherResponse
import com.practice.repository.TeacherRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeacherService(
    private val teacherRepository: TeacherRepository
) {
    @Transactional
    fun createTeacher(request: TeacherCreateRequest): TeacherResponse {
        val teacher = Teacher(
            name = request.name
        )
        return teacherRepository.save(teacher).toResponse()
    }

    @Transactional
    fun getOrCreateTeacher(email: String, name: String): Teacher {
        val existing = teacherRepository.findByEmail(email)
        if (existing != null) {
            if (existing.name != name && name.isNotBlank()) {
                existing.name = name
                return teacherRepository.save(existing)
            }
            return existing
        }

        return teacherRepository.save(
            Teacher(
                email = email,
                name = name
            )
        )
    }

    fun getTeacherResponse(teacher: Teacher): TeacherResponse {
        return teacher.toResponse()
    }

    private fun Teacher.toResponse() = TeacherResponse(
        name = this.name,
        email = this.email,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
