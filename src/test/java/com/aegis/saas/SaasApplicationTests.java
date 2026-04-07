package com.aegis.saas;

import com.aegis.saas.config.ApiKeyInterceptor;
import com.aegis.saas.config.ApiUsageInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
		"spring.datasource.driverClassName=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.base-url=http://localhost:8080",
		"APP_BASE_URL=http://localhost:8080",
		"spring.mail.host=localhost",
		"spring.mail.port=1025",
		"spring.mail.username=",
		"spring.mail.password=",
		"application.security.jwt.secret-key=test-secret-key-for-testing-purposes-only-must-be-at-least-32-chars",
		"jwt.expiration=86400000",
		"CORS_ALLOWED_ORIGINS=http://localhost:3000",
		"GEMINI_API_KEY=dummy-key-for-tests",
		"DATABASE_URL=jdbc:h2:mem:testdb",
		"DATABASE_USERNAME=sa",
		"DATABASE_PASSWORD="
})
@ActiveProfiles("test")
class SaasApplicationTests {

	@MockBean
	private ApiKeyInterceptor apiKeyInterceptor;

	@MockBean
	private ApiUsageInterceptor apiUsageInterceptor;

	@Test
	void contextLoads() {
	}

}
