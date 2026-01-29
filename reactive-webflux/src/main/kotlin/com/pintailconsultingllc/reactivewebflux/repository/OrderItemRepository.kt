package com.pintailconsultingllc.reactivewebflux.repository

import com.pintailconsultingllc.reactivewebflux.domain.OrderItem
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.UUID

@Repository
interface OrderItemRepository : R2dbcRepository<OrderItem, UUID> {
    fun findByOrderId(orderId: UUID): Flux<OrderItem>
}
