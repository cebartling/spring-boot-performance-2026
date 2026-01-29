package com.pintailconsultingllc.nonreactivemvc.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CreateOrderItemRequest(
    @field:NotBlank(message = "Product name is required")
    val productName: String,

    @field:NotNull(message = "Quantity is required")
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    val price: BigDecimal
)
