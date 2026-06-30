package tcc.bes.api_monolito.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "app.jobs.worker.enabled=false",
        "app.jobs.expiration.enabled=false",
        "app.bootstrap.manager.username=admin",
        "app.bootstrap.manager.password=admin123",
        "app.security.jwt-secret=test-jwt-secret-with-more-than-32-bytes",
        "app.security.encryption-secret=test-encryption-secret-with-32-bytes"
})
public abstract class PostgresIntegrationTestSupport {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
