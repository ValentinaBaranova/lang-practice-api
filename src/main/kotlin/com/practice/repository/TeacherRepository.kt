package com.practice.repository

import com.practice.domain.Teacher
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TeacherRepository : JpaRepository<Teacher, UUID>
