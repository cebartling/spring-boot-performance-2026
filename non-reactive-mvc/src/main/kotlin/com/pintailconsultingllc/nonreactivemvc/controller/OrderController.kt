package com.pintailconsultingllc.nonreactivemvc.controller

import com.pintailconsultingllc.nonreactivemvc.domain.Order
import com.pintailconsultingllc.nonreactivemvc.dto.CreateOrderRequest
import com.pintailconsultingllc.nonreactivemvc.dto.OrderDto
import com.pintailconsultingllc.nonreactivemvc.dto.UpdateOrderRequest
import com.pintailconsultingllc.nonreactivemvc.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api")
class OrderController(
    private val orderService: OrderService
) {

    @GetMapping("/orders/{id}")
    fun getOrderById(@PathVariable id: UUID): OrderDto {
        return orderService.getOrderById(id)
    }

    @GetMapping("/customers/{customerId}/orders")
    fun getOrdersByCustomerId(@PathVariable customerId: UUID): List<Order> {
        return orderService.getOrdersByCustomerId(customerId)
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@Valid @RequestBody request: CreateOrderRequest): OrderDto {
        return orderService.createOrder(request)
    }

    @PutMapping("/orders/{id}")
    fun updateOrder(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateOrderRequest
    ): Order {
        return orderService.updateOrder(id, request)
    }

    @DeleteMapping("/orders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteOrder(@PathVariable id: UUID) {
        orderService.deleteOrder(id)
    }
}
