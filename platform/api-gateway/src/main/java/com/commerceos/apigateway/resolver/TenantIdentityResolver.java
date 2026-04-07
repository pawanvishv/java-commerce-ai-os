package com.commerceos.apigateway.resolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantIdentityResolver {

    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String TENANT_KEY_PREFIX = "tenant:identity:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    public Mono<Map<String, String>> resolve(Jwt jwt) {
        String tenantId = jwt.getClaimAsString("tenant_id");

        if (tenantId == null) {
            return Mono.error(new IllegalArgumentException(
                    "JWT missing tenant_id claim"));
        }

        String cacheKey = TENANT_KEY_PREFIX + tenantId;

        return redisTemplate.opsForHash()
                .entries(cacheKey)
                .collectMap(
                        entry -> String.valueOf(entry.getKey()),
                        entry -> String.valueOf(entry.getValue())
                )
                .flatMap(cached -> {
                    if (!cached.isEmpty()) {
                        log.debug("Tenant identity cache hit: {}", tenantId);
                        return Mono.just(cached);
                    }

                    log.debug("Tenant identity cache miss: {}", tenantId);
                    return fetchAndCache(tenantId, cacheKey);
                });
    }

    private Mono<Map<String, String>> fetchAndCache(String tenantId, String cacheKey) {

        // TODO: Replace with actual call to platform-admin-service
        Map<String, String> identity = Map.of(
                "tenantId", tenantId,
                "schemaName", "tenant_" + tenantId,
                "profile", "FULL_COMMERCE",
                "status", "ACTIVE"
        );

        return redisTemplate.opsForHash()
                .putAll(cacheKey, identity)
                .flatMap(success -> redisTemplate.expire(cacheKey, CACHE_TTL))
                .thenReturn(identity);
    }
}