package com.commerceos.tenant.adapter;

import com.commerceos.tenant.context.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;

@Slf4j
@RequiredArgsConstructor
public class TenantTransactionSynchronizationAdapter implements TransactionSynchronization {

    private final EntityManager entityManager;
    private final String schemaName;

    /**
     * Sets search_path at transaction start — the ONLY safe place to do this.
     * PgBouncer in transaction-pooling mode requires SET LOCAL, not SET.
     */
    public void setSearchPath() {
        if (schemaName == null || schemaName.isBlank()) {
            log.warn("Schema name is blank — skipping search_path override");
            return;
        }
        log.debug("Setting search_path to: {}", schemaName);
        entityManager.createNativeQuery(
                "SET LOCAL search_path TO " + schemaName + ", public"
        ).executeUpdate();
    }

    @Override
    public void afterCompletion(int status) {
        TenantContext.clear();
    }
}
