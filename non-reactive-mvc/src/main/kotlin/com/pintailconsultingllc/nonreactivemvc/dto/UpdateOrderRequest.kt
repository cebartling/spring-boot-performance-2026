package com.pintailconsultingllc.nonreactivemvc.dto

import com.pintailconsultingllc.nonreactivemvc.domain.OrderStatus
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class UpdateOrderRequest(
    @field:NotNull(message = "Total amount is required")
    @field:DecimalMin(value = "0.0", message = "Total amount must be non-negative")
    val totalAmount: BigDecimal,

    @field:NotNull(message = "Status is required")
    val status: OrderStatus
)
