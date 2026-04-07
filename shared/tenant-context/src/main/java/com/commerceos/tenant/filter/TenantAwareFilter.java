package com.commerceos.tenant.filter;

import com.commerceos.tenant.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(
        name = "commerce.tenant.filter.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class TenantAwareFilter extends OncePerRequestFilter {

    public static final String HEADER_TENANT_ID   = "X-Tenant-ID";
    public static final String HEADER_SCHEMA_NAME = "X-Schema-Name";
    public static final String HEADER_PROFILE     = "X-Tenant-Profile";

    private static final String[] EXEMPT_PATHS = {
            "/actuator", "/health", "/.well-known"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String exempt : EXEMPT_PATHS) {
            if (path.startsWith(exempt)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId   = request.getHeader(HEADER_TENANT_ID);
        String schemaName = request.getHeader(HEADER_SCHEMA_NAME);
        String profile    = request.getHeader(HEADER_PROFILE);

        if (tenantId == null || tenantId.isBlank()) {
            log.warn("Request missing X-Tenant-ID header: {} {}",
                    request.getMethod(), request.getRequestURI());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing required header: X-Tenant-ID");
            return;
        }

        try {
            TenantContext.setTenantId(tenantId);
            TenantContext.setSchemaName(schemaName);
            TenantContext.setTenantProfile(profile);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}