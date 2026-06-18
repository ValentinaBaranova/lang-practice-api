package com.practice.service

import com.practice.domain.ExerciseQuestion
import com.practice.domain.ExerciseSet
import com.practice.domain.ExerciseType
import com.practice.domain.ExerciseVisibility
import com.practice.domain.Gap
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
        // Special handling for multiline fill-the-gap: treat whole input as a single question with multiple gaps
        if (type == ExerciseType.FILL_GAP_TEXT_MULTILINE) {
            return parseMultilineInput(bulkInput, throwOnError)
        }

        val errors = mutableListOf<String>()
        val questions = bulkInput.lineSequence()
            .filter { it.isNotBlank() }
            .mapIndexed { index, line -> parseSingleLine(index, line.trim(), type, throwOnError, errors) }
            .filterNotNull()
            .toList()

        if (throwOnError && errors.isNotEmpty()) {
            throw IllegalArgumentException(errors.joinToString("\n"))
        }

        return questions
    }

    private fun parseMultilineInput(bulkInput: String, throwOnError: Boolean): List<ExerciseQuestion> {
        val sourceText = bulkInput.trim()

        val answerMatches = ANSWER_REGEX.findAll(sourceText).toList()
        val rawOptions = extractOptions(sourceText)

        if (answerMatches.isEmpty()) {
            if (throwOnError) throw IllegalArgumentException("Multiline exercise must contain at least one answer in []")
            return emptyList()
        }
        // Options in multiline mode are ignored; always save an empty list of options

        val gaps = answerMatches.mapIndexed { matchIndex, matchResult ->
            Gap(index = matchIndex, correctAnswer = matchResult.groupValues[1])
        }

        // Build prompt: preserve new lines, remove options, replace each [answer] with ___
        var prompt = sourceText.replace(OPTIONS_REGEX, "")
        prompt = prompt.replace(ANSWER_REGEX, "___")

        val question = ExerciseQuestion(
            id = UUID.randomUUID(),
            prompt = prompt,
            sourceText = sourceText,
            options = emptyList(),
            gaps = gaps
        )
        return listOf(question)
    }

    private fun parseSingleLine(
        index: Int,
        sourceText: String,
        type: ExerciseType,
        throwOnError: Boolean,
        errors: MutableList<String>
    ): ExerciseQuestion? {
        val answers = extractAnswers(sourceText)
        val rawOptions = extractOptions(sourceText)

        // Support no-gap format for MULTIPLE_CHOICE: correct answer marked with * in options
        val hasStarredOption = rawOptions.any { it.startsWith("*") }
        val isNoGapMultipleChoice = type == ExerciseType.MULTIPLE_CHOICE && answers.isEmpty() && hasStarredOption

        return if (isNoGapMultipleChoice) {
            val correctAnswer = rawOptions.first { it.startsWith("*") }.removePrefix("*").trim()
            val options = rawOptions.map { it.removePrefix("*").trim() }
            val gaps = listOf(Gap(index = 0, correctAnswer = correctAnswer))

            val prompt = buildPromptWithoutAnswers(sourceText)

            if (options.size < 2) {
                if (throwOnError) errors.add("Line ${index + 1}: Multiple choice exercise must have at least 2 options in {}: $sourceText")
            }

            ExerciseQuestion(
                id = UUID.randomUUID(),
                prompt = prompt,
                sourceText = sourceText,
                options = options,
                gaps = gaps
            )
        } else if (answers.isEmpty()) {
            if (throwOnError) errors.add("Line ${index + 1}: Each line must contain at least one answer in [] or a starred option (*) in {}: $sourceText")
            null
        } else {
            val gaps = answers.mapIndexed { matchIndex, correct -> Gap(index = matchIndex, correctAnswer = correct) }
            val options = rawOptions

            val prompt = buildPromptWithAnswers(sourceText)

            if (type == ExerciseType.MULTIPLE_CHOICE) {
                if (answers.size > 1) {
                    if (throwOnError) errors.add("Line ${index + 1}: Multiple choice exercise must have exactly one answer in []: $sourceText")
                }
                if (options.size < 2) {
                    if (throwOnError) errors.add("Line ${index + 1}: Multiple choice exercise must have at least 2 options in {}: $sourceText")
                }
                if (!options.contains(gaps.first().correctAnswer)) {
                    if (throwOnError) errors.add("Line ${index + 1}: Options must contain the correct answer: $sourceText")
                }
            }

            ExerciseQuestion(
                id = UUID.randomUUID(),
                prompt = prompt,
                sourceText = sourceText,
                options = options,
                gaps = gaps
            )
        }
    }

    private fun extractAnswers(text: String): List<String> =
        ANSWER_REGEX.findAll(text).map { it.groupValues[1] }.toList()

    private fun extractOptions(text: String): List<String> =
        OPTIONS_REGEX.find(text)?.groupValues?.get(1)?.split("|")?.map { it.trim() } ?: emptyList()

    private fun buildPromptWithoutAnswers(sourceText: String): String =
        sourceText.replace(OPTIONS_REGEX, "").replace("\\s+".toRegex(), " ").trim()

    private fun buildPromptWithAnswers(sourceText: String): String {
        var prompt = sourceText.replace(OPTIONS_REGEX, "").replace("\\s+".toRegex(), " ").trim()
        prompt = if (prompt.contains("___") || prompt.contains("____")) {
            prompt.replace(ANSWER_REGEX, "").replace("\\s+".toRegex(), " ").trim()
        } else {
            prompt.replace(ANSWER_REGEX, "___").trim()
        }
        return prompt
    }

    companion object {
        private val ANSWER_REGEX = "\\[(.*?)]".toRegex()
        private val OPTIONS_REGEX = "\\{+([^{}]*?)}".toRegex()
    }

    fun validateAnswer(request: com.practice.dto.ValidateAnswerRequest): com.practice.dto.ValidateAnswerResponse {
        val question = request.question.toDomain()
        val gapResults = request.answers.map { answerReq ->
            val gap = question.gaps.singleOrNull() ?: question.gaps.find { it.index == answerReq.index }
            val expectedAnswer = gap?.correctAnswer
            val isCorrect = isAnswerCorrect(question, answerReq.answer, answerReq.index)

            com.practice.dto.GapAnswerResponse(
                index = answerReq.index,
                answer = answerReq.answer,
                isCorrect = isCorrect,
                expectedAnswer = expectedAnswer
            )
        }
        return com.practice.dto.ValidateAnswerResponse(
            isCorrect = gapResults.all { it.isCorrect },
            gapResults = gapResults
        )
    }

    fun isAnswerCorrect(question: ExerciseQuestion, answer: String, gapIndex: Int? = null): Boolean {
        val gap = if (gapIndex != null) {
            question.gaps.find { it.index == gapIndex }
        } else {
            question.gaps.singleOrNull()
        }

        val correctAnswerToUse = gap?.correctAnswer

        if (correctAnswerToUse != null) {
            val normalizedCorrectAnswer = normalizeText(correctAnswerToUse)
            val normalizedAnswer = normalizeText(answer)
            return normalizedCorrectAnswer.equals(normalizedAnswer, ignoreCase = true)
        }

        return false
    }

    fun isAnswerCorrect(question: ExerciseQuestion, answers: List<com.practice.dto.GapAnswerRequest>): Boolean {
        if (question.gaps.isEmpty()) return false
        if (question.gaps.size != answers.size) return false
        val providedAnswers = answers.associate { it.index to it.answer }
        return question.gaps.all { gap ->
            val providedAnswer = providedAnswers[gap.index] ?: return@all false
            normalizeText(gap.correctAnswer).equals(normalizeText(providedAnswer), ignoreCase = true)
        }
    }

    internal fun normalizeText(text: String): String {
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
        sourceText = this.sourceText,
        options = this.options ?: emptyList(),
        gaps = this.gaps?.map { Gap(it.index, it.correctAnswer) } ?: emptyList()
    )
}

internal fun com.practice.domain.ExerciseQuestion.toDto() = ExerciseQuestionDto(
    id = this.id,
    prompt = this.prompt,
    sourceText = this.sourceText,
    options = if (this.options.isNotEmpty()) this.options else null,
    gaps = if (this.gaps.isNotEmpty()) this.gaps.map { com.practice.dto.GapDto(it.index, it.correctAnswer) } else null
)
