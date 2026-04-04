package com.aegis.saas.controller;

import com.aegis.saas.dto.AppResponse;
import com.aegis.saas.dto.PaginatedResponse;
import com.aegis.saas.dto.SessionDto;
import com.aegis.saas.entity.RefreshToken;
import com.aegis.saas.service.TokenService;
import com.aegis.saas.auth.AuthContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Auth - Session Management", description = "Manage user sessions (active refresh tokens)")
@RestController
@RequestMapping("/api/v1/auth/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final TokenService tokenService;
    private final AuthContext authContext;

    /**
     * Get all active sessions for current user (paginated)
     */
    @GetMapping
    public ResponseEntity<AppResponse<PaginatedResponse<SessionDto>>> getActiveSessions(Pageable pageable) {
        Long userId = authContext.getCurrentUserId();
        Page<RefreshToken> tokens = tokenService.getActiveSessions(userId, pageable);
        Page<SessionDto> sessions = tokens.map(SessionDto::from);
        PaginatedResponse<SessionDto> data = PaginatedResponse.from(sessions);

        AppResponse<PaginatedResponse<SessionDto>> response = AppResponse.<PaginatedResponse<SessionDto>>builder()
                .message("Active sessions retrieved")
                .data(data)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Revoke a specific session by token ID
     */
    @DeleteMapping("/{tokenId}")
    public ResponseEntity<AppResponse<String>> revokeSession(@PathVariable Long tokenId) {
        Long userId = authContext.getCurrentUserId();
        tokenService.revokeSession(tokenId, userId);

        AppResponse<String> response = AppResponse.<String>builder()
                .message("Session revoked successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Logout from all devices (revoke all sessions)
     */
    @PostMapping("/logout-all")
    public ResponseEntity<AppResponse<String>> logoutAllDevices() {
        Long userId = authContext.getCurrentUserId();
        tokenService.revokeAllTokensForUser(userId);

        AppResponse<String> response = AppResponse.<String>builder()
                .message("Logged out from all devices successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
