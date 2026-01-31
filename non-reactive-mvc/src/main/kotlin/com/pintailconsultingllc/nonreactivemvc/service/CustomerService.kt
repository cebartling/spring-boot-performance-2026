package com.pintailconsultingllc.nonreactivemvc.service

import com.pintailconsultingllc.nonreactivemvc.domain.Customer
import com.pintailconsultingllc.nonreactivemvc.dto.CreateCustomerRequest
import com.pintailconsultingllc.nonreactivemvc.dto.UpdateCustomerRequest
import com.pintailconsultingllc.nonreactivemvc.exception.ResourceNotFoundException
import com.pintailconsultingllc.nonreactivemvc.messaging.EventPublisher
import com.pintailconsultingllc.nonreactivemvc.repository.CustomerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val eventPublisher: EventPublisher
) {

    fun getAllCustomers(): List<Customer> {
        return customerRepository.findAll().toList()
    }

    fun getCustomerById(id: UUID): Customer {
        return customerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Customer not found with id: $id") }
    }

    fun createCustomer(request: CreateCustomerRequest): Customer {
        val customer = Customer(
            name = request.name,
            email = request.email,
            address = request.address
        )
        val savedCustomer = customerRepository.save(customer)
        eventPublisher.publishCustomerCreated(savedCustomer)
        return savedCustomer
    }

    fun updateCustomer(id: UUID, request: UpdateCustomerRequest): Customer {
        val existing = customerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Customer not found with id: $id") }
        val updated = existing.copy(
            name = request.name,
            email = request.email,
            address = request.address
        )
        val savedCustomer = customerRepository.save(updated)
        eventPublisher.publishCustomerUpdated(savedCustomer)
        return savedCustomer
    }

    fun deleteCustomer(id: UUID) {
        val customer = customerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Customer not found with id: $id") }
        customerRepository.delete(customer)
        eventPublisher.publishCustomerDeleted(id)
    }
}
