package com.pintailconsultingllc.nonreactivemvc.dto

import com.pintailconsultingllc.nonreactivemvc.domain.Customer
import com.pintailconsultingllc.nonreactivemvc.domain.Order
import com.pintailconsultingllc.nonreactivemvc.domain.OrderItem
import com.pintailconsultingllc.nonreactivemvc.domain.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderDto(
    val id: UUID,
    val orderDate: LocalDateTime,
    val totalAmount: BigDecimal,
    val status: OrderStatus,
    val customer: CustomerSummary,
    val items: List<OrderItemSummary>
) {
    companion object {
        fun from(order: Order, customer: Customer, items: List<OrderItem>): OrderDto {
            return OrderDto(
                id = order.id!!,
                orderDate = order.orderDate,
                totalAmount = order.totalAmount,
                status = order.status,
                customer = CustomerSummary(
                    id = customer.id!!,
                    name = customer.name,
                    email = customer.email
                ),
                items = items.map { item ->
                    OrderItemSummary(
                        id = item.id!!,
                        productName = item.productName,
                        quantity = item.quantity,
                        price = item.price
                    )
                }
            )
        }
    }
}

data class CustomerSummary(
    val id: UUID,
    val name: String,
    val email: String
)

data class OrderItemSummary(
    val id: UUID,
    val productName: String,
    val quantity: Int,
    val price: BigDecimal
)
