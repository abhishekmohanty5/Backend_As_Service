package com.jobhunt.saas.repository;

import com.jobhunt.saas.entity.Subscription;
import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepo extends JpaRepository<Tenant,Long> {
    // Get tenant by ID
    Optional<Tenant> findById(Long id);

    // If you need tenant by status
    Optional<Tenant> findByIdAndStatus(Long tenantId, SubscriptionStatus status);

    // OR (commonly used)
    Optional<Tenant> findByIdAndStatusNot(
            Long tenantId,
            SubscriptionStatus status
    );
}
