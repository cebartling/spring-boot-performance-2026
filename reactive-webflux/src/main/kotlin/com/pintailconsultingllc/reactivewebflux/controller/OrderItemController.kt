package com.pintailconsultingllc.reactivewebflux.controller

import com.pintailconsultingllc.reactivewebflux.domain.OrderItem
import com.pintailconsultingllc.reactivewebflux.dto.CreateOrderItemRequest
import com.pintailconsultingllc.reactivewebflux.dto.UpdateOrderItemRequest
import com.pintailconsultingllc.reactivewebflux.service.OrderItemService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api")
class OrderItemController(
    private val orderItemService: OrderItemService
) {

    @GetMapping("/orders/{orderId}/items")
    fun getItemsByOrderId(@PathVariable orderId: UUID): Flux<OrderItem> {
        return orderItemService.getItemsByOrderId(orderId)
    }

    @PostMapping("/orders/{orderId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItemToOrder(
        @PathVariable orderId: UUID,
        @Valid @RequestBody request: CreateOrderItemRequest
    ): Mono<OrderItem> {
        return orderItemService.addItemToOrder(orderId, request)
    }

    @PutMapping("/order-items/{id}")
    fun updateOrderItem(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateOrderItemRequest
    ): Mono<OrderItem> {
        return orderItemService.updateOrderItem(id, request)
    }

    @DeleteMapping("/order-items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteOrderItem(@PathVariable id: UUID): Mono<Void> {
        return orderItemService.deleteOrderItem(id)
    }
}
