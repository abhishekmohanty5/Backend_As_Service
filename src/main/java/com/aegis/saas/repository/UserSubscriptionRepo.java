package com.aegis.saas.repository;

import com.aegis.saas.entity.SubscriptionStatus;
import com.aegis.saas.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserSubscriptionRepo extends JpaRepository<UserSubscription, Long> {

        List<UserSubscription> findByUserId(Long userId);

        List<UserSubscription> findByUser_TenantId(Long tenantId);

        List<UserSubscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

        // CORRECT - Following the actual field name "tenantPlan"
        List<UserSubscription> findByUserIdAndTenantPlanId(Long userId, Long tenantPlanId);

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
