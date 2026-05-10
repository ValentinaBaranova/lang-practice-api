package com.practice.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "attempt")
class Attempt(
    @Id
    @GeneratedValue
    var id: UUID? = null,

    @Column(name = "exercise_set_id", nullable = false)
    var exerciseSetId: UUID,

    @Column(name = "student_name")
    var studentName: String? = null,

    var correctCount: Int? = null,
    var totalCount: Int? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: OffsetDateTime? = null
)
