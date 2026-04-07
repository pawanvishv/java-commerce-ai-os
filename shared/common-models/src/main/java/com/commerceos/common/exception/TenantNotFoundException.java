package com.commerceos.common.exception;

public class TenantNotFoundException extends BusinessException {

    public TenantNotFoundException(String tenantId) {
        super("TENANT_NOT_FOUND", "Tenant not found: " + tenantId, 404);
    }
}
