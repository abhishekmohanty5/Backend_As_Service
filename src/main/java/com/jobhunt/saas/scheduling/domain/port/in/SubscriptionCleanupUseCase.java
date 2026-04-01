package com.jobhunt.saas.scheduling.domain.port.in;

public interface SubscriptionCleanupUseCase {
    void expireUserSubscriptions();
    void expireTenantSubscriptions();
}
