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

        ensurePlan("FREE", BigDecimal.ZERO, 14);
        ensurePlan("Starter", BigDecimal.valueOf(499), 30);
        ensurePlan("Pro", BigDecimal.valueOf(1499), 30);
        ensurePlan("Enterprise", BigDecimal.valueOf(3999), 30);

        log.info("Infrastructure pricing plans are seeded and up to date.");
    }

    private void ensurePlan(String name, BigDecimal price, int durationInDays) {
        java.util.Optional<Plan> existingOpt = planRepo.findByName(name);
        LocalDateTime now = LocalDateTime.now();

        if (existingOpt.isEmpty()) {
            log.info("Seeding {} plan...", name);
            Plan plan = new Plan();
            plan.setName(name);
            plan.setPrice(price);
            plan.setDurationInDays(durationInDays);
            plan.setActive(true);
            plan.setCreatedAt(now);
            plan.setUpdatedAt(now);
            planRepo.save(plan);
            return;
        }

        Plan existing = existingOpt.get();
        boolean changed = false;

        if (existing.getPrice() == null || existing.getPrice().compareTo(price) != 0) {
            existing.setPrice(price);
            changed = true;
        }

        if (existing.getDurationInDays() != durationInDays) {
            existing.setDurationInDays(durationInDays);
            changed = true;
        }

        if (!existing.isActive()) {
            existing.setActive(true);
            changed = true;
        }

        if (changed) {
            existing.setUpdatedAt(now);
            planRepo.save(existing);
            log.info("Updated {} plan to the canonical pricing.", name);
        }
    }
}
