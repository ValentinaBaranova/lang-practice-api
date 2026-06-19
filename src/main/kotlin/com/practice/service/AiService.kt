package com.practice.service

import com.practice.domain.ExerciseType
import com.practice.domain.Teacher
import com.practice.dto.AiGenerateResponse
import com.practice.repository.TeacherRepository
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.logging.Logger

@Service
open class AiService(
    private val chatModel: ChatModel,
    private val teacherRepository: TeacherRepository,
    private val exerciseSetService: ExerciseSetService,
    @Value("\${application.ai.requests-limit}")
    private val aiRequestsLimit: Int
) {
    private val logger = Logger.getLogger(AiService::class.java.name)

    companion object {

        private const val BASE_PROMPT = """
            Generate [AMOUNT] sentences in Argentine Spanish about topic "[TOPIC]" for [PRACTICE_TYPE].                        
            Don't add line numeration.
            [FORMAT_INSTRUCTIONS]
            If the answer requires a reflexive or object pronoun, include it in the hint.
            Focus on common everyday situations and Argentine vocabulary.
        """

        private const val FILL_GAP_INSTRUCTIONS = """
            Plain text, no empty lines. One sentence per line. Do not include line numbers.
            Put only the element of topic being practiced inside square brackets.
            Use the format: Sentence with [answer] (infinitive_verb_or_hint). 
            Example: Mañana [iremos] (nosotros, ir) al cine.
        """

        private const val FILL_GAP_MULTILINE_INSTRUCTIONS = """
            Make it as a story. You may include multiple gaps across the passage.
            Put only the elements being practiced inside square brackets.            
            Use the format: Sentence with [answer] (infinitive_verb_or_hint).
            Example:
            Hoy [estamos] (nosotros, estar) muy cansados. Mañana [iremos] (nosotros, ir) a la plaza.
        """

        private const val MULTIPLE_CHOICE_INSTRUCTIONS = """
            Plain text, no empty lines. One sentence per line. Do not include line numbers.
            Put only the element of topic being practiced inside square brackets.
            Use the format: Sentence with [correct answer] {option1|option2|option3}. 
            Example: No [hables] {hablas|hables|hablar} tan rápido.
        """

        private const val SOLVE_PROMPT = """
            Solve the following Argentinian Spanish language exercises about [TOPIC].
            Provide only the missing words, one per line, in order.
            Do not include brackets or other special formatting in the sentences.
            Plain text, no empty lines. One sentence per line. Do not include line numbers.
        """

        const val DEFAULT_TOPIC = "Presente"
    }

    @Transactional
    open fun generateExercise(type: ExerciseType, topic: String, amount: Int, teacher: Teacher?): AiGenerateResponse {
        if (teacher != null) {
            val now = OffsetDateTime.now()
            val lastRequest = teacher.lastAiRequestAt

            if (lastRequest != null && lastRequest.toLocalDate().isEqual(now.toLocalDate())) {
                if (teacher.aiRequestsCount >= aiRequestsLimit) {
                    return AiGenerateResponse("ERROR: Daily AI request limit ($aiRequestsLimit) reached. Please try again tomorrow.")
                }
                teacher.aiRequestsCount++
            } else {
                teacher.aiRequestsCount = 1
            }
            teacher.lastAiRequestAt = now
            teacherRepository.save(teacher)
        }

        val verificationAmount = if (type == ExerciseType.FILL_GAP_TEXT_MULTILINE) amount else amount * 2
        val generateExercisesPrompt = buildGenerateQuestionsPrompt(type, topic, verificationAmount)

        // For multiline exercises: single attempt (no per-line validation)
        if (type == ExerciseType.FILL_GAP_TEXT_MULTILINE) {
            return try {
                val response = chatModel.call(Prompt(generateExercisesPrompt))
                val generatedExercises = response.result?.output?.text
                if (generatedExercises.isNullOrBlank()) {
                    return AiGenerateResponse("ERROR: AI generated an empty response. Please try again or check your topic.")
                }
                val content = generatedExercises.trim()
                val questions = exerciseSetService.parseBulkInput(content, type, throwOnError = false)
                    .map { it.toDto() }
                AiGenerateResponse(content, questions)
            } catch (e: Exception) {
                AiGenerateResponse("ERROR: AI generation failed: ${e.message}")
            }
        }

        // For single-line exercises: retry up to 2 more times if not enough valid questions
        val maxAttempts = 3
        for (attempt in 1..maxAttempts) {
            try {
                val response = chatModel.call(Prompt(generateExercisesPrompt))
                val generatedExercises = response.result?.output?.text
                if (generatedExercises.isNullOrBlank()) {
                    logger.warning("AI returned empty response on attempt $attempt/$maxAttempts for topic '$topic'. Retrying...")
                    if (attempt == maxAttempts) {
                        return AiGenerateResponse("ERROR: AI generated an empty response. Please try again or check your topic.")
                    }
                    continue
                }

                val resultSentences = validateExercises(generatedExercises, type, topic, amount)
                val content = resultSentences.joinToString("\n")
                val questions = exerciseSetService.parseBulkInput(content, type, throwOnError = false)
                    .map { it.toDto() }

                return AiGenerateResponse(content, questions)
            } catch (e: Exception) {
                logger.warning("Attempt $attempt/$maxAttempts failed to generate enough valid questions for topic '$topic': ${e.message}")
                if (attempt == maxAttempts) {
                    return AiGenerateResponse("ERROR: AI generation failed: ${e.message}")
                }
                // else continue to next attempt
            }
        }

        // Should not reach here
        return AiGenerateResponse("ERROR: AI generation failed due to an unknown error.")
    }

    private fun validateExercises(
        generatedExercises: String,
        type: ExerciseType,
        topic: String,
        amount: Int
    ): List<String> {
        val questions = exerciseSetService.parseBulkInput(generatedExercises, type, throwOnError = false)

        val solvePrompt = SOLVE_PROMPT.trimIndent().trim()
            .replace("[TOPIC]", topic) + "\n\n" +
            questions.joinToString("\n") { it.prompt }

        val solveResponse = chatModel.call(Prompt(solvePrompt))
        val solveContent = solveResponse.result?.output?.text
        val aiAnswers = solveContent?.lines()?.filter { it.isNotBlank() } ?: emptyList()

        val validResults = mutableListOf<String>()
        val invalidResults = mutableListOf<String>()
        questions.forEachIndexed { index, question ->
            val aiAnswer = aiAnswers.getOrNull(index)
            val isValid = aiAnswer != null && exerciseSetService.isAnswerCorrect(question, aiAnswer)
            if (!isValid) {
                invalidResults.add(question.sourceText)
                logger.warning(
                    "AI generated invalid sentences for topic '$topic': " +
                        "${question.sourceText} (expected: ${question.gaps.firstOrNull()?.correctAnswer}, got: ${aiAnswer ?: "null"})"
                )
            } else {
                validResults.add(question.sourceText)
            }
        }

        if (invalidResults.isNotEmpty()) {
            logger.warning(
                "AI generated ${invalidResults.size} invalid sentences " +
                    "from ${amount} for topic '$topic'"
            )
        }

        if (validResults.count() < amount) {
            throw RuntimeException("Not enough valid questions! topic=$topic, generated exercises: $generatedExercises")
        }
        return validResults.take(amount)
    }

    fun buildGenerateQuestionsPrompt(type: ExerciseType, topic: String?, amount: Int): String {
        val resolvedTopic = topic.takeIf { !it.isNullOrBlank() } ?: "preterito indefinido"
        val practiceType = when (type) {
            ExerciseType.MULTIPLE_CHOICE -> "multiple choice practice"
            else -> "language practice where students fill in the blanks"
        }

        val instructions = when (type) {
            ExerciseType.MULTIPLE_CHOICE -> MULTIPLE_CHOICE_INSTRUCTIONS
            ExerciseType.FILL_GAP_TEXT_MULTILINE -> FILL_GAP_MULTILINE_INSTRUCTIONS
            else -> FILL_GAP_INSTRUCTIONS
        }

        return BASE_PROMPT.trimIndent()
            .replace("[AMOUNT]", amount.toString())
            .replace("[TOPIC]", resolvedTopic)
            .replace("[PRACTICE_TYPE]", practiceType)
            .replace("[FORMAT_INSTRUCTIONS]", instructions.trimIndent().trimEnd())
    }
}
