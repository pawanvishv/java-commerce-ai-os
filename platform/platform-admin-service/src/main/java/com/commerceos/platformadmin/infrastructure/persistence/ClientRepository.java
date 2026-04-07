package com.commerceos.platformadmin.infrastructure.persistence;

import com.commerceos.platformadmin.domain.model.Client;
import com.commerceos.platformadmin.domain.enums.ClientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByTenantId(String tenantId);
    Optional<Client> findBySlug(String slug);
    Optional<Client> findByEmail(String email);
    List<Client> findByStatus(ClientStatus status);
    boolean existsByEmail(String email);
    boolean existsByTenantId(String tenantId);
}
