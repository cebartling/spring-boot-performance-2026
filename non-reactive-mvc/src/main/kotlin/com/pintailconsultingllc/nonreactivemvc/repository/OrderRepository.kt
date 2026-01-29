package com.pintailconsultingllc.nonreactivemvc.repository

import com.pintailconsultingllc.nonreactivemvc.domain.Order
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderRepository : CrudRepository<Order, UUID> {
    fun findByCustomerId(customerId: UUID): List<Order>
}
