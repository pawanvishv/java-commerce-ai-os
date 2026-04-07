package com.commerceos.tenant.context;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TenantContext {

    private static final ThreadLocal<String> TENANT_ID = new InheritableThreadLocal<>();
    private static final ThreadLocal<String> SCHEMA_NAME = new InheritableThreadLocal<>();
    private static final ThreadLocal<String> TENANT_PROFILE = new InheritableThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(String tenantId) {
        log.debug("Setting tenant ID: {}", tenantId);
        TENANT_ID.set(tenantId);
    }

    public static String getTenantId() {
        return TENANT_ID.get();
    }

    public static void setSchemaName(String schemaName) {
        SCHEMA_NAME.set(schemaName);
    }

    public static String getSchemaName() {
        return SCHEMA_NAME.get();
    }

    public static void setTenantProfile(String profile) {
        TENANT_PROFILE.set(profile);
    }

    public static String getTenantProfile() {
        return TENANT_PROFILE.get();
    }

    public static void clear() {
        TENANT_ID.remove();
        SCHEMA_NAME.remove();
        TENANT_PROFILE.remove();
    }

    public static boolean hasTenant() {
        return TENANT_ID.get() != null;
    }
}