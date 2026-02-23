package com.jobhunt.saas.service;

import com.jobhunt.saas.dto.RegRequest;
import com.jobhunt.saas.dto.RegResponse;
import com.jobhunt.saas.entity.*;
import com.jobhunt.saas.entity.TenantSubscription;
import com.jobhunt.saas.repository.PlanRepo;
import com.jobhunt.saas.repository.TenantRepo;
import com.jobhunt.saas.repository.UserRepo;
import com.jobhunt.saas.repository.TenantSubscriptionRepo;
import com.jobhunt.saas.tenant.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepo userRepo;
    private final TenantRepo tenantRepo;
    private final PlanRepo planRepo;
    private final TenantSubscriptionRepo tenantSubscriptionRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegResponse addUser(RegRequest requestDto) {

        // 1. Fetch default FREE/TRIAL plan
        Plan defaultPlan = planRepo.findByName("FREE").orElseThrow(
                () -> new RuntimeException("Plan Not Found"));

        // 2. Create Tenant
        Tenant tenant = new Tenant();
        tenant.setName(requestDto.getTenantName());
        tenant = tenantRepo.save(tenant);

        // 2.5 Create Engine Subscription
        TenantSubscription ts = new TenantSubscription();
        ts.setTenant(tenant);
        ts.setPlan(defaultPlan);
        ts.setStatus(SubscriptionStatus.ACTIVE);
        ts.setStartDate(LocalDateTime.now());
        ts.setExpireDate(LocalDateTime.now().plusDays(defaultPlan.getDurationInDays()));
        tenantSubscriptionRepo.save(ts);

        if (userRepo.existsByEmail(requestDto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // 3. Create TENANT ADMIN User
        Users user = new Users();
        user.setUsername(requestDto.getUserName());
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setRole(Role.ROLE_TENANT_ADMIN);
        user.setTenant(tenant);

        // 4. Save user
        userRepo.save(user);

        // 5. Return response
        return new RegResponse(user.getUsername(), user.getEmail());
    }

    public Users getUserByEmail(String email) {
        Long tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new RuntimeException("Tenant not resolved");
        }

        return userRepo.findByEmailAndTenant_Id(email, tenantId);
    }
}
