package com.commerceos.apigateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gateway")
public class AppGatewayProperties {

    private RateLimit rateLimit = new RateLimit();
    private long tenantCacheTtlSeconds = 3600;

    @Getter
    @Setter
    public static class RateLimit {
        private int defaultRequestsPerSecond = 100;
        private int burstCapacity = 200;
    }
}
