package com.practice.service

import com.practice.domain.ExerciseType
import com.practice.domain.ExerciseVisibility
import com.practice.domain.TelegramUser
import com.practice.dto.ExerciseSetCreateRequest
import com.practice.repository.TelegramUserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.time.OffsetDateTime
import java.util.logging.Logger

@Service
class TelegramBotService(
    private val telegramUserRepository: TelegramUserRepository,
    private val aiService: AiService,
    private val exerciseSetService: ExerciseSetService,
    @Value("\${telegram.bot.token:}")
    private val botToken: String,
    @Value("\${telegram.bot.username:}")
    private val botUsername: String,
    @Value("\${application.base-url:http://localhost:3000}")
    private val baseUrl: String
) : TelegramLongPollingBot(botToken) {

    private val logger = Logger.getLogger(TelegramBotService::class.java.name)

    companion object {
        val TOPICS = listOf(
            "Presente",
            "Pretérito Indefinido",
            "Pretérito Imperfecto",
            "Pretérito Perfecto",
            "Futuro Simple",
            "Imperativo",
            "Imperativo Negativo",
            "Subjuntivo Presente",
            "Ser vs Estar",
            "Por vs Para",
        )
        const val TEACHER_ACCESS_CODE = "DEFAULT001"
    }

    override fun getBotUsername(): String = botUsername

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val messageText = update.message.text
            val chatId = update.message.chatId

            when {
                messageText == "/start" -> handleStart(chatId)
                messageText == "/subscribe" -> handleSubscribe(chatId)
                messageText == "/unsubscribe" -> handleUnsubscribe(chatId)
                messageText == "/practice" -> handlePractice(chatId)
                messageText == "/topic" -> handleTopic(chatId)
                else -> sendHelpMessage(chatId)
            }
        } else if (update.hasCallbackQuery()) {
            val callbackData = update.callbackQuery.data
            val chatId = update.callbackQuery.message.chatId
            
            if (callbackData.startsWith("subscribe_topic:")) {
                val topic = callbackData.substringAfter("subscribe_topic:")
                handleTopicSelection(chatId, topic)
            } else if (callbackData.startsWith("set_topic:")) {
                val topic = callbackData.substringAfter("set_topic:")
                handleTopicSelectionOnly(chatId, topic)
            }
        }
    }

    private fun handleStart(chatId: Long) {
        val user = telegramUserRepository.findByChatId(chatId) ?: TelegramUser(chatId = chatId)
        telegramUserRepository.save(user)
        
        sendMessage(chatId, "Welcome to Language Practice Bot! 🇦🇷\n\nI can send you daily exercises in Argentine Spanish.\nUse /topic to set your preferred topic, /subscribe for daily exercises, or /practice to get an exercise right now.")
    }

    private fun handleSubscribe(chatId: Long) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = "Choose a topic to subscribe for daily exercises:"
        
        val markup = InlineKeyboardMarkup()
        val rows = TOPICS.map { topic ->
            val button = InlineKeyboardButton()
            button.text = topic
            button.callbackData = "subscribe_topic:$topic"
            listOf(button)
        }
        markup.keyboard = rows
        message.replyMarkup = markup
        
        execute(message)
    }

    private fun handleTopicSelection(chatId: Long, topic: String) {
        val user = telegramUserRepository.findByChatId(chatId) ?: TelegramUser(chatId = chatId)
        user.topic = topic
        user.isSubscribed = true
        telegramUserRepository.save(user)
        
        sendMessage(chatId, "Success! You are now subscribed to '$topic'. You will receive a new exercise once per day.\n\nYou can also practice right now using /practice")
    }

    private fun handleTopic(chatId: Long) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = "Choose your preferred topic for practice:"
        
        val markup = InlineKeyboardMarkup()
        val rows = TOPICS.map { topic ->
            val button = InlineKeyboardButton()
            button.text = topic
            button.callbackData = "set_topic:$topic"
            listOf(button)
        }
        markup.keyboard = rows
        message.replyMarkup = markup
        
        execute(message)
    }

    private fun handleTopicSelectionOnly(chatId: Long, topic: String) {
        val user = telegramUserRepository.findByChatId(chatId) ?: TelegramUser(chatId = chatId)
        user.topic = topic
        telegramUserRepository.save(user)
        
        sendMessage(chatId, "Topic set to '$topic'. You can now use /practice to get an exercise on this topic.")
    }

    private fun handleUnsubscribe(chatId: Long) {
        val user = telegramUserRepository.findByChatId(chatId)
        if (user != null && user.isSubscribed) {
            user.isSubscribed = false
            telegramUserRepository.save(user)
            sendMessage(chatId, "You have been unsubscribed from daily exercises. You can always subscribe again using /subscribe")
        } else {
            sendMessage(chatId, "You are not currently subscribed.")
        }
    }

    private fun handlePractice(chatId: Long) {
        val user = telegramUserRepository.findByChatId(chatId)
        val topic = user?.topic ?: "Pretérito Indefinido"
        
        sendMessage(chatId, "Generating an exercise for you on '$topic'...")
        
        try {
            val shareLink = generateAndSaveExercise(topic)
            sendMessage(chatId, "Here is your practice link: $shareLink\n¡Suerte!")
        } catch (e: Exception) {
            logger.severe("Failed to generate exercise: ${e.message}")
            sendMessage(chatId, "Sorry, I couldn't generate an exercise right now. Please try again later.")
        }
    }

    private fun generateAndSaveExercise(topic: String): String {
        val aiResponse = aiService.generateExercise(
            type = "FILL_GAP_TEXT",
            topic = topic,
            amount = 10,
            teacherAccessCode = TEACHER_ACCESS_CODE
        )

        if (aiResponse.content.startsWith("ERROR:")) {
            throw RuntimeException(aiResponse.content)
        }

        val request = ExerciseSetCreateRequest(
            title = "Daily Practice: $topic",
            type = ExerciseType.FILL_GAP_TEXT,
            visibility = ExerciseVisibility.PRIVATE,
            bulkInput = aiResponse.content,
            teacherAccessCode = TEACHER_ACCESS_CODE
        )

        val exerciseSet = exerciseSetService.createExerciseSet(request)
        return "$baseUrl/es/practice/${exerciseSet.shareSlug}"
    }

    @Scheduled(cron = "\${scheduling.cron.send-daily-exercises:0 0 9 * * *}") // Configurable via application.yml; default: every day at 9 AM
    @Transactional
    fun sendDailyExercises() {
        val subscribers = telegramUserRepository.findByIsSubscribedTrue()
        logger.info("Sending daily exercises to ${subscribers.size} subscribers")
        
        for (user in subscribers) {
            val topic = user.topic ?: continue
            try {
                val shareLink = generateAndSaveExercise(topic)
                sendMessage(user.chatId, "Good morning! ☀️ Here is your daily exercise on '$topic':\n$shareLink")
                user.lastExerciseSentAt = OffsetDateTime.now()
                telegramUserRepository.save(user)
            } catch (e: Exception) {
                logger.severe("Failed to send daily exercise to ${user.chatId}: ${e.message}")
            }
        }
    }

    private fun sendHelpMessage(chatId: Long) {
        sendMessage(chatId, "Available commands:\n/start - Start the bot\n/topic - Set preferred topic\n/subscribe - Choose topic for daily exercises\n/unsubscribe - Stop daily exercises\n/practice - Practice immediately")
    }

    private fun sendMessage(chatId: Long, text: String) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = text
        execute(message)
    }
}
