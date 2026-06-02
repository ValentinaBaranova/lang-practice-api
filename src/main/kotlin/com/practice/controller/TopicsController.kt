package com.practice.controller

import com.practice.config.TopicsConfig
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/topics")
class TopicsController(
    private val topicsConfig: TopicsConfig
) {
    @GetMapping
    fun getTopics(): List<String> {
        return topicsConfig.topics
    }
}
