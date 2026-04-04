package com.aegis.saas.repository;

import com.aegis.saas.entity.SubscriptionStatus;
import com.aegis.saas.entity.UserSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserSubscriptionRepo extends JpaRepository<UserSubscription, Long> {

        List<UserSubscription> findByUserId(Long userId);

        Page<UserSubscription> findByUserId(Long userId, Pageable pageable);

        List<UserSubscription> findByUser_TenantId(Long tenantId);

        Page<UserSubscription> findByUser_TenantId(Long tenantId, Pageable pageable);

        List<UserSubscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

        Page<UserSubscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status, Pageable pageable);

        // CORRECT - Following the actual field name "tenantPlan"
        List<UserSubscription> findByUserIdAndTenantPlanId(Long userId, Long tenantPlanId);

        Page<UserSubscription> findByUserIdAndTenantPlanId(Long userId, Long tenantPlanId, Pageable pageable);

        List<UserSubscription> findByUserIdAndNextBillingDateBetween(
                        Long userId,
                        LocalDate startDate,
                        LocalDate endDate);

        List<UserSubscription> findByNextBillingDateBetweenAndStatus(
                        LocalDate startDate,
                        LocalDate endDate,
                        SubscriptionStatus status

        );

        List<UserSubscription> findAllByStatusAndNextBillingDateBefore(SubscriptionStatus status, LocalDate date);
}
