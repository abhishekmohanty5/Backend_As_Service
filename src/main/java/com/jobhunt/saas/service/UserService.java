package com.jobhunt.saas.service;

import com.jobhunt.saas.dto.RegistrationRequest;
import com.jobhunt.saas.dto.RegistrationResponse;
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

    public RegistrationResponse addUser(RegistrationRequest registrationRequest) {

        // 1. Fetch default FREE/TRIAL plan
        Plan defaultPlan = planRepo.findByName("FREE").orElseThrow(
                () -> new RuntimeException("Plan Not Found"));

        // 2. Create Tenant
        Tenant tenant = new Tenant();
<<<<<<< HEAD
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
=======
        tenant.setName(registrationRequest.getTenantName());
        tenant.setPlan(defaultPlan);
        tenant.setStatus(SubscriptionStatus.ACTIVE);
        tenantRepo.save(tenant);
>>>>>>> 0548ce46dba79041dabbb5c9a85f5e31e2afd07b

        if (userRepo.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // 3. Create TENANT ADMIN User
        Users user = new Users();
<<<<<<< HEAD
        user.setUsername(requestDto.getUserName());
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setRole(Role.ROLE_TENANT_ADMIN);
=======
        user.setUsername(registrationRequest.getUserName());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setRole(Role.ROLE_ADMIN);
>>>>>>> 0548ce46dba79041dabbb5c9a85f5e31e2afd07b
        user.setTenant(tenant);

        // 4. Save user
        userRepo.save(user);

        // 5. Return response
        return new RegistrationResponse(user.getUsername(), user.getEmail());
    }

    public Users getUserByEmail(String email) {
        Long tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new RuntimeException("Tenant not resolved");
        }

        return userRepo.findByEmailAndTenant_Id(email, tenantId);
    }
}
