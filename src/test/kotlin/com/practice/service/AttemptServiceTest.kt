package com.practice.service

import com.practice.IntegrationTestBase
import com.practice.domain.Attempt
import com.practice.domain.ExerciseQuestion
import com.practice.domain.ExerciseSet
import com.practice.domain.ExerciseType
import com.practice.dto.GapAnswerRequest
import com.practice.dto.QuestionAnswerRequest
import com.practice.repository.AttemptRepository
import com.practice.repository.ExerciseSetRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class AttemptServiceTest : IntegrationTestBase() {

    @Autowired
    private lateinit var attemptRepository: AttemptRepository

    @Autowired
    private lateinit var exerciseSetRepository: ExerciseSetRepository

    @Autowired
    private lateinit var attemptService: AttemptService

    @Test
    fun `submitAnswer should accept answer with different case and without accents`() {
        // Given
        val teacherId: UUID? = null
        val questionId = UUID.randomUUID()
        
        val question = ExerciseQuestion(
            id = questionId,
            prompt = "Yo ___ la puerta.",
            sourceText = "Yo [Abrí] la puerta.",
            gaps = listOf(com.practice.domain.Gap(0, "Abrí"))
        )
        
        val exerciseSet = exerciseSetRepository.save(ExerciseSet(
            teacherId = teacherId,
            title = "Test Set",
            type = ExerciseType.FILL_GAP_TEXT,
            questions = listOf(question)
        ))
        
        val attempt = attemptRepository.save(Attempt(
            exerciseSetId = exerciseSet.id!!,
            studentName = "Student",
            totalQuestions = 1,
            answeredQuestions = 0,
            correctAnswers = 0
        ))

        // When
        val request = QuestionAnswerRequest(questionId = questionId, answers = listOf(GapAnswerRequest(0, "abri")))
        val response = attemptService.submitAnswer(attempt.id!!, request)

        // Then
        assertTrue(response.answers.all { it.isCorrect })
        val updatedAttempt = attemptRepository.findById(attempt.id!!).get()
        assertEquals(1, updatedAttempt.answeredQuestions)
        assertEquals(1, updatedAttempt.correctAnswers)
    }

    @Test
    fun `submitAnswer should reject incorrect answer`() {
        // Given
        val teacherId: UUID? = null
        val questionId = UUID.randomUUID()
        
        val question = ExerciseQuestion(
            id = questionId,
            prompt = "The [quick] brown fox.",
            sourceText = "The [quick] brown fox.",
            gaps = listOf(com.practice.domain.Gap(0, "quick"))
        )
        
        val exerciseSet = exerciseSetRepository.save(ExerciseSet(
            teacherId = teacherId,
            title = "Test Set",
            type = ExerciseType.FILL_GAP_TEXT,
            questions = listOf(question)
        ))
        
        val attempt = attemptRepository.save(Attempt(
            exerciseSetId = exerciseSet.id!!,
            studentName = "Student",
            totalQuestions = 1,
            answeredQuestions = 0,
            correctAnswers = 0
        ))

        // When
        val request = QuestionAnswerRequest(questionId = questionId, answers = listOf(GapAnswerRequest(0, "slow")))
        val response = attemptService.submitAnswer(attempt.id!!, request)

        // Then
        assertFalse(response.answers.all { it.isCorrect })
        val updatedAttempt = attemptRepository.findById(attempt.id!!).get()
        assertEquals(1, updatedAttempt.answeredQuestions)
        assertEquals(0, updatedAttempt.correctAnswers)
    }

    @Test
    fun `submitAnswer should throw NoSuchElementException when attempt not found`() {
        val attemptId = UUID.randomUUID()
        val request = QuestionAnswerRequest(questionId = UUID.randomUUID(), answers = listOf(GapAnswerRequest(0, "any")))
        
        assertThrows(NoSuchElementException::class.java) {
            attemptService.submitAnswer(attemptId, request)
        }
    }
    
    @Test
    fun `normalize should handle accents and case correctly`() {
        // Given
        val teacherId: UUID? = null
        val questionId = UUID.randomUUID()

        val question = ExerciseQuestion(id = questionId, prompt = "", sourceText = "", gaps = listOf(com.practice.domain.Gap(0, "ÁéÍóÚñ")))
        val exerciseSet = exerciseSetRepository.save(ExerciseSet(
            teacherId = teacherId,
            title = "Accent Set",
            type = ExerciseType.FILL_GAP_TEXT,
            questions = listOf(question)
        ))

        val attempt = attemptRepository.save(Attempt(
            exerciseSetId = exerciseSet.id!!,
            studentName = "Student",
            totalQuestions = 1,
            answeredQuestions = 0,
            correctAnswers = 0
        ))

        assertTrue(attemptService.submitAnswer(attempt.id!!, QuestionAnswerRequest(questionId, listOf(GapAnswerRequest(0, "aeioun")))).answers.all { it.isCorrect })
        assertTrue(attemptService.submitAnswer(attempt.id!!, QuestionAnswerRequest(questionId, listOf(GapAnswerRequest(0, "AEIOUN")))).answers.all { it.isCorrect })
    }

    @Test
    fun `normalize should collapse multiple spaces`() {
        val teacherId: UUID? = null
        val questionId = UUID.randomUUID()
        val question = ExerciseQuestion(
            id = questionId,
            prompt = "The quick brown fox.",
            sourceText = "The [quick] brown fox.",
            gaps = listOf(com.practice.domain.Gap(0, "quick"))
        )
        val exerciseSet = exerciseSetRepository.save(ExerciseSet(
            teacherId = teacherId,
            title = "Space Set",
            type = ExerciseType.FILL_GAP_TEXT,
            questions = listOf(question)
        ))
        val attempt = attemptRepository.save(Attempt(
            exerciseSetId = exerciseSet.id!!,
            studentName = "Student",
            totalQuestions = 1,
            answeredQuestions = 0,
            correctAnswers = 0
        ))

        assertTrue(attemptService.submitAnswer(attempt.id!!, QuestionAnswerRequest(questionId, listOf(GapAnswerRequest(0, "  The  quick  brown  fox.  ")))).answers.all { it.isCorrect })
        assertTrue(attemptService.submitAnswer(attempt.id!!, QuestionAnswerRequest(questionId, listOf(GapAnswerRequest(0, "  quick  ")))).answers.all { it.isCorrect })
    }

    @Test
    fun `submitAnswer should support multiple gap answers`() {
        val teacherId: UUID? = null
        val questionId = UUID.randomUUID()
        
        val question = ExerciseQuestion(
            id = questionId,
            prompt = "Yo ___ y tú ___.",
            sourceText = "Yo [estudio] y tú [trabajas].",
            gaps = listOf(
                com.practice.domain.Gap(0, "estudio"),
                com.practice.domain.Gap(1, "trabajas")
            )
        )
        
        val exerciseSet = exerciseSetRepository.save(ExerciseSet(
            teacherId = teacherId,
            title = "Multi Gap Set",
            type = ExerciseType.FILL_GAP_TEXT,
            questions = listOf(question)
        ))
        
        val attempt = attemptRepository.save(Attempt(
            exerciseSetId = exerciseSet.id!!,
            studentName = "Student",
            totalQuestions = 1,
            answeredQuestions = 0,
            correctAnswers = 0
        ))

        // When
        val request = QuestionAnswerRequest(
            questionId = questionId, 
            answers = listOf(
                com.practice.dto.GapAnswerRequest(0, "estudio"),
                com.practice.dto.GapAnswerRequest(1, "trabajas")
            )
        )
        val response = attemptService.submitAnswer(attempt.id!!, request)

        // Then
        assertTrue(response.answers.all { it.isCorrect })
        assertEquals("estudio", response.answers[0].answer)
        assertEquals("trabajas", response.answers[1].answer)
        assertNotNull(response.answers)
        assertEquals(2, response.answers?.size)
        assertTrue(response.answers!![0].isCorrect)
        assertTrue(response.answers!![1].isCorrect)
        
        val updatedAttempt = attemptRepository.findById(attempt.id!!).get()
        assertEquals(2, updatedAttempt.correctAnswers)
        assertEquals(2, updatedAttempt.answeredQuestions)
    }

    @Test
    fun `submitAnswer should handle partial correctness in multi-gap questions`() {
        val teacherId: UUID? = null
        val questionId = UUID.randomUUID()
        
        val question = ExerciseQuestion(
            id = questionId,
            prompt = "Yo ___ y tú ___.",
            sourceText = "Yo [estudio] y tú [trabajas].",
            gaps = listOf(
                com.practice.domain.Gap(0, "estudio"),
                com.practice.domain.Gap(1, "trabajas")
            )
        )
        
        val exerciseSet = exerciseSetRepository.save(ExerciseSet(
            teacherId = teacherId,
            title = "Partial Multi Gap Set",
            type = ExerciseType.FILL_GAP_TEXT,
            questions = listOf(question)
        ))
        
        val attempt = attemptService.createAttempt(com.practice.dto.AttemptCreateRequest(exerciseSet.id!!, "Student"))
        
        // When
        val request = QuestionAnswerRequest(
            questionId = questionId, 
            answers = listOf(
                com.practice.dto.GapAnswerRequest(0, "estudio"),
                com.practice.dto.GapAnswerRequest(1, "wrong")
            )
        )
        val response = attemptService.submitAnswer(attempt.id, request)

        // Then
        assertFalse(response.answers.all { it.isCorrect })
        assertNotNull(response.answers)
        assertEquals(2, response.answers?.size)
        assertTrue(response.answers!![0].isCorrect)
        assertFalse(response.answers!![1].isCorrect)
        
        val updatedAttempt = attemptService.getAttempt(attempt.id)
        assertEquals(2, updatedAttempt.totalQuestions)
        assertEquals(2, updatedAttempt.answeredQuestions)
        assertEquals(1, updatedAttempt.correctAnswers)
    }
}
