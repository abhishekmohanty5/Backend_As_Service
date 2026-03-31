package com.jobhunt.saas;

import com.jobhunt.saas.config.ApiKeyInterceptor;
import com.jobhunt.saas.config.ApiUsageInterceptor;
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
		"spring.mail.host=localhost",
		"spring.mail.port=1025",
		"spring.mail.username=",
		"spring.mail.password=",
		"jwt.secret=test-secret-key-for-testing-purposes-only-must-be-at-least-32-chars",
		"jwt.expiration=86400000"
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
