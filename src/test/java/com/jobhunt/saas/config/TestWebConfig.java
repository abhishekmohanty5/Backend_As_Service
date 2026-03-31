package com.jobhunt.saas.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Test configuration to bypass interceptor loading during tests
 */
@TestConfiguration
public class TestWebConfig implements WebMvcConfigurer {
    // Empty implementation - no interceptors for tests
}
