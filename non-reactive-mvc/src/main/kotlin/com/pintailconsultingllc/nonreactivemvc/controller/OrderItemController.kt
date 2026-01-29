package com.pintailconsultingllc.nonreactivemvc.controller

import com.pintailconsultingllc.nonreactivemvc.domain.OrderItem
import com.pintailconsultingllc.nonreactivemvc.dto.CreateOrderItemRequest
import com.pintailconsultingllc.nonreactivemvc.service.OrderItemService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api")
class OrderItemController(
    private val orderItemService: OrderItemService
) {

    @GetMapping("/orders/{orderId}/items")
    fun getItemsByOrderId(@PathVariable orderId: UUID): List<OrderItem> {
        return orderItemService.getItemsByOrderId(orderId)
    }

    @PostMapping("/orders/{orderId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItemToOrder(
        @PathVariable orderId: UUID,
        @Valid @RequestBody request: CreateOrderItemRequest
    ): OrderItem {
        return orderItemService.addItemToOrder(orderId, request)
    }

    @DeleteMapping("/order-items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteOrderItem(@PathVariable id: UUID) {
        orderItemService.deleteOrderItem(id)
    }
}
