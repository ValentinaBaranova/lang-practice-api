package com.practice.service

import com.practice.dto.AiGenerateResponse
import com.practice.repository.TeacherRepository
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class AiService(
    private val chatModel: ChatModel,
    private val teacherRepository: TeacherRepository,
    @Value("\${application.ai.requests-limit}")
    private val aiRequestsLimit: Int
) {
    companion object {
        private const val SYSTEM_MESSAGE = "You are a helpful assistant that generates Spanish language exercises."

        private const val BASE_PROMPT = """
            Generate [AMOUNT] sentences in Argentine Spanish about [TOPIC] for [PRACTICE_TYPE].
            Plain text, no empty lines. One sentence per line. Do not include line numbers.
            [FORMAT_INSTRUCTIONS]
            If the answer requires a reflexive or object pronoun, include it in the hint.
            Focus on common everyday situations and Argentine vocabulary.
        """

        private const val FILL_GAP_INSTRUCTIONS = """
            Use the format: Sentence with [answer] (infinitive_verb_or_hint). 
            Example: Mañana [iremos] (nosotros, ir) al cine.
        """

        private const val MULTIPLE_CHOICE_INSTRUCTIONS = """
            Use the format: Sentence with [correct answer] {'option1|option2|option3'}. 
            Example: No [hables] {'hablas|hables|hablar'} tan rápido.
        """
    }

    @Transactional
    fun generateExercise(type: String, topic: String?, amount: Int, teacherAccessCode: String): AiGenerateResponse {
        val teacher = teacherRepository.findByAccessCode(teacherAccessCode)
            ?: throw NoSuchElementException("Teacher not found with accessCode: $teacherAccessCode")

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

        val userMessageText = buildUserMessage(type, topic, amount)

        val systemMessage = SystemPromptTemplate(SYSTEM_MESSAGE)
            .createMessage()
        val userMessage = UserMessage(userMessageText)

        return try {
            val response = chatModel.call(Prompt(listOf(systemMessage, userMessage)))
            val content = response.result?.output?.text
            if (content.isNullOrBlank()) {
                AiGenerateResponse("ERROR: AI generated an empty response. Please try again or check your topic.")
            } else {
                AiGenerateResponse(content)
            }
        } catch (e: Exception) {
            AiGenerateResponse("ERROR: AI generation failed: ${e.message}")
        }
    }

    fun buildExercisePrompt(type: String, topic: String?, amount: Int): String {
        val userMessage = buildUserMessage(type, topic, amount)
        return "$SYSTEM_MESSAGE\n\n$userMessage"
    }

    fun buildUserMessage(type: String, topic: String?, amount: Int): String {
        val practiceType = if (type == "MULTIPLE_CHOICE") {
            "multiple choice practice"
        } else {
            "language practice where students fill in the blanks"
        }

        val instructions = if (type == "MULTIPLE_CHOICE") {
            MULTIPLE_CHOICE_INSTRUCTIONS
        } else {
            FILL_GAP_INSTRUCTIONS
        }

        return BASE_PROMPT.trimIndent()
            .replace("[AMOUNT]", amount.toString())
            .replace("[TOPIC]", topic.takeIf { !it.isNullOrBlank() } ?: "preterito indefinido")
            .replace("[PRACTICE_TYPE]", practiceType)
            .replace("[FORMAT_INSTRUCTIONS]", instructions.trimIndent().trimEnd())
    }
}
