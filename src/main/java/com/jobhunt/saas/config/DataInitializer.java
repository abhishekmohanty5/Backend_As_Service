package com.jobhunt.saas.config;

import com.jobhunt.saas.entity.Plan;
import com.jobhunt.saas.repository.PlanRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(PlanRepo planRepo) {
        return args -> {
            if (planRepo.findByName("FREE").isEmpty()) {
                Plan freePlan = new Plan();
                freePlan.setName("FREE");
                freePlan.setPrice(BigDecimal.ZERO);
                freePlan.setDurationInDays(14);
                freePlan.setActive(true);
                freePlan.setCreatedAt(LocalDateTime.now());
                freePlan.setUpdatedAt(LocalDateTime.now());
                planRepo.save(freePlan);
                System.out.println("✅ Automatically created FREE plan in Database!");
            }
        };
    }
}
