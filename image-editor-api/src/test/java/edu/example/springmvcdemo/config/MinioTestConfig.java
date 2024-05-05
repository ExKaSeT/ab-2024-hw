package edu.example.springmvcdemo.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.MinIOContainer;
import java.time.Duration;

@SpringBootTest
@ContextConfiguration
public class MinioTestConfig {

    private static volatile MinIOContainer minIOContainer = null;

    private static MinIOContainer getMinIOContainer() {
        MinIOContainer instance = minIOContainer;
        if (instance == null) {
            synchronized (MinIOContainer.class) {
                instance = minIOContainer;
                if (instance == null) {
                    minIOContainer = instance = new MinIOContainer(
                            "minio/minio:RELEASE.2024-03-30T09-41-56Z")
                            .withUserName("username")
                            .withPassword("password")
                            .withStartupTimeout(Duration.ofSeconds(60))
                            .withReuse(true);
                    minIOContainer.start();
                }
            }
        }
        return instance;
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            var minIOContainer = getMinIOContainer();

            var url = minIOContainer.getS3URL();
            var username = minIOContainer.getUserName();
            var password = minIOContainer.getPassword();

            TestPropertyValues.of(
                    "minio.datasource.url=" + url,
                    "minio.datasource.username=" + username,
                    "minio.datasource.password=" + password
            ).applyTo(applicationContext.getEnvironment());
        }
    }

}