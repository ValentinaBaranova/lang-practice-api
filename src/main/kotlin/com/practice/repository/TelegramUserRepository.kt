package com.practice.repository

import com.practice.domain.TelegramUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TelegramUserRepository : JpaRepository<TelegramUser, UUID> {
    fun findByChatId(chatId: Long): TelegramUser?
    fun findByIsSubscribedTrue(): List<TelegramUser>
}
