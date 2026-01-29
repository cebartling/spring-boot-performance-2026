package com.pintailconsultingllc.reactivewebflux.repository

import com.pintailconsultingllc.reactivewebflux.domain.Customer
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerRepository : R2dbcRepository<Customer, UUID>
