package com.practice.service

import com.practice.domain.ExerciseSet
import com.practice.domain.ExerciseType
import com.practice.dto.ExerciseSetCreateRequest
import com.practice.repository.ExerciseSetRepository
import com.practice.repository.TeacherRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID

class ExerciseSetServiceTest {

    private val exerciseSetRepository = mock(ExerciseSetRepository::class.java)
    private val teacherRepository = mock(TeacherRepository::class.java)
    private val exerciseSetService = ExerciseSetService(exerciseSetRepository, teacherRepository)

    @Test
    fun `test createExerciseSet generates slug from title`() {
        val request = ExerciseSetCreateRequest(
            title = "Spanish Preterite",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = "Yo [hablé] español."
        )

        setupMockSave()
        `when`(exerciseSetRepository.findByShareSlug(anyString())).thenReturn(null)

        val response = exerciseSetService.createExerciseSet(null, request)

        assertThat(response.shareSlug).startsWith("spanish-preterite-")
        assertThat(response.shareSlug?.length).isEqualTo("spanish-preterite-".length + 6)
    }

    @Test
    fun `test generateUniqueShareSlug handles long titles and trailing hyphens`() {
        val request = ExerciseSetCreateRequest(
            title = "A".repeat(49) + "-something",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = "Yo [hablé] español."
        )

        setupMockSave()
        `when`(exerciseSetRepository.findByShareSlug(anyString())).thenReturn(null)

        val response = exerciseSetService.createExerciseSet(null, request)
        
        assertThat(response.shareSlug).startsWith("a".repeat(49) + "-")
        assertThat(response.shareSlug).doesNotContain("--")
    }

    @Test
    fun `test parseBulkInput with multiple gaps`() {
        val bulkInput = "Cuando yo era chico, siempre [jugaba] (yo, jugar) en la plaza. Cuando tu eres chico, siempre [estudiabas] (vos, estudiar)"
        val type = ExerciseType.FILL_GAP_TEXT
        
        val questions = exerciseSetService.parseBulkInput(bulkInput, type)
        
        assertThat(questions).hasSize(1)
        val question = questions[0]
        assertThat(question.gaps).hasSize(2)
        assertThat(question.gaps[0].correctAnswer).isEqualTo("jugaba")
        assertThat(question.gaps[1].correctAnswer).isEqualTo("estudiabas")
        assertThat(question.prompt).isEqualTo("Cuando yo era chico, siempre ___ (yo, jugar) en la plaza. Cuando tu eres chico, siempre ___ (vos, estudiar)")
    }

    @Test
    fun `test isAnswerCorrect with multiple gaps`() {
        val bulkInput = "Yo [estudio] (yo, estudiar). Vos [trabajás] (vos, trabajar)."
        val type = ExerciseType.FILL_GAP_TEXT
        val question = exerciseSetService.parseBulkInput(bulkInput, type)[0]
        
        // Full correct sentence
        assertThat(exerciseSetService.isAnswerCorrect(question, "Yo estudio (yo, estudiar). Vos trabajás (vos, trabajar).")).isTrue()
        
        // Incorrect full sentence
        assertThat(exerciseSetService.isAnswerCorrect(question, "Yo estudio (yo, estudiar). Vos nado (vos, trabajar).")).isFalse()
        
        // Only first gap (should be false now because it's multiple gaps)
        assertThat(exerciseSetService.isAnswerCorrect(question, "estudio")).isFalse()
    }

    @Test
    fun `test isAnswerCorrect with list of GapAnswerRequest`() {
        val bulkInput = "Yo [estudio] (yo, estudiar). Vos [trabajás] (vos, trabajar)."
        val type = ExerciseType.FILL_GAP_TEXT
        val question = exerciseSetService.parseBulkInput(bulkInput, type)[0]
        
        val correctAnswers = listOf(
            com.practice.dto.GapAnswerRequest(0, "estudio"),
            com.practice.dto.GapAnswerRequest(1, "trabajás")
        )
        assertThat(exerciseSetService.isAnswerCorrect(question, correctAnswers)).isTrue()
        
        val incorrectAnswers = listOf(
            com.practice.dto.GapAnswerRequest(0, "estudio"),
            com.practice.dto.GapAnswerRequest(1, "nado")
        )
        assertThat(exerciseSetService.isAnswerCorrect(question, incorrectAnswers)).isFalse()

        val missingAnswers = listOf(
            com.practice.dto.GapAnswerRequest(0, "estudio")
        )
        assertThat(exerciseSetService.isAnswerCorrect(question, missingAnswers)).isFalse()
    }

    @Test
    fun `test isAnswerCorrect prefers gaps`() {
        val question = com.practice.domain.ExerciseQuestion(
            id = UUID.randomUUID(),
            prompt = "Yo ___ la puerta.",
            sourceText = "Yo [Abrí] la puerta.",
            gaps = listOf(com.practice.domain.Gap(0, "Abrí"))
        )
        
        // Should be correct because gaps says "Abrí"
        assertThat(exerciseSetService.isAnswerCorrect(question, "abrí")).isTrue()
    }

    @Test
    fun `test isAnswerCorrect works`() {
        val question = com.practice.domain.ExerciseQuestion(
            id = UUID.randomUUID(),
            prompt = "Yo ___ la puerta.",
            sourceText = "Yo [Abrí] la puerta.",
            gaps = listOf(com.practice.domain.Gap(0, "Abrí"))
        )
        
        assertThat(exerciseSetService.isAnswerCorrect(question, "abrí")).isTrue()
    }

    private fun setupMockSave() {
        `when`(exerciseSetRepository.save(any())).thenAnswer {
            val set = it.arguments[0] as ExerciseSet
            if (set.id == null) {
                set.id = UUID.randomUUID()
            }
            set
        }
    }
}
