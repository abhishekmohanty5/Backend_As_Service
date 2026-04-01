package com.jobhunt.saas.scheduling.application.service;

import com.jobhunt.saas.shared.domain.model.SubscriptionStatus;
import com.jobhunt.saas.subscription.domain.model.TenantSubscription;
import com.jobhunt.saas.subscription.adapter.out.persistence.TenantSubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EngineSubscriptionCleanup {
    private final TenantSubscriptionRepo tenantSubscriptionRepo;

    /**
     * Runs every day at midnight to check if Engine Plans (Tenants) have expired.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @net.javacrumbs.shedlock.spring.annotation.SchedulerLock(name = "checkAndExpireTenantPlans", lockAtLeastFor = "5m", lockAtMostFor = "10m")
    @Transactional
    public void checkAndExpireTenantPlans() {
        log.info("Starting Daily Engine Plan Cleanup Job...");

        List<TenantSubscription> expiredSubs = tenantSubscriptionRepo
                .findAllByStatusAndExpireDateBefore(SubscriptionStatus.ACTIVE, LocalDateTime.now());

        if (!expiredSubs.isEmpty()) {
            for (TenantSubscription sub : expiredSubs) {
                log.info("Tenant '{}' (ID: {}) exceeded their {} plan duration.",
                        sub.getTenant().getName(), sub.getTenant().getId(), sub.getPlan().getName());

                sub.setStatus(SubscriptionStatus.EXPIRED);
            }
            tenantSubscriptionRepo.saveAll(expiredSubs);
        }

        log.info("Engine Plan Cleanup Complete. Expired {} tenants.", expiredSubs.size());
    }
}
