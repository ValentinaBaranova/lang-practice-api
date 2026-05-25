package com.practice.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = ex.bindingResult.fieldErrors.map { it.defaultMessage ?: "Invalid value" }
        return ResponseEntity.badRequest().body(
            mapOf(
                "message" to "Validation failed",
                "errors" to errors
            )
        )
    }

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

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.status(404).body(
            mapOf(
                "message" to (ex.message ?: "Resource not found")
            )
        )
    }

    @ExceptionHandler(IllegalAccessException::class)
    fun handleIllegalAccessException(ex: IllegalAccessException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.status(403).body(
            mapOf(
                "message" to (ex.message ?: "Access denied")
            )
        )
    }
}
