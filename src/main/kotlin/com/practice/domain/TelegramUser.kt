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

@Entity
@Table(name = "telegram_user")
class TelegramUser(
    @Id
    @GeneratedValue
    var id: UUID? = null,

    @Column(name = "chat_id", nullable = false, unique = true)
    var chatId: Long,

    @Column(name = "topic")
    var topic: String? = null,

    @Column(name = "is_subscribed", nullable = false)
    var isSubscribed: Boolean = false,

    @Column(name = "last_exercise_sent_at")
    var lastExerciseSentAt: OffsetDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime? = null
)
