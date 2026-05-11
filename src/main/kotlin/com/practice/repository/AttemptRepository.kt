package com.practice.repository

import com.practice.domain.Attempt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AttemptRepository : JpaRepository<Attempt, UUID> {
    fun findAllByExerciseSetId(exerciseSetId: UUID): List<Attempt>
}
