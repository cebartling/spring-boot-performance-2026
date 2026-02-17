# Performance Exploration of Spring Boot Applications

> **Disclaimer:** AI tools were used to assist in generating portions of this blog post. However, all experimentation, data collection, and results presented here were performed and validated by the author.

## Introduction

For more than a decade, scalability in Spring-based web applications has been closely tied to the choice between **blocking** and **non-blocking** I/O. Traditional Spring MVC applications rely on a *thread-per-request* model, where each incoming request is handled by a dedicated thread that may block while waiting on I/O. At high concurrency levels, this approach can become constrained by thread exhaustion, context switching overhead, and memory pressure. Spring WebFlux emerged as an alternative designed specifically to address these limitations. By embracing a non-blocking, reactive execution model, WebFlux enables a small number of threads to efficiently multiplex large numbers of concurrent requests. In exchange, developers adopt a fundamentally different programming paradigm—reactive streams, backpressure, and asynchronous composition—which, while powerful, introduces additional cognitive and operational complexity.

The introduction of **virtual threads** in Java 21 challenges the assumptions that originally motivated this divide. Virtual threads dramatically reduce the cost of blocking by decoupling application concurrency from operating system threads. Blocking calls no longer monopolize scarce platform threads, allowing applications to scale to large numbers of concurrent operations while retaining a familiar, imperative programming model.

This raises an important and practical question for modern Spring applications:

> Can Spring MVC with blocking I/O, when executed on virtual threads, perform as well as Spring WebFlux?

This experiment is an attempt to answer that question empirically. Rather than debating architectural preferences or theoretical models, it focuses on measurable outcomes—throughput, latency, and resource utilization—under controlled and repeatable conditions. By running equivalent workloads against Spring MVC configured to use virtual threads and Spring WebFlux using its reactive execution model, this study aims to understand whether virtual threads meaningfully alter the performance tradeoffs between these two approaches.

The goal is not to declare a universal winner, but to provide data that helps teams make informed decisions in a post–Java 21 world, where blocking I/O may no longer imply poor scalability and reactive programming may no longer be the only path to high concurrency.

## Experimental Setup

The experiment is designed to provide a controlled, apples-to-apples comparison between two modern Spring-based approaches to handling high-concurrency, I/O-bound workloads: Spring WebFlux using a fully reactive, non-blocking stack, and Spring MVC using traditional blocking I/O executed on Java virtual threads. Both applications are implemented in Kotlin 2.2 and built with Spring Boot 4.0.2 running on Java 24, and they expose identical REST APIs over the same e-commerce domain so that differences in behavior can be attributed to the execution model rather than business logic.

Each application connects to the same shared PostgreSQL 18 database, which is initialized with an identical schema and sample data set. The WebFlux application uses Spring Data R2DBC with a reactive PostgreSQL driver and runs on Netty, while the MVC application uses Spring Data JDBC with the standard PostgreSQL JDBC driver and runs on Tomcat, with virtual threads enabled via Spring Boot’s built-in configuration. This setup intentionally reflects realistic production choices: a fully non-blocking stack on the reactive side and a conventional, blocking stack enhanced by Project Loom on the imperative side.

To extend the comparison beyond simple request-response behavior, both applications also publish domain events to Apache Kafka as part of write operations. Kafka runs in KRaft mode and is paired with Confluent Schema Registry, with all events serialized using Avro. Each create, update, or delete operation on customers, orders, and order items emits a corresponding event containing the full entity payload. Event publication occurs after the database transaction commits and is handled in a best-effort, asynchronous manner so that Kafka availability does not affect request success. The WebFlux application uses Reactor Kafka for non-blocking event publishing, while the MVC application uses Spring Kafka with a `KafkaTemplate`, reflecting idiomatic usage in each programming model.  

All infrastructure components—including PostgreSQL, Kafka, Schema Registry, Prometheus, Grafana, Vault, and both Spring Boot applications—are orchestrated using Docker Compose to ensure repeatability. Both services expose Spring Boot Actuator endpoints, which Prometheus scrapes at regular intervals. Grafana dashboards are preconfigured with identical layouts for each application, making it possible to compare throughput, latency percentiles, CPU usage, memory consumption, thread counts, garbage collection behavior, database connection usage, and Kafka producer metrics side by side under equivalent load.  

Load is generated using [k6](https://k6.io/), with scripted scenarios that exercise baseline traffic, read-heavy mixes, stress conditions, and traffic spikes. Tests are run sequentially against each application with cooldown periods in between to minimize cross-test interference. During each run, metrics are observed in real time via Grafana and recorded for later analysis. The experiment defines explicit success criteria, including achieving at least 95 percent of WebFlux throughput with comparable tail latency for the MVC virtual-thread implementation, while also considering qualitative factors such as operational simplicity, debuggability, and ecosystem compatibility.  

Overall, the setup aims to answer a focused question with empirical evidence: whether the combination of Spring MVC and virtual threads can match the performance characteristics traditionally associated with reactive WebFlux, even when realistic database access and event-driven Kafka messaging are part of the workload, rather than relying on theoretical arguments or simplified benchmarks


## Load tests

### Baseline test 

Purpose: Establish fundamental performance characteristics with gradual load increase.


  Load Pattern:
  - Gradual ramp-up: 0 → 50 users (30s)
  - Sustained: 50 users (2 minutes)
  - Increase: 50 → 100 users (30s)
  - Sustained: 100 users (2 minutes)
  - Ramp-down: 100 → 0 users (30s)

  Duration: 5 minutes 30 seconds

  Target: `/api/customers` endpoint

  Success Thresholds:
  - P95 latency < 500ms
  - Error rate < 1%

  Use Case: Comparing normal operating performance between WebFlux and MVC under controlled, predictable load.


### Read-Heavy test 


  Purpose: Simulate realistic production traffic patterns where reads dominate writes.

  Load Pattern:
  - 80% read operations (`GET` requests)
  - 20% write operations (`POST`/`PUT`/`DELETE` requests)
  - 100 concurrent users
  - Steady state throughout test

  Duration: 5 minutes

  Targets: Customer and order endpoints (mixed operations)

  Use Case: Evaluating performance under typical e-commerce traffic patterns where most users browse rather than transact.




## Results

### Baseline test performance

Period: 6 minutes

| Metric                      | reactive-webflux | mvc-virtual-threads |
| --------------------------- | ---------------: | ------------------: |
| P95 Response Time (ms)      |               14 |                  10 |
| Mean Response Time (ms)     |                6 |                   4 |
| Request Rate (requests/sec) |            69.26 |               71.45 |
| Thread Count                |             35.4 |                29.0 |
| Heap Memory (MB)            |               67 |                  55 |
| CPU Usage (%)               |             2.18 |                1.71 |
| GC Pause Time (ms)          |             3.03 |                1.68 |

### Read-heavy test performance

Period: 5 minutes

| Metric                      | reactive-webflux | mvc-virtual-threads |
| --------------------------- | ---------------: | ------------------: |
| P95 Response Time (ms)      |               20 |                  23 |
| Mean Response Time (ms)     |                7 |                   8 |
| Request Rate (requests/sec) |            56.80 |               56.71 |
| Thread Count                |             39.0 |                29.3 |
| Heap Memory (MB)            |              123 |                 220 |
| CPU Usage (%)               |             4.03 |                4.76 |
| GC Pause Time (ms)          |             2.26 |                2.65 |



## Conclusions

The results of this experiment make a compelling case for the transformative impact of virtual threads on Java server-side performance. In the baseline test, Spring MVC with virtual threads didn't merely keep pace with WebFlux—it outperformed it across every measured dimension: lower P95 latency (10ms vs. 14ms), higher throughput (71.45 req/s vs. 69.26 req/s), fewer threads consumed (29 vs. 35.4), less heap memory used (55 MB vs. 67 MB), lower CPU utilization (1.71% vs. 2.18%), and shorter GC pause times (1.68ms vs. 3.03ms). Under the read-heavy mixed workload, the two approaches converged to near parity in throughput and latency, with WebFlux holding a modest advantage in memory efficiency while MVC maintained its leaner thread footprint. The takeaway is clear: virtual threads have effectively closed the performance gap that once justified the complexity of a fully reactive stack. Blocking I/O on virtual threads scales just as well as non-blocking reactive streams, and in many scenarios it scales better.

What makes virtual threads truly significant is not just the benchmark numbers, but what they mean for the broader Java ecosystem. Before Project Loom, teams facing high-concurrency requirements had two choices: accept the thread-exhaustion ceiling of traditional thread-per-request models, or adopt reactive programming with all of its associated complexity—unfamiliar APIs, difficult debugging, stack traces that obscure rather than illuminate, and a steep learning curve that slows onboarding and increases the risk of subtle bugs. Virtual threads eliminate this forced tradeoff. Developers can write straightforward, imperative, sequential code using the same patterns and libraries that have powered Java applications for decades, and the runtime handles concurrency scaling transparently. Existing JDBC drivers, ORMs, logging frameworks, and testing tools all work without modification. This is the real power of virtual threads in Java 21 and beyond: they democratize high-concurrency performance, making it accessible to every Java application without demanding an architectural overhaul or a paradigm shift. For teams evaluating their server-side stack today, the evidence strongly suggests that Spring MVC on virtual threads deserves to be the default starting point, with reactive WebFlux reserved for the narrow set of use cases where streaming, backpressure, or specific non-blocking integrations genuinely demand it.
