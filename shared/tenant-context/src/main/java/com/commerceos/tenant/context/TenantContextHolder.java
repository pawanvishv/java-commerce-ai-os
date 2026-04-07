package com.commerceos.tenant.context;

import com.commerceos.tenant.model.TenantIdentity;
import lombok.extern.slf4j.Slf4j;

/**
 * Used in Kafka consumers and async tasks where headers
 * carry tenant info instead of HTTP headers.
 */
@Slf4j
public final class TenantContextHolder {

    private static final ThreadLocal<TenantIdentity> TENANT_IDENTITY =
            new InheritableThreadLocal<>();

    private TenantContextHolder() {}

    public static void set(TenantIdentity identity) {
        TENANT_IDENTITY.set(identity);
        TenantContext.setTenantId(identity.getTenantId());
        TenantContext.setSchemaName(identity.getSchemaName());
        TenantContext.setTenantProfile(identity.getProfile());
    }

    public static TenantIdentity get() {
        return TENANT_IDENTITY.get();
    }

    public static void clear() {
        TENANT_IDENTITY.remove();
        TenantContext.clear();
    }

    public static boolean hasIdentity() {
        return TENANT_IDENTITY.get() != null;
    }
}
