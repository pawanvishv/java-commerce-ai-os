package com.commerceos.tenant.context;

import com.commerceos.tenant.filter.TenantAwareFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "com.commerceos.tenant")
public class TenantContextAutoConfiguration {

    @Bean
    public TenantAwareFilter tenantAwareFilter() {
        return new TenantAwareFilter();
    }
}