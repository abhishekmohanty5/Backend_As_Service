package com.jobhunt.saas.service;

import com.jobhunt.saas.auth.AuthContext;
import com.jobhunt.saas.dto.SubscriptionStatsDto;
import com.jobhunt.saas.dto.UserSubscriptionDto;

import com.jobhunt.saas.entity.*;
import com.jobhunt.saas.repository.TenantPlanRepo;
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
    private final TenantPlanRepo tenantPlanRepo;
    private final UserRepo userRepo;
    private final AuthContext authContext;

    // Service -1

    @Transactional
    public void createSubscription(UserSubscriptionDto requestDto) {

        Long userId = authContext.getCurrentUserId();

        Users user = userRepo.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found"));

        Long tenantPlanId = requestDto.getTenantPlanId();

        TenantPlan tenantPlan = tenantPlanRepo.findById(tenantPlanId).orElseThrow(
                () -> new RuntimeException("Invalid tenant plan"));

        LocalDate startDate = requestDto.getStartDate();
        LocalDate nextBillingDate = getNextBillingDate(startDate, requestDto.getBillingCycle());

        UserSubscription userSubscription = UserSubscription.builder()
                .user(user)
                .subscriptionName(requestDto.getSubscriptionName())
                .notes(requestDto.getNotes())
                .amount(requestDto.getAmount())
                .status(SubscriptionStatus.ACTIVE)
                .tenantPlan(tenantPlan)
                .startDate(startDate)
                .nextBillingDate(nextBillingDate)
                .billingCycle(requestDto.getBillingCycle())
                .build();

        userSubscriptionRepo.save(userSubscription);
    }

    // get users all Subscriptions
    // Service -2

    public List<UserSubscription> getUserSubscriptions() {
        Long userId = authContext.getCurrentUserId();

        Users user = userRepo.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found"));

        return userSubscriptionRepo.findByUserId(user.getId());
    }

    // get Only Active Subscriptions

    public List<UserSubscription> getActiveSubscription() {

        Long userId = authContext.getCurrentUserId();

        Users user = userRepo.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found"));

        return userSubscriptionRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
    }

    // get Subscriptions By TenantPlan

    public List<UserSubscription> getSubscriptionByTenantPlan(Long tenantPlanId) {

        Long userId = authContext.getCurrentUserId();

        tenantPlanRepo.findById(tenantPlanId)
                .orElseThrow(() -> new RuntimeException("Invalid tenant plan"));

        return userSubscriptionRepo.findByUserIdAndTenantPlanId(userId, tenantPlanId);
    }

    // Update Subscription
    @Transactional
    public void updateSubscription(Long id, UserSubscriptionDto dto) {

        Long userId = authContext.getCurrentUserId();

        UserSubscription subscription = userSubscriptionRepo.findById(id).orElseThrow(
                () -> new RuntimeException("Invalid subscription"));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: This subscription does not belong to you");
        }

        subscription.setSubscriptionName(dto.getSubscriptionName());
        subscription.setNotes(dto.getNotes());
        subscription.setAmount(dto.getAmount());
        subscription.setBillingCycle(dto.getBillingCycle());
        subscription.setNextBillingDate(getNextBillingDate(dto.getStartDate(), dto.getBillingCycle()));
        subscription.setStartDate(dto.getStartDate());
        userSubscriptionRepo.save(subscription);

    }

    // Cancel Subscription
    @Transactional
    public void cancelSubscription(Long id) {

        Long userId = authContext.getCurrentUserId();

        UserSubscription subscription = userSubscriptionRepo.findById(id).orElseThrow(
                () -> new RuntimeException("Invalid subscription"));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: This subscription does not belong to you");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);

    }

    // delete Subscription

    @Transactional
    public void deleteSubscription(Long id) {

        Long userId = authContext.getCurrentUserId();

        UserSubscription subscription = userSubscriptionRepo.findById(id).orElseThrow(
                () -> new RuntimeException("Invalid subscription"));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: This subscription does not belong to you");
        }

        userSubscriptionRepo.delete(subscription);
    }

    // Renewal in next N days
    @Transactional
    public List<UserSubscription> getUpcomingRenewals(int days) {

        Long userId = authContext.getCurrentUserId();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        return userSubscriptionRepo.findByUserIdAndNextBillingDateBetween(userId, startDate, endDate);
    }

    public SubscriptionStatsDto getSubscriptionStatistics() {
        Long userId = authContext.getCurrentUserId();

        List<UserSubscription> activeSubs = userSubscriptionRepo.findByUserIdAndStatus(
                userId, SubscriptionStatus.ACTIVE);

        BigDecimal monthlyTotal = activeSubs.stream()
                .filter(subscription -> subscription.getBillingCycle() == BillingCycle.MONTHLY)
                .map(UserSubscription::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal yearlyAsMonthly = activeSubs.stream()
                .filter(subscription -> subscription.getBillingCycle() == BillingCycle.YEARLY)
                .map(subscription -> subscription.getAmount().divide(
                        BigDecimal.valueOf(12),
                        2,
                        RoundingMode.HALF_UP))

                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SubscriptionStatsDto(
                monthlyTotal.add(yearlyAsMonthly),
                activeSubs.size());
    }

    public List<String> getSubscriptionInsights() {

        Long userId = authContext.getCurrentUserId();

        List<UserSubscription> subs = userSubscriptionRepo.findByUserIdAndStatus(
                userId, SubscriptionStatus.ACTIVE);

        List<String> insights = new ArrayList<>();

        long entertainmentCount = subs.stream()
                .filter(s -> s.getTenantPlan().getName().equalsIgnoreCase("Entertainment"))
                .count();

        if (entertainmentCount > 2) {
            insights.add("You have multiple entertainment subscriptions. Consider cancelling one.");
        }

        subs.stream()
                .filter(s -> s.getBillingCycle() == BillingCycle.MONTHLY
                        && s.getAmount().compareTo(BigDecimal.valueOf(500)) > 0)
                .forEach(s -> insights.add(
                        "Switch " + s.getSubscriptionName() + " to yearly plan to save money."));

        return insights;
    }

    // helper -1

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
