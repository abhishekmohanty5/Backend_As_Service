package com.jobhunt.saas.service;

import com.jobhunt.saas.auth.JWTService;
import com.jobhunt.saas.dto.LoginRequest;
import com.jobhunt.saas.dto.LoginResponse;
import com.jobhunt.saas.entity.Users;
import com.jobhunt.saas.exception.InvalidCredentialException;
import com.jobhunt.saas.repository.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JWTService jwtService;

    public LoginResponse login(LoginRequest loginRequest) {

        String email = loginRequest.getEmail();

        if (!userRepo.existsByEmail(email)) {
            throw new InvalidCredentialException("Email not Exist Please Register");
        }
        Users user = userRepo.findByEmail(email);

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Invalid credentials. Please try again.");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getTenant().getId());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setEmail(email);
        return loginResponse;
    }
}
