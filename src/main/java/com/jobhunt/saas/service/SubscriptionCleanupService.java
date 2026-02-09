package com.jobhunt.saas.service;

import com.jobhunt.saas.entity.Subscription;
import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.repository.SubscriptionRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionCleanupService {

    @Autowired
    SubscriptionRepo subscriptionRepo;

    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void expireSubscriptions()
    {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiredSubscriptions =
                subscriptionRepo.findAllByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, now);

        if(!expiredSubscriptions.isEmpty()){
            expiredSubscriptions.forEach(sub ->
                    sub.setStatus(SubscriptionStatus.EXPIRED));

            subscriptionRepo.saveAll(expiredSubscriptions);
            System.out.println("Expired " + expiredSubscriptions.size() + " subscriptions.");
        }

    }

}
