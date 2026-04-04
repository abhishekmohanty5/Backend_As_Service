package com.aegis.saas.service;

import com.aegis.saas.auth.JWTService;
import com.aegis.saas.dto.LoginRequest;
import com.aegis.saas.dto.LoginResponse;
import com.aegis.saas.dto.TokenResponse;
import com.aegis.saas.entity.*;
import com.aegis.saas.exception.BusinessException;
import com.aegis.saas.exception.InvalidCredentialException;
import com.aegis.saas.exception.UnauthorizedException;
import com.aegis.saas.repository.EmailVerificationTokenRepo;
import com.aegis.saas.repository.PlanRepo;
import com.aegis.saas.repository.TenantRepo;
import com.aegis.saas.repository.TenantSubscriptionRepo;
import com.aegis.saas.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final TokenService tokenService;
    private final EmailVerificationTokenRepo emailTokenRepository;
    private final TenantSubscriptionRepo tenantSubscriptionRepo;
    private final TenantRepo tenantRepo;
    private final PlanRepo planRepo;

    public LoginResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        Users user = userRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialException("Invalid credentials. Please try again."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Invalid credentials. Please try again.");
        }

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException(
                    "Email not verified. Please check your inbox or use the resend verification option.");
        }

        // Generate access and refresh tokens
        TokenResponse tokenResponse = tokenService.generateTokens(user.getEmail(), user.getTenant().getId(), request);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(tokenResponse.getAccessToken());
        loginResponse.setRefreshToken(tokenResponse.getRefreshToken());
        loginResponse.setTokenType(tokenResponse.getTokenType());
        loginResponse.setExpiresIn(tokenResponse.getExpiresIn());
        loginResponse.setEmail(loginRequest.getEmail());
        loginResponse.setRole(user.getRole());
        
        log.info("User logged in: {}", user.getEmail());
        return loginResponse;
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid or expired verification token."));

        if (verificationToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Verification token has expired. Please request a new one.");
        }

        if (verificationToken.isVerified()) {
            throw new BusinessException("Email has already been verified. You can log in.");
        }

        // 1. Mark user as verified
        Users user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepo.save(user);

        // 2. Activate the Tenant
        Tenant tenant = user.getTenant();
        tenant.setSetEnable(true);
        tenantRepo.save(tenant);

        // 3. Create FREE subscription — clock starts at verification time, not registration
        Plan freePlan = planRepo.findByName("FREE")
                .orElseThrow(() -> new BusinessException("FREE plan not found. Please contact support."));

        Optional<TenantSubscription> existingSub =
                tenantSubscriptionRepo.findFirstByTenantIdOrderByCreatedAtDesc(tenant.getId());

        if (existingSub.isPresent()) {
            // Edge case: subscription already exists (e.g. re-verification attempt) — just activate it
            TenantSubscription sub = existingSub.get();
            sub.setStatus(SubscriptionStatus.ACTIVE);
            sub.setPlan(freePlan);
            sub.setStartDate(LocalDateTime.now());
            sub.setExpireDate(LocalDateTime.now().plusDays(freePlan.getDurationInDays()));
            tenantSubscriptionRepo.save(sub);
        } else {
            // Normal new-user path — create fresh subscription
            TenantSubscription subscription = new TenantSubscription();
            subscription.setTenant(tenant);
            subscription.setPlan(freePlan);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartDate(LocalDateTime.now());
            subscription.setExpireDate(LocalDateTime.now().plusDays(freePlan.getDurationInDays()));
            tenantSubscriptionRepo.save(subscription);
        }

        // 4. Mark token as used
        verificationToken.setVerified(true);
        emailTokenRepository.save(verificationToken);

        log.info("Email verified for: {}. Tenant activated. FREE 14-day subscription started.", user.getEmail());
    }

    public void logout(String refreshToken) {
        tokenService.revokeToken(refreshToken);
        log.info("User logged out");
    }
}
