package com.pintailconsultingllc.nonreactivemvc

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
@Tag("integration")
class IntegrationTest {

    @Test
    fun contextLoadsWithPostgres() {
        // Integration test with PostgreSQL testcontainer
    }
}
