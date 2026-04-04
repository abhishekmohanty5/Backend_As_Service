package com.aegis.saas.service;

import com.aegis.saas.auth.JWTService;
import com.aegis.saas.dto.TokenResponse;
import com.aegis.saas.entity.RefreshToken;
import com.aegis.saas.entity.Users;
import com.aegis.saas.exception.BusinessException;
import com.aegis.saas.repository.RefreshTokenRepo;
import com.aegis.saas.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TokenService {

    private final RefreshTokenRepo refreshTokenRepo;
    private final UserRepo userRepo;
    private final JWTService jwtService;

    @Value("${application.security.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration;

    /**
     * Generate new access and refresh tokens
     */
    public TokenResponse generateTokens(String email, Long tenantId, HttpServletRequest request) {
        String accessToken = jwtService.generateToken(email, tenantId);
        String refreshTokenValue = UUID.randomUUID().toString();

        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000))
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();

        refreshTokenRepo.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(refreshTokenExpiration / 1000)  // seconds
                .build();
    }

    /**
     * Refresh access token using refresh token (with rotation)
     */
    public TokenResponse refreshAccessToken(String refreshTokenValue, HttpServletRequest request) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new BusinessException("Refresh token has expired or been revoked");
        }

        Users user = refreshToken.getUser();

        // Generate new tokens
        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getTenant().getId());
        String newRefreshTokenValue = UUID.randomUUID().toString();

        // Token rotation: create new refresh token and mark old one as rotated
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000))
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();

        refreshTokenRepo.save(newRefreshToken);

        refreshToken.setRotatedToId(newRefreshToken.getId());
        refreshTokenRepo.save(refreshToken);

        log.info("Refresh token rotated for user: {}", user.getEmail());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(refreshTokenExpiration / 1000)
                .build();
    }

    /**
     * Revoke refresh token
     */
    public void revokeToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException("Refresh token not found"));

        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepo.save(refreshToken);

        log.info("Refresh token revoked for user: {}", refreshToken.getUser().getEmail());
    }

    /**
     * Revoke all tokens for a user (logout from all devices)
     */
    public void revokeAllTokensForUser(Long userId) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        refreshTokenRepo.findByUser(user, Pageable.unpaged()).forEach(token -> {
            if (!token.isRevoked()) {
                token.setRevoked(true);
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepo.save(token);
            }
        });

        log.info("All refresh tokens revoked for user: {}", user.getEmail());
    }

    /**
     * Get paginated list of active sessions (valid refresh tokens)
     */
    public Page<RefreshToken> getActiveSessions(Long userId, Pageable pageable) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        return refreshTokenRepo.findByUserAndRevokedFalse(user, pageable);
    }

    /**
     * Revoke a specific session by token ID
     */
    public void revokeSession(Long tokenId, Long userId) {
        RefreshToken token = refreshTokenRepo.findById(tokenId)
                .orElseThrow(() -> new BusinessException("Session not found"));

        if (!token.getUser().getId().equals(userId)) {
            throw new BusinessException("Unauthorized");
        }

        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());
        refreshTokenRepo.save(token);

        log.info("Session revoked - Token ID: {}", tokenId);
    }

    /**
     * Cleanup expired and rotated tokens
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepo.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Expired refresh tokens cleaned up");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
