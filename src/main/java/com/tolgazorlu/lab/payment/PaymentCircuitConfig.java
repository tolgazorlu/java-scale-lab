package com.tolgazorlu.lab.payment;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Mission 09: stop calling a provider that is clearly broken, and check back now and then. */
@Configuration
class PaymentCircuitConfig {

    @Bean
    CircuitBreakerRegistry circuitBreakerRegistry(MeterRegistry meters) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(20)                         // judge the last 20 calls
                .minimumNumberOfCalls(10)                      // but only after 10 calls
                .failureRateThreshold(50)                      // open when half of them failed
                .slowCallDurationThreshold(Duration.ofSeconds(1))
                .slowCallRateThreshold(50)                     // or when half were slower than 1 s
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)      // then let 3 test calls through
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry).bindTo(meters);
        return registry;
    }

    @Bean
    CircuitBreaker paymentCircuit(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("payment");
    }
}
