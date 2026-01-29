package com.pintailconsultingllc.nonreactivemvc.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Table("orders")
data class Order(
    @Id
    val id: UUID? = null,
    val customerId: UUID,
    val orderDate: LocalDateTime = LocalDateTime.now(),
    val totalAmount: BigDecimal,
    val status: OrderStatus
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
