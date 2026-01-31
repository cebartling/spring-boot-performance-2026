package com.pintailconsultingllc.reactivewebflux.messaging

import com.pintailconsultingllc.reactivewebflux.domain.Customer
import com.pintailconsultingllc.reactivewebflux.domain.Order
import com.pintailconsultingllc.reactivewebflux.domain.OrderItem
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import java.util.*

@Component
class EventPublisher(
    private val kafkaSender: KafkaSender<String, Any>,
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

    fun publishCustomerCreated(customer: Customer): Mono<Void> {
        val event = eventMapper.toCustomerCreatedEvent(customer)
        return publishEvent(customerCreatedTopic, customer.id.toString(), event)
    }

    fun publishCustomerUpdated(customer: Customer): Mono<Void> {
        val event = eventMapper.toCustomerUpdatedEvent(customer)
        return publishEvent(customerUpdatedTopic, customer.id.toString(), event)
    }

    fun publishCustomerDeleted(id: UUID): Mono<Void> {
        val event = eventMapper.toCustomerDeletedEvent(id)
        return publishEvent(customerDeletedTopic, id.toString(), event)
    }

    fun publishOrderCreated(order: Order): Mono<Void> {
        val event = eventMapper.toOrderCreatedEvent(order)
        return publishEvent(orderCreatedTopic, order.id.toString(), event)
    }

    fun publishOrderUpdated(order: Order): Mono<Void> {
        val event = eventMapper.toOrderUpdatedEvent(order)
        return publishEvent(orderUpdatedTopic, order.id.toString(), event)
    }

    fun publishOrderDeleted(id: UUID): Mono<Void> {
        val event = eventMapper.toOrderDeletedEvent(id)
        return publishEvent(orderDeletedTopic, id.toString(), event)
    }

    fun publishOrderItemCreated(item: OrderItem): Mono<Void> {
        val event = eventMapper.toOrderItemCreatedEvent(item)
        return publishEvent(orderItemCreatedTopic, item.id.toString(), event)
    }

    fun publishOrderItemUpdated(item: OrderItem): Mono<Void> {
        val event = eventMapper.toOrderItemUpdatedEvent(item)
        return publishEvent(orderItemUpdatedTopic, item.id.toString(), event)
    }

    fun publishOrderItemDeleted(id: UUID): Mono<Void> {
        val event = eventMapper.toOrderItemDeletedEvent(id)
        return publishEvent(orderItemDeletedTopic, id.toString(), event)
    }

    private fun publishEvent(topic: String, key: String, event: Any): Mono<Void> {
        val record = SenderRecord.create(
            org.apache.kafka.clients.producer.ProducerRecord(topic, key, event),
            null
        )

        return kafkaSender.send(Mono.just(record))
            .doOnError { error ->
                logger.error("Failed to publish event to topic {}: {}", topic, error.message, error)
            }
            .onErrorResume { Mono.empty() }
            .then()
    }
}
