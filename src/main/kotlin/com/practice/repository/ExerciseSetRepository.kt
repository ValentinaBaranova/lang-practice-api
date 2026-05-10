package com.practice.repository

import com.practice.domain.ExerciseSet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ExerciseSetRepository : JpaRepository<ExerciseSet, UUID>
