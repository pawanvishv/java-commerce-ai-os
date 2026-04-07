package com.commerceos.resilience.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceAutoConfiguration {

    /**
     * AI sidecar circuit breaker:
     * 50% failure over 10 calls → OPEN; resets after 30s
     */
    @Bean("aiSidecarCircuitBreakerConfig")
    public CircuitBreakerConfig aiSidecarCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
    }

    /**
     * External gateway circuit breaker (Razorpay, Stripe, PhonePe)
     */
    @Bean("gatewayCircuitBreakerConfig")
    public CircuitBreakerConfig gatewayCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(60)
                .slidingWindowSize(20)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(5)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
    }

    /**
     * Default retry config for inter-service calls
     */
    @Bean("defaultRetryConfig")
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(Exception.class)
                .build();
    }

    /**
     * Time limiter for external API calls
     */
    @Bean("externalApiTimeLimiterConfig")
    public TimeLimiterConfig externalApiTimeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .build();
    }
}
