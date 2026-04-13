package com.aegis.saas.exception;

import com.aegis.saas.dto.AppResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<AppResponse<String>> handleInvalidCredential(InvalidCredentialException ex) {
        log.error("InvalidCredentialException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AppResponse<>("error", ex.getMessage(), 401, LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        log.error("Validation failed: {}", errors);
        return ResponseEntity.badRequest()
                .body(new AppResponse<>("Validation failed", errors, 400, LocalDateTime.now()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<AppResponse<String>> handleIllegalState(IllegalStateException ex) {
        log.error("IllegalStateException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AppResponse<>("Operation Failed", ex.getMessage(), 400, LocalDateTime.now()));
    }

    @ExceptionHandler(SubscriptionException.class)
    public ResponseEntity<AppResponse<String>> handleSubscriptionException(SubscriptionException ex) {
        log.error("SubscriptionException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AppResponse<>("Subscription Error", ex.getMessage(), 400, LocalDateTime.now()));
    }

    /** 404 — resource not found */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<AppResponse<String>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("ResourceNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new AppResponse<>(ex.getMessage(), null, 404, LocalDateTime.now()));
    }

    /** 400 — business rule violation */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<AppResponse<String>> handleBusinessException(BusinessException ex) {
        log.warn("BusinessException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AppResponse<>(ex.getMessage(), null, 400, LocalDateTime.now()));
    }

    /** 400 — unverified email logic */
    @ExceptionHandler(UnverifiedEmailException.class)
    public ResponseEntity<AppResponse<String>> handleUnverifiedEmailException(UnverifiedEmailException ex) {
        log.warn("UnverifiedEmailException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AppResponse<>(ex.getMessage(), null, 400, LocalDateTime.now()));
    }

    /** 403 — ownership / permission violation */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<AppResponse<String>> handleUnauthorized(UnauthorizedException ex) {
        log.warn("UnauthorizedException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new AppResponse<>(ex.getMessage(), null, 403, LocalDateTime.now()));
    }

    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<AppResponse<String>> handleDisabledException(org.springframework.security.authentication.DisabledException ex) {
        log.warn("DisabledException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new AppResponse<>("Account is disabled. Please verify your email.", null, 403, LocalDateTime.now()));
    }

    /** 500 — last resort catch-all */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AppResponse<String>> handleRuntimeException(RuntimeException ex) {
        log.error("Unhandled RuntimeException", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AppResponse<>("Internal Server Error",
                        "An unexpected error occurred. Please try again later.", 500, LocalDateTime.now()));
    }
}
