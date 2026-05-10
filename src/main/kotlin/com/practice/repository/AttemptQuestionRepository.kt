package com.practice.repository

import com.practice.domain.AttemptQuestion
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AttemptQuestionRepository : JpaRepository<AttemptQuestion, UUID> {
    fun findAllByAttemptId(attemptId: UUID): List<AttemptQuestion>
}
