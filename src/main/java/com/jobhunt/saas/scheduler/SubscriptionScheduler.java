package com.jobhunt.saas.scheduler;

import com.jobhunt.saas.service.ApplicationSubscriptionCleanup;

import com.jobhunt.saas.service.UserSubscriptionReminder;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionScheduler {

  private final ApplicationSubscriptionCleanup subscriptionCleanupService;
  private final UserSubscriptionReminder userSubscriptionReminder;

  // SaaS plan expiration
  @Scheduled(cron = "0 0 2 * * ?") // 2 AM
  @net.javacrumbs.shedlock.spring.annotation.SchedulerLock(name = "expireSaasSubscriptions", lockAtLeastFor = "5m", lockAtMostFor = "10m")
  public void expireSaasSubscriptions() {
    subscriptionCleanupService.expireSubscriptions();
  }

  // User subscription reminders
  @Scheduled(cron = "0 0 9 * * ?") // 9 AM
  @net.javacrumbs.shedlock.spring.annotation.SchedulerLock(name = "sendRenewalReminders", lockAtLeastFor = "5m", lockAtMostFor = "10m")
  public void sendRenewalReminders() {
    userSubscriptionReminder.sendRenewalNotification();
  }

}
