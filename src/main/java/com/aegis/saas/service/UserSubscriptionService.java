package com.aegis.saas.service;

import com.aegis.saas.auth.AuthContext;
import com.aegis.saas.dto.SubscriptionStatsDto;
import com.aegis.saas.dto.UserSubscriptionDto;
import com.aegis.saas.entity.*;
import com.aegis.saas.exception.ResourceNotFoundException;
import com.aegis.saas.exception.UnauthorizedException;
import com.aegis.saas.repository.TenantPlanRepo;
import com.aegis.saas.repository.UserRepo;
import com.aegis.saas.repository.UserSubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Value("${saas.insights.yearly-switch-threshold:500}")
    private BigDecimal yearlySwitchThreshold;

    @Value("${saas.insights.entertainment-category:Entertainment}")
    private String entertainmentCategory;

    @Value("${saas.insights.entertainment-max-count:2}")
    private int entertainmentMaxCount;

    @Transactional
    public void createSubscription(UserSubscriptionDto requestDto) {
        Long userId = authContext.getCurrentUserId();
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        TenantPlan tenantPlan = tenantPlanRepo.findById(requestDto.getTenantPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("TenantPlan", requestDto.getTenantPlanId()));

        LocalDate startDate = requestDto.getStartDate();
        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .subscriptionName(requestDto.getSubscriptionName())
                .notes(requestDto.getNotes())
                .amount(requestDto.getAmount())
                .status(SubscriptionStatus.ACTIVE)
                .tenantPlan(tenantPlan)
                .startDate(startDate)
                .nextBillingDate(getNextBillingDate(startDate, requestDto.getBillingCycle()))
                .billingCycle(requestDto.getBillingCycle())
                .build();
        userSubscriptionRepo.save(subscription);
    }

    public List<UserSubscription> getUserSubscriptions() {
        return userSubscriptionRepo.findByUserId(authContext.getCurrentUserId());
    }

    public Page<UserSubscription> getUserSubscriptionsPaginated(Pageable pageable) {
        return userSubscriptionRepo.findByUserId(authContext.getCurrentUserId(), pageable);
    }

    public List<UserSubscription> getActiveSubscription() {
        return userSubscriptionRepo.findByUserIdAndStatus(authContext.getCurrentUserId(), SubscriptionStatus.ACTIVE);
    }

    public List<UserSubscription> getSubscriptionByTenantPlan(Long tenantPlanId) {
        tenantPlanRepo.findById(tenantPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("TenantPlan", tenantPlanId));
        return userSubscriptionRepo.findByUserIdAndTenantPlanId(authContext.getCurrentUserId(), tenantPlanId);
    }

    @Transactional
    public void updateSubscription(Long id, UserSubscriptionDto dto) {
        Long userId = authContext.getCurrentUserId();
        UserSubscription subscription = userSubscriptionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserSubscription", id));
        if (!subscription.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This subscription does not belong to you.");
        }
        subscription.setSubscriptionName(dto.getSubscriptionName());
        subscription.setNotes(dto.getNotes());
        subscription.setAmount(dto.getAmount());
        subscription.setBillingCycle(dto.getBillingCycle());
        subscription.setStartDate(dto.getStartDate());
        subscription.setNextBillingDate(getNextBillingDate(dto.getStartDate(), dto.getBillingCycle()));
        userSubscriptionRepo.save(subscription);
    }

    @Transactional
    public void cancelSubscription(Long id) {
        Long userId = authContext.getCurrentUserId();
        UserSubscription subscription = userSubscriptionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserSubscription", id));
        if (!subscription.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This subscription does not belong to you.");
        }
        subscription.setStatus(SubscriptionStatus.CANCELLED);
    }

    @Transactional
    public void deleteSubscription(Long id) {
        Long userId = authContext.getCurrentUserId();
        UserSubscription subscription = userSubscriptionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserSubscription", id));
        if (!subscription.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This subscription does not belong to you.");
        }
        userSubscriptionRepo.delete(subscription);
    }

    public List<UserSubscription> getUpcomingRenewals(int days) {
        Long userId = authContext.getCurrentUserId();
        return userSubscriptionRepo.findByUserIdAndNextBillingDateBetween(
                userId, LocalDate.now(), LocalDate.now().plusDays(days));
    }

    public SubscriptionStatsDto getSubscriptionStatistics() {
        Long userId = authContext.getCurrentUserId();
        List<UserSubscription> activeSubs = userSubscriptionRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);

        BigDecimal monthlyTotal = activeSubs.stream()
                .filter(s -> s.getBillingCycle() == BillingCycle.MONTHLY)
                .map(UserSubscription::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal yearlyAsMonthly = activeSubs.stream()
                .filter(s -> s.getBillingCycle() == BillingCycle.YEARLY)
                .map(s -> s.getAmount().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SubscriptionStatsDto(monthlyTotal.add(yearlyAsMonthly), activeSubs.size());
    }

    public List<String> getSubscriptionInsights() {
        Long userId = authContext.getCurrentUserId();
        List<UserSubscription> subs = userSubscriptionRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        List<String> insights = new ArrayList<>();

        long catCount = subs.stream()
                .filter(s -> s.getTenantPlan().getName().equalsIgnoreCase(entertainmentCategory))
                .count();
        if (catCount > entertainmentMaxCount) {
            insights.add("You have " + catCount + " " + entertainmentCategory
                    + " subscriptions. Consider consolidating to save money.");
        }

        subs.stream()
                .filter(s -> s.getBillingCycle() == BillingCycle.MONTHLY
                        && s.getAmount().compareTo(yearlySwitchThreshold) > 0)
                .forEach(s -> insights.add("Switch \"" + s.getSubscriptionName()
                        + "\" to a yearly plan to potentially save up to 20%."));

        return insights;
    }

    private LocalDate getNextBillingDate(LocalDate start, BillingCycle cycle) {
        return switch (cycle) {
            case WEEKLY -> start.plusWeeks(1);
            case MONTHLY -> start.plusMonths(1);
            case YEARLY -> start.plusYears(1);
            default -> throw new IllegalArgumentException("Unsupported billing cycle: " + cycle);
        };
    }
}
