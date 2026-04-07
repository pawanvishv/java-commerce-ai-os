package com.commerceos.paymentadapter.infrastructure.persistence;

import com.commerceos.paymentadapter.domain.model.PaymentTender;
import com.commerceos.paymentadapter.domain.enums.TenderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTenderRepository
        extends JpaRepository<PaymentTender, UUID> {

    Optional<PaymentTender> findByIdempotencyKey(String idempotencyKey);
    List<PaymentTender> findByOrderIdAndTenantId(
            String orderId, String tenantId);
    List<PaymentTender> findByStatus(TenderStatus status);
}
