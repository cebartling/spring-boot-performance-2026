package com.pintailconsultingllc.reactivewebflux.controller

import com.pintailconsultingllc.reactivewebflux.domain.Customer
import com.pintailconsultingllc.reactivewebflux.dto.CreateCustomerRequest
import com.pintailconsultingllc.reactivewebflux.service.CustomerService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerService: CustomerService
) {

    @GetMapping
    fun getAllCustomers(): Flux<Customer> {
        return customerService.getAllCustomers()
    }

    @GetMapping("/{id}")
    fun getCustomerById(@PathVariable id: UUID): Mono<Customer> {
        return customerService.getCustomerById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCustomer(@Valid @RequestBody request: CreateCustomerRequest): Mono<Customer> {
        return customerService.createCustomer(request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCustomer(@PathVariable id: UUID): Mono<Void> {
        return customerService.deleteCustomer(id)
    }
}
