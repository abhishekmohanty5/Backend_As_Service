package com.jobhunt.saas.subscription.domain.port.in;

import com.jobhunt.saas.subscription.application.dto.EnginePlanUpgradeRequest;
import com.jobhunt.saas.subscription.domain.model.TenantSubscription;

public interface TenantSubscriptionUseCase {
    TenantSubscription getCurrentSubscription();
    TenantSubscription upgradePlan(Long newPlanId, String billingInterval, String transactionId);
}
