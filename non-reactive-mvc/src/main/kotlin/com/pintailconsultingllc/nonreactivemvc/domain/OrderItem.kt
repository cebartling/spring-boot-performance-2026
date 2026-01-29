package com.pintailconsultingllc.nonreactivemvc.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.UUID

@Table("order_items")
data class OrderItem(
    @Id
    val id: UUID? = null,
    val orderId: UUID,
    val productName: String,
    val quantity: Int,
    val price: BigDecimal
)
