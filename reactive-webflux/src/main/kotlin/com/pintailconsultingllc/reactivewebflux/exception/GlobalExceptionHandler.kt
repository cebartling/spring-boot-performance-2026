package com.pintailconsultingllc.reactivewebflux.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Resource not found"))
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationErrors(ex: WebExchangeBindException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("Validation failed", errors))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("An unexpected error occurred: ${ex.message}"))
    }
}

data class ErrorResponse(
    val message: String,
    val errors: List<String>? = null
)
