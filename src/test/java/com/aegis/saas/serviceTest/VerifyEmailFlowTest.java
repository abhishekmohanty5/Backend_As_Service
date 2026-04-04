package com.aegis.saas.serviceTest;

import com.aegis.saas.auth.JWTService;
import com.aegis.saas.entity.*;
import com.aegis.saas.exception.BusinessException;
import com.aegis.saas.repository.*;
import com.aegis.saas.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Verification Flow Tests")
public class VerifyEmailFlowTest {

    @Mock private UserRepo userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JWTService jwtService;
    @Mock private EmailVerificationTokenRepo emailTokenRepository;
    @Mock private TenantSubscriptionRepo tenantSubscriptionRepo;
    @Mock private TenantRepo tenantRepo;
    @Mock private PlanRepo planRepo;

    @InjectMocks
    private AuthService authService;

    private Plan freePlan;
    private Tenant tenant;
    private Users user;
    private EmailVerificationToken validToken;

    @BeforeEach
    void setUp() {
        freePlan = new Plan();
        freePlan.setId(1L);
        freePlan.setName("FREE");
        freePlan.setPrice(BigDecimal.ZERO);
        freePlan.setDurationInDays(14);   // ← 14 days as per our plan
        freePlan.setActive(true);

        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("Test Corp");

        user = new Users();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setEmailVerified(false);
        user.setTenant(tenant);

        validToken = new EmailVerificationToken();
        validToken.setToken("valid-token-123");
        validToken.setUser(user);
        validToken.setExpiryTime(LocalDateTime.now().plusHours(24)); // not expired
        validToken.setVerified(false);
    }

    // ─── Test 1 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ Valid token: user verified, tenant activated, FREE 14-day sub created")
    void verifyEmail_validToken_activatesEverythingCorrectly() {
        // Arrange
        when(emailTokenRepository.findByToken("valid-token-123")).thenReturn(Optional.of(validToken));
        when(planRepo.findByName("FREE")).thenReturn(Optional.of(freePlan));
        when(userRepo.save(any())).thenReturn(user);
        when(tenantRepo.save(any())).thenReturn(tenant);

        ArgumentCaptor<TenantSubscription> subCaptor = ArgumentCaptor.forClass(TenantSubscription.class);

        // Act
        authService.verifyEmail("valid-token-123");

        // Assert — User verified
        assertTrue(user.isEmailVerified(), "User should be marked as email verified");

        // Assert — Tenant activated
        assertTrue(tenant.isSetEnable(), "Tenant should be activated after verification");

        // Assert — Subscription created with correct values
        verify(tenantSubscriptionRepo, times(1)).save(subCaptor.capture());
        TenantSubscription savedSub = subCaptor.getValue();

        assertEquals(SubscriptionStatus.ACTIVE, savedSub.getStatus(),
                "Subscription status must be ACTIVE");
        assertEquals(tenant, savedSub.getTenant(),
                "Subscription must be linked to correct tenant");
        assertEquals(freePlan, savedSub.getPlan(),
                "Subscription must use FREE plan");

        // ✅ KEY CHECK: expireDate = startDate + 14 days
        assertNotNull(savedSub.getStartDate());
        assertNotNull(savedSub.getExpireDate());
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(
                savedSub.getStartDate(), savedSub.getExpireDate());
        assertEquals(14, daysDiff, "FREE subscription must last exactly 14 days");

        // Assert — token marked as used
        assertTrue(validToken.isVerified(), "Verification token should be marked as used");
    }

    // ─── Test 2 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ Invalid token: throws BusinessException")
    void verifyEmail_invalidToken_throwsException() {
        when(emailTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> authService.verifyEmail("bad-token")
        );

        assertTrue(ex.getMessage().contains("Invalid or expired"));
        verify(tenantSubscriptionRepo, never()).save(any());  // No subscription created
        verify(tenantRepo, never()).save(any());              // Tenant not touched
    }

    // ─── Test 3 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ Expired token: throws BusinessException, no subscription created")
    void verifyEmail_expiredToken_throwsException() {
        validToken.setExpiryTime(LocalDateTime.now().minusHours(1)); // expired
        when(emailTokenRepository.findByToken("valid-token-123")).thenReturn(Optional.of(validToken));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> authService.verifyEmail("valid-token-123")
        );

        assertTrue(ex.getMessage().contains("expired"));
        verify(tenantSubscriptionRepo, never()).save(any());  // No subscription created
    }

    // ─── Test 4 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ Already verified token: throws BusinessException")
    void verifyEmail_alreadyVerifiedToken_throwsException() {
        validToken.setVerified(true);  // already used
        when(emailTokenRepository.findByToken("valid-token-123")).thenReturn(Optional.of(validToken));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> authService.verifyEmail("valid-token-123")
        );

        assertTrue(ex.getMessage().contains("already been verified"));
        verify(tenantSubscriptionRepo, never()).save(any());  // No duplicate subscription
    }
}
