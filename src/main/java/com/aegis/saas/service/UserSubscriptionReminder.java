package com.aegis.saas.service;

import com.aegis.saas.entity.SubscriptionStatus;
import com.aegis.saas.entity.UserSubscription;
import com.aegis.saas.entity.TenantSubscription;
import com.aegis.saas.entity.Users;
import com.aegis.saas.repository.TenantSubscriptionRepo;
import com.aegis.saas.repository.UserSubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSubscriptionReminder {

    private final EmailService emailService;
    private final UserSubscriptionRepo userSubscriptionRepo;
    private final TenantSubscriptionRepo tenantSubscriptionRepo;

    @Transactional
    public void sendRenewalNotification() {

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);

        List<UserSubscription> userSubscriptions = userSubscriptionRepo.findByNextBillingDateBetweenAndStatus(
                startDate,
                endDate,
                SubscriptionStatus.ACTIVE);

        for (UserSubscription userSubscription : userSubscriptions) {
            Users user = userSubscription.getUser();

            // Check Tenant's Engine Plan (Must be PRO or ENTERPRISE)
            TenantSubscription tenantSub = tenantSubscriptionRepo
                    .findFirstByTenantIdOrderByCreatedAtDesc(user.getTenant().getId())
                    .orElse(null);

            if (tenantSub != null && tenantSub.getStatus() == SubscriptionStatus.ACTIVE) {
                String enginePlan = tenantSub.getPlan().getName().toUpperCase();
                if ("PRO".equals(enginePlan) || "ENTERPRISE".equals(enginePlan)) {
                    String email = user.getEmail();
                    emailService.sendEmail(email,
                            "Your next renewal is coming up",
                            buildEmailBody(user, userSubscription));
                }
            }
        }

    }

    private String buildEmailBody(Users user, UserSubscription sub) {
        String tenantName = user.getTenant() != null ? user.getTenant().getName() : "us";
        return "Hi " + user.getUsername() + ",\n\n" +
                "Your subscription for " + sub.getSubscriptionName() +
                " at " + tenantName + " will renew on " + sub.getNextBillingDate() + ".\n\n" +
                "Amount: ₹" + sub.getAmount() + "\n\n" +
                "Thanks,\n" + tenantName + " Team";
    }

}
