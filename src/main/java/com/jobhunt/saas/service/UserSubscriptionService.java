package com.jobhunt.saas.service;

import com.jobhunt.saas.auth.AuthContext;
import com.jobhunt.saas.dto.SubscriptionStatsDto;
import com.jobhunt.saas.dto.UserSubscriptionDto;

import com.jobhunt.saas.entity.*;
import com.jobhunt.saas.repository.SubscriptionCategoryRepo;
import com.jobhunt.saas.repository.UserRepo;
import com.jobhunt.saas.repository.UserSubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSubscriptionService {

    private final UserSubscriptionRepo userSubscriptionRepo;
    private final SubscriptionCategoryRepo subscriptionCategoryRepo;
    private final UserRepo userRepo;
    private final AuthContext authContext;


    //Service -1

    @Transactional
    public void createSubscription(UserSubscriptionDto requestDto){

        Long userId = authContext.getCurrentUserId();

        Users user = userRepo.findById(userId).orElseThrow(
                ()-> new RuntimeException("User not found")
        );

        Long categoryId = requestDto.getCategory().getId();

        subscriptionCategoryRepo.findById(categoryId).orElseThrow(
                ()-> new RuntimeException("Invalid subscription category")
        );

        LocalDate startDate = requestDto.getStartDate();
        LocalDate nextBillingDate = getNextBillingDate(startDate,requestDto.getBillingCycle());

        UserSubscription userSubscription = UserSubscription.builder()
                .user(user)
                .subscriptionName(requestDto.getSubscriptionName())
                .notes(requestDto.getNotes())
                .amount(requestDto.getAmount())
                .status(SubscriptionStatus.ACTIVE)
                .subscriptionCategory(requestDto.getCategory())
                .startDate(startDate)
                .nextBillingDate(nextBillingDate)
                .billingCycle(requestDto.getBillingCycle())
                .build();

        userSubscriptionRepo.save(userSubscription);
    }

    //get users all Subscriptions
    //Service -2

    public List<UserSubscription> getUserSubscriptions(){
        Long userId = authContext.getCurrentUserId();

        Users user = userRepo.findById(userId).orElseThrow(
                ()-> new RuntimeException("User not found")
        );

        return userSubscriptionRepo.findByUserId(user.getId());
    }

    //get Only Active Subscriptions

    public List<UserSubscription> getActiveSubscription(){

        Long userId = authContext.getCurrentUserId();

        Users user = userRepo.findById(userId).orElseThrow(
                ()-> new RuntimeException("User not found")
        );

        return userSubscriptionRepo.findByUserIdAndStatus(userId,SubscriptionStatus.ACTIVE);
    }

    //get Subscriptions By Category

    public List<UserSubscription> getSubscriptionByCategory(Long categoryId){

        Long  userId = authContext.getCurrentUserId();

        subscriptionCategoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Invalid subscription category"));

        return userSubscriptionRepo.findByUserIdAndSubscriptionCategoryId(userId,categoryId);
    }

    //Update Subscription
    @Transactional
    public void updateSubscription(Long id, UserSubscriptionDto subscriptionRequest){

        Long userId = authContext.getCurrentUserId();


        UserSubscription subscription = userSubscriptionRepo.findById(id).orElseThrow(
                ()-> new RuntimeException("Invalid subscription")
        );

        if(! subscription.getUser().getId().equals(userId)){
            throw new RuntimeException("Unauthorized: This subscription does not belong to you");
        }

        subscription.setSubscriptionName(subscriptionRequest.getSubscriptionName());
        subscription.setNotes(subscriptionRequest.getNotes());
        subscription.setAmount(subscriptionRequest.getAmount());
        subscription.setBillingCycle(subscriptionRequest.getBillingCycle());
        subscription.setNextBillingDate(getNextBillingDate(subscriptionRequest.getStartDate(), subscriptionRequest.getBillingCycle()));
        subscription.setStartDate(subscriptionRequest.getStartDate());

    }

    //Cancel Subscription
    @Transactional
    public void cancelSubscription(Long id){

        Long userId = authContext.getCurrentUserId();

        UserSubscription subscription = userSubscriptionRepo.findById(id).orElseThrow(
                ()-> new RuntimeException("Invalid subscription")
        );

        if(! subscription.getUser().getId().equals(userId)){
            throw new RuntimeException("Unauthorized: This subscription does not belong to you");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);

    }

    //delete Subscription

    @Transactional
    public void deleteSubscription(Long id){

        Long userId = authContext.getCurrentUserId();

        UserSubscription subscription = userSubscriptionRepo.findById(id).orElseThrow(
                ()-> new RuntimeException("Invalid subscription")
        );

        if(! subscription.getUser().getId().equals(userId)){
            throw new RuntimeException("Unauthorized: This subscription does not belong to you");
        }

        userSubscriptionRepo.delete(subscription);
    }

    //Renewal in next N days
    @Transactional
    public List<UserSubscription> getUpcomingRenewals(int days){

        Long userId = authContext.getCurrentUserId();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate= startDate.plusDays(days);

     return userSubscriptionRepo.
             findByUserIdAndNextBillingDateBetween(userId,startDate,endDate);
    }

    public SubscriptionStatsDto getSubscriptionStatistics() {
        Long userId = authContext.getCurrentUserId();

        List<UserSubscription> activeSubscriptions =
                userSubscriptionRepo.findByUserIdAndStatus(
                        userId, SubscriptionStatus.ACTIVE);

        BigDecimal monthlyTotal = activeSubscriptions.stream()
                .filter(subscription -> subscription.getBillingCycle() == BillingCycle.MONTHLY)
                .map(UserSubscription::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal yearlyAsMonthly = activeSubscriptions.stream()
                .filter(subscription -> subscription.getBillingCycle() == BillingCycle.YEARLY)
                .map(subscription -> subscription.getAmount().divide(
                        BigDecimal.valueOf(12),
                        2,
                        RoundingMode.HALF_UP
                ))

                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SubscriptionStatsDto(
                monthlyTotal.add(yearlyAsMonthly),
                activeSubscriptions.size()
        );
    }


    public List<String> getSubscriptionInsights() {

        Long userId = authContext.getCurrentUserId();

        List<UserSubscription> activeSubscriptions =
                userSubscriptionRepo.findByUserIdAndStatus(
                        userId, SubscriptionStatus.ACTIVE);

        List<String> insights = new ArrayList<>();

        long entertainmentCount = activeSubscriptions.stream()
                .filter(subscription -> subscription.getSubscriptionCategory().getName().equalsIgnoreCase("Entertainment"))
                .count();

        if (entertainmentCount > 2) {
            insights.add("You have multiple entertainment subscriptions. Consider cancelling one.");
        }

        activeSubscriptions.stream()
                .filter(subscription -> subscription.getBillingCycle() == BillingCycle.MONTHLY && subscription.getAmount().compareTo(BigDecimal.valueOf(500)) > 0)
                .forEach(subscription -> insights.add(
                        "Switch " + subscription.getSubscriptionName() + " to yearly plan to save money."
                ));

        return insights;
    }

    //helper -1

    private LocalDate getNextBillingDate(LocalDate startingDate, BillingCycle billingCycle) {
        switch (billingCycle) {
            case WEEKLY:
                return startingDate.plusWeeks(1);

            case MONTHLY:
                return startingDate.plusMonths(1);

            case YEARLY:
                return startingDate.plusYears(1);
            default:
                throw new IllegalArgumentException("Invalid billing cycle");
        }
    }
}
