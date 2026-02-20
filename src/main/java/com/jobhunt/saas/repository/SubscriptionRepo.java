package com.jobhunt.saas.repository;

import com.jobhunt.saas.entity.Subscription;
import com.jobhunt.saas.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {

        Optional<Subscription> findByUserIdAndTenantId(Long userId, Long tenantId);

        boolean existsByUserIdAndTenantIdAndStatus(
                        Long userId,
                        Long tenantId,
                        SubscriptionStatus status);

        Optional<Subscription> findByUserIdAndTenantIdAndStatus(
                        Long userId,
                        Long tenantId,
                        SubscriptionStatus status);

        // Find All user Where Status Is Active and End Time Before currentTime
        List<Subscription> findAllByStatusAndEndDateBefore(SubscriptionStatus status, LocalDateTime dateTime);
}