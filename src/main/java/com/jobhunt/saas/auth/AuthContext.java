package com.jobhunt.saas.auth;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class AuthContext {
    public Long getCurrentUserId() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof CustomUserDetail)) {
            throw new SecurityException("Unauthenticated user");
        }
        return ((CustomUserDetail) principal).getId();
    }

}
