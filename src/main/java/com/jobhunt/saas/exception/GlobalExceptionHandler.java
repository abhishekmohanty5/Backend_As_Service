package com.jobhunt.saas.exception;

import com.jobhunt.saas.dto.AppResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<AppResponse<String>> handleException(InvalidCredentialException ex) {
        AppResponse<String> response = new AppResponse<>("error", ex.getMessage(), 401, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        AppResponse<Map<String, String>> response = new AppResponse<>(
                "Validation failed",
                errors,
                400,
                LocalDateTime.now());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<AppResponse<String>> handleIllegalState(IllegalStateException ex) {

        AppResponse<String> response = new AppResponse<>(
                "Operation Failed",
                ex.getMessage(),
                400,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(SubscriptionException.class)
    public ResponseEntity<AppResponse<String>> handleSubscriptionException(SubscriptionException ex) {
        AppResponse<String> response = new AppResponse<>(
                "Subscription Error",
                ex.getMessage(),
                400,
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AppResponse<String>> handleRuntimeException(RuntimeException ex) {
        AppResponse<String> response = new AppResponse<>(
                "Error",
                ex.getMessage(),
                500,
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
