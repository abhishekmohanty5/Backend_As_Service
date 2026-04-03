package com.aegis.saas.service;

import com.aegis.saas.auth.JWTService;
import com.aegis.saas.dto.LoginRequest;
import com.aegis.saas.dto.LoginResponse;
import com.aegis.saas.entity.EmailVerificationToken;
import com.aegis.saas.entity.Users;
import com.aegis.saas.exception.BusinessException;
import com.aegis.saas.exception.InvalidCredentialException;
import com.aegis.saas.exception.UnauthorizedException;
import com.aegis.saas.repository.EmailVerificationTokenRepo;
import com.aegis.saas.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final EmailVerificationTokenRepo emailTokenRepository;

    public LoginResponse login(LoginRequest loginRequest) {
        Users user = userRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialException("Invalid credentials. Please try again."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Invalid credentials. Please try again.");
        }

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException(
                    "Email not verified. Please check your inbox or use the resend verification option.");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getTenant().getId());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setEmail(loginRequest.getEmail());
        loginResponse.setRole(user.getRole());
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

        Users user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepo.save(user);

        verificationToken.setVerified(true);
        emailTokenRepository.save(verificationToken);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }
}
