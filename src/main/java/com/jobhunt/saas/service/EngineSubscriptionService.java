package com.jobhunt.saas.service;

import com.jobhunt.saas.entity.Plan;
import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.Tenant;
import com.jobhunt.saas.entity.TenantSubscription;
import com.jobhunt.saas.repository.PlanRepo;
import com.jobhunt.saas.repository.TenantRepo;
import com.jobhunt.saas.repository.TenantSubscriptionRepo;
import com.jobhunt.saas.tenant.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EngineSubscriptionService {

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
     */
    @Transactional
    public TenantSubscription upgradePlan(Long newPlanId) {
        Long tenantId = TenantContext.getTenantId();

        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Plan targetPlan = planRepo.findById(newPlanId)
                .orElseThrow(() -> new RuntimeException("Target plan not found"));

        if (!targetPlan.isActive()) {
            throw new RuntimeException("This plan is no longer active for new subscriptions");
        }

        // Reset the creation time so the duration timer starts over from today
        TenantSubscription currentSub = tenantSubscriptionRepo.findFirstByTenantIdOrderByCreatedAtDesc(tenantId)
                .orElse(null);

        if (currentSub != null) {
            currentSub.setStatus(SubscriptionStatus.CANCELLED);
            currentSub.setExpireDate(LocalDateTime.now());
            tenantSubscriptionRepo.save(currentSub);
        }

        TenantSubscription newSub = new TenantSubscription();
        newSub.setTenant(tenant);
        newSub.setPlan(targetPlan);
        newSub.setStatus(SubscriptionStatus.ACTIVE);
        newSub.setStartDate(LocalDateTime.now());
        newSub.setExpireDate(LocalDateTime.now().plusDays(targetPlan.getDurationInDays()));

        return tenantSubscriptionRepo.save(newSub);
    }
}
