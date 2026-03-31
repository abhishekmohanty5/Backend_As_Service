package com.jobhunt.saas.service;

import com.jobhunt.saas.auth.JWTService;
import com.jobhunt.saas.dto.LoginRequest;
import com.jobhunt.saas.dto.LoginResponse;
import com.jobhunt.saas.entity.EmailVerificationToken;
import com.jobhunt.saas.entity.Users;
import com.jobhunt.saas.exception.InvalidCredentialException;
import com.jobhunt.saas.repository.EmailVerificationTokenRepo;
import com.jobhunt.saas.repository.UserRepo;
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

        String email = loginRequest.getEmail();

        if (!userRepo.existsByEmail(email)) {
            throw new InvalidCredentialException("Email not Exist Please Register");
        }
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Invalid credentials. Please try again.");
        }

        // Check if email is verified
        if (!user.isEmailVerified()) {
            throw new InvalidCredentialException("Email not verified. Please check your inbox or resend the verification email.");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getTenant().getId());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setEmail(email);
        loginResponse.setRole(user.getRole());
        return loginResponse;
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        // Check if token has expired
        if (verificationToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        // Check if token is already used
        if (verificationToken.isVerified()) {
            throw new RuntimeException("Email has already been verified");
        }

        // Get the user and mark email as verified
        Users user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepo.save(user);

        // Mark the token as used
        verificationToken.setVerified(true);
        emailTokenRepository.save(verificationToken);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }
}
