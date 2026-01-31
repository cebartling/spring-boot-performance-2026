package com.pintailconsultingllc.nonreactivemvc.messaging

import com.pintailconsultingllc.nonreactivemvc.domain.Customer
import com.pintailconsultingllc.nonreactivemvc.domain.Order
import com.pintailconsultingllc.nonreactivemvc.domain.OrderItem
import com.pintailconsultingllc.nonreactivemvc.events.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.*

@Component
class EventMapper(
    @Value("\${spring.application.name}")
    private val applicationName: String
) {

    fun toCustomerCreatedEvent(customer: Customer): CustomerCreatedEvent {
        return CustomerCreatedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("CUSTOMER_CREATED")
            .setTimestamp(System.currentTimeMillis())
            .setCustomerId(customer.id.toString())
            .setCustomerName(customer.name)
            .setCustomerEmail(customer.email)
            .setCustomerAddress(customer.address)
            .setCreatedAt(customer.createdAt.toInstant(ZoneOffset.UTC).toEpochMilli())
            .setMetadata(
                EventMetadata.newBuilder()
                    .setSource(applicationName)
                    .setVersion("1.0")
                    .build()
            )
            .build()
    }

    fun toCustomerUpdatedEvent(customer: Customer): CustomerUpdatedEvent {
        return CustomerUpdatedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("CUSTOMER_UPDATED")
            .setTimestamp(System.currentTimeMillis())
            .setCustomerId(customer.id.toString())
            .setCustomerName(customer.name)
            .setCustomerEmail(customer.email)
            .setCustomerAddress(customer.address)
            .setUpdatedAt(customer.createdAt.toInstant(ZoneOffset.UTC).toEpochMilli())
            .setMetadata(
                EventMetadata.newBuilder()
                    .setSource(applicationName)
                    .setVersion("1.0")
                    .build()
            )
            .build()
    }

    fun toCustomerDeletedEvent(customerId: UUID): CustomerDeletedEvent {
        return CustomerDeletedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("CUSTOMER_DELETED")
            .setTimestamp(System.currentTimeMillis())
            .setCustomerId(customerId.toString())
            .setMetadata(
                EventMetadata.newBuilder()
                    .setSource(applicationName)
                    .setVersion("1.0")
                    .build()
            )
            .build()
    }

    fun toOrderCreatedEvent(order: Order): OrderCreatedEvent {
        return OrderCreatedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("ORDER_CREATED")
            .setTimestamp(System.currentTimeMillis())
            .setOrderId(order.id.toString())
            .setCustomerId(order.customerId.toString())
            .setTotalAmount(order.totalAmount.toString())
            .setStatus(order.status.name)
            .setCreatedAt(order.orderDate.toInstant(ZoneOffset.UTC).toEpochMilli())
            .setMetadata(
                EventMetadata.newBuilder()
                    .setSource(applicationName)
                    .setVersion("1.0")
                    .build()
            )
            .build()
    }

    fun toOrderUpdatedEvent(order: Order): OrderUpdatedEvent {
        return OrderUpdatedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("ORDER_UPDATED")
            .setTimestamp(System.currentTimeMillis())
            .setOrderId(order.id.toString())
            .setCustomerId(order.customerId.toString())
            .setTotalAmount(order.totalAmount.toString())
            .setStatus(order.status.name)
            .setUpdatedAt(order.orderDate.toInstant(ZoneOffset.UTC).toEpochMilli())
            .setMetadata(
                EventMetadata.newBuilder()
                    .setSource(applicationName)
                    .setVersion("1.0")
                    .build()
            )
            .build()
    }

    fun toOrderDeletedEvent(orderId: UUID): OrderDeletedEvent {
        return OrderDeletedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("ORDER_DELETED")
            .setTimestamp(System.currentTimeMillis())
            .setOrderId(orderId.toString())
            .setMetadata(
                EventMetadata.newBuilder()
                    .setSource(applicationName)
                    .setVersion("1.0")
                    .build()
            )
            .build()
    }

    fun toOrderItemCreatedEvent(orderItem: OrderItem): OrderItemCreatedEvent {
        return OrderItemCreatedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("ORDER_ITEM_CREATED")
            .setTimestamp(System.currentTimeMillis())
            .setOrderItemId(orderItem.id.toString())
            .setOrderId(orderItem.orderId.toString())
            .setProductName(orderItem.productName)
            .setQuantity(orderItem.quantity)
            .setPrice(orderItem.price.toString())
            .setCreatedAt(System.currentTimeMillis())
            .setMetadata(
                EventMetadata.newBuilder()
                    .setSource(applicationName)
                    .setVersion("1.0")
                    .build()
            )
            .build()
    }

    fun toOrderItemUpdatedEvent(orderItem: OrderItem): OrderItemUpdatedEvent {
        return OrderItemUpdatedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("ORDER_ITEM_UPDATED")
            .setTimestamp(System.currentTimeMillis())
            .setOrderItemId(orderItem.id.toString())
            .setOrderId(orderItem.orderId.toString())
            .setProductName(orderItem.productName)
            .setQuantity(orderItem.quantity)
            .setPrice(orderItem.price.toString())
            .setUpdatedAt(System.currentTimeMillis())
            .setMetadata(
                EventMetadata.newBuilder()
                    .setSource(applicationName)
                    .setVersion("1.0")
                    .build()
            )
            .build()
    }

    fun toOrderItemDeletedEvent(orderItemId: UUID): OrderItemDeletedEvent {
        return OrderItemDeletedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("ORDER_ITEM_DELETED")
            .setTimestamp(System.currentTimeMillis())
            .setOrderItemId(orderItemId.toString())
            .setMetadata(
                EventMetadata.newBuilder()
                    .setSource(applicationName)
                    .setVersion("1.0")
                    .build()
            )
            .build()
    }
}
