package com.commerceos.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class AuthProperties {

    private String issuer;
    private long accessTokenTtlMinutes = 60;
    private long refreshTokenTtlDays = 7;
    private String jwksKeyId;
}
