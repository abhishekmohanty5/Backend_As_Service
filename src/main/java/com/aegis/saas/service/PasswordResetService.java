package com.aegis.saas.service;

import com.aegis.saas.entity.PasswordResetToken;
import com.aegis.saas.entity.Users;
import com.aegis.saas.exception.BusinessException;
import com.aegis.saas.exception.ResourceNotFoundException;
import com.aegis.saas.repository.PasswordResetTokenRepo;
import com.aegis.saas.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${password.reset.token.expiration-hours:1}")
    private int resetTokenExpirationHours;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    /**
     * Request password reset - generates token and sends email
     */
    public void requestPasswordReset(String email) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        // Remove old reset tokens
        passwordResetTokenRepo.deleteByUser(user);

        // Generate new reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(resetTokenExpirationHours))
                .build();

        passwordResetTokenRepo.save(resetToken);

        // Send reset email
        if (isEmailConfigured()) {
            try {
                String resetLink = baseUrl + "/reset-password?token=" + token;
                emailService.sendEmail(
                        email,
                        "Reset your password",
                        """
                        <h1>Reset your password</h1>
                        <p>We received a request to reset your password for your AegisInfra account.</p>
                        <p>If you requested this change, please click the button below to set a new password:</p>
                        <div class="btn-container">
                            <a href="%s" class="btn">Reset Password</a>
                        </div>
                        <p class="fallback-link">
                            Alternatively, you can copy and paste this link into your browser:<br>
                            %s
                        </p>
                        <div class="divider"></div>
                        <p style="font-size: 14px; color: #64748b;">
                            This link will expire in <strong>%d hours</strong>.<br>
                            If you did not request this, you can safely ignore this email; your password will remain unchanged.
                        </p>
                        """.formatted(resetLink, resetLink, resetTokenExpirationHours)
                );
                log.info("Password reset email sent to: {}", email);
            } catch (Exception e) {
                log.error("Failed to send password reset email to: {}", email, e);
                throw new BusinessException("Failed to send reset email");
            }
        } else {
            log.info("Email service not configured — password reset token generated: {}", token);
        }
    }

    /**
     * Validate reset token
     */
    public void validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid reset token"));

        if (resetToken.isUsed()) {
            throw new BusinessException("Reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new BusinessException("Reset token has expired");
        }
    }

    /**
     * Reset password with token
     */
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid reset token"));

        if (!resetToken.isValid()) {
            throw new BusinessException("Reset token is invalid or expired");
        }

        // Update password
        Users user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepo.save(resetToken);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    /**
     * Cleanup expired reset tokens
     */
    @Transactional
    public void cleanupExpiredResetTokens() {
        passwordResetTokenRepo.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Expired password reset tokens cleaned up");
    }

    private boolean isEmailConfigured() {
        return mailUsername != null && !mailUsername.isBlank()
                && !mailUsername.contains("placeholder");
    }
}
