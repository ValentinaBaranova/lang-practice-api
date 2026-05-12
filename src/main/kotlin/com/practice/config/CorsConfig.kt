package com.practice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfiguration(
    @Value("\${application.cors.allowed-origins:*}") private val allowedOriginsProp: String
) {
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                val origins = allowedOriginsProp.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                val mapping = registry.addMapping("/api/**")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)

                if (origins.size == 1 && origins[0] == "*") {
                    mapping.allowedOriginPatterns("*")
                } else {
                    mapping.allowedOrigins(*origins.toTypedArray())
                }
            }
        }
    }
}
