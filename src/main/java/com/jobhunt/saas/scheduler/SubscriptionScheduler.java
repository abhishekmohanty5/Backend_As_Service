package com.jobhunt.saas.scheduler;

import com.jobhunt.saas.service.ApplicationSubscriptionCleanup;
import com.jobhunt.saas.service.UserService;
<<<<<<< HEAD
import com.jobhunt.saas.service.UserSubscriptionReminder;
=======
import com.jobhunt.saas.service.UserSubscriptionRenewalReminder;
>>>>>>> 0548ce46dba79041dabbb5c9a85f5e31e2afd07b
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionScheduler {

<<<<<<< HEAD
  private final ApplicationSubscriptionCleanup subscriptionCleanupService;
  private final UserSubscriptionReminder userSubscriptionReminder;
=======
    private final ApplicationSubscriptionCleanup subscriptionCleanupService;
    private final UserSubscriptionRenewalReminder renewalReminder;
>>>>>>> 0548ce46dba79041dabbb5c9a85f5e31e2afd07b

  // SaaS plan expiration
  @Scheduled(cron = "0 0 2 * * ?") // 2 AM
  @net.javacrumbs.shedlock.spring.annotation.SchedulerLock(name = "expireSaasSubscriptions", lockAtLeastFor = "5m", lockAtMostFor = "10m")
  public void expireSaasSubscriptions() {
    subscriptionCleanupService.expireSubscriptions();
  }

<<<<<<< HEAD
  // User subscription reminders
  @Scheduled(cron = "0 0 9 * * ?") // 9 AM
  @net.javacrumbs.shedlock.spring.annotation.SchedulerLock(name = "sendRenewalReminders", lockAtLeastFor = "5m", lockAtMostFor = "10m")
  public void sendRenewalReminders() {
    userSubscriptionReminder.sendRenewalNotification();
  }
=======
    // User subscription reminders
    @Scheduled(cron = "0 0 9 * * ?") // 9 AM
    public void sendRenewalReminders() {
        renewalReminder.sendRenewalNotification();
    }
>>>>>>> 0548ce46dba79041dabbb5c9a85f5e31e2afd07b

}
