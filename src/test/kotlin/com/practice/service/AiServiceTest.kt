package com.practice.service

import com.practice.domain.ExerciseType
import com.practice.repository.TeacherRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt

class AiServiceTest {

    private val chatModel = mock(ChatModel::class.java)
    private val teacherRepository = mock(TeacherRepository::class.java)
    private val exerciseSetService = ExerciseSetService(mock(com.practice.repository.ExerciseSetRepository::class.java), teacherRepository)
    private val aiService = AiService(chatModel, teacherRepository, exerciseSetService, 10)

    @Test
    fun `test generateExercise with accent-insensitive verification`() {
        val type = ExerciseType.FILL_GAP_TEXT
        val topic = "preterito"
        val amount = 1

        val chatResponse1 = mock(ChatResponse::class.java)
        val generation1 = mock(Generation::class.java)
        val assistantMessage1 = mock(org.springframework.ai.chat.messages.AssistantMessage::class.java)

        // Sentence has "habló", AI returns "hablo"
        `when`(assistantMessage1.text).thenReturn("Él [habló] español.")
        `when`(generation1.output).thenReturn(assistantMessage1)
        `when`(chatResponse1.result).thenReturn(generation1)

        val chatResponse2 = mock(ChatResponse::class.java)
        val generation2 = mock(Generation::class.java)
        val assistantMessage2 = mock(org.springframework.ai.chat.messages.AssistantMessage::class.java)

        // AI returns only missing word
        `when`(assistantMessage2.text).thenReturn("hablo")
        `when`(generation2.output).thenReturn(assistantMessage2)
        `when`(chatResponse2.result).thenReturn(generation2)

        `when`(chatModel.call(any(Prompt::class.java)))
            .thenReturn(chatResponse1)
            .thenReturn(chatResponse2)

        val response = aiService.generateExercise(type, topic, amount, null)
        assertThat(response.content).contains("Él [habló] español.")
    }

    @Test
    fun `test generateExercise with case-insensitive verification`() {
        val type = ExerciseType.FILL_GAP_TEXT
        val topic = "preterito"
        val amount = 1

        val chatResponse1 = mock(ChatResponse::class.java)
        val generation1 = mock(Generation::class.java)
        val assistantMessage1 = mock(org.springframework.ai.chat.messages.AssistantMessage::class.java)

        `when`(assistantMessage1.text).thenReturn("Yo [Hablo] español.")
        `when`(generation1.output).thenReturn(assistantMessage1)
        `when`(chatResponse1.result).thenReturn(generation1)

        val chatResponse2 = mock(ChatResponse::class.java)
        val generation2 = mock(Generation::class.java)
        val assistantMessage2 = mock(org.springframework.ai.chat.messages.AssistantMessage::class.java)

        // AI returns only missing word
        `when`(assistantMessage2.text).thenReturn("Hablo")
        `when`(generation2.output).thenReturn(assistantMessage2)
        `when`(chatResponse2.result).thenReturn(generation2)

        `when`(chatModel.call(any(Prompt::class.java)))
            .thenReturn(chatResponse1)
            .thenReturn(chatResponse2)

        val response = aiService.generateExercise(type, topic, amount, null)
        assertThat(response.content).contains("Yo [Hablo] español.")
    }

    @Test
    fun `test generateExercise with verification logic`() {
        val type = ExerciseType.FILL_GAP_TEXT
        val topic = "preterito"
        val amount = 2 // verificationAmount will be 2 * 2 = 4
        
        val chatResponse1 = mock(ChatResponse::class.java)
        val generation1 = mock(Generation::class.java)
        val assistantMessage1 = mock(org.springframework.ai.chat.messages.AssistantMessage::class.java)
        
        `when`(assistantMessage1.text).thenReturn("Sentence 1 [answer1] (hint1)\nSentence 2 [answer2] (hint2)\nSentence 3 [wrong] (hint3)\nSentence 4 [answer4] (hint4)")
        `when`(generation1.output).thenReturn(assistantMessage1)
        `when`(chatResponse1.result).thenReturn(generation1)

        val chatResponse2 = mock(ChatResponse::class.java)
        val generation2 = mock(Generation::class.java)
        val assistantMessage2 = mock(org.springframework.ai.chat.messages.AssistantMessage::class.java)
        
        // AI solves them with missing words
        `when`(assistantMessage2.text).thenReturn("answer1\nanswer2\ncorrect_answer3\nanswer4")
        `when`(generation2.output).thenReturn(assistantMessage2)
        `when`(chatResponse2.result).thenReturn(generation2)

        `when`(chatModel.call(any(Prompt::class.java)))
            .thenReturn(chatResponse1)
            .thenReturn(chatResponse2)

        val response = aiService.generateExercise(type, topic, amount, null)

        // Verification of prompts
        val promptCaptor = ArgumentCaptor.forClass(Prompt::class.java)
        verify(chatModel, times(2)).call(promptCaptor.capture())

        val firstPrompt = promptCaptor.allValues[0].instructions[0].text
        assertThat(firstPrompt).contains("about topic \"$topic\"")

        val secondPrompt = promptCaptor.allValues[1].instructions[0].text
        assertThat(secondPrompt).contains("about $topic")

        // It should take 2 sentences. Since 1 and 2 are valid, it should return them.
        val lines = response.content.lines()
        assertThat(lines).hasSize(2)
        assertThat(response.content).contains("Sentence 1 [answer1] (hint1)")
        assertThat(response.content).contains("Sentence 2 [answer2] (hint2)")
        assertThat(response.content).doesNotContain("Sentence 3 [wrong] (hint3)")
    }

    @Test
    fun `test generateExercise with MULTIPLE_CHOICE`() {
        val type = ExerciseType.MULTIPLE_CHOICE
        val topic = "presente"
        val amount = 1
        
        val chatResponse1 = mock(ChatResponse::class.java)
        val generation1 = mock(Generation::class.java)
        val assistantMessage1 = mock(org.springframework.ai.chat.messages.AssistantMessage::class.java)
        
        `when`(assistantMessage1.text).thenReturn("Yo [hablo] {'hablo|hables|hablar'} español.\nTu [hablas] {'hablo|hablas|hablar'} español.")
        `when`(generation1.output).thenReturn(assistantMessage1)
        `when`(chatResponse1.result).thenReturn(generation1)

        val chatResponse2 = mock(ChatResponse::class.java)
        val generation2 = mock(Generation::class.java)
        val assistantMessage2 = mock(org.springframework.ai.chat.messages.AssistantMessage::class.java)
        
        `when`(assistantMessage2.text).thenReturn("hablo\nhablas")
        `when`(generation2.output).thenReturn(assistantMessage2)
        `when`(chatResponse2.result).thenReturn(generation2)

        `when`(chatModel.call(any(Prompt::class.java)))
            .thenReturn(chatResponse1)
            .thenReturn(chatResponse2)

        val response = aiService.generateExercise(type, topic, amount, null)

        assertThat(response.content).isEqualTo("Yo [hablo] {'hablo|hables|hablar'} español.")
    }
}
