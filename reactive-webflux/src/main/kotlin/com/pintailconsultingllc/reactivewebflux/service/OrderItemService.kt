package com.pintailconsultingllc.reactivewebflux.service

import com.pintailconsultingllc.reactivewebflux.domain.OrderItem
import com.pintailconsultingllc.reactivewebflux.dto.CreateOrderItemRequest
import com.pintailconsultingllc.reactivewebflux.dto.UpdateOrderItemRequest
import com.pintailconsultingllc.reactivewebflux.exception.ResourceNotFoundException
import com.pintailconsultingllc.reactivewebflux.messaging.EventPublisher
import com.pintailconsultingllc.reactivewebflux.repository.OrderItemRepository
import com.pintailconsultingllc.reactivewebflux.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
@Transactional
class OrderItemService(
    private val orderItemRepository: OrderItemRepository,
    private val orderRepository: OrderRepository,
    private val eventPublisher: EventPublisher
) {

    fun getItemsByOrderId(orderId: UUID): Flux<OrderItem> {
        return orderRepository.findById(orderId)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Order not found with id: $orderId")))
            .flatMapMany { orderItemRepository.findByOrderId(orderId) }
    }

    fun addItemToOrder(orderId: UUID, request: CreateOrderItemRequest): Mono<OrderItem> {
        return orderRepository.findById(orderId)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Order not found with id: $orderId")))
            .flatMap { order ->
                val orderItem = OrderItem(
                    orderId = orderId,
                    productName = request.productName,
                    quantity = request.quantity,
                    price = request.price
                )

                orderItemRepository.save(orderItem).flatMap { savedItem ->
                    val newTotal = order.totalAmount.add(request.price.multiply(request.quantity.toBigDecimal()))
                    orderRepository.save(order.copy(totalAmount = newTotal))
                        .then(eventPublisher.publishOrderItemCreated(savedItem))
                        .thenReturn(savedItem)
                }
            }
    }

    fun updateOrderItem(id: UUID, request: UpdateOrderItemRequest): Mono<OrderItem> {
        return orderItemRepository.findById(id)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Order item not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    productName = request.productName,
                    quantity = request.quantity,
                    price = request.price
                )
                orderItemRepository.save(updated)
            }
            .flatMap { updatedItem ->
                eventPublisher.publishOrderItemUpdated(updatedItem)
                    .thenReturn(updatedItem)
            }
    }

    fun deleteOrderItem(id: UUID): Mono<Void> {
        return orderItemRepository.findById(id)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Order item not found with id: $id")))
            .flatMap { item ->
                orderRepository.findById(item.orderId)
                    .flatMap { order ->
                        val newTotal = order.totalAmount.subtract(item.price.multiply(item.quantity.toBigDecimal()))
                        orderRepository.save(order.copy(totalAmount = newTotal))
                            .then(orderItemRepository.delete(item))
                            .then(eventPublisher.publishOrderItemDeleted(id))
                    }
            }
    }
}
