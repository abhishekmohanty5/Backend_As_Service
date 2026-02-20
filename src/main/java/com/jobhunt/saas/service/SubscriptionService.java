package com.jobhunt.saas.service;

import com.jobhunt.saas.tenant.TenantContext;
import com.jobhunt.saas.auth.AuthContext;
import com.jobhunt.saas.dto.SubscriptionResponse;
import com.jobhunt.saas.entity.Plan;
import com.jobhunt.saas.entity.Subscription;
import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.repository.PlanRepo;
import com.jobhunt.saas.repository.SubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepo subscriptionRepo;
    private final PlanRepo planRepo;
    private final AuthContext authContext;

    public Long getCurrentUserId() {
        return authContext.getCurrentUserId();
    }

    // Subscription enforcement

    public void ensureActiveSubscription() {
        getCurrentUserActiveSubscription();
    }

    // Subscribe Service-1

    @Transactional
    public SubscriptionResponse subscribe(Long planId) {
        Long userId = getCurrentUserId();
        Long tenantId = TenantContext.getTenantId();

        // Check for User Has already Active Subscription
        subscriptionRepo
                .findByUserIdAndTenantIdAndStatus(userId, tenantId, SubscriptionStatus.ACTIVE)
                .ifPresent(sub -> {
                    if (isExpired(sub)) {
                        sub.setStatus(SubscriptionStatus.EXPIRED);
                        subscriptionRepo.save(sub);
                    } else {
                        throw new IllegalStateException(
                                "User already has an active subscription");
                    }
                });

        // Fetch Plan By Plain id and Check For The Plan Is Active or Not
        Plan plan = planRepo.findById(planId).orElseThrow(() -> new IllegalStateException("Plan not found"));

        if (!plan.isActive()) {
            throw new IllegalStateException("Plan is not active");
        }

        // Calculate Starting and Plan Duration
        int planDurationInDays = plan.getDurationInDays();
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(planDurationInDays);

        // Create New Subscription and Save In Subscription Db
        Subscription subscription = new Subscription();
        subscription.setPlan(plan);
        subscription.setUserId(userId);
        subscription.setTenantId(tenantId);
        subscription.setStartDate(startTime);
        subscription.setEndDate(endTime);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscriptionRepo.save(subscription);

        SubscriptionResponse subscriptionResponse = new SubscriptionResponse();
        subscriptionResponse.setMessage("Subscribed To " + plan.getName() + "Plan");
        subscriptionResponse.setId(subscription.getId());
        subscriptionResponse.setPlanId(plan.getId());
        subscriptionResponse.setStartDate(subscription.getStartDate());
        subscriptionResponse.setEndDate(subscription.getEndDate());

        return subscriptionResponse;
    }

    // Service -2 Cancel Subscription

    @Transactional
    public void cancelSubscription() {
        Long userId = getCurrentUserId();
        Long tenantId = TenantContext.getTenantId();

        // Check For NonActive Subscription
        Subscription subscription = subscriptionRepo
                .findByUserIdAndTenantIdAndStatus(userId, tenantId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active subscription exists to cancel"));

        // Cancel Subscription & Save in DB
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepo.save(subscription);
    }

    public boolean isExpired(Subscription subscription) {
        return subscription.getEndDate().isBefore(LocalDateTime.now());
    }

    // Service -3 get Current User Active Subscription

    public Subscription getActiveSubscriptionForUser(Long userId) {
        Long tenantId = TenantContext.getTenantId();

        Subscription sub = subscriptionRepo
                .findByUserIdAndTenantIdAndStatus(userId, tenantId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active subscription found"));

        if (isExpired(sub)) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepo.save(sub);
            throw new IllegalStateException("Subscription expired");
        }

        return sub;
    }

    // Service get Current User Active Subscription
    public Subscription getCurrentUserActiveSubscription() {
        return getActiveSubscriptionForUser(getCurrentUserId());
    }

}
