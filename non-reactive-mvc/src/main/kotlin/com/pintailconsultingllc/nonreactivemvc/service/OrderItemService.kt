package com.pintailconsultingllc.nonreactivemvc.service

import com.pintailconsultingllc.nonreactivemvc.domain.OrderItem
import com.pintailconsultingllc.nonreactivemvc.dto.CreateOrderItemRequest
import com.pintailconsultingllc.nonreactivemvc.dto.UpdateOrderItemRequest
import com.pintailconsultingllc.nonreactivemvc.exception.ResourceNotFoundException
import com.pintailconsultingllc.nonreactivemvc.messaging.EventPublisher
import com.pintailconsultingllc.nonreactivemvc.repository.OrderItemRepository
import com.pintailconsultingllc.nonreactivemvc.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class OrderItemService(
    private val orderItemRepository: OrderItemRepository,
    private val orderRepository: OrderRepository,
    private val eventPublisher: EventPublisher
) {

    fun getItemsByOrderId(orderId: UUID): List<OrderItem> {
        orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $orderId") }

        return orderItemRepository.findByOrderId(orderId)
    }

    fun addItemToOrder(orderId: UUID, request: CreateOrderItemRequest): OrderItem {
        val order = orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $orderId") }

        val orderItem = OrderItem(
            orderId = orderId,
            productName = request.productName,
            quantity = request.quantity,
            price = request.price
        )

        val savedItem = orderItemRepository.save(orderItem)

        val newTotal = order.totalAmount.add(request.price.multiply(request.quantity.toBigDecimal()))
        orderRepository.save(order.copy(totalAmount = newTotal))

        eventPublisher.publishOrderItemCreated(savedItem)
        return savedItem
    }

    fun updateOrderItem(id: UUID, request: UpdateOrderItemRequest): OrderItem {
        val existing = orderItemRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Order item not found with id: $id") }
        val updated = existing.copy(
            productName = request.productName,
            quantity = request.quantity,
            price = request.price
        )
        val savedItem = orderItemRepository.save(updated)
        eventPublisher.publishOrderItemUpdated(savedItem)
        return savedItem
    }

    fun deleteOrderItem(id: UUID) {
        val item = orderItemRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Order item not found with id: $id") }

        val order = orderRepository.findById(item.orderId)
            .orElseThrow { ResourceNotFoundException("Order not found with id: ${item.orderId}") }

        val newTotal = order.totalAmount.subtract(item.price.multiply(item.quantity.toBigDecimal()))
        orderRepository.save(order.copy(totalAmount = newTotal))

        orderItemRepository.delete(item)
        eventPublisher.publishOrderItemDeleted(id)
    }
}
