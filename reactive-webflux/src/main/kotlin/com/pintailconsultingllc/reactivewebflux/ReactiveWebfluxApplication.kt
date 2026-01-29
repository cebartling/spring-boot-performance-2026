package com.pintailconsultingllc.reactivewebflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactiveWebfluxApplication

fun main(args: Array<String>) {
    runApplication<ReactiveWebfluxApplication>(*args)
}
