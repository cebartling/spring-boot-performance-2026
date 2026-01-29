package com.pintailconsultingllc.nonreactivemvc.repository

import com.pintailconsultingllc.nonreactivemvc.domain.Customer
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerRepository : CrudRepository<Customer, UUID>
