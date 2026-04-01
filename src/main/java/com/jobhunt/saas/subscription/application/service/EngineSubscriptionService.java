package com.jobhunt.saas.subscription.application.service;

import com.jobhunt.saas.plan.domain.model.Plan;
import com.jobhunt.saas.shared.domain.model.SubscriptionStatus;
import com.jobhunt.saas.tenant.domain.model.Tenant;
import com.jobhunt.saas.subscription.domain.model.TenantSubscription;
import com.jobhunt.saas.plan.adapter.out.persistence.PlanRepo;
import com.jobhunt.saas.tenant.adapter.out.persistence.TenantRepo;
import com.jobhunt.saas.subscription.adapter.out.persistence.TenantSubscriptionRepo;
import com.jobhunt.saas.shared.TenantContext;
import com.jobhunt.saas.subscription.domain.port.in.TenantSubscriptionUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EngineSubscriptionService implements TenantSubscriptionUseCase {

    private final TenantRepo tenantRepo;
    private final PlanRepo planRepo;
    private final TenantSubscriptionRepo tenantSubscriptionRepo;

    /**
     * Get the current engine subscription for the logged-in Tenant admin.
     */
    public TenantSubscription getCurrentSubscription() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("No tenant in context");
        }
        return tenantSubscriptionRepo.findFirstByTenantIdOrderByCreatedAtDesc(tenantId)
                .orElseThrow(() -> new RuntimeException("Subscription not found for tenant"));
    }

    /**
     * Upgrades/changes the Tenant's Engine Plan.
     *
     * @param newPlanId        the target plan to switch to
     * @param billingInterval  "MONTHLY" (30 days) or "ANNUAL" (365 days, 20% off)
     * @param transactionId    opaque transaction reference from the mock payment step
     */
    @Transactional
    public TenantSubscription upgradePlan(Long newPlanId, String billingInterval, String transactionId) {
        Long tenantId = TenantContext.getTenantId();

        // --- Validate transactionId was provided (proves payment step was executed) ---
        if (transactionId == null || transactionId.isBlank()) {
            throw new RuntimeException("Payment transaction ID is required. Complete payment first.");
        }

        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Plan targetPlan = planRepo.findById(newPlanId)
                .orElseThrow(() -> new RuntimeException("Target plan not found"));

        if (!targetPlan.isActive()) {
            throw new RuntimeException("This plan is no longer active for new subscriptions");
        }

        // --- Determine duration based on billing interval ---
        boolean isAnnual = "ANNUAL".equalsIgnoreCase(billingInterval);
        int durationDays = isAnnual ? 365 : 30;

        // --- Cancel any existing active subscription ---
        TenantSubscription currentSub = tenantSubscriptionRepo
                .findFirstByTenantIdOrderByCreatedAtDesc(tenantId)
                .orElse(null);

        if (currentSub != null) {
            currentSub.setStatus(SubscriptionStatus.CANCELLED);
            currentSub.setExpireDate(LocalDateTime.now());
            tenantSubscriptionRepo.save(currentSub);
        }

        // --- Create new subscription ---
        TenantSubscription newSub = new TenantSubscription();
        newSub.setTenant(tenant);
        newSub.setPlan(targetPlan);
        newSub.setStatus(SubscriptionStatus.ACTIVE);
        newSub.setStartDate(LocalDateTime.now());
        newSub.setExpireDate(LocalDateTime.now().plusDays(durationDays));


        return tenantSubscriptionRepo.save(newSub);
    }
}

