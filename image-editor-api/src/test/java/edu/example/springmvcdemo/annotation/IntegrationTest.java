package edu.example.springmvcdemo.annotation;

import edu.example.springmvcdemo.config.MinioTestConfig;
import edu.example.springmvcdemo.config.PostgreTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles({"test", "dev"})
@SpringBootTest(value = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration, org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration")
@ContextConfiguration(initializers = {PostgreTestConfig.Initializer.class, MinioTestConfig.Initializer.class})
public @interface IntegrationTest {
}
