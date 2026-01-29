package com.pintailconsultingllc.reactivewebflux.controller

import com.pintailconsultingllc.reactivewebflux.domain.Order
import com.pintailconsultingllc.reactivewebflux.dto.CreateOrderRequest
import com.pintailconsultingllc.reactivewebflux.dto.OrderDto
import com.pintailconsultingllc.reactivewebflux.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api")
class OrderController(
    private val orderService: OrderService
) {

    @GetMapping("/orders/{id}")
    fun getOrderById(@PathVariable id: UUID): Mono<OrderDto> {
        return orderService.getOrderById(id)
    }

    @GetMapping("/customers/{customerId}/orders")
    fun getOrdersByCustomerId(@PathVariable customerId: UUID): Flux<Order> {
        return orderService.getOrdersByCustomerId(customerId)
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@Valid @RequestBody request: CreateOrderRequest): Mono<OrderDto> {
        return orderService.createOrder(request)
    }

    @DeleteMapping("/orders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteOrder(@PathVariable id: UUID): Mono<Void> {
        return orderService.deleteOrder(id)
    }
}
