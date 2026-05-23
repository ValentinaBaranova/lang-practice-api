package com.practice.service

import com.practice.IntegrationTestBase
import com.practice.domain.TelegramUser
import com.practice.repository.TelegramUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.argThat
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.telegram.telegrambots.meta.api.objects.Update

class TelegramBotServiceTest : IntegrationTestBase() {

    @Autowired
    private lateinit var telegramUserRepository: TelegramUserRepository

    @MockitoSpyBean
    private lateinit var telegramBotService: TelegramBotService

    @Test
    fun `test handleUnsubscribe sets isSubscribed to false`() {
        val chatId = 12345L
        val user = TelegramUser(chatId = chatId, isSubscribed = true)
        telegramUserRepository.save(user)
        
        // Mock the execute method to avoid actually calling Telegram API
        doReturn(null).`when`(telegramBotService).execute(any<org.telegram.telegrambots.meta.api.methods.send.SendMessage>())

        val update = mock(Update::class.java)
        val message = mock(org.telegram.telegrambots.meta.api.objects.Message::class.java)
        `when`(update.hasMessage()).thenReturn(true)
        `when`(update.message).thenReturn(message)
        `when`(message.hasText()).thenReturn(true)
        `when`(message.text).thenReturn("/unsubscribe")
        `when`(message.chatId).thenReturn(chatId)
        
        telegramBotService.onUpdateReceived(update)
        
        val updatedUser = telegramUserRepository.findByChatId(chatId)!!
        assertThat(updatedUser.isSubscribed).isFalse()
    }

    @Test
    fun `test handleUnsubscribe when not subscribed`() {
        val chatId = 54321L
        // User not in DB
        
        doReturn(null).`when`(telegramBotService).execute(any<org.telegram.telegrambots.meta.api.methods.send.SendMessage>())

        val update = mock(Update::class.java)
        val message = mock(org.telegram.telegrambots.meta.api.objects.Message::class.java)
        `when`(update.hasMessage()).thenReturn(true)
        `when`(update.message).thenReturn(message)
        `when`(message.hasText()).thenReturn(true)
        `when`(message.text).thenReturn("/unsubscribe")
        `when`(message.chatId).thenReturn(chatId)
        
        telegramBotService.onUpdateReceived(update)
        
        val updatedUser = telegramUserRepository.findByChatId(chatId)
        assertThat(updatedUser).isNull()
        
        verify(telegramBotService).execute(argThat<org.telegram.telegrambots.meta.api.methods.send.SendMessage> { 
            it.text == "You are not currently subscribed." 
        })
    }
}
