package com.aegis.saas.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aegis.saas.dto.TenantPlanDto;
import com.aegis.saas.entity.BillingCycle;
import com.aegis.saas.entity.UserSubscription;
import com.aegis.saas.repository.TenantPlanRepo;
import com.aegis.saas.repository.UserSubscriptionRepo;
import com.aegis.saas.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
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

        @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com}")
        private String geminiApiBaseUrl;

        @Value("${gemini.api.version:v1beta}")
        private String geminiApiVersion;

        @Value("${gemini.api.model:gemini-2.5-flash}")
        private String geminiModel;

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
                if (geminiApiKey == null || geminiApiKey.isBlank()) {
                        throw new IllegalStateException("Gemini API key is not configured.");
                }

                String url = buildGeminiUrl();

                Map<String, Object> requestBody = Map.of(
                                "contents", List.of(Map.of(
                                                "parts", List.of(Map.of("text", prompt)))));

                try {
                        String responseJson = restClient.post()
                                        .uri(url)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .header("x-goog-api-key", geminiApiKey)
                                        .body(requestBody)
                                        .retrieve()
                                        .body(String.class);

                        JsonNode root = objectMapper.readTree(responseJson);
                        JsonNode candidates = root.path("candidates");
                        if (!candidates.isArray() || candidates.isEmpty()) {
                                throw new IllegalStateException("Gemini returned no candidates.");
                        }

                        String text = candidates.get(0)
                                        .path("content").path("parts").path(0)
                                        .path("text").asText("");

                        if (text.isBlank()) {
                                throw new IllegalStateException("Gemini returned an empty response.");
                        }

                        return text;

                } catch (Exception e) {
                        log.error("Gemini API call failed for model {} on {}: {}", geminiModel, url, e.getMessage(), e);
                        throw new RuntimeException("Failed to generate content: " + e.getMessage());
                }
        }

        private String buildGeminiUrl() {
                String sanitizedBaseUrl = geminiApiBaseUrl.endsWith("/")
                                ? geminiApiBaseUrl.substring(0, geminiApiBaseUrl.length() - 1)
                                : geminiApiBaseUrl;
                return "%s/%s/models/%s:generateContent".formatted(sanitizedBaseUrl, geminiApiVersion, geminiModel);
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

                try {
                        return callGemini(prompt);
                } catch (RuntimeException ex) {
                        log.warn("Falling back to rule-based subscription analytics for tenant {}: {}", tenantId,
                                        ex.getMessage());
                        return buildFallbackSubscriptionAnalytics(subscriptions.size(), activeCount, cancelledCount,
                                        totalRevenue);
                }
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

                try {
                        String rawResponse = callGemini(prompt);

                        // Robustly extract JSON array even if model adds extra text
                        int startIndex = rawResponse.indexOf("[");
                        int endIndex = rawResponse.lastIndexOf("]");
                        if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
                                throw new RuntimeException("No valid JSON array found in AI response.");
                        }
                        String jsonResponse = rawResponse.substring(startIndex, endIndex + 1);
                        return objectMapper.readValue(jsonResponse, new TypeReference<List<TenantPlanDto>>() {
                        });
                } catch (Exception e) {
                        log.warn("Falling back to generated default pricing plans for '{}': {}", businessDescription,
                                        e.getMessage());
                        return buildFallbackPricingPlans(businessDescription);
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

                try {
                        return callGemini(prompt);
                } catch (RuntimeException ex) {
                        log.warn("Falling back to rule-based churn analysis for user {} in tenant {}: {}", userId,
                                        tenantId, ex.getMessage());
                        return buildFallbackChurnRisk(currentTenantSubs.size(), latestSub.getStatus().name());
                }
        }

        private String buildFallbackSubscriptionAnalytics(long totalSubscriptions, long activeCount,
                        long cancelledCount, double totalRevenue) {
                if (totalSubscriptions == 0) {
                        return "No subscription activity is available yet. Start by publishing a plan and onboarding your first customers.";
                }

                double cancellationRate = totalSubscriptions == 0 ? 0
                                : (cancelledCount * 100.0) / totalSubscriptions;

                if (cancellationRate >= 35) {
                        return String.format(
                                        "Revenue is currently $%.2f across %d subscriptions, but churn looks elevated with %d cancelled accounts. Focus on plan value communication and win-back campaigns over the next billing cycle.",
                                        totalRevenue, totalSubscriptions, cancelledCount);
                }

                if (activeCount >= Math.max(1, cancelledCount * 2)) {
                        return String.format(
                                        "The business looks healthy with %d active subscriptions and $%.2f in tracked revenue. Keep momentum by nudging monthly customers toward annual plans and highlighting your most-used features.",
                                        activeCount, totalRevenue);
                }

                return String.format(
                                "You have %d subscriptions with %d active and %d cancelled, generating $%.2f so far. Monitor renewal behavior closely and reach out to at-risk users before the next billing window.",
                                totalSubscriptions, activeCount, cancelledCount, totalRevenue);
        }

        private String buildFallbackChurnRisk(int subscriptionCount, String latestStatus) {
                if ("CANCELLED".equalsIgnoreCase(latestStatus)) {
                        return "**High** - the user already has a recent cancellation on record, so immediate retention outreach is recommended.";
                }

                if (subscriptionCount >= 2) {
                        return "**Medium** - the account has recurring subscription history, so keep engagement high and monitor renewal behavior.";
                }

                return "**Low** - the available subscription history does not currently show strong churn signals.";
        }

        private List<TenantPlanDto> buildFallbackPricingPlans(String businessDescription) {
                String normalizedDescription = businessDescription == null || businessDescription.isBlank()
                                ? "your SaaS product"
                                : businessDescription.trim();

                return List.of(
                                createFallbackPlan(
                                                "Starter",
                                                "Entry plan for teams getting started with " + normalizedDescription + ".",
                                                BigDecimal.valueOf(499),
                                                BillingCycle.MONTHLY,
                                                "Core platform access,Up to 3 team members,Email support,Usage dashboard"),
                                createFallbackPlan(
                                                "Growth",
                                                "Balanced plan for growing businesses that need more control and visibility.",
                                                BigDecimal.valueOf(1499),
                                                BillingCycle.MONTHLY,
                                                "Everything in Starter,Advanced analytics,Priority support,Workflow automation"),
                                createFallbackPlan(
                                                "Enterprise",
                                                "High-capacity plan for mature teams that need scale, governance, and flexibility.",
                                                BigDecimal.valueOf(14999),
                                                BillingCycle.YEARLY,
                                                "Custom onboarding,Dedicated support,Advanced security,Flexible integrations"));
        }

        private TenantPlanDto createFallbackPlan(String name, String description, BigDecimal price,
                        BillingCycle billingCycle, String features) {
                TenantPlanDto plan = new TenantPlanDto();
                plan.setName(name);
                plan.setDescription(description);
                plan.setPrice(price);
                plan.setBillingCycle(billingCycle);
                plan.setFeatures(features);
                return plan;
        }
}
