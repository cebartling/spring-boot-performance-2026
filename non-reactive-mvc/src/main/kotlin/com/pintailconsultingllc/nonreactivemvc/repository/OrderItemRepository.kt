package com.pintailconsultingllc.nonreactivemvc.repository

import com.pintailconsultingllc.nonreactivemvc.domain.OrderItem
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderItemRepository : CrudRepository<OrderItem, UUID> {
    fun findByOrderId(orderId: UUID): List<OrderItem>
}
