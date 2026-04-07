package com.commerceos.tenant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantIdentity implements Serializable {

    private String tenantId;
    private String schemaName;
    private String profile;
    private String status;
    private boolean active;

    public static TenantIdentity of(String tenantId, String schemaName,
                                     String profile, String status) {
        return TenantIdentity.builder()
                .tenantId(tenantId)
                .schemaName(schemaName)
                .profile(profile)
                .status(status)
                .active("ACTIVE".equals(status))
                .build();
    }
}
