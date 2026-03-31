package com.jobhunt.saas.tenant;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;


import java.util.Map;

@Configuration
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<Long>, HibernatePropertiesCustomizer {

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long tenantId = TenantContext.getTenantId();
        // Hibernate 6 requires a non-null identifier, or it might throw exceptions if a
        // tenant is missing.
        // Return a dummy value or the actual ID. Some tables (like SaaS admin) might
        // not need a tenant.
        // We can throw if null, but let's see how the app works. Let's just return a
        // default value like 0L if missing.
        return tenantId != null ? tenantId : 0L;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    // This makes sure Hibernate picks up this resolver
    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
