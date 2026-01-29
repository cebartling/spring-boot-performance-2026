package com.pintailconsultingllc.nonreactivemvc

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<NonReactiveMvcApplication>()
        .with(TestcontainersConfiguration::class)
        .run(*args)
}
