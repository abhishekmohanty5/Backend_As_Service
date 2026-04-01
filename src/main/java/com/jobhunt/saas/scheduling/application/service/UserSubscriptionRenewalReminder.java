package com.jobhunt.saas.scheduling.application.service;

import com.jobhunt.saas.shared.domain.model.SubscriptionStatus;
import com.jobhunt.saas.subscription.domain.model.UserSubscription;
import com.jobhunt.saas.user.domain.model.Users;
import com.jobhunt.saas.subscription.adapter.out.persistence.UserSubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import com.jobhunt.saas.notification.application.service.EmailService;

@Service
@RequiredArgsConstructor
public class UserSubscriptionRenewalReminder {

    private final EmailService emailService;
    private final UserSubscriptionRepo userSubscriptionRepo;

     @Transactional
     public void sendRenewalNotification(){

         LocalDate startDate= LocalDate.now();
         LocalDate endDate= startDate.plusDays(7);

         List<UserSubscription> userSubscriptions =
                   userSubscriptionRepo.findByNextBillingDateBetweenAndStatus(
                          startDate,
                           endDate,
                    SubscriptionStatus.ACTIVE);


         for(UserSubscription userSubscription:userSubscriptions){
             Users user =  userSubscription.getUser();
             String email = user.getEmail();

             emailService.sendEmail(email,
                     "Your next renewal is coming up",
                     buildEmailBody(user,userSubscription));
         }


     }
    private String buildEmailBody(Users user, UserSubscription subscription) {
        return "Hi " + user.getUsername() + ",\n\n" +
                "Your subscription for " + subscription.getSubscriptionName() +
                " will renew on " + subscription.getNextBillingDate() + ".\n\n" +
                "Amount: ₹" + subscription.getAmount() + "\n\n" +
                "Thanks,\nSaaS Team";
    }

}
