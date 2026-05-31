package com.practice.service

import com.practice.domain.Attempt
import com.practice.domain.AttemptQuestion
import com.practice.dto.AttemptCreateRequest
import com.practice.dto.AttemptQuestionResponse
import com.practice.dto.AttemptResponse
import com.practice.dto.QuestionAnswerRequest
import com.practice.repository.AttemptQuestionRepository
import com.practice.repository.AttemptRepository
import com.practice.repository.ExerciseSetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AttemptService(
    private val attemptRepository: AttemptRepository,
    private val attemptQuestionRepository: AttemptQuestionRepository,
    private val exerciseSetRepository: ExerciseSetRepository,
    private val exerciseSetService: ExerciseSetService
) {

    @Transactional
    fun createAttempt(request: AttemptCreateRequest): AttemptResponse {
        val exerciseSet = exerciseSetRepository.findById(request.exerciseSetId)
            .orElseThrow { NoSuchElementException("Exercise set not found with id: ${request.exerciseSetId}") }

        val attempt = Attempt(
            exerciseSetId = exerciseSet.id!!,
            studentName = request.studentName,
            totalQuestions = exerciseSet.questions.size,
            answeredQuestions = 0,
            correctAnswers = 0
        )

        return attemptRepository.save(attempt).toResponse()
    }

    @Transactional
    fun submitAnswer(attemptId: UUID, request: QuestionAnswerRequest): AttemptQuestionResponse {
        val attempt = attemptRepository.findById(attemptId)
            .orElseThrow { NoSuchElementException("Attempt not found with id: $attemptId") }

        val exerciseSet = exerciseSetRepository.findById(attempt.exerciseSetId)
            .orElseThrow { NoSuchElementException("Exercise set not found for attempt: $attemptId") }

        val question = exerciseSet.questions.find { it.id == request.questionId }
            .let { it ?: throw NoSuchElementException("Question not found with id: ${request.questionId}") }

        val isCorrect = exerciseSetService.isAnswerCorrect(question, request.answer)

        val attemptQuestion = AttemptQuestion(
            attemptId = attemptId,
            questionId = request.questionId,
            answer = request.answer,
            isCorrect = isCorrect
        )

        val savedQuestion = attemptQuestionRepository.save(attemptQuestion)

        // Update attempt statistics
        attempt.answeredQuestions = (attempt.answeredQuestions ?: 0) + 1
        if (isCorrect) {
            attempt.correctAnswers = (attempt.correctAnswers ?: 0) + 1
        }
        attemptRepository.save(attempt)

        return savedQuestion.toResponse()
    }


    @Transactional(readOnly = true)
    fun getAttempt(id: UUID): AttemptResponse {
        return attemptRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { NoSuchElementException("Attempt not found with id: $id") }
    }

    @Transactional(readOnly = true)
    fun getAttemptQuestions(attemptId: UUID): List<AttemptQuestionResponse> {
        return attemptQuestionRepository.findAllByAttemptId(attemptId)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getAttemptsByExerciseSetId(exerciseSetId: UUID): List<AttemptResponse> {
        return attemptRepository.findAllByExerciseSetId(exerciseSetId)
            .map { it.toResponse() }
    }

    private fun Attempt.toResponse() = AttemptResponse(
        id = this.id!!,
        exerciseSetId = this.exerciseSetId,
        studentName = this.studentName,
        totalQuestions = this.totalQuestions,
        answeredQuestions = this.answeredQuestions,
        correctAnswers = this.correctAnswers
    )

    private fun AttemptQuestion.toResponse() = AttemptQuestionResponse(
        id = this.id!!,
        attemptId = this.attemptId,
        questionId = this.questionId,
        answer = this.answer,
        isCorrect = this.isCorrect
    )
}
