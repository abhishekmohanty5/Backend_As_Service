package com.jobhunt.saas.service;

import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.TenantSubscription;
import com.jobhunt.saas.entity.UserSubscription;
import com.jobhunt.saas.entity.Users;
import com.jobhunt.saas.repository.TenantSubscriptionRepo;
import com.jobhunt.saas.repository.UserSubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChurnService {

    private final EmailService emailService;
    private final UserSubscriptionRepo userSubscriptionRepo;
    private final TenantSubscriptionRepo tenantSubscriptionRepo;

    @org.springframework.beans.factory.annotation.Value("${saas.churn.discount-code:KEEP20}")
    private String discountCode;

    /**
     * Runs daily at 10:00 AM to predict user churn.
     * Only applies to end-users of Tenants on the ENTERPRISE plan.
     */
    @Scheduled(cron = "0 0 10 * * ?")
    @net.javacrumbs.shedlock.spring.annotation.SchedulerLock(name = "predictAndPreventChurn", lockAtLeastFor = "5m", lockAtMostFor = "10m")
    @Transactional
    public void predictAndPreventChurn() {
        log.info("Starting AI Churn Prediction Job...");

        // Find users whose subscriptions are expiring in exactly 3 days (high churn
        // risk)
        LocalDate today = LocalDate.now();
        LocalDate threeDaysFromNow = today.plusDays(3);

        List<UserSubscription> atRiskSubscriptions = userSubscriptionRepo.findByNextBillingDateBetweenAndStatus(
                threeDaysFromNow,
                threeDaysFromNow,
                SubscriptionStatus.ACTIVE);

        for (UserSubscription sub : atRiskSubscriptions) {
            Users user = sub.getUser();

            // Check if Tenant has ENTERPRISE Engine Plan
            TenantSubscription tenantSub = tenantSubscriptionRepo
                    .findFirstByTenantIdOrderByCreatedAtDesc(user.getTenant().getId())
                    .orElse(null);

            if (tenantSub != null && tenantSub.getStatus() == SubscriptionStatus.ACTIVE) {
                String enginePlan = tenantSub.getPlan().getName().toUpperCase();

                // Only ENTERPRISE tenants get the AI churn discount feature
                if ("ENTERPRISE".equals(enginePlan)) {
                    sendDiscountEmailOnBehalfOfTenant(user, sub, discountCode);
                }
            }
        }
    }

    private void sendDiscountEmailOnBehalfOfTenant(Users user, UserSubscription sub, String code) {
        String tenantName = user.getTenant() != null ? user.getTenant().getName() : "us";
        String email = user.getEmail();

        String subject = "A special offer just for you from " + tenantName;
        String body = "Hi " + user.getUsername() + ",\n\n" +
                "We noticed your " + sub.getSubscriptionName() + " subscription is expiring soon.\n\n" +
                "We'd love to keep you around! Here is an exclusive 20% discount code to use on your next renewal:\n\n"
                +
                "Code: " + code + "\n\n" +
                "Thanks,\nThe " + tenantName + " Team";

        emailService.sendEmail(email, subject, body);
        log.info("Sent AI Churn discount email to {} on behalf of {}", email, tenantName);
    }
}
