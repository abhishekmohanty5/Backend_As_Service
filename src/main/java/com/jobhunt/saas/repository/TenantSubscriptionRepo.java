package com.jobhunt.saas.repository;

import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.TenantSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TenantSubscriptionRepo extends JpaRepository<TenantSubscription, Long> {

    Optional<TenantSubscription> findFirstByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<TenantSubscription> findAllByStatusAndExpireDateBefore(SubscriptionStatus status, LocalDateTime dateTime);

    List<TenantSubscription> findAllByStatus(SubscriptionStatus status);
}
