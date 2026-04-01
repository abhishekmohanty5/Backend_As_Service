package com.jobhunt.saas.user.domain.port.in;

import com.jobhunt.saas.user.application.dto.RegistrationRequest;
import com.jobhunt.saas.user.application.dto.RegistrationResponse;
import com.jobhunt.saas.user.domain.model.Users;

public interface UserUseCase {
    RegistrationResponse addUser(RegistrationRequest registrationRequest);
    void resendVerificationEmail(String email);
    Users getUserByEmail(String email);
}
