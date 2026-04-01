package com.jobhunt.saas.subscription.adapter.out.persistence;

import com.jobhunt.saas.shared.domain.model.SubscriptionStatus;
import com.jobhunt.saas.subscription.domain.model.UserSubscription;
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
