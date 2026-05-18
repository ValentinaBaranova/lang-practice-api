package com.practice.repository

import com.practice.domain.ExerciseSet
import com.practice.domain.ExerciseVisibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ExerciseSetRepository : JpaRepository<ExerciseSet, UUID> {
    fun findByShareSlug(shareSlug: String): ExerciseSet?
    fun findByTeacherId(teacherId: UUID, pageable: Pageable): Page<ExerciseSet>
    fun findByVisibility(visibility: ExerciseVisibility, pageable: Pageable): Page<ExerciseSet>
}
