package com.commerceos.ledger.infrastructure.persistence;

import com.commerceos.ledger.domain.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByTenantIdAndOrderId(
            String tenantId, String orderId);

    @Query("""
        SELECT SUM(CASE WHEN e.direction = 'DEBIT'
            THEN e.amountPaise ELSE 0 END) -
               SUM(CASE WHEN e.direction = 'CREDIT'
            THEN e.amountPaise ELSE 0 END)
        FROM LedgerEntry e
        WHERE e.tenantId = :tenantId
        """)
    Long getBalanceForTenant(@Param("tenantId") String tenantId);
}
