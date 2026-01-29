package com.pintailconsultingllc.reactivewebflux.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("customers")
data class Customer(
    @Id
    val id: UUID? = null,
    val name: String,
    val email: String,
    val address: String?,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
