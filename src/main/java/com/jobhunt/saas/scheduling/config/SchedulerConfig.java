package com.jobhunt.saas.scheduling.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SchedulerConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        org.springframework.jdbc.core.JdbcTemplate jdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(
                dataSource);

        // Ensure table exists (H2/MySQL compatible schema creation)
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS shedlock(" +
                "name VARCHAR(64) NOT NULL, " +
                "lock_until TIMESTAMP(3) NOT NULL, " +
                "locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), " +
                "locked_by VARCHAR(255) NOT NULL, " +
                "PRIMARY KEY (name))");

        return new JdbcTemplateLockProvider(
                net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(jdbcTemplate)
                        .usingDbTime()
                        .build());
    }
}
