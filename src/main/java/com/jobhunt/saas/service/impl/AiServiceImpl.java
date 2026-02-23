package com.jobhunt.saas.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobhunt.saas.dto.TenantPlanDto;
import com.jobhunt.saas.entity.TenantPlan;
import com.jobhunt.saas.entity.UserSubscription;
import com.jobhunt.saas.repository.TenantPlanRepo;
import com.jobhunt.saas.repository.UserSubscriptionRepo;
import com.jobhunt.saas.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

        private final ChatClient chatClient;
        private final UserSubscriptionRepo userSubscriptionRepo;
        private final TenantPlanRepo tenantPlanRepo;
        private final ObjectMapper objectMapper;

        public AiServiceImpl(ChatClient.Builder chatClientBuilder,
                        UserSubscriptionRepo userSubscriptionRepo,
                        TenantPlanRepo tenantPlanRepo,
                        ObjectMapper objectMapper) {
                this.chatClient = chatClientBuilder.build();
                this.userSubscriptionRepo = userSubscriptionRepo;
                this.tenantPlanRepo = tenantPlanRepo;
                this.objectMapper = objectMapper;
        }

        @Override
        public String generateSubscriptionAnalytics(Long tenantId) {
                log.info("Generating AI Analytics for Tenant: {}", tenantId);

                List<UserSubscription> subscriptions = userSubscriptionRepo.findByUser_TenantId(tenantId);

                if (subscriptions.isEmpty()) {
                        return "You do not have any active subscriptions yet to analyze. Try creating your first pricing plan and onboarding users!";
                }

                long activeCount = subscriptions.stream().filter(s -> "ACTIVE".equals(s.getStatus().name())).count();
                long cancelledCount = subscriptions.stream().filter(s -> "CANCELLED".equals(s.getStatus().name()))
                                .count();
                double totalRevenue = subscriptions.stream().mapToDouble(s -> s.getAmount().doubleValue()).sum();

                String prompt = String.format(
                                """
                                                You are an expert SaaS financial analyst. Analyze the following subscription metrics for a business.
                                                Provide a summary of their performance, an insight into their churn or growth, and one actionable piece of advice to improve revenue.
                                                Be concise, encouraging, and format your output in readable Markdown. Keep it under 150 words.

                                                Data:
                                                Total Subscriptions: %d
                                                Active Subscriptions: %d
                                                Cancelled Subscriptions: %d
                                                Total Revenue: $%.2f
                                                """,
                                subscriptions.size(), activeCount, cancelledCount, totalRevenue);

                return chatClient.prompt()
                                .user(prompt)
                                .call()
                                .content();
        }

        @Override
        public List<TenantPlanDto> generatePricingPlans(String businessDescription) {
                log.info("Generating AI Pricing Plans for business: {}", businessDescription);

                String prompt = String.format("""
                                You are a SaaS monetization expert. The user has described their business as: "%s".
                                Generate 3 subscription pricing plans (e.g. Basic, Pro, Enterprise) for them.
                                Respond STRICTLY with a JSON array where each object has the following keys:
                                - planName (String, e.g. "Basic")
                                - description (String, short description of the plan)
                                - price (Number, e.g. 9.99)
                                - billingCycle (String, MUST be exactly "MONTHLY" or "YEARLY")

                                Do not include any Markdown formatting like ```json, just output the raw JSON array.
                                """, businessDescription);

                String jsonResponse = chatClient.prompt()
                                .user(prompt)
                                .call()
                                .content();

                try {
                        // Remove potential markdown blocks if the AI model disobeys the prompt
                        if (jsonResponse.startsWith("```json")) {
                                jsonResponse = jsonResponse.substring(7, jsonResponse.length() - 3);
                        } else if (jsonResponse.startsWith("```")) {
                                jsonResponse = jsonResponse.substring(3, jsonResponse.length() - 3);
                        }

                        return objectMapper.readValue(jsonResponse, new TypeReference<List<TenantPlanDto>>() {
                        });
                } catch (JsonProcessingException e) {
                        log.error("Failed to parse AI generated pricing plans: {}", jsonResponse, e);
                        throw new RuntimeException("AI failed to generate valid pricing plans. Please try again.");
                }
        }

        @Override
        public String predictChurnRisk(Long userId, Long tenantId) {
                log.info("Predicting Churn Risk for User: {}, Tenant: {}", userId, tenantId);

                List<UserSubscription> userSubscriptions = userSubscriptionRepo.findByUserId(userId);

                // Filter out subscriptions that don't belong to the tenant to ensure data
                // isolation
                List<UserSubscription> currentTenantSubs = userSubscriptions.stream()
                                .filter(sub -> sub.getUser().getTenant().getId().equals(tenantId))
                                .toList();

                if (currentTenantSubs.isEmpty()) {
                        return "No subscription data available for this user.";
                }

                UserSubscription latestSub = currentTenantSubs.get(currentTenantSubs.size() - 1);
                String subHistory = currentTenantSubs.stream()
                                .map(sub -> String.format("- Plan: %s, Status: %s, Started: %s, Amount: $%.2f",
                                                sub.getSubscriptionName(), sub.getStatus().name(),
                                                sub.getStartDate().toString(),
                                                sub.getAmount().doubleValue()))
                                .collect(Collectors.joining("\n"));

                String prompt = String.format(
                                """
                                                You are an AI Churn Prediction model for a SaaS platform. Analyze the following user's subscription history and calculate their churn risk.

                                                User Subscription History:
                                                %s

                                                Current Next Billing Date: %s
                                                Notes on Account: %s

                                                Based on this pattern, is the user at a Low, Medium, or High risk of cancelling?
                                                Provide the Risk Level in bold, followed by a 1-sentence justification.
                                                """,
                                subHistory, latestSub.getNextBillingDate(),
                                latestSub.getNotes() != null ? latestSub.getNotes() : "None");

                return chatClient.prompt()
                                .user(prompt)
                                .call()
                                .content();
        }
}
