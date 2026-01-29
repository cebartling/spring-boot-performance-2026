package com.pintailconsultingllc.reactivewebflux.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateCustomerRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    val address: String?
)
