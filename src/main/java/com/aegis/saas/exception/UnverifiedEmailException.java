package com.aegis.saas.exception;

public class UnverifiedEmailException extends RuntimeException {
    public UnverifiedEmailException(String message) {
        super(message);
    }
}
