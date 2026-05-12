package com.practice.repository

import com.practice.domain.ExerciseSet
import com.practice.domain.ExerciseVisibility
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ExerciseSetRepository : JpaRepository<ExerciseSet, UUID> {
    fun findByShareSlug(shareSlug: String): ExerciseSet?
    fun findByTeacherIdOrderByCreatedAtDesc(teacherId: UUID): List<ExerciseSet>
    fun findAllByOrderByCreatedAtDesc(): List<ExerciseSet>
    fun findByVisibilityOrderByCreatedAtDesc(visibility: ExerciseVisibility): List<ExerciseSet>
}
