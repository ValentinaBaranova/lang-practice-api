package com.practice.service

import com.practice.IntegrationTestBase
import com.practice.domain.Attempt
import com.practice.domain.ExerciseQuestion
import com.practice.domain.ExerciseSet
import com.practice.domain.ExerciseType
import com.practice.dto.QuestionAnswerRequest
import com.practice.repository.AttemptQuestionRepository
import com.practice.repository.AttemptRepository
import com.practice.repository.ExerciseSetRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class AttemptServiceTest : IntegrationTestBase() {

    private val defaultTeacherId = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @Autowired
    private lateinit var attemptRepository: AttemptRepository

    @Autowired
    private lateinit var attemptQuestionRepository: AttemptQuestionRepository

    @Autowired
    private lateinit var exerciseSetRepository: ExerciseSetRepository

    @Autowired
    private lateinit var attemptService: AttemptService

    @Test
    fun `submitAnswer should accept answer with different case and without accents`() {
        // Given
        val teacherId = defaultTeacherId
        val questionId = UUID.randomUUID()
        
        val question = ExerciseQuestion(
            id = questionId,
            prompt = "Yo ___ la puerta.",
            correctAnswer = "Abrí",
            sourceText = "Yo [Abrí] la puerta."
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
        val request = QuestionAnswerRequest(questionId = questionId, answer = "abri")
        val response = attemptService.submitAnswer(attempt.id!!, request)

        // Then
        assertTrue(response.isCorrect)
        val updatedAttempt = attemptRepository.findById(attempt.id!!).get()
        assertEquals(1, updatedAttempt.answeredQuestions)
        assertEquals(1, updatedAttempt.correctAnswers)
    }

    @Test
    fun `submitAnswer should reject incorrect answer`() {
        // Given
        val teacherId = defaultTeacherId
        val questionId = UUID.randomUUID()
        
        val question = ExerciseQuestion(
            id = questionId,
            prompt = "The [quick] brown fox.",
            correctAnswer = "quick",
            sourceText = "The [quick] brown fox."
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
        val request = QuestionAnswerRequest(questionId = questionId, answer = "slow")
        val response = attemptService.submitAnswer(attempt.id!!, request)

        // Then
        assertFalse(response.isCorrect)
        val updatedAttempt = attemptRepository.findById(attempt.id!!).get()
        assertEquals(1, updatedAttempt.answeredQuestions)
        assertEquals(0, updatedAttempt.correctAnswers)
    }

    @Test
    fun `submitAnswer should throw NoSuchElementException when attempt not found`() {
        val attemptId = UUID.randomUUID()
        val request = QuestionAnswerRequest(questionId = UUID.randomUUID(), answer = "any")
        
        assertThrows(NoSuchElementException::class.java) {
            attemptService.submitAnswer(attemptId, request)
        }
    }
    
    @Test
    fun `normalize should handle accents and case correctly`() {
        // Given
        val teacherId = defaultTeacherId
        val questionId = UUID.randomUUID()
        
        val question = ExerciseQuestion(id = questionId, prompt = "", correctAnswer = "ÁéÍóÚñ", sourceText = "")
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

        assertTrue(attemptService.submitAnswer(attempt.id!!, QuestionAnswerRequest(questionId, "aeioun")).isCorrect)
        assertTrue(attemptService.submitAnswer(attempt.id!!, QuestionAnswerRequest(questionId, "AEIOUN")).isCorrect)
    }
}
