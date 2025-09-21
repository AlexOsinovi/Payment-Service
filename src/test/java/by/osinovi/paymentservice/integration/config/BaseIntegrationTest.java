package by.osinovi.paymentservice.integration.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    protected static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withStartupTimeout(java.time.Duration.ofMinutes(2));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "payment_service_test");
        registry.add("spring.data.mongodb.host", () -> "localhost");
        registry.add("spring.data.mongodb.username", () -> "testuser");
        registry.add("spring.data.mongodb.password", () -> "testpass");
    }
}