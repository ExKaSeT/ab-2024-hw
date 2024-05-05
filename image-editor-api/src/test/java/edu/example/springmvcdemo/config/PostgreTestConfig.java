package edu.example.springmvcdemo.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import java.time.Duration;
import static java.util.Objects.isNull;


public class PostgreTestConfig {

    private static volatile PostgreSQLContainer<?> postgreSQLContainer = null;

    private static PostgreSQLContainer getPosgreSQLContainer() {
        PostgreSQLContainer instance = postgreSQLContainer;
        if (isNull(instance)) {
            synchronized (PostgreSQLContainer.class) {
                instance = postgreSQLContainer;
                if (isNull(instance)) {
                    postgreSQLContainer = instance = new PostgreSQLContainer<>("postgres:15.5")
                            .withDatabaseName("dbName")
                            .withUsername("username")
                            .withPassword("password")
                            .withStartupTimeout(Duration.ofSeconds(60))
                            .withReuse(true);
                    postgreSQLContainer.start();
                }
            }
        }
        return instance;
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            var postgresSQLContainer = getPosgreSQLContainer();

            var jdbcUrl = postgresSQLContainer.getJdbcUrl();
            var username = postgresSQLContainer.getUsername();
            var password = postgresSQLContainer.getPassword();

            TestPropertyValues.of(
                    "spring.datasource.url=" + jdbcUrl,
                    "spring.datasource.username=" + username,
                    "spring.datasource.password=" + password,
                    "spring.jpa.properties.hibernate.dialect=" + "org.hibernate.dialect.PostgreSQLDialect"
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
