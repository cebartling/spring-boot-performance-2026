package com.pintailconsultingllc.reactivewebflux.controller

import com.pintailconsultingllc.reactivewebflux.TestcontainersConfiguration
import com.pintailconsultingllc.reactivewebflux.domain.Customer
import com.pintailconsultingllc.reactivewebflux.dto.CreateCustomerRequest
import com.pintailconsultingllc.reactivewebflux.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class CustomerControllerIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @AfterEach
    fun cleanup() {
        customerRepository.deleteAll().block()
    }

    @Test
    fun `should create customer successfully`() {
        val request = CreateCustomerRequest(
            name = "John Doe",
            email = "john@example.com",
            address = "123 Main St"
        )

        webTestClient.post()
            .uri("/api/customers")
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Customer>()
            .consumeWith { response ->
                val customer = response.responseBody!!
                assert(customer.name == "John Doe")
                assert(customer.email == "john@example.com")
                assert(customer.address == "123 Main St")
                assert(customer.id != null)
            }
    }

    @Test
    fun `should get all customers`() {
        val customer1 = customerRepository.save(
            Customer(name = "Customer 1", email = "customer1@example.com", address = null)
        ).block()!!
        val customer2 = customerRepository.save(
            Customer(name = "Customer 2", email = "customer2@example.com", address = null)
        ).block()!!

        webTestClient.get()
            .uri("/api/customers")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Customer>()
            .hasSize(2)
    }

    @Test
    fun `should get customer by id`() {
        val customer = customerRepository.save(
            Customer(name = "John Doe", email = "john@example.com", address = "123 Main St")
        ).block()!!

        webTestClient.get()
            .uri("/api/customers/${customer.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<Customer>()
            .consumeWith { response ->
                val retrieved = response.responseBody!!
                assert(retrieved.id == customer.id)
                assert(retrieved.name == "John Doe")
                assert(retrieved.email == "john@example.com")
            }
    }

    @Test
    fun `should return 404 when customer not found`() {
        val nonExistentId = java.util.UUID.randomUUID()

        webTestClient.get()
            .uri("/api/customers/$nonExistentId")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should delete customer successfully`() {
        val customer = customerRepository.save(
            Customer(name = "John Doe", email = "john@example.com", address = null)
        ).block()!!

        webTestClient.delete()
            .uri("/api/customers/${customer.id}")
            .exchange()
            .expectStatus().isNoContent

        val exists = customerRepository.existsById(customer.id!!).block()
        assert(exists == false)
    }

    @Test
    fun `should return 400 for invalid customer data`() {
        val invalidRequest = mapOf(
            "name" to "",
            "email" to "invalid-email",
            "address" to "123 Main St"
        )

        webTestClient.post()
            .uri("/api/customers")
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest
    }
}
