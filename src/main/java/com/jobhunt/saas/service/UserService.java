package com.jobhunt.saas.service;

import com.jobhunt.saas.dto.RegistrationRequest;
import com.jobhunt.saas.dto.RegistrationResponse;
import com.jobhunt.saas.entity.*;
import com.jobhunt.saas.entity.TenantSubscription;
import com.jobhunt.saas.repository.PlanRepo;
import com.jobhunt.saas.repository.TenantRepo;
import com.jobhunt.saas.repository.UserRepo;
import com.jobhunt.saas.repository.TenantSubscriptionRepo;
import com.jobhunt.saas.repository.EmailVerificationTokenRepo;
import com.jobhunt.saas.tenant.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepo userRepo;
    private final TenantRepo tenantRepo;
    private final PlanRepo planRepo;
    private final TenantSubscriptionRepo tenantSubscriptionRepo;
    private final EmailVerificationTokenRepo emailTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${email.token.expiration-hours:24}")
    private int tokenExpirationHours;


    public RegistrationResponse addUser(RegistrationRequest registrationRequest) {

        // 1. Fetch default FREE/TRIAL plan
        Plan defaultPlan = planRepo.findByName("FREE").orElseThrow(
                () -> new RuntimeException("Plan Not Found"));

        // 2. Create Tenant
        Tenant tenant = new Tenant();
        tenant.setName(registrationRequest.getTenantName());
        tenant = tenantRepo.save(tenant);
        


        // 2.5 Create Engine Subscription
        TenantSubscription ts = new TenantSubscription();
        ts.setTenant(tenant);
        ts.setPlan(defaultPlan);
        //ts.setStatus(SubscriptionStatus.ACTIVE);
        ts.setStartDate(LocalDateTime.now());
        ts.setExpireDate(LocalDateTime.now().plusDays(defaultPlan.getDurationInDays()));
        tenantSubscriptionRepo.save(ts);

        if (userRepo.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // . Create TENANT ADMIN User
        Users user = new Users();
        user.setUsername(registrationRequest.getUserName());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setRole(Role.ROLE_TENANT_ADMIN);
        user.setTenant(tenant);
        user.setEmailVerified(false);  // Mark as unverified

        // 4. Save user
        userRepo.save(user);

        // 5. Generate and save verification token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(tokenExpirationHours);
        
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryTime(expiryTime);
        emailTokenRepo.save(verificationToken);

        // 6. Send verification email
        try {
            String verifyLink = baseUrl + "/api/auth/verify-email?token=" + token;
            emailService.sendEmail(
                    registrationRequest.getEmail(),
                    "Verify your email",
                    "Please click the following link to verify your email: " + verifyLink
            );
            log.info("Verification email sent to: {}", registrationRequest.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", registrationRequest.getEmail(), e);
            throw new RuntimeException("Failed to send verification email");
        }

        // 7. Return response
        return new RegistrationResponse(user.getUsername(), user.getEmail());
    }

    public void resendVerificationEmail(String email) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        // Remove old tokens
        emailTokenRepo.deleteByUser(user);

        // Create new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(tokenExpirationHours);

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryTime(expiryTime);
        emailTokenRepo.save(verificationToken);

        try {
            String verifyLink = baseUrl + "/api/auth/verify-email?token=" + token;
            emailService.sendEmail(
                    email,
                    "Verify your email",
                    "Please click the following link to verify your email: " + verifyLink
            );
        } catch (Exception e) {
            log.error("Failed to resend verification email to: {}", email, e);
            throw new RuntimeException("Failed to send verification email");
        }
    }

    public Users getUserByEmail(String email) {
        Long tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new RuntimeException("Tenant not resolved");
        }

        return userRepo.findByEmailAndTenant_Id(email, tenantId);
    }
}
