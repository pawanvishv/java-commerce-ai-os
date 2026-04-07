package com.commerceos.inventory.infrastructure.persistence;

import com.commerceos.inventory.domain.model.PendingReservation;
import com.commerceos.inventory.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingReservationRepository
        extends JpaRepository<PendingReservation, UUID> {

    Optional<PendingReservation> findByOrderIdAndSkuAndTenantId(
            String orderId, String sku, String tenantId);

    @Query("""
        SELECT r FROM PendingReservation r
        WHERE r.status = 'PENDING'
        AND r.expiresAt < :now
        """)
    List<PendingReservation> findExpired(
            @org.springframework.data.repository.query.Param("now")
            Instant now);
}
