package com.commerceos.sellerservice.infrastructure.persistence;

import com.commerceos.sellerservice.domain.model.SellerCommissionRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommissionRateRepository
        extends JpaRepository<SellerCommissionRate, UUID> {

    @Query("""
        SELECT r FROM SellerCommissionRate r
        WHERE r.sellerId = :sellerId
        AND r.tenantId = :tenantId
        ORDER BY r.effectiveFrom DESC
        LIMIT 1
        """)
    Optional<SellerCommissionRate> findLatestRate(
            @Param("sellerId") String sellerId,
            @Param("tenantId") String tenantId);
}
