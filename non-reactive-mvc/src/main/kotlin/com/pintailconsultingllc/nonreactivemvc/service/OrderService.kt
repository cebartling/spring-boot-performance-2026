package com.pintailconsultingllc.nonreactivemvc.service

import com.pintailconsultingllc.nonreactivemvc.domain.Order
import com.pintailconsultingllc.nonreactivemvc.domain.OrderItem
import com.pintailconsultingllc.nonreactivemvc.dto.CreateOrderRequest
import com.pintailconsultingllc.nonreactivemvc.dto.OrderDto
import com.pintailconsultingllc.nonreactivemvc.dto.UpdateOrderRequest
import com.pintailconsultingllc.nonreactivemvc.exception.ResourceNotFoundException
import com.pintailconsultingllc.nonreactivemvc.messaging.EventPublisher
import com.pintailconsultingllc.nonreactivemvc.repository.CustomerRepository
import com.pintailconsultingllc.nonreactivemvc.repository.OrderItemRepository
import com.pintailconsultingllc.nonreactivemvc.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val customerRepository: CustomerRepository,
    private val eventPublisher: EventPublisher
) {

    fun getOrderById(id: UUID): OrderDto {
        val order = orderRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $id") }

        val customer = customerRepository.findById(order.customerId)
            .orElseThrow { ResourceNotFoundException("Customer not found with id: ${order.customerId}") }

        val items = orderItemRepository.findByOrderId(order.id!!)

        return OrderDto.from(order, customer, items)
    }

    fun getOrdersByCustomerId(customerId: UUID): List<Order> {
        customerRepository.findById(customerId)
            .orElseThrow { ResourceNotFoundException("Customer not found with id: $customerId") }

        return orderRepository.findByCustomerId(customerId)
    }

    fun createOrder(request: CreateOrderRequest): OrderDto {
        val customer = customerRepository.findById(request.customerId)
            .orElseThrow { ResourceNotFoundException("Customer not found with id: ${request.customerId}") }

        val totalAmount = request.items.sumOf { it.price.multiply(it.quantity.toBigDecimal()) }

        val order = Order(
            customerId = request.customerId,
            totalAmount = totalAmount,
            status = request.status
        )

        val savedOrder = orderRepository.save(order)

        val items = request.items.map { itemRequest ->
            OrderItem(
                orderId = savedOrder.id!!,
                productName = itemRequest.productName,
                quantity = itemRequest.quantity,
                price = itemRequest.price
            )
        }

        val savedItems = items.map { orderItemRepository.save(it) }
        eventPublisher.publishOrderCreated(savedOrder)

        return OrderDto.from(savedOrder, customer, savedItems)
    }

    fun updateOrder(id: UUID, request: UpdateOrderRequest): Order {
        val existing = orderRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $id") }
        val updated = existing.copy(
            totalAmount = request.totalAmount,
            status = request.status
        )
        val savedOrder = orderRepository.save(updated)
        eventPublisher.publishOrderUpdated(savedOrder)
        return savedOrder
    }

    fun deleteOrder(id: UUID) {
        val order = orderRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $id") }

        val items = orderItemRepository.findByOrderId(order.id!!)
        items.forEach { orderItemRepository.delete(it) }
        orderRepository.delete(order)
        eventPublisher.publishOrderDeleted(id)
    }
}
