package com.commerceos.sellerservice.infrastructure.persistence;

import com.commerceos.sellerservice.domain.enums.PayoutStatus;
import com.commerceos.sellerservice.domain.model.SellerPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayoutRepository
        extends JpaRepository<SellerPayout, UUID> {

    List<SellerPayout> findBySellerIdAndTenantId(
            String sellerId, String tenantId);
    List<SellerPayout> findByStatus(PayoutStatus status);
}
