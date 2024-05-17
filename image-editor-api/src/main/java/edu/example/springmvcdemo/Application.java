package edu.example.springmvcdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableConfigurationProperties
@EnableRetry
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

