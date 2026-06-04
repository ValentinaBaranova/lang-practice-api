package com.practice.service

import com.practice.domain.Attempt
import com.practice.dto.*
import com.practice.repository.AttemptRepository
import com.practice.repository.ExerciseSetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AttemptService(
    private val attemptRepository: AttemptRepository,
    private val exerciseSetRepository: ExerciseSetRepository,
    private val exerciseSetService: ExerciseSetService
) {

    @Transactional
    fun createAttempt(request: AttemptCreateRequest): AttemptResponse {
        val exerciseSet = exerciseSetRepository.findById(request.exerciseSetId)
            .orElseThrow { NoSuchElementException("Exercise set not found with id: ${request.exerciseSetId}") }

        val totalQuestions = exerciseSet.questions.sumOf { 
            if (it.gaps.isEmpty()) 1 else it.gaps.size 
        }

        val attempt = Attempt(
            exerciseSetId = exerciseSet.id!!,
            studentName = request.studentName,
            totalQuestions = totalQuestions,
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

        val questionGapCount = if (question.gaps.isEmpty()) 1 else question.gaps.size
        var correctCount = 0

        val gapAnswerResponses = request.answers.map { answerReq ->
            val isCorrect = if (question.gaps.size <= 1) {
                exerciseSetService.isAnswerCorrect(question, answerReq.answer)
            } else {
                val gap = question.gaps.find { it.index == answerReq.index }
                gap != null && exerciseSetService.normalizeText(gap.correctAnswer)
                    .equals(exerciseSetService.normalizeText(answerReq.answer), ignoreCase = true)
            }

            if (isCorrect) correctCount++

            GapAnswerResponse(answerReq.index, answerReq.answer, isCorrect)
        }

        // Update attempt statistics
        attempt.answeredQuestions = (attempt.answeredQuestions ?: 0) + questionGapCount
        attempt.correctAnswers = (attempt.correctAnswers ?: 0) + correctCount
        attemptRepository.save(attempt)

        return AttemptQuestionResponse(
            id = UUID.randomUUID(),
            attemptId = attemptId,
            questionId = request.questionId,
            answers = gapAnswerResponses
        )
    }


    @Transactional(readOnly = true)
    fun getAttempt(id: UUID): AttemptResponse {
        return attemptRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { NoSuchElementException("Attempt not found with id: $id") }
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

}
