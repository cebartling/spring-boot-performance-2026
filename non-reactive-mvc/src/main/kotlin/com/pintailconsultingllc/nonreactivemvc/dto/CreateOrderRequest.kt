package com.pintailconsultingllc.nonreactivemvc.dto

import com.pintailconsultingllc.nonreactivemvc.domain.OrderStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CreateOrderRequest(
    @field:NotNull(message = "Customer ID is required")
    val customerId: UUID,

    @field:NotNull(message = "Status is required")
    val status: OrderStatus = OrderStatus.PENDING,

    @field:NotEmpty(message = "Order must have at least one item")
    @field:Valid
    val items: List<CreateOrderItemRequest>
)
