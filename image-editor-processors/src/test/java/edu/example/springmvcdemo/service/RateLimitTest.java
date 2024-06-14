package edu.example.springmvcdemo.service;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitException;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import edu.example.springmvcdemo.annotation.IntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@IntegrationTest
@RequiredArgsConstructor
@TestPropertySource(locations = "classpath:rate-limit-test.properties")
public class RateLimitTest {
    private static final String RATE_LIMIT_CACHE_TABLE_NAME = "rate_limiting_cache";

    private final DataSource dataSource;
    private final ComponentWithRateLimit componentWithRateLimit;

    @AfterEach
    public void clear() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            connection.prepareStatement("DELETE FROM " + RATE_LIMIT_CACHE_TABLE_NAME).execute();
        }
    }

    @Test
    public void checkRateLimitNonExceedAfterMethodThrowingError() {
        for (int checks = 0; checks < 5; checks++) {
            try {
                componentWithRateLimit.testMethodThrowsException();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    @Test
    public void checkRateLimitExceed() {
        componentWithRateLimit.testMethodWithoutException();
        assertThrows(RateLimitException.class, componentWithRateLimit::testMethodWithoutException);
    }
}

@Component
@RateLimiting(
        name = "rate-limit-test",
        ratePerMethod = true
)
class ComponentWithRateLimit {
    public void testMethodThrowsException() {
        throw new IllegalStateException();
    }

    public void testMethodWithoutException() {
    }
}