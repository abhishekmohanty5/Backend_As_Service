package com.jobhunt.saas.auth.application.dto;

import com.jobhunt.saas.shared.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String email;
    private String token;
    private Role role;
}
