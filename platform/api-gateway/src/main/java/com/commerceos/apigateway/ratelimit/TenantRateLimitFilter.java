package com.commerceos.apigateway.ratelimit;

import com.commerceos.apigateway.config.AppGatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
public class TenantRateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final AppGatewayProperties gatewayProperties;

    public TenantRateLimitFilter(
            @Qualifier("reactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
            AppGatewayProperties gatewayProperties) {
        this.redisTemplate = redisTemplate;
        this.gatewayProperties = gatewayProperties;
    }

    private static final String RATE_KEY_PREFIX = "rate:";

    @Override
    public int getOrder() {
        return -90;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String tenantId = exchange.getRequest()
                .getHeaders().getFirst("X-Tenant-ID");

        if (tenantId == null) {
            return chain.filter(exchange);
        }

        String key = RATE_KEY_PREFIX + tenantId + ":"
                + getCurrentWindow();

        int limit = gatewayProperties.getRateLimit()
                .getDefaultRequestsPerSecond();

        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        return redisTemplate.expire(key, Duration.ofSeconds(1))
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > limit) {
                        log.warn("Rate limit exceeded for tenant: {}", tenantId);
                        exchange.getResponse()
                                .setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders()
                                .set("X-RateLimit-Limit", String.valueOf(limit));
                        exchange.getResponse().getHeaders()
                                .set("X-RateLimit-Remaining", "0");
                        return exchange.getResponse().setComplete();
                    }
                    exchange.getResponse().getHeaders()
                            .set("X-RateLimit-Remaining", String.valueOf(limit - count));
                    return chain.filter(exchange);
                });
    }

    private long getCurrentWindow() {
        return System.currentTimeMillis() / 1000;
    }
}