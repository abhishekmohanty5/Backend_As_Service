package com.aegis.saas.dto;

import com.aegis.saas.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String email;
    private String name;
    private String token;  // access token
    private String refreshToken;
    private String tokenType;
    private long expiresIn;  // in seconds
    private Role role;
}
