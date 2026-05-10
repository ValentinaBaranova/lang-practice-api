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
        val teacher = teacherRepository.findAll().firstOrNull() 
            ?: throw IllegalStateException("Default teacher not found")

        val exerciseSet = ExerciseSet(
            teacherId = teacher.id!!,
            title = request.title,
            type = request.type,
            questions = request.questions,
            shareSlug = generateUniqueShareSlug()
        )

        return exerciseSetRepository.save(exerciseSet).toResponse()
    }

    @Transactional(readOnly = true)
    fun getExerciseSetById(id: UUID): ExerciseSetResponse {
        return exerciseSetRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { NoSuchElementException("Exercise set not found with id: $id") }
    }

    @Transactional(readOnly = true)
    fun getExerciseSetByShareSlug(shareSlug: String): ExerciseSetResponse {
        val exerciseSet = exerciseSetRepository.findByShareSlug(shareSlug)
            ?: throw NoSuchElementException("Exercise set not found with shareSlug: $shareSlug")
        return exerciseSet.toResponse()
    }

    @Transactional(readOnly = true)
    fun listExerciseSets(): List<ExerciseSetResponse> {
        return exerciseSetRepository.findAll().map { it.toResponse() }
    }

    @Transactional
    fun updateExerciseSet(id: UUID, request: ExerciseSetUpdateRequest): ExerciseSetResponse {
        val exerciseSet = exerciseSetRepository.findById(id)
            .orElseThrow { NoSuchElementException("Exercise set not found with id: $id") }

        exerciseSet.title = request.title
        exerciseSet.questions = request.questions

        return exerciseSetRepository.save(exerciseSet).toResponse()
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

    private fun ExerciseSet.toResponse() = ExerciseSetResponse(
        id = this.id!!,
        teacherId = this.teacherId,
        title = this.title,
        type = this.type,
        questions = this.questions,
        shareSlug = this.shareSlug,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
