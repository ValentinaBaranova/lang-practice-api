package com.practice.service

import com.practice.dto.AiGenerateResponse
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.stereotype.Service

@Service
class AiService(
    private val chatModel: ChatModel
) {
    companion object {
        const val FILL_GAP_PROMPT = """
            Generate [AMOUNT] sentences in Argentine Spanish about [TOPIC] for language practice where students fill in the blanks. 
            Use the format: Sentence with [answer] (infinitive_verb_or_hint). 
            Example: Mañana [iremos] (nosotros, ir) al cine.
            Focus on common everyday situations and Argentine vocabulary.
        """

        const val MULTIPLE_CHOICE_PROMPT = """
            Generate [AMOUNT] sentences in Argentine Spanish about [TOPIC] for multiple choice practice. 
            Use the format: Sentence with [correct answer] {'option1|option2|option3'}. 
            Example: No [hables] {'hablas|hables|hablar'} tan rápido.
            Focus on common everyday situations and Argentine vocabulary.
        """
    }

    fun generateExercise(type: String, topic: String?, amount: Int): AiGenerateResponse {
        val promptTemplate = if (type == "MULTIPLE_CHOICE") {
            MULTIPLE_CHOICE_PROMPT
        } else {
            FILL_GAP_PROMPT
        }

        val promptText = promptTemplate.trimIndent()
            .replace("[TOPIC]", topic ?: "preterito indefinido")
            .replace("[AMOUNT]", amount.toString())

        val systemMessage = SystemPromptTemplate("You are a helpful assistant that generates Spanish language exercises.")
            .createMessage()
        val userMessage = UserMessage(promptText)

        return try {
            val response = chatModel.call(Prompt(listOf(systemMessage, userMessage)))
            val content = response.result?.output?.text ?: "No content generated"
            AiGenerateResponse(content)
        } catch (e: Exception) {
            AiGenerateResponse("ERROR: AI generation failed")
        }
    }

    fun buildExercisePrompt(type: String, topic: String?, amount: Int): String {
        val template = if (type == "MULTIPLE_CHOICE") {
            MULTIPLE_CHOICE_PROMPT
        } else {
            FILL_GAP_PROMPT
        }

        return template.trimIndent()
            .replace("[TOPIC]", topic ?: "preterito indefinido")
            .replace("[AMOUNT]", amount.toString())
    }
}
