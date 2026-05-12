package com.practice.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, Any>> {
        val message = ex.message ?: "Invalid request"
        val errors = message.split("\n")
        return ResponseEntity.badRequest().body(
            mapOf(
                "message" to message,
                "errors" to errors
            )
        )
    }
}
