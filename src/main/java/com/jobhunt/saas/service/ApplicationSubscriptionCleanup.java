package com.jobhunt.saas.service;

import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.UserSubscription;
import com.jobhunt.saas.repository.UserSubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class ApplicationSubscriptionCleanup {

    @Autowired
    UserSubscriptionRepo userSubscriptionRepo;

    @Transactional
    public void expireSubscriptions() {
        LocalDate now = LocalDate.now();
        List<UserSubscription> expiredSubscriptions = userSubscriptionRepo
                .findAllByStatusAndNextBillingDateBefore(SubscriptionStatus.ACTIVE, now);

        if (!expiredSubscriptions.isEmpty()) {
            expiredSubscriptions.forEach(sub -> sub.setStatus(SubscriptionStatus.EXPIRED));

            userSubscriptionRepo.saveAll(expiredSubscriptions);
            log.info("Expired {} user subscriptions.", expiredSubscriptions.size());
        }

    }

}
