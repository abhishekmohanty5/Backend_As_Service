package com.jobhunt.saas.service;

import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.UserSubscription;
import com.jobhunt.saas.entity.Users;
import com.jobhunt.saas.repository.UserSubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSubscriptionRenewalRemender {

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
    private String buildEmailBody(Users user, UserSubscription sub) {
        return "Hi " + user.getUsername() + ",\n\n" +
                "Your subscription for " + sub.getSubscriptionName() +
                " will renew on " + sub.getNextBillingDate() + ".\n\n" +
                "Amount: ₹" + sub.getAmount() + "\n\n" +
                "Thanks,\nSaaS Team";
    }

}
