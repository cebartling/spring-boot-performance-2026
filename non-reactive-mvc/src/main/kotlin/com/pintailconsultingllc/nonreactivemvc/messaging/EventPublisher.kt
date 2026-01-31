package com.pintailconsultingllc.nonreactivemvc.messaging

import com.pintailconsultingllc.nonreactivemvc.domain.Customer
import com.pintailconsultingllc.nonreactivemvc.domain.Order
import com.pintailconsultingllc.nonreactivemvc.domain.OrderItem
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class EventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val eventMapper: EventMapper,
    @Value("\${app.kafka.topics.customer-created}")
    private val customerCreatedTopic: String,
    @Value("\${app.kafka.topics.customer-updated}")
    private val customerUpdatedTopic: String,
    @Value("\${app.kafka.topics.customer-deleted}")
    private val customerDeletedTopic: String,
    @Value("\${app.kafka.topics.order-created}")
    private val orderCreatedTopic: String,
    @Value("\${app.kafka.topics.order-updated}")
    private val orderUpdatedTopic: String,
    @Value("\${app.kafka.topics.order-deleted}")
    private val orderDeletedTopic: String,
    @Value("\${app.kafka.topics.order-item-created}")
    private val orderItemCreatedTopic: String,
    @Value("\${app.kafka.topics.order-item-updated}")
    private val orderItemUpdatedTopic: String,
    @Value("\${app.kafka.topics.order-item-deleted}")
    private val orderItemDeletedTopic: String
) {
    private val logger = LoggerFactory.getLogger(EventPublisher::class.java)

    fun publishCustomerCreated(customer: Customer) {
        try {
            val event = eventMapper.toCustomerCreatedEvent(customer)
            kafkaTemplate.send(customerCreatedTopic, customer.id.toString(), event)
        } catch (e: Exception) {
            logger.error("Failed to publish customer created event: {}", e.message, e)
        }
    }

    fun publishCustomerUpdated(customer: Customer) {
        try {
            val event = eventMapper.toCustomerUpdatedEvent(customer)
            kafkaTemplate.send(customerUpdatedTopic, customer.id.toString(), event)
        } catch (e: Exception) {
            logger.error("Failed to publish customer updated event: {}", e.message, e)
        }
    }

    fun publishCustomerDeleted(id: UUID) {
        try {
            val event = eventMapper.toCustomerDeletedEvent(id)
            kafkaTemplate.send(customerDeletedTopic, id.toString(), event)
        } catch (e: Exception) {
            logger.error("Failed to publish customer deleted event: {}", e.message, e)
        }
    }

    fun publishOrderCreated(order: Order) {
        try {
            val event = eventMapper.toOrderCreatedEvent(order)
            kafkaTemplate.send(orderCreatedTopic, order.id.toString(), event)
        } catch (e: Exception) {
            logger.error("Failed to publish order created event: {}", e.message, e)
        }
    }

    fun publishOrderUpdated(order: Order) {
        try {
            val event = eventMapper.toOrderUpdatedEvent(order)
            kafkaTemplate.send(orderUpdatedTopic, order.id.toString(), event)
        } catch (e: Exception) {
            logger.error("Failed to publish order updated event: {}", e.message, e)
        }
    }

    fun publishOrderDeleted(id: UUID) {
        try {
            val event = eventMapper.toOrderDeletedEvent(id)
            kafkaTemplate.send(orderDeletedTopic, id.toString(), event)
        } catch (e: Exception) {
            logger.error("Failed to publish order deleted event: {}", e.message, e)
        }
    }

    fun publishOrderItemCreated(item: OrderItem) {
        try {
            val event = eventMapper.toOrderItemCreatedEvent(item)
            kafkaTemplate.send(orderItemCreatedTopic, item.id.toString(), event)
        } catch (e: Exception) {
            logger.error("Failed to publish order item created event: {}", e.message, e)
        }
    }

    fun publishOrderItemUpdated(item: OrderItem) {
        try {
            val event = eventMapper.toOrderItemUpdatedEvent(item)
            kafkaTemplate.send(orderItemUpdatedTopic, item.id.toString(), event)
        } catch (e: Exception) {
            logger.error("Failed to publish order item updated event: {}", e.message, e)
        }
    }

    fun publishOrderItemDeleted(id: UUID) {
        try {
            val event = eventMapper.toOrderItemDeletedEvent(id)
            kafkaTemplate.send(orderItemDeletedTopic, id.toString(), event)
        } catch (e: Exception) {
            logger.error("Failed to publish order item deleted event: {}", e.message, e)
        }
    }
}
