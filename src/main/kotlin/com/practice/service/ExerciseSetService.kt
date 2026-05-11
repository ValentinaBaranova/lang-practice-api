package com.practice.service

import com.practice.domain.ExerciseSet
import com.practice.dto.ExerciseSetCreateRequest
import com.practice.dto.ExerciseSetResponse
import com.practice.dto.ExerciseSetUpdateRequest
import com.practice.repository.ExerciseSetRepository
import com.practice.repository.TeacherRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.random.Random

@Service
class ExerciseSetService(
    private val exerciseSetRepository: ExerciseSetRepository,
    private val teacherRepository: TeacherRepository
) {

    @Transactional
    fun createExerciseSet(request: ExerciseSetCreateRequest): ExerciseSetResponse {
        val teacher = teacherRepository.findById(request.teacherId)
            .orElseThrow { NoSuchElementException("Teacher not found with id: ${request.teacherId}") }

        val exerciseSet = ExerciseSet(
            teacherId = request.teacherId,
            title = request.title,
            type = request.type,
            questions = request.questions,
            shareSlug = generateUniqueShareSlug()
        )

        return exerciseSetRepository.save(exerciseSet).toResponse(teacher.name)
    }

    @Transactional(readOnly = true)
    fun getExerciseSetById(id: UUID): ExerciseSetResponse {
        val exerciseSet = exerciseSetRepository.findById(id)
            .orElseThrow { NoSuchElementException("Exercise set not found with id: $id") }
        val teacher = teacherRepository.findById(exerciseSet.teacherId)
            .orElseThrow { NoSuchElementException("Teacher not found with id: ${exerciseSet.teacherId}") }
        return exerciseSet.toResponse(teacher.name)
    }

    @Transactional(readOnly = true)
    fun getExerciseSetByShareSlug(shareSlug: String): ExerciseSetResponse {
        val exerciseSet = exerciseSetRepository.findByShareSlug(shareSlug)
            ?: throw NoSuchElementException("Exercise set not found with shareSlug: $shareSlug")
        val teacher = teacherRepository.findById(exerciseSet.teacherId)
            .orElseThrow { NoSuchElementException("Teacher not found with id: ${exerciseSet.teacherId}") }
        return exerciseSet.toResponse(teacher.name)
    }

    @Transactional(readOnly = true)
    fun listExerciseSets(teacherId: UUID? = null): List<ExerciseSetResponse> {
        val exerciseSets = if (teacherId != null) {
            exerciseSetRepository.findByTeacherId(teacherId)
        } else {
            exerciseSetRepository.findAll()
        }
        
        val teacherNames = teacherRepository.findAllById(exerciseSets.map { it.teacherId }.distinct())
            .associate { it.id to it.name }

        return exerciseSets.map { it.toResponse(teacherNames[it.teacherId] ?: "Unknown") }
    }

    @Transactional
    fun updateExerciseSet(id: UUID, request: ExerciseSetUpdateRequest): ExerciseSetResponse {
        val exerciseSet = exerciseSetRepository.findById(id)
            .orElseThrow { NoSuchElementException("Exercise set not found with id: $id") }

        exerciseSet.title = request.title
        exerciseSet.questions = request.questions

        val saved = exerciseSetRepository.save(exerciseSet)
        val teacher = teacherRepository.findById(saved.teacherId)
            .orElseThrow { NoSuchElementException("Teacher not found with id: ${saved.teacherId}") }
        
        return saved.toResponse(teacher.name)
    }

    private fun generateUniqueShareSlug(): String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        var slug: String
        do {
            slug = (1..8)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
        } while (exerciseSetRepository.findByShareSlug(slug) != null)
        return slug
    }

    private fun ExerciseSet.toResponse(teacherName: String) = ExerciseSetResponse(
        id = this.id!!,
        teacherId = this.teacherId,
        teacherName = teacherName,
        title = this.title,
        type = this.type,
        questions = this.questions,
        shareSlug = this.shareSlug,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
