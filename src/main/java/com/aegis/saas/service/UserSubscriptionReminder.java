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
        String tenantName = user.getTenant() != null ? user.getTenant().getName() : "AegisInfra";
        return "Hi " + user.getUsername() + ",<br><br>" +
                "This is a professional reminder regarding your upcoming subscription renewal.<br><br>" +
                "<div style=\"background-color: #f8fafc; padding: 24px; border-radius: 12px; border: 1px solid #e2e8f0; border-left: 4px solid #2563eb;\">" +
                "<strong>Service:</strong> " + sub.getSubscriptionName() + "<br>" +
                "<strong>Provider:</strong> " + tenantName + "<br>" +
                "<strong>Renewal Date:</strong> <span class=\"highlight\">" + sub.getNextBillingDate() + "</span><br>" +
                "<strong>Amount:</strong> ₹" + sub.getAmount() +
                "</div><br>" +
                "The renewal will be processed automatically on the date mentioned above. If you wish to make any changes to your subscription, please do so before the renewal date.<br><br>" +
                "Thank you for being a valued customer.";
    }

}
