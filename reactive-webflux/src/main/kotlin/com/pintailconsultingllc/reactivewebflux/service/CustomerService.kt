package com.pintailconsultingllc.reactivewebflux.service

import com.pintailconsultingllc.reactivewebflux.domain.Customer
import com.pintailconsultingllc.reactivewebflux.dto.CreateCustomerRequest
import com.pintailconsultingllc.reactivewebflux.exception.ResourceNotFoundException
import com.pintailconsultingllc.reactivewebflux.repository.CustomerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
@Transactional
class CustomerService(
    private val customerRepository: CustomerRepository
) {

    fun getAllCustomers(): Flux<Customer> {
        return customerRepository.findAll()
    }

    fun getCustomerById(id: UUID): Mono<Customer> {
        return customerRepository.findById(id)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Customer not found with id: $id")))
    }

    fun createCustomer(request: CreateCustomerRequest): Mono<Customer> {
        val customer = Customer(
            name = request.name,
            email = request.email,
            address = request.address
        )
        return customerRepository.save(customer)
    }

    fun deleteCustomer(id: UUID): Mono<Void> {
        return customerRepository.findById(id)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Customer not found with id: $id")))
            .flatMap { customerRepository.delete(it) }
    }
}
