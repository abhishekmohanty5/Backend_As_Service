package com.jobhunt.saas.dto;

import com.jobhunt.saas.entity.Role;
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
