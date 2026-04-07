package com.aegis.saas.service;

import com.aegis.saas.entity.SubscriptionStatus;
import com.aegis.saas.entity.TenantSubscription;
import com.aegis.saas.entity.Users;
import com.aegis.saas.repository.TenantSubscriptionRepo;
import com.aegis.saas.repository.UserRepo;
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
    @Scheduled(cron = "0 0 8 * * ?")
    @net.javacrumbs.shedlock.spring.annotation.SchedulerLock(name = "sendEngineRenewalNotifications", lockAtLeastFor = "5m", lockAtMostFor = "10m")
    @Transactional
    public void sendEngineRenewalNotifications() {
        log.info("Starting Daily Engine Plan Reminder Job...");

        List<TenantSubscription> activeSubs = tenantSubscriptionRepo
                .findAllByStatus(SubscriptionStatus.ACTIVE);

        for (TenantSubscription sub : activeSubs) {
            LocalDateTime expireDate = sub.getExpireDate();
            LocalDateTime now = LocalDateTime.now();

            if (expireDate == null)
                continue;

            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(now.toLocalDate(), expireDate.toLocalDate());

            Integer status = sub.getReminderStatus();
            if (status == null)
                status = 0;

            if (daysLeft <= 7 && daysLeft > 1 && status < 1) {
                sendEmailToTenantAdmin(sub, (int) daysLeft);
                sub.setReminderStatus(1);
                tenantSubscriptionRepo.save(sub);
            } else if (daysLeft <= 1 && daysLeft >= 0 && status < 2) {
                sendEmailToTenantAdmin(sub, (int) daysLeft);
                sub.setReminderStatus(2);
                tenantSubscriptionRepo.save(sub);
            }
        }
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

    @org.springframework.beans.factory.annotation.Value("${app.base-url}")
    private String baseUrl;

    private String buildEmailBody(String adminName, String tenantName, String planName, LocalDateTime expireDate,
            int daysLeft) {
        return "Hi " + adminName + ",<br><br>" +
                "This is a professional reminder that your <strong>Aegis Infra Engine Subscription</strong> for the infrastructure <strong>'" + tenantName
                + "'</strong> is nearing its expiration.<br><br>" +
                "<div style=\"background-color: #f8fafc; padding: 24px; border-radius: 12px; border: 1px solid #e2e8f0; border-left: 4px solid #2563eb;\">" +
                "<strong>Current Plan:</strong> " + planName + "<br>" +
                "<strong>Expiration date:</strong> <span class=\"highlight\">" + expireDate.toLocalDate() + "</span> (" + daysLeft + " days remaining)" +
                "</div><br>" +
                "To ensure uninterrupted infrastructure and API services, please log in to your dashboard to renew or upgrade your current plan.<br><br>" +
                "<a href=\"" + (baseUrl != null ? baseUrl : "https://aegisinfra.me/dashboard") + "\" style=\"display: inline-block; padding: 12px 24px; background-color: #2563eb; color: #ffffff !important; text-decoration: none; border-radius: 8px; font-weight: 600;\">Access Dashboard</a><br><br>" +
                "If you've already renewed, please ignore this message. Thank you for trusting AegisInfra.";
    }
}
