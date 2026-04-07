package com.commerceos.apigateway.filter;

import com.commerceos.apigateway.resolver.TenantIdentityResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final TenantIdentityResolver tenantIdentityResolver;

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                              GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    if (ctx.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                        Jwt jwt = jwtAuth.getToken();
                        return tenantIdentityResolver.resolve(jwt)
                                .flatMap(identity -> {
                                    String tenantId   = identity.get("tenantId");
                                    String schemaName = identity.get("schemaName");
                                    String profile    = identity.get("profile");
                                    String status     = identity.get("status");

                                    if (!"ACTIVE".equals(status)) {
                                        log.warn("Tenant {} is not active: {}",
                                                tenantId, status);
                                        exchange.getResponse()
                                                .setStatusCode(HttpStatus.FORBIDDEN);
                                        return exchange.getResponse().setComplete();
                                    }

                                    ServerWebExchange mutated = exchange.mutate()
                                            .request(r -> r.headers(headers -> {
                                                headers.set("X-Tenant-ID", tenantId);
                                                headers.set("X-Schema-Name", schemaName);
                                                headers.set("X-Tenant-Profile", profile);
                                                headers.set("X-User-ID",
                                                        jwt.getSubject());
                                            }))
                                            .build();

                                    log.debug("Forwarding request for tenant: {}",
                                            tenantId);
                                    return chain.filter(mutated);
                                });
                    }
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }));
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator")
                || path.startsWith("/health");
    }
}
