package com.jobhunt.saas.repository;

import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserSubscriptionRepo  extends JpaRepository<UserSubscription,Long> {

    List<UserSubscription> findByUserId(Long userId);
    List<UserSubscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
    // CORRECT - Following the actual field name "subscriptionCategory"
    List<UserSubscription> findByUserIdAndSubscriptionCategoryId(Long userId, Long categoryId);
    List<UserSubscription> findByUserIdAndNextBillingDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
}
