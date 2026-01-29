package com.pintailconsultingllc.reactivewebflux.repository

import com.pintailconsultingllc.reactivewebflux.domain.Order
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.UUID

@Repository
interface OrderRepository : R2dbcRepository<Order, UUID> {
    fun findByCustomerId(customerId: UUID): Flux<Order>
}
