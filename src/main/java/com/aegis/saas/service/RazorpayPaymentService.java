package com.aegis.saas.service;

import com.aegis.saas.dto.RazorpayOrderRequest;
import com.aegis.saas.dto.RazorpayOrderResponse;
import com.aegis.saas.dto.RazorpayVerifyRequest;
import com.aegis.saas.dto.RazorpayVerifyResponse;
import com.aegis.saas.dto.TenantSubscriptionResponseDto;
import com.aegis.saas.entity.Plan;
import com.aegis.saas.exception.BusinessException;
import com.aegis.saas.exception.ResourceNotFoundException;
import com.aegis.saas.repository.PlanRepo;
import com.aegis.saas.tenant.TenantContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RazorpayPaymentService {

    private static final URI ORDER_API = URI.create("https://api.razorpay.com/v1/orders");

    private final PlanRepo planRepo;
    private final ObjectMapper objectMapper;
    private final EngineSubscriptionService engineSubscriptionService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${payment.razorpay.enabled:true}")
    private boolean enabled;

    @Value("${payment.razorpay.mode:test}")
    private String gatewayMode;

    @Value("${payment.razorpay.key-id:}")
    private String keyId;

    @Value("${payment.razorpay.key-secret:}")
    private String keySecret;

    @Value("${payment.razorpay.currency:INR}")
    private String currency;

    @Value("${payment.razorpay.merchant-name:Aegis Infra}")
    private String merchantName;

    @Value("${payment.razorpay.description-prefix:Aegis subscription payment}")
    private String descriptionPrefix;

    public RazorpayOrderResponse createOrder(RazorpayOrderRequest request) {
        ensureEnabled();

        Long tenantId = getTenantId();
        String billingInterval = normalizeBillingInterval(request.getBillingInterval());
        Plan plan = planRepo.findById(request.getTargetPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.getTargetPlanId()));

        if (!plan.isActive()) {
            throw new BusinessException("The plan '" + plan.getName() + "' is no longer available.");
        }

        long amountInPaise = toPaise(plan.getPrice());
        String receipt = "rcpt_" + tenantId + "_" + plan.getId() + "_" + System.currentTimeMillis();
        String description = descriptionPrefix + " for " + plan.getName();

        try {
            ObjectNode orderRequest = objectMapper.createObjectNode();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency.toUpperCase(Locale.ROOT));
            orderRequest.put("receipt", receipt);

            ObjectNode notes = orderRequest.putObject("notes");
            notes.put("tenant_id", String.valueOf(tenantId));
            notes.put("plan_id", String.valueOf(plan.getId()));
            notes.put("plan_name", plan.getName());
            notes.put("billing_interval", billingInterval);

            HttpRequest httpRequest = HttpRequest.newBuilder(ORDER_API)
                    .header("Content-Type", "application/json")
                    .header("Authorization", basicAuthHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(orderRequest.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(parseRazorpayError(response.body(), response.statusCode()));
            }

            JsonNode responseNode = objectMapper.readTree(response.body());
            String orderId = responseNode.path("id").asText(null);
            if (orderId == null || orderId.isBlank()) {
                throw new BusinessException("Razorpay order creation succeeded, but no order ID was returned.");
            }

            return RazorpayOrderResponse.builder()
                    .gatewayMode(gatewayMode.toUpperCase(Locale.ROOT))
                    .keyId(keyId)
                    .merchantName(merchantName)
                    .description(description)
                    .currency(currency.toUpperCase(Locale.ROOT))
                    .amountInPaise(amountInPaise)
                    .amount(plan.getPrice())
                    .planId(plan.getId())
                    .planName(plan.getName())
                    .billingInterval(billingInterval)
                    .orderId(orderId)
                    .receipt(receipt)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Razorpay order request was interrupted.");
        } catch (Exception ex) {
            if (ex instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException("Unable to create Razorpay order: " + ex.getMessage());
        }
    }

    @Transactional
    public RazorpayVerifyResponse verifyAndUpgrade(RazorpayVerifyRequest request) {
        ensureEnabled();

        String billingInterval = normalizeBillingInterval(request.getBillingInterval());
        validateRequired(request.getRazorpayOrderId(), "Razorpay order ID is required.");
        validateRequired(request.getRazorpayPaymentId(), "Razorpay payment ID is required.");
        validateRequired(request.getRazorpaySignature(), "Razorpay signature is required.");

        String expectedSignature = generateSignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId());
        if (!constantTimeEquals(expectedSignature, request.getRazorpaySignature())) {
            throw new BusinessException("Payment verification failed. Razorpay signature mismatch.");
        }

        TenantSubscriptionResponseDto upgraded = engineSubscriptionService.upgradePlanDto(
                request.getTargetPlanId(),
                billingInterval,
                request.getRazorpayPaymentId());

        return RazorpayVerifyResponse.builder()
                .verified(true)
                .message("Payment verified successfully and subscription upgraded.")
                .orderId(request.getRazorpayOrderId())
                .paymentId(request.getRazorpayPaymentId())
                .signature(request.getRazorpaySignature())
                .upgradedSubscription(upgraded)
                .build();
    }

    public String generateSignature(String orderId, String paymentId) {
        validateRequired(orderId, "Razorpay order ID is required.");
        validateRequired(paymentId, "Razorpay payment ID is required.");
        ensureSecretConfigured();

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] digest = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (GeneralSecurityException ex) {
            throw new BusinessException("Unable to verify Razorpay signature.");
        }
    }

    public boolean isSignatureValid(String orderId, String paymentId, String signature) {
        return constantTimeEquals(generateSignature(orderId, paymentId), signature);
    }

    private void ensureEnabled() {
        if (!enabled) {
            throw new BusinessException("Razorpay payments are disabled.");
        }
        if (keyId == null || keyId.isBlank()) {
            throw new BusinessException("Razorpay key ID is not configured.");
        }
        ensureSecretConfigured();
    }

    private void ensureSecretConfigured() {
        if (keySecret == null || keySecret.isBlank()) {
            throw new BusinessException("Razorpay key secret is not configured.");
        }
    }

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context not resolved. Please ensure your token is valid.");
        }
        return tenantId;
    }

    private String normalizeBillingInterval(String billingInterval) {
        validateRequired(billingInterval, "Billing interval is required.");
        String normalized = billingInterval.trim().toUpperCase(Locale.ROOT);
        if (!"MONTHLY".equals(normalized) && !"ANNUAL".equals(normalized)) {
            throw new BusinessException("Billing interval must be MONTHLY or ANNUAL.");
        }
        return normalized;
    }

    private long toPaise(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessException("Plan amount is missing.");
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
    }

    private String basicAuthHeader() {
        String token = keyId + ":" + keySecret;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private String parseRazorpayError(String responseBody, int statusCode) {
        if (responseBody == null || responseBody.isBlank()) {
            return "Razorpay returned HTTP " + statusCode + " without an error body.";
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode error = root.path("error");
            String description = error.path("description").asText(null);
            if (description != null && !description.isBlank()) {
                return description;
            }
            String message = error.path("message").asText(null);
            if (message != null && !message.isBlank()) {
                return message;
            }
        } catch (Exception ignored) {
            // Fall through to raw body.
        }

        return "Razorpay order creation failed (HTTP " + statusCode + "): " + responseBody;
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
