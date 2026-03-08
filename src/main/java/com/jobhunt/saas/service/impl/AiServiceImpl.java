package com.jobhunt.saas.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobhunt.saas.dto.TenantPlanDto;
import com.jobhunt.saas.entity.UserSubscription;
import com.jobhunt.saas.repository.TenantPlanRepo;
import com.jobhunt.saas.repository.UserSubscriptionRepo;
import com.jobhunt.saas.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

        private final UserSubscriptionRepo userSubscriptionRepo;
        private final TenantPlanRepo tenantPlanRepo;
        private final ObjectMapper objectMapper;
        private final RestClient restClient;

        @Value("${gemini.api.key:}")
        private String geminiApiKey;

        private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash-latest:generateContent";

        public AiServiceImpl(UserSubscriptionRepo userSubscriptionRepo,
                        TenantPlanRepo tenantPlanRepo,
                        ObjectMapper objectMapper,
                        RestClient.Builder restClientBuilder) {
                this.userSubscriptionRepo = userSubscriptionRepo;
                this.tenantPlanRepo = tenantPlanRepo;
                this.objectMapper = objectMapper;
                this.restClient = restClientBuilder.build();
        }

        /**
         * Calls the Gemini REST API with a given prompt and returns the text response.
         */
        private String callGemini(String prompt) {
                String url = GEMINI_URL + "?key=" + geminiApiKey;

                Map<String, Object> requestBody = Map.of(
                                "contents", List.of(Map.of(
                                                "parts", List.of(Map.of("text", prompt)))));

                try {
                        String responseJson = restClient.post()
                                        .uri(url)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(requestBody)
                                        .retrieve()
                                        .body(String.class);

                        JsonNode root = objectMapper.readTree(responseJson);
                        return root.path("candidates").get(0)
                                        .path("content").path("parts").get(0)
                                        .path("text").asText();

                } catch (Exception e) {
                        log.error("Gemini API call failed: {}", e.getMessage(), e);
                        throw new RuntimeException("Failed to generate content: " + e.getMessage());
                }
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

                return callGemini(prompt);
        }

        @Override
        public List<TenantPlanDto> generatePricingPlans(String businessDescription) {
                log.info("Generating AI Pricing Plans for: {}", businessDescription);

                String prompt = String.format(
                                """
                                                You are an expert SaaS Pricing Strategist. Help a business design their subscription pricing tiers.

                                                Business Description: %s

                                                Generate exactly 3 subscription pricing plans (e.g. Basic, Pro, Enterprise).
                                                Respond STRICTLY with a raw JSON array. No markdown, no extra text, just the JSON array.
                                                Each object must have these exact keys:
                                                - name (String)
                                                - description (String, one short sentence)
                                                - price (Number, e.g. 999.00)
                                                - billingCycle (String, must be exactly "MONTHLY" or "YEARLY")
                                                - features (String, 3-5 key features separated by commas)
                                                """,
                                businessDescription);

                String rawResponse = callGemini(prompt);

                try {
                        // Robustly extract JSON array even if model adds extra text
                        int startIndex = rawResponse.indexOf("[");
                        int endIndex = rawResponse.lastIndexOf("]");
                        if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
                                throw new RuntimeException("No valid JSON array found in AI response.");
                        }
                        String jsonResponse = rawResponse.substring(startIndex, endIndex + 1);
                        return objectMapper.readValue(jsonResponse, new TypeReference<List<TenantPlanDto>>() {
                        });
                } catch (JsonProcessingException e) {
                        log.error("Failed to parse AI response: {}", rawResponse, e);
                        throw new RuntimeException("AI failed to generate valid pricing plans. Please try again.");
                }
        }

        @Override
        public String predictChurnRisk(Long userId, Long tenantId) {
                log.info("Predicting Churn Risk for User: {}, Tenant: {}", userId, tenantId);

                List<UserSubscription> userSubscriptions = userSubscriptionRepo.findByUserId(userId);

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
                                                You are an AI Churn Prediction model for a SaaS platform. Analyze the following user subscription history.

                                                User Subscription History:
                                                %s

                                                Current Next Billing Date: %s
                                                Notes on Account: %s

                                                Based on this pattern, is the user at a Low, Medium, or High risk of cancelling?
                                                Provide the Risk Level in bold, followed by a 1-sentence justification.
                                                """,
                                subHistory, latestSub.getNextBillingDate(),
                                latestSub.getNotes() != null ? latestSub.getNotes() : "None");

                return callGemini(prompt);
        }
}
