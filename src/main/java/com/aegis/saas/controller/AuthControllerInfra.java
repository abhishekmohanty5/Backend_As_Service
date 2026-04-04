package com.aegis.saas.controller;

import com.aegis.saas.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.aegis.saas.service.AuthService;
import com.aegis.saas.service.UserService;
import com.aegis.saas.service.PasswordResetService;
import com.aegis.saas.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Auth - Tenant Registration", description = "Tenant company registration, login, email verification, password reset")
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthControllerInfra {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<AppResponse<RegistrationResponse>> regUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        RegistrationResponse response = userService.addUser(registrationRequest);
        AppResponse<RegistrationResponse> appResponse =
                new AppResponse<>("Registration successful. Please verify your email.", response, 200, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(appResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AppResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        LoginResponse response = authService.login(loginRequest, request);
        AppResponse<LoginResponse> body =
                new AppResponse<>("Login successful", response, 200, LocalDateTime.now());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<AppResponse<String>> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        authService.logout(refreshTokenRequest.getRefreshToken());
        AppResponse<String> body =
                new AppResponse<>("Logged out successfully", null, 200, LocalDateTime.now());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AppResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest, HttpServletRequest request) {
        TokenResponse response = tokenService.refreshAccessToken(refreshTokenRequest.getRefreshToken(), request);
        AppResponse<TokenResponse> body =
                new AppResponse<>("Token refreshed successfully", response, 200, LocalDateTime.now());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<AppResponse<String>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        AppResponse<String> body =
                new AppResponse<>("Email verified successfully. You can now log in.",
                        null,
                        200,
                        LocalDateTime.now()
                );
        return ResponseEntity.ok(body);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AppResponse<String>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        AppResponse<String> body =
                new AppResponse<>("If the email exists, a password reset link has been sent.",
                        null,
                        200,
                        LocalDateTime.now()
                );
        return ResponseEntity.ok(body);
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<AppResponse<String>> validateResetToken(@RequestParam String token) {
        passwordResetService.validateResetToken(token);
        AppResponse<String> body =
                new AppResponse<>("Token is valid", null, 200, LocalDateTime.now());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AppResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(
                    new AppResponse<>("Passwords do not match", null, 400, LocalDateTime.now()));
        }

        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        AppResponse<String> body =
                new AppResponse<>("Password reset successfully. Please log in with your new password.",
                        null,
                        200,
                        LocalDateTime.now()
                );
        return ResponseEntity.ok(body);
    }
}
