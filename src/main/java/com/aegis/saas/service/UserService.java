package com.aegis.saas.service;

import com.aegis.saas.dto.RegistrationRequest;
import com.aegis.saas.dto.RegistrationResponse;
import com.aegis.saas.entity.*;
import com.aegis.saas.exception.BusinessException;
import com.aegis.saas.exception.ResourceNotFoundException;
import com.aegis.saas.repository.EmailVerificationTokenRepo;
import com.aegis.saas.repository.TenantRepo;
import com.aegis.saas.repository.UserRepo;
import org.springframework.transaction.annotation.Transactional;
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
    private final EmailVerificationTokenRepo emailTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${email.token.expiration-hours:24}")
    private int tokenExpirationHours;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Transactional(noRollbackFor = com.aegis.saas.exception.UnverifiedEmailException.class)
    public RegistrationResponse addUser(RegistrationRequest registrationRequest) {

        String email=registrationRequest.getEmail();
        Users existUser= userRepo.findByEmail(email).orElse(null);


        if(existUser !=null ){
            if (!existUser.isEmailVerified()) {
                resendVerificationEmail(existUser.getEmail());
                throw new com.aegis.saas.exception.UnverifiedEmailException("Email is registered but unverified. A new verification email has been sent.");
            } else {
                throw new BusinessException("Email is already registered. Please login or use a different email.");
            }
        }

        // 2. Create Tenant (INACTIVE until email verification)
        Tenant tenant = new Tenant();
        tenant.setName(registrationRequest.getTenantName());
        String rawTenantSecret = "sk_" + UUID.randomUUID().toString().replace("-", "");
        tenant.setClientSecret(passwordEncoder.encode(rawTenantSecret));
        tenant.setClientSecretGeneratedAt(LocalDateTime.now());
        tenant = tenantRepo.save(tenant);
        // NOTE: No TenantSubscription created here.
        // The FREE plan subscription (14 days) is created after email verification
        // so the subscription clock starts from actual verification, not registration.

        // 3. Create TENANT ADMIN User
        Users user = new Users();
        user.setUsername(registrationRequest.getUserName());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setRole(Role.ROLE_TENANT_ADMIN);
        user.setTenant(tenant);
        user.setEmailVerified(false);
        userRepo.save(user);

        // 6. Generate email verification token
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryTime(LocalDateTime.now().plusHours(tokenExpirationHours));
        emailTokenRepo.save(verificationToken);

        // 7. Send verification email — skip gracefully if not configured
        if (isEmailConfigured()) {
            try {
                String verifyLink = baseUrl + "/verify-email?token=" + token;
                emailService.sendEmail(
                        registrationRequest.getEmail(),
                        "Verify your AegisInfra Account",
                        "Welcome to AegisInfra! We're excited to have you on board.<br><br>" +
                        "To get started, please verify your email address by clicking the button below:<br><br>" +
                        "<a href=\"" + verifyLink + "\" style=\"display: inline-block; padding: 12px 24px; background-color: #2563eb; color: #ffffff !important; text-decoration: none; border-radius: 8px; font-weight: 600;\">Verify Email Address</a><br><br>" +
                        "Alternatively, you can copy and paste this link into your browser:<br>" +
                        "<span style=\"word-break: break-all; color: #64748b; font-size: 14px;\">" + verifyLink + "</span><br><br>" +
                        "This link will expire in <span class=\"highlight\">" + tokenExpirationHours + " hours</span>."
                );
                log.info("Verification email sent to: {}", registrationRequest.getEmail());
            } catch (Exception e) {
                log.warn("Failed to send verification email to: {} — user was still created successfully",
                        registrationRequest.getEmail(), e);
            }
        } else {
            log.info("Email service not configured — skipping verification email for: {}",
                    registrationRequest.getEmail());
        }

        return new RegistrationResponse(user.getUsername(), user.getEmail());
    }

    public void resendVerificationEmail(String email) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        if (user.isEmailVerified()) {
            throw new BusinessException("Email is already verified.");
        }

        emailTokenRepo.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryTime(LocalDateTime.now().plusHours(tokenExpirationHours));
        emailTokenRepo.save(verificationToken);

        if (!isEmailConfigured()) {
            log.info("Email service not configured — skipping resend verification email");
            return;
        }
        try {
            String verifyLink = baseUrl + "/verify-email?token=" + token;
            emailService.sendEmail(email, "Verify your email address",
                    "Please click the following link to verify your email: " + verifyLink);
            log.info("Verification email resent to: {}", email);
        } catch (Exception e) {
            log.warn("Failed to resend verification email to: {}", email, e);
        }
    }

    private boolean isEmailConfigured() {
        return mailUsername != null && !mailUsername.isBlank()
                && !mailUsername.contains("placeholder")
                && mailPassword != null && !mailPassword.isBlank()
                && !mailPassword.contains("placeholder");
    }
}
