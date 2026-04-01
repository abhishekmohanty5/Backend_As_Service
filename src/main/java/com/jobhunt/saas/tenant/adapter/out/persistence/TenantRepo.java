package com.jobhunt.saas.tenant.adapter.out.persistence;

import com.jobhunt.saas.tenant.domain.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepo extends JpaRepository<Tenant, Long> {
    // Get tenant by ID
    Optional<Tenant> findById(Long id);

    // Get tenant by API Keys
    Optional<Tenant> findByClientIdAndClientSecret(String clientId, String clientSecret);
}
