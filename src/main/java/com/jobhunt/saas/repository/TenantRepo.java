package com.jobhunt.saas.repository;

import com.jobhunt.saas.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepo extends JpaRepository<Tenant, Long> {
    // Get tenant by ID
    Optional<Tenant> findById(Long id);

    // Get tenant by API Keys
    Optional<Tenant> findByClientIdAndClientSecret(String clientId, String clientSecret);
}
