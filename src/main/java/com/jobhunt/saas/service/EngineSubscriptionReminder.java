package com.jobhunt.saas.service;

import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.TenantSubscription;
import com.jobhunt.saas.entity.Users;
import com.jobhunt.saas.repository.TenantSubscriptionRepo;
import com.jobhunt.saas.repository.UserRepo;
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
public class EngineSubscriptionReminder {

    private final EmailService emailService;
    private final TenantSubscriptionRepo tenantSubscriptionRepo;
    private final UserRepo userRepo;

    /**
     * Runs daily at 9:00 AM to send reminder emails to Tenants
     * 7 days and 1 day before their Engine plan expires.
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void sendEngineRenewalNotifications() {
        log.info("Starting Daily Engine Plan Reminder Job...");

        List<TenantSubscription> activeSubs = tenantSubscriptionRepo
                .findAllByStatus(SubscriptionStatus.ACTIVE);

        for (TenantSubscription sub : activeSubs) {
            LocalDateTime expireDate = sub.getExpireDate();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sevenDaysFromNow = now.plusDays(7);
            LocalDateTime oneDayFromNow = now.plusDays(1);

            // Check if expiring in exactly 7 days (matching the day)
            if (isSameDay(sevenDaysFromNow, expireDate)) {
                sendEmailToTenantAdmin(sub, 7);
            }
            // Check if expiring in exactly 1 day (matching the day)
            else if (isSameDay(oneDayFromNow, expireDate)) {
                sendEmailToTenantAdmin(sub, 1);
            }
        }
    }

    private boolean isSameDay(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null)
            return false;
        return date1.toLocalDate().isEqual(date2.toLocalDate());
    }

    private void sendEmailToTenantAdmin(TenantSubscription sub, int daysLeft) {
        // Find the Tenant Admin user for this tenant to send the email
        List<Users> admins = userRepo.findByTenantId(sub.getTenant().getId());
        if (admins == null || admins.isEmpty())
            return;

        // Pick the administrator or simply the first user registered for the tenant
        Users admin = admins.stream()
                .filter(u -> u.getRole() != null && u.getRole().name().equals("ROLE_TENANT_ADMIN"))
                .findFirst()
                .orElse(admins.get(0));

        String email = admin.getEmail();
        String tenantName = sub.getTenant().getName();
        String planName = sub.getPlan().getName();

        emailService.sendEmail(
                email,
                "Aegis Infra: " + tenantName + " Plan Expiring in " + daysLeft + " Days",
                buildEmailBody(admin.getUsername(), tenantName, planName, sub.getExpireDate(), daysLeft));
        log.info("Sent {} day expiration reminder to {}", daysLeft, email);
    }

    private String buildEmailBody(String adminName, String tenantName, String planName, LocalDateTime expireDate,
            int daysLeft) {
        return "Hi " + adminName + ",\n\n" +
                "This is a reminder that your Aegis Infra Engine Subscription for '" + tenantName
                + "' is expiring soon.\n\n" +
                "Current Plan: " + planName + "\n" +
                "Expiration Date: " + expireDate.toLocalDate() + " (" + daysLeft + " days left)\n\n" +
                "Please log in to your dashboard to upgrade or renew your plan to avoid any interruption in API services.\n\n"
                +
                "Thanks,\nAegis Infra Team";
    }
}
