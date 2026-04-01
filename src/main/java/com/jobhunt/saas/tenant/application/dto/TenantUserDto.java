package com.jobhunt.saas.tenant.application.dto;

import com.jobhunt.saas.shared.domain.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantUserDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
}
