package com.commerceos.authservice.domain.model;

import com.commerceos.common.model.BaseEntity;
import com.commerceos.authservice.domain.enums.CredentialStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "api_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCredential extends BaseEntity {

    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;

    @Column(name = "client_secret_hash", nullable = false)
    private String clientSecretHash;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CredentialStatus status;

    @Column(name = "scopes")
    private String scopes;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "last_rotated_at")
    private Instant lastRotatedAt;
}
