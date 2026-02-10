package com.jobhunt.saas.service;

import com.jobhunt.saas.entity.Subscription;
import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.repository.SubscriptionRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationSubscriptionCleanup {

    @Autowired
    SubscriptionRepo subscriptionRepo;

    @Transactional
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
