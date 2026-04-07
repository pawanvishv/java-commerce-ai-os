package com.commerceos.provisioning.infrastructure.persistence;

import com.commerceos.provisioning.domain.model.ProvisioningJob;
import com.commerceos.provisioning.domain.enums.ProvisioningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProvisioningJobRepository
        extends JpaRepository<ProvisioningJob, UUID> {

    Optional<ProvisioningJob> findByTenantId(String tenantId);
    List<ProvisioningJob> findByStatus(ProvisioningStatus status);
}
