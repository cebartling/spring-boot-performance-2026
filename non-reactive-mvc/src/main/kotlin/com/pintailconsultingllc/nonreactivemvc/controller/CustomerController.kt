package com.pintailconsultingllc.nonreactivemvc.controller

import com.pintailconsultingllc.nonreactivemvc.domain.Customer
import com.pintailconsultingllc.nonreactivemvc.dto.CreateCustomerRequest
import com.pintailconsultingllc.nonreactivemvc.service.CustomerService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerService: CustomerService
) {

    @GetMapping
    fun getAllCustomers(): List<Customer> {
        return customerService.getAllCustomers()
    }

    @GetMapping("/{id}")
    fun getCustomerById(@PathVariable id: UUID): Customer {
        return customerService.getCustomerById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCustomer(@Valid @RequestBody request: CreateCustomerRequest): Customer {
        return customerService.createCustomer(request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCustomer(@PathVariable id: UUID) {
        customerService.deleteCustomer(id)
    }
}
