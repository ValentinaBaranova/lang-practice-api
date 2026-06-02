package com.practice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "application")
class TopicsConfig {
    var topics: List<String> = mutableListOf()
}
