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
@Table(name = "attempt_question")
class AttemptQuestion(
    @Id
    @GeneratedValue
    var id: UUID? = null,

    @Column(name = "attempt_id", nullable = false)
    var attemptId: UUID,

    @Column(name = "question_id", nullable = false)
    var questionId: String,

    @Column(name = "answer", nullable = false)
    var answer: String,

    @Column(name = "is_correct", nullable = false)
    var isCorrect: Boolean,

    @CreationTimestamp
    @Column(name = "answered_at", updatable = false)
    var answeredAt: OffsetDateTime? = null
)
