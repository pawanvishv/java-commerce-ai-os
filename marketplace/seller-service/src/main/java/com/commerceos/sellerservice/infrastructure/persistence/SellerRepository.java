package com.commerceos.sellerservice.infrastructure.persistence;

import com.commerceos.sellerservice.domain.enums.SellerStatus;
import com.commerceos.sellerservice.domain.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SellerRepository
        extends JpaRepository<Seller, UUID> {

    Optional<Seller> findBySellerId(String sellerId);
    Optional<Seller> findBySellerIdAndTenantId(
            String sellerId, String tenantId);
    List<Seller> findByTenantIdAndStatus(
            String tenantId, SellerStatus status);
    boolean existsBySellerId(String sellerId);
}
