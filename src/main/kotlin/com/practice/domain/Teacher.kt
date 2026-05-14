package com.practice.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.random.Random

@Entity
@Table(name = "teacher")
class Teacher(
    @Id
    @GeneratedValue
    var id: UUID? = null,

    @Column(nullable = false, unique = true, length = 10)
    var accessCode: String = generateAccessCode(),

    @Column(nullable = false)
    var name: String,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime? = null,

    @Column(name = "ai_requests_count", nullable = false)
    var aiRequestsCount: Int = 0,

    @Column(name = "last_ai_request_at")
    var lastAiRequestAt: OffsetDateTime? = null
) {
    companion object {
        fun generateAccessCode(): String {
            val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return (1..10)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
        }
    }
}
