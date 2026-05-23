package com.practice.config

import com.practice.service.TelegramBotService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Configuration
@ConditionalOnProperty(name = ["telegram.bot.enabled"], havingValue = "true", matchIfMissing = true)
class TelegramBotConfig {

    @Bean
    fun telegramBotsApi(telegramBotService: TelegramBotService): TelegramBotsApi {
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        botsApi.registerBot(telegramBotService)
        return botsApi
    }
}
