package com.jobhunt.saas.auth.domain.port.in;

import com.jobhunt.saas.auth.application.dto.LoginRequest;
import com.jobhunt.saas.auth.application.dto.LoginResponse;

public interface AuthUseCase {
    LoginResponse login(LoginRequest loginRequest);
    void verifyEmail(String token);
}
