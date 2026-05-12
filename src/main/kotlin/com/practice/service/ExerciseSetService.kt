package com.practice.service

import com.practice.domain.ExerciseQuestion
import com.practice.domain.ExerciseSet
import com.practice.dto.ExerciseQuestion as ExerciseQuestionDto
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

        val questions = parseBulkInput(request.bulkInput, request.type)
        val exerciseSet = ExerciseSet(
            teacherId = request.teacherId,
            title = request.title,
            type = request.type,
            visibility = request.visibility,
            questions = questions,
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
            exerciseSetRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId)
        } else {
            exerciseSetRepository.findAllByOrderByCreatedAtDesc()
        }
        
        val teacherNames = teacherRepository.findAllById(exerciseSets.map { it.teacherId }.distinct())
            .associate { it.id to it.name }

        return exerciseSets.map { it.toResponse(teacherNames[it.teacherId] ?: "Unknown") }
    }

    @Transactional(readOnly = true)
    fun listPublicExerciseSets(): List<ExerciseSetResponse> {
        val exerciseSets = exerciseSetRepository.findByVisibilityOrderByCreatedAtDesc(com.practice.domain.ExerciseVisibility.PUBLIC)

        val teacherNames = teacherRepository.findAllById(exerciseSets.map { it.teacherId }.distinct())
            .associate { it.id to it.name }

        return exerciseSets.map { it.toResponse(teacherNames[it.teacherId] ?: "Unknown") }
    }

    @Transactional
    fun updateExerciseSet(id: UUID, request: ExerciseSetUpdateRequest): ExerciseSetResponse {
        val exerciseSet = exerciseSetRepository.findById(id)
            .orElseThrow { NoSuchElementException("Exercise set not found with id: $id") }

        exerciseSet.title = request.title
        exerciseSet.visibility = request.visibility
        exerciseSet.questions = parseBulkInput(request.bulkInput, exerciseSet.type)

        val saved = exerciseSetRepository.save(exerciseSet)
        val teacher = teacherRepository.findById(saved.teacherId)
            .orElseThrow { NoSuchElementException("Teacher not found with id: ${saved.teacherId}") }
        
        return saved.toResponse(teacher.name)
    }

    internal fun parseBulkInput(bulkInput: String, type: com.practice.domain.ExerciseType): List<ExerciseQuestion> {
        val errors = mutableListOf<String>()
        val questions = bulkInput.lineSequence()
            .filter { it.isNotBlank() }
            .mapIndexed { index, line ->
                val sourceText = line.trim()
                val answerRegex = "\\[(.*?)]".toRegex()
                val optionsRegex = "\\{(.*?)}".toRegex()
                
                val answerMatch = answerRegex.find(sourceText)
                
                if (answerMatch == null) {
                    errors.add("Line ${index + 1}: Each line must contain at least one answer in []: $sourceText")
                    null
                } else {
                    val correctAnswer = answerMatch.groupValues[1]
                    val optionsMatch = optionsRegex.find(sourceText)
                    val options = optionsMatch?.groupValues?.get(1)?.split("|")?.map { it.trim() } ?: emptyList()

                    var prompt = sourceText.replace(optionsRegex, "").trim()
                    if (prompt.contains("___") || prompt.contains("____")) {
                        prompt = prompt.replace(answerRegex, "").replace("\\s+".toRegex(), " ").trim()
                    } else {
                        prompt = prompt.replace(answerRegex, "___").trim()
                    }

                    if (type == com.practice.domain.ExerciseType.MULTIPLE_CHOICE) {
                        if (answerRegex.findAll(sourceText).count() > 1) {
                            errors.add("Line ${index + 1}: Multiple choice exercise must have exactly one answer in []: $sourceText")
                        }
                        if (options.size < 2) {
                            errors.add("Line ${index + 1}: Multiple choice exercise must have at least 2 options in {}: $sourceText")
                        }
                        if (!options.contains(correctAnswer)) {
                            errors.add("Line ${index + 1}: Options must contain the correct answer: $sourceText")
                        }
                    }

                    ExerciseQuestion(
                        id = UUID.randomUUID(),
                        prompt = prompt,
                        correctAnswer = correctAnswer,
                        sourceText = sourceText,
                        options = options
                    )
                }
            }
            .filterNotNull()
            .toList()

        if (errors.isNotEmpty()) {
            throw IllegalArgumentException(errors.joinToString("\n"))
        }

        return questions
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
        visibility = this.visibility,
        questions = this.questions.map { it.toDto() },
        shareSlug = this.shareSlug,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )

    private fun ExerciseQuestionDto.toDomain() = ExerciseQuestion(
        id = this.id ?: UUID.randomUUID(),
        prompt = this.prompt,
        correctAnswer = this.correctAnswer,
        sourceText = this.sourceText,
        options = this.options ?: emptyList()
    )

    private fun ExerciseQuestion.toDto() = ExerciseQuestionDto(
        id = this.id,
        prompt = this.prompt,
        correctAnswer = this.correctAnswer,
        sourceText = this.sourceText,
        options = if (this.options.isNotEmpty()) this.options else null
    )
}
