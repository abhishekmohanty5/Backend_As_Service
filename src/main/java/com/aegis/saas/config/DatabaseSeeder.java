package com.aegis.saas.config;

import com.aegis.saas.entity.Plan;
import com.aegis.saas.repository.PlanRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final PlanRepo planRepo;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking infrastructure data...");

        if (planRepo.findByName("FREE").isEmpty()) {
            log.info("FREE plan not found. Seeding default infrastructure plan...");

            Plan defaultPlan = new Plan();
            defaultPlan.setName("FREE");
            defaultPlan.setPrice(BigDecimal.ZERO);
            defaultPlan.setDurationInDays(14);
            defaultPlan.setActive(true);
            defaultPlan.setCreatedAt(LocalDateTime.now());
            defaultPlan.setUpdatedAt(LocalDateTime.now());

            planRepo.save(defaultPlan);
            
            log.info("Successfully seeded FREE plan into database!");
        } else {
            log.info("Infrastructure data already exists. Skipping seed.");
        }
    }
}
