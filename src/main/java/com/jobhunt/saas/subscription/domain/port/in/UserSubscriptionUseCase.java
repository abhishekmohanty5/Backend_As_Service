package com.jobhunt.saas.subscription.domain.port.in;

import com.jobhunt.saas.subscription.application.dto.SubscriptionStatsDto;
import com.jobhunt.saas.subscription.application.dto.UserSubscriptionDto;
import com.jobhunt.saas.subscription.domain.model.UserSubscription;

import java.util.List;

public interface UserSubscriptionUseCase {
    void createSubscription(UserSubscriptionDto requestDto);
    List<UserSubscription> getUserSubscriptions();
    List<UserSubscription> getActiveSubscription();
    List<UserSubscription> getSubscriptionByTenantPlan(Long tenantPlanId);
    void updateSubscription(Long id, UserSubscriptionDto dto);
    void cancelSubscription(Long id);
    void deleteSubscription(Long id);
    List<UserSubscription> getUpcomingRenewals(int days);
    SubscriptionStatsDto getSubscriptionStatistics();
    List<String> getSubscriptionInsights();
}
