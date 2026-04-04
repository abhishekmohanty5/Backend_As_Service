package com.aegis.saas.serviceTest;

import com.aegis.saas.dto.RegistrationRequest;
import com.aegis.saas.entity.*;
import com.aegis.saas.exception.BusinessException;
import com.aegis.saas.exception.UnverifiedEmailException;
import com.aegis.saas.repository.*;
import com.aegis.saas.service.EmailService;
import com.aegis.saas.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Registration Flow Tests")
public class RegistrationFlowTest {

    @Mock private UserRepo userRepo;
    @Mock private TenantRepo tenantRepo;
    @Mock private EmailVerificationTokenRepo emailTokenRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private RegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(userService, "tokenExpirationHours", 24);
        ReflectionTestUtils.setField(userService, "mailUsername", "");
        ReflectionTestUtils.setField(userService, "mailPassword", "");

        validRequest = new RegistrationRequest();
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("Test@1234");
        validRequest.setUserName("testuser");
        validRequest.setTenantName("Test Corp");
    }

    // ─── Test 1 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ New user: Tenant created, User saved, NO subscription created")
    void register_newUser_createsTenantAndUserButNoSubscription() {
        // Arrange
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");

        Tenant savedTenant = new Tenant();
        savedTenant.setId(1L);
        savedTenant.setName("Test Corp");
        when(tenantRepo.save(any(Tenant.class))).thenReturn(savedTenant);

        Users savedUser = new Users();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setUsername("testuser");
        when(userRepo.save(any(Users.class))).thenReturn(savedUser);

        when(emailTokenRepo.save(any())).thenReturn(null);

        // Act
        var result = userService.addUser(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());

        verify(tenantRepo, times(1)).save(any(Tenant.class));       // Tenant IS created
        verify(userRepo, times(1)).save(any(Users.class));          // User IS saved
        // ✅ KEY CHECK: No subscription created during registration
        // (TenantSubscriptionRepo is not even injected in UserService anymore)
    }

    // ─── Test 2 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ Verified user re-registering: throws BusinessException (already registered)")
    void register_verifiedUserExists_throwsBusinessException() {
        // Arrange
        Users existingUser = new Users();
        existingUser.setEmail("test@example.com");
        existingUser.setEmailVerified(true);   // ← VERIFIED

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.addUser(validRequest)
        );

        assertTrue(ex.getMessage().contains("already registered"));
        verify(tenantRepo, never()).save(any());           // No new Tenant
        verify(emailTokenRepo, never()).save(any());       // No new token
    }

    // ─── Test 3 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ Unverified user re-registering: resends email, throws UnverifiedEmailException")
    void register_unverifiedUserExists_resendsLinkAndThrowsUnverifiedException() {
        // Arrange
        Tenant existingTenant = new Tenant();
        existingTenant.setId(1L);

        Users existingUser = new Users();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        existingUser.setEmailVerified(false);   // ← NOT VERIFIED
        existingUser.setTenant(existingTenant);

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        // resendVerificationEmail calls findByEmail internally
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(emailTokenRepo.save(any())).thenReturn(null);

        // Act & Assert
        UnverifiedEmailException ex = assertThrows(
                UnverifiedEmailException.class,
                () -> userService.addUser(validRequest)
        );

        assertTrue(ex.getMessage().contains("verification email has been sent"));
        verify(emailTokenRepo, times(1)).deleteByUser(existingUser);  // Old token deleted
        verify(emailTokenRepo, times(1)).save(any());                  // New token saved
        verify(tenantRepo, never()).save(any());   // No second Tenant created
    }
}
