package com.practice.service

import com.practice.domain.ExerciseQuestion
import com.practice.domain.ExerciseSet
import com.practice.domain.ExerciseType
import com.practice.domain.ExerciseVisibility
import com.practice.domain.Teacher
import com.practice.dto.ExerciseSetCreateRequest
import com.practice.dto.ExerciseSetResponse
import com.practice.dto.ExerciseSetUpdateRequest
import com.practice.repository.ExerciseSetRepository
import com.practice.repository.TeacherRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.Normalizer
import java.util.UUID
import kotlin.random.Random
import com.practice.dto.ExerciseQuestion as ExerciseQuestionDto

@Service
class ExerciseSetService(
    private val exerciseSetRepository: ExerciseSetRepository,
    private val teacherRepository: TeacherRepository
) {

    @Transactional
    fun createExerciseSet(teacher: Teacher?, request: ExerciseSetCreateRequest): ExerciseSetResponse {
        val questions = parseBulkInput(request.bulkInput, request.type)
        val exerciseSet = ExerciseSet(
            teacherId = teacher?.id,
            title = request.title,
            type = request.type,
            visibility = ExerciseVisibility.PRIVATE,
            questions = questions,
            shareSlug = generateUniqueShareSlug(request.title)
        )

        return exerciseSetRepository.save(exerciseSet).toResponse(teacher)
    }

    @Transactional(readOnly = true)
    fun getExerciseSetById(id: UUID, teacher: Teacher): ExerciseSetResponse {
        val exerciseSet = getExerciseSetDomainById(id)
        if (exerciseSet.teacherId != teacher.id) {
            throw IllegalAccessException("You are not allowed to access this exercise set")
        }
        return exerciseSet.toResponse(teacher)
    }

    private fun getExerciseSetDomainById(id: UUID): ExerciseSet {
        return exerciseSetRepository.findById(id)
            .orElseThrow { NoSuchElementException("Exercise set not found with id: $id") }
    }

    @Transactional(readOnly = true)
    fun getExerciseSetByShareSlug(shareSlug: String): ExerciseSetResponse {
        val exerciseSet = exerciseSetRepository.findByShareSlug(shareSlug)
            ?: throw NoSuchElementException("Exercise set not found with shareSlug: $shareSlug")
        val teacher = exerciseSet.teacherId?.let {
            teacherRepository.findById(it)
                .orElseThrow { NoSuchElementException("Teacher not found with id: $it") }
        }
        return exerciseSet.toResponse(teacher)
    }

    @Transactional(readOnly = true)
    fun listExerciseSets(teacher: Teacher, pageable: Pageable): Page<ExerciseSetResponse> {
        val exerciseSets = exerciseSetRepository.findByTeacherId(teacher.id!!, pageable)
        
        return exerciseSets.map { it.toResponse(teacher) }
    }

    @Transactional(readOnly = true)
    fun listPublicExerciseSets(pageable: Pageable): Page<ExerciseSetResponse> {
        val exerciseSets = exerciseSetRepository.findByVisibility(com.practice.domain.ExerciseVisibility.PUBLIC, pageable)

        return exerciseSets.map { it.toResponse() }
    }

    @Transactional
    fun updateExerciseSet(id: UUID, teacher: Teacher, request: ExerciseSetUpdateRequest): ExerciseSetResponse {
        val exerciseSet = getExerciseSetDomainById(id)
        if (exerciseSet.teacherId != teacher.id) {
            throw IllegalAccessException("You are not allowed to update this exercise set")
        }
        exerciseSet.title = request.title
        exerciseSet.questions = parseBulkInput(request.bulkInput, exerciseSet.type)

        val saved = exerciseSetRepository.save(exerciseSet)
        
        return saved.toResponse(teacher)
    }

    internal fun parseBulkInput(bulkInput: String, type: ExerciseType, throwOnError: Boolean = true): List<ExerciseQuestion> {
        val errors = mutableListOf<String>()
        val questions = bulkInput.lineSequence()
            .filter { it.isNotBlank() }
            .mapIndexed { index, line ->
                val sourceText = line.trim()
                val answerRegex = "\\[(.*?)]".toRegex()
                val optionsRegex = "\\{+([^{}]*?)}".toRegex()
                
                val answerMatch = answerRegex.find(sourceText)
                
                if (answerMatch == null) {
                    if (throwOnError) errors.add("Line ${index + 1}: Each line must contain at least one answer in []: $sourceText")
                    null
                } else {
                    val correctAnswer = answerMatch.groupValues[1]
                    val optionsMatch = optionsRegex.find(sourceText)
                    val options = optionsMatch?.groupValues?.get(1)?.split("|")?.map { it.trim() } ?: emptyList()

                    var prompt = sourceText.replace(optionsRegex, "").replace("\\s+".toRegex(), " ").trim()
                    if (prompt.contains("___") || prompt.contains("____")) {
                        prompt = prompt.replace(answerRegex, "").replace("\\s+".toRegex(), " ").trim()
                    } else {
                        prompt = prompt.replace(answerRegex, "___").trim()
                    }

                    if (type == com.practice.domain.ExerciseType.MULTIPLE_CHOICE) {
                        if (answerRegex.findAll(sourceText).count() > 1) {
                            if (throwOnError) errors.add("Line ${index + 1}: Multiple choice exercise must have exactly one answer in []: $sourceText")
                        }
                        if (options.size < 2) {
                            if (throwOnError) errors.add("Line ${index + 1}: Multiple choice exercise must have at least 2 options in {}: $sourceText")
                        }
                        if (!options.contains(correctAnswer)) {
                            if (throwOnError) errors.add("Line ${index + 1}: Options must contain the correct answer: $sourceText")
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

        if (throwOnError && errors.isNotEmpty()) {
            throw IllegalArgumentException(errors.joinToString("\n"))
        }

        return questions
    }

    fun validateAnswer(request: com.practice.dto.ValidateAnswerRequest): Boolean {
        return isAnswerCorrect(request.question.toDomain(), request.answer)
    }

    fun isAnswerCorrect(question: ExerciseQuestion, answer: String): Boolean {
        val normalizedAnswer = normalizeText(answer)
        val normalizedCorrectAnswer = normalizeText(question.correctAnswer)
        val normalizedSourceText = normalizeText(question.sourceText
            .replace("[", "")
            .replace("]", "")
            .replace(Regex("\\{.*?\\}"), ""))

        return normalizedCorrectAnswer.equals(normalizedAnswer, ignoreCase = true) ||
                normalizedSourceText.equals(normalizedAnswer, ignoreCase = true)
    }

    private fun normalizeText(text: String): String {
        return Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .replace("\\s+".toRegex(), " ")
    }

    private fun generateUniqueShareSlug(title: String): String {
        val baseSlug = slugify(title).take(50).trim('-')
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        var slug: String
        do {
            val randomPart = (1..6)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
            slug = if (baseSlug.isNotEmpty()) "$baseSlug-$randomPart" else randomPart
        } while (exerciseSetRepository.findByShareSlug(slug) != null)
        return slug
    }

    private fun slugify(text: String): String {
        return Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .replace("[^a-z0-9]+".toRegex(), "-")
            .replace("-+".toRegex(), "-")
            .trim('-')
    }

    private fun ExerciseSet.toResponse(teacher: com.practice.domain.Teacher? = null) = ExerciseSetResponse(
        id = this.id!!,
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
}

internal fun com.practice.domain.ExerciseQuestion.toDto() = ExerciseQuestionDto(
    id = this.id,
    prompt = this.prompt,
    correctAnswer = this.correctAnswer,
    sourceText = this.sourceText,
    options = if (this.options.isNotEmpty()) this.options else null
)
