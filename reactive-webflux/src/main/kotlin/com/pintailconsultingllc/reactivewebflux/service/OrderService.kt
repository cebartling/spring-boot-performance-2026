package com.pintailconsultingllc.reactivewebflux.service

import com.pintailconsultingllc.reactivewebflux.domain.Order
import com.pintailconsultingllc.reactivewebflux.domain.OrderItem
import com.pintailconsultingllc.reactivewebflux.dto.CreateOrderRequest
import com.pintailconsultingllc.reactivewebflux.dto.OrderDto
import com.pintailconsultingllc.reactivewebflux.exception.ResourceNotFoundException
import com.pintailconsultingllc.reactivewebflux.repository.CustomerRepository
import com.pintailconsultingllc.reactivewebflux.repository.OrderItemRepository
import com.pintailconsultingllc.reactivewebflux.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val customerRepository: CustomerRepository
) {

    fun getOrderById(id: UUID): Mono<OrderDto> {
        return orderRepository.findById(id)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Order not found with id: $id")))
            .flatMap { order ->
                val customerMono = customerRepository.findById(order.customerId)
                    .switchIfEmpty(Mono.error(ResourceNotFoundException("Customer not found with id: ${order.customerId}")))
                val itemsFlux = orderItemRepository.findByOrderId(order.id!!).collectList()

                Mono.zip(customerMono, itemsFlux)
                    .map { tuple -> OrderDto.from(order, tuple.t1, tuple.t2) }
            }
    }

    fun getOrdersByCustomerId(customerId: UUID): Flux<Order> {
        return customerRepository.findById(customerId)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Customer not found with id: $customerId")))
            .flatMapMany { orderRepository.findByCustomerId(customerId) }
    }

    fun createOrder(request: CreateOrderRequest): Mono<OrderDto> {
        return customerRepository.findById(request.customerId)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Customer not found with id: ${request.customerId}")))
            .flatMap { customer ->
                val totalAmount = request.items.sumOf { it.price.multiply(it.quantity.toBigDecimal()) }

                val order = Order(
                    customerId = request.customerId,
                    totalAmount = totalAmount,
                    status = request.status
                )

                orderRepository.save(order).flatMap { savedOrder ->
                    val items = request.items.map { itemRequest ->
                        OrderItem(
                            orderId = savedOrder.id!!,
                            productName = itemRequest.productName,
                            quantity = itemRequest.quantity,
                            price = itemRequest.price
                        )
                    }

                    orderItemRepository.saveAll(items).collectList()
                        .map { savedItems -> OrderDto.from(savedOrder, customer, savedItems) }
                }
            }
    }

    fun deleteOrder(id: UUID): Mono<Void> {
        return orderRepository.findById(id)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Order not found with id: $id")))
            .flatMap { order ->
                orderItemRepository.findByOrderId(order.id!!).collectList()
                    .flatMap { items ->
                        orderItemRepository.deleteAll(items)
                            .then(orderRepository.delete(order))
                    }
            }
    }
}
