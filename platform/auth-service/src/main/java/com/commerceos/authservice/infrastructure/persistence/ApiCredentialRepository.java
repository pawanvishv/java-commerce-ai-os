package com.commerceos.authservice.infrastructure.persistence;

import com.commerceos.authservice.domain.model.ApiCredential;
import com.commerceos.authservice.domain.enums.CredentialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiCredentialRepository extends JpaRepository<ApiCredential, UUID> {

    Optional<ApiCredential> findByClientId(String clientId);

    Optional<ApiCredential> findByClientIdAndStatus(String clientId,
                                                     CredentialStatus status);

    boolean existsByClientId(String clientId);
}
