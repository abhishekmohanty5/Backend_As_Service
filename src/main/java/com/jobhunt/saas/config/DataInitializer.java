package com.jobhunt.saas.config;

import com.jobhunt.saas.entity.BillingCycle;
import com.jobhunt.saas.entity.Plan;
import com.jobhunt.saas.entity.Role;
import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.Tenant;
import com.jobhunt.saas.entity.TenantPlan;
import com.jobhunt.saas.entity.UserSubscription;
import com.jobhunt.saas.entity.Users;
import com.jobhunt.saas.repository.PlanRepo;
import com.jobhunt.saas.repository.TenantPlanRepo;
import com.jobhunt.saas.repository.TenantRepo;
import com.jobhunt.saas.repository.TenantSubscriptionRepo;
import com.jobhunt.saas.repository.UserRepo;
import com.jobhunt.saas.repository.UserSubscriptionRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Configuration
@org.springframework.context.annotation.Profile("!test")
public class DataInitializer {

    @org.springframework.beans.factory.annotation.Value("${APP_ADMIN_PASSWORD:superadmin123}")
    private String adminPassword;

    @org.springframework.beans.factory.annotation.Value("${APP_USER_PASSWORD:password123}")
    private String userPassword;

    @Bean
    public CommandLineRunner initData(PlanRepo planRepo, TenantRepo tenantRepo, UserRepo userRepo,
            PasswordEncoder passwordEncoder, TenantPlanRepo tenantPlanRepo, UserSubscriptionRepo userSubscriptionRepo,
            TenantSubscriptionRepo tenantSubscriptionRepo) {
        return args -> {
            // 1. Create Engine SaaS Plans if missing
            Plan freePlan = planRepo.findByName("FREE").orElseGet(() -> {
                Plan p = new Plan();
                p.setName("FREE");
                p.setPrice(BigDecimal.ZERO);
                p.setDurationInDays(14); // 14-day trial
                p.setActive(true);
                p.setCreatedAt(LocalDateTime.now());
                p.setUpdatedAt(LocalDateTime.now());
                log.info("✅ Created FREE plan in Database");
                return planRepo.save(p);
            });

            Plan starterPlan = planRepo.findByName("STARTER").orElseGet(() -> {
                Plan p = new Plan();
                p.setName("STARTER");
                p.setPrice(BigDecimal.valueOf(499.00));
                p.setDurationInDays(30);
                p.setActive(true);
                p.setCreatedAt(LocalDateTime.now());
                p.setUpdatedAt(LocalDateTime.now());
                log.info("✅ Created STARTER plan (₹499) in Database");
                return planRepo.save(p);
            });
            // Update price if it was previously seeded at old value
            if (starterPlan.getPrice().compareTo(BigDecimal.valueOf(499.00)) != 0) {
                starterPlan.setPrice(BigDecimal.valueOf(499.00));
                starterPlan.setUpdatedAt(LocalDateTime.now());
                planRepo.save(starterPlan);
                log.info("🔄 Updated STARTER plan price to ₹499");
            }

            Plan proPlan = planRepo.findByName("PRO").orElseGet(() -> {
                Plan p = new Plan();
                p.setName("PRO");
                p.setPrice(BigDecimal.valueOf(1499.00));
                p.setDurationInDays(30);
                p.setActive(true);
                p.setCreatedAt(LocalDateTime.now());
                p.setUpdatedAt(LocalDateTime.now());
                log.info("✅ Created PRO plan (₹1499) in Database");
                return planRepo.save(p);
            });
            // Update price if it was previously seeded at old value
            if (proPlan.getPrice().compareTo(BigDecimal.valueOf(1499.00)) != 0) {
                proPlan.setPrice(BigDecimal.valueOf(1499.00));
                proPlan.setUpdatedAt(LocalDateTime.now());
                planRepo.save(proPlan);
                log.info("🔄 Updated PRO plan price to ₹1499");
            }

            Plan enterprisePlan = planRepo.findByName("ENTERPRISE").orElseGet(() -> {
                Plan p = new Plan();
                p.setName("ENTERPRISE");
                p.setPrice(BigDecimal.valueOf(3999.00));
                p.setDurationInDays(30);
                p.setActive(true);
                p.setCreatedAt(LocalDateTime.now());
                p.setUpdatedAt(LocalDateTime.now());
                log.info("✅ Created ENTERPRISE plan (₹3999) in Database");
                return planRepo.save(p);
            });
            // Update price if it was previously seeded at old value
            if (enterprisePlan.getPrice().compareTo(BigDecimal.valueOf(3999.00)) != 0) {
                enterprisePlan.setPrice(BigDecimal.valueOf(3999.00));
                enterprisePlan.setUpdatedAt(LocalDateTime.now());
                planRepo.save(enterprisePlan);
                log.info("🔄 Updated ENTERPRISE plan price to ₹3999");
            }

            // Deactivate old PREMIUM plan if it exists (renamed to ENTERPRISE)
            planRepo.findByName("PREMIUM").ifPresent(oldPlan -> {
                if (oldPlan.isActive()) {
                    oldPlan.setActive(false);
                    oldPlan.setUpdatedAt(LocalDateTime.now());
                    planRepo.save(oldPlan);
                    log.info("🔄 Deactivated old PREMIUM plan (replaced by ENTERPRISE)");
                }
            });

            // 2. Create Master Tenant (Aegis Infra)
            Tenant aegisInfra = tenantRepo.findById(1L).orElseGet(() -> {
                Tenant t = new Tenant();
                t.setName("Aegis Infra");
                t.setCreatedAt(LocalDateTime.now());
                // ClientId and ClientSecret generate via @PrePersist
                Tenant savedT = tenantRepo.save(t);

                com.jobhunt.saas.entity.TenantSubscription ts = new com.jobhunt.saas.entity.TenantSubscription();
                ts.setTenant(savedT);
                ts.setPlan(freePlan);
                ts.setStatus(SubscriptionStatus.ACTIVE);
                ts.setStartDate(LocalDateTime.now());
                ts.setExpireDate(LocalDateTime.now().plusDays(freePlan.getDurationInDays()));
                tenantSubscriptionRepo.save(ts);

                log.info("✅ Created Aegis Infra Master Tenant");
                return savedT;
            });

            // 3. Create Super Admin User
            if (userRepo.findByEmailAndTenant_Id("admin@aegisinfra.com", aegisInfra.getId()) == null) {
                Users admin = new Users();
                admin.setUsername("Aegis Super Admin");
                admin.setEmail("admin@aegisinfra.com");
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ROLE_SUPER_ADMIN);
                admin.setTenant(aegisInfra);
                userRepo.save(admin);
                log.info("✅ Created Aegis Infra Super Admin (admin@aegisinfra.com)");
            }

            // 4. Create Dummy Data for AI Testing under Aegis Infra
            // A. Create Plans
            TenantPlan basicPlan = tenantPlanRepo.findByTenantIdAndName(aegisInfra.getId(), "Starter Plan").stream()
                    .findFirst().orElseGet(() -> {
                        TenantPlan tp = new TenantPlan();
                        tp.setTenant(aegisInfra);
                        tp.setName("Starter Plan");
                        tp.setDescription("Basic entry-level tier.");
                        tp.setPrice(BigDecimal.valueOf(15.00));
                        tp.setBillingCycle(BillingCycle.MONTHLY);
                        return tenantPlanRepo.save(tp);
                    });

            TenantPlan growthPlan = tenantPlanRepo.findByTenantIdAndName(aegisInfra.getId(), "Growth Plan").stream()
                    .findFirst().orElseGet(() -> {
                        TenantPlan tp = new TenantPlan();
                        tp.setTenant(aegisInfra);
                        tp.setName("Growth Plan");
                        tp.setDescription("Most popular for small teams.");
                        tp.setPrice(BigDecimal.valueOf(49.00));
                        tp.setBillingCycle(BillingCycle.MONTHLY);
                        return tenantPlanRepo.save(tp);
                    });

            TenantPlan demoEnterprisePlan = tenantPlanRepo.findByTenantIdAndName(aegisInfra.getId(), "Enterprise Plan")
                    .stream().findFirst().orElseGet(() -> {
                        TenantPlan tp = new TenantPlan();
                        tp.setTenant(aegisInfra);
                        tp.setName("Enterprise Plan");
                        tp.setDescription("Full-featured for large organizations.");
                        tp.setPrice(BigDecimal.valueOf(199.00));
                        tp.setBillingCycle(BillingCycle.YEARLY);
                        return tenantPlanRepo.save(tp);
                    });

            // B. Create Users
            Users userAlice = createDummyUser(userRepo, passwordEncoder, aegisInfra, "Alice Johnson",
                    "alice@example.com");
            Users userBob = createDummyUser(userRepo, passwordEncoder, aegisInfra, "Bob Smith", "bob@example.com");
            Users userCharlie = createDummyUser(userRepo, passwordEncoder, aegisInfra, "Charlie Davis",
                    "charlie@example.com");

            // C. Create Subscriptions if they don't exist
            if (userSubscriptionRepo.findByUserId(userAlice.getId()).isEmpty()) {
                // Alice: Steady Growth Plan customer
                createDummySub(userSubscriptionRepo, userAlice, growthPlan, LocalDate.now().minusMonths(6),
                        LocalDate.now().plusDays(15), SubscriptionStatus.ACTIVE,
                        "Loyal customer, upgraded from starter 2 months ago.");
            }

            if (userSubscriptionRepo.findByUserId(userBob.getId()).isEmpty()) {
                // Bob: Churned customer on Starter Plan
                createDummySub(userSubscriptionRepo, userBob, basicPlan, LocalDate.now().minusMonths(3),
                        LocalDate.now().minusMonths(1), SubscriptionStatus.CANCELLED,
                        "Cancelled due to budget constraints.");
                // Bob: Briefly tried returning but payment failed
                createDummySub(userSubscriptionRepo, userBob, basicPlan, LocalDate.now().minusDays(10),
                        LocalDate.now().minusDays(3), SubscriptionStatus.EXPIRED, "Payment failed on renewal attempt.");
            }

            if (userSubscriptionRepo.findByUserId(userCharlie.getId()).isEmpty()) {
                // Charlie: Enterprise yearly customer, high value
                createDummySub(userSubscriptionRepo, userCharlie, demoEnterprisePlan, LocalDate.now().minusMonths(11),
                        LocalDate.now().plusMonths(1), SubscriptionStatus.ACTIVE,
                        "VIP Client. Needs account review before renewal.");

                log.info("✅ Custom AI Demo Data Seeded for Aegis Infra Tenant");
            }

        };
    }

    private Users createDummyUser(UserRepo userRepo, PasswordEncoder passwordEncoder, Tenant tenant, String name,
            String email) {
        Users user = userRepo.findByEmailAndTenant_Id(email, tenant.getId());
        if (user == null) {
            user = new Users();
            user.setUsername(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(userPassword));
            user.setRole(Role.ROLE_USER);
            user.setTenant(tenant);
            return userRepo.save(user);
        }
        return user;
    }

    private void createDummySub(UserSubscriptionRepo repo, Users user, TenantPlan plan, LocalDate start,
            LocalDate nextBilling, SubscriptionStatus status, String notes) {
        UserSubscription sub = new UserSubscription();
        sub.setUser(user);
        sub.setSubscriptionName(plan.getName());
        sub.setTenantPlan(plan);
        sub.setAmount(plan.getPrice());
        sub.setBillingCycle(plan.getBillingCycle());
        sub.setStartDate(start);
        sub.setNextBillingDate(nextBilling);
        sub.setStatus(status);
        sub.setNotes(notes);
        repo.save(sub);
    }
}
