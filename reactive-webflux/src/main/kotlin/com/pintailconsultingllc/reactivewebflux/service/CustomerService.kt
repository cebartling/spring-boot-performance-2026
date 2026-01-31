package com.pintailconsultingllc.reactivewebflux.service

import com.pintailconsultingllc.reactivewebflux.domain.Customer
import com.pintailconsultingllc.reactivewebflux.dto.CreateCustomerRequest
import com.pintailconsultingllc.reactivewebflux.dto.UpdateCustomerRequest
import com.pintailconsultingllc.reactivewebflux.exception.ResourceNotFoundException
import com.pintailconsultingllc.reactivewebflux.messaging.EventPublisher
import com.pintailconsultingllc.reactivewebflux.repository.CustomerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
@Transactional
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val eventPublisher: EventPublisher
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
            .flatMap { savedCustomer ->
                eventPublisher.publishCustomerCreated(savedCustomer)
                    .thenReturn(savedCustomer)
            }
    }

    fun updateCustomer(id: UUID, request: UpdateCustomerRequest): Mono<Customer> {
        return customerRepository.findById(id)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Customer not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    name = request.name,
                    email = request.email,
                    address = request.address
                )
                customerRepository.save(updated)
            }
            .flatMap { updatedCustomer ->
                eventPublisher.publishCustomerUpdated(updatedCustomer)
                    .thenReturn(updatedCustomer)
            }
    }

    fun deleteCustomer(id: UUID): Mono<Void> {
        return customerRepository.findById(id)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Customer not found with id: $id")))
            .flatMap { customer ->
                customerRepository.delete(customer)
                    .then(eventPublisher.publishCustomerDeleted(id))
            }
    }
}
