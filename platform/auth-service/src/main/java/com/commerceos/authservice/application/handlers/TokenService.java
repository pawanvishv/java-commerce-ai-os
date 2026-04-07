package com.commerceos.authservice.application.handlers;

import com.commerceos.authservice.config.AuthProperties;
import com.commerceos.authservice.domain.enums.CredentialStatus;
import com.commerceos.authservice.infrastructure.persistence.ApiCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final ApiCredentialRepository credentialRepository;
    private final AuthProperties authProperties;
    private final PasswordEncoder passwordEncoder;

    public String issueToken(String clientId, String clientSecret) {

        var credential = credentialRepository
                .findByClientIdAndStatus(clientId, CredentialStatus.ACTIVE)
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(clientSecret, credential.getClientSecretHash())) {
            throw invalidCredentials();
        }

        Instant now = Instant.now();
        Instant expiry = now.plus(authProperties.getAccessTokenTtlMinutes(), ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(authProperties.getIssuer())
                .subject(clientId)
                .issuedAt(now)
                .expiresAt(expiry)
                .id(UUID.randomUUID().toString())
                .claim("tenant_id", credential.getTenantId())
                .claim("scopes", credential.getScopes())
                .build();

        JwsHeader header = JwsHeader.with(
                        org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256)
                .build();

        log.debug("Issuing JWT for clientId: {}, tenantId: {}", clientId, credential.getTenantId());

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }

    private IllegalArgumentException invalidCredentials() {
        return new IllegalArgumentException("Invalid client credentials");
    }
}