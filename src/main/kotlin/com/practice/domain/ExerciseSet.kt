package com.practice.domain

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "exercise_set")
class ExerciseSet(
    @Id
    @GeneratedValue
    var id: UUID? = null,

    @Column(name = "teacher_id", nullable = false)
    var teacherId: UUID,

    @Column(nullable = false)
    var title: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: ExerciseType,

    @Type(JsonBinaryType::class)
    @Column(columnDefinition = "jsonb", nullable = false)
    var questions: List<ExerciseQuestion>,

    @Column(name = "share_slug", unique = true)
    var shareSlug: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime? = null
)
