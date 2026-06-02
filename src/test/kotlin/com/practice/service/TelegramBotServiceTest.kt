package com.practice.service

import com.practice.IntegrationTestBase
import com.practice.domain.ExerciseType
import com.practice.domain.TelegramUser
import com.practice.repository.TelegramUserRepository
import com.practice.repository.ExerciseSetRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.any
import org.mockito.Mockito.argThat
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.objects.Update

class TelegramBotServiceTest : IntegrationTestBase() {

    @Autowired
    private lateinit var telegramUserRepository: TelegramUserRepository

    @Autowired
    private lateinit var exerciseSetRepository: ExerciseSetRepository

    private fun <T> anyNotNull(defaultValue: T): T {
        any<T>()
        return defaultValue
    }

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

    @Test
    fun `test handleTopicSelectionOnly sets topic but not isSubscribed`() {
        val chatId = 67890L
        
        doReturn(null).`when`(telegramBotService).execute(any<org.telegram.telegrambots.meta.api.methods.send.SendMessage>())

        val update = mock(Update::class.java)
        val callbackQuery = mock(org.telegram.telegrambots.meta.api.objects.CallbackQuery::class.java)
        val message = mock(org.telegram.telegrambots.meta.api.objects.Message::class.java)
        
        `when`(update.hasCallbackQuery()).thenReturn(true)
        `when`(update.callbackQuery).thenReturn(callbackQuery)
        `when`(callbackQuery.data).thenReturn("set_topic:Ser vs Estar")
        `when`(callbackQuery.message).thenReturn(message)
        `when`(message.chatId).thenReturn(chatId)
        
        telegramBotService.onUpdateReceived(update)
        
        val updatedUser = telegramUserRepository.findByChatId(chatId)!!
        assertThat(updatedUser.topic).isEqualTo("Ser vs Estar")
        assertThat(updatedUser.isSubscribed).isFalse()
    }

    @Test
    fun `test handleTopicSelection sets topic and isSubscribed`() {
        val chatId = 13579L
        
        doReturn(null).`when`(telegramBotService).execute(any<org.telegram.telegrambots.meta.api.methods.send.SendMessage>())

        val update = mock(Update::class.java)
        val callbackQuery = mock(org.telegram.telegrambots.meta.api.objects.CallbackQuery::class.java)
        val message = mock(org.telegram.telegrambots.meta.api.objects.Message::class.java)
        
        `when`(update.hasCallbackQuery()).thenReturn(true)
        `when`(update.callbackQuery).thenReturn(callbackQuery)
        `when`(callbackQuery.data).thenReturn("subscribe_topic:Por vs Para")
        `when`(callbackQuery.message).thenReturn(message)
        `when`(message.chatId).thenReturn(chatId)
        
        telegramBotService.onUpdateReceived(update)
        
        val updatedUser = telegramUserRepository.findByChatId(chatId)!!
        assertThat(updatedUser.topic).isEqualTo("Por vs Para")
        assertThat(updatedUser.isSubscribed).isTrue()
    }
}
