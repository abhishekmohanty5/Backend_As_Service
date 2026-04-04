package com.aegis.saas.dto;

import com.aegis.saas.entity.RefreshToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDto {
    private Long id;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private boolean revoked;
    private boolean expired;
    private LocalDateTime createdAt;

    public static SessionDto from(RefreshToken token) {
        return SessionDto.builder()
                .id(token.getId())
                .ipAddress(token.getIpAddress())
                .userAgent(token.getUserAgent())
                .issuedAt(token.getIssuedAt())
                .expiresAt(token.getExpiresAt())
                .revoked(token.isRevoked())
                .expired(token.isExpired())
                .createdAt(token.getCreatedAt())
                .build();
    }
}
