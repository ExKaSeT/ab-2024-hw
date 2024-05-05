package edu.example.springmvcdemo.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquibaseConfig {

    @Bean
    public SpringLiquibase liquibase(ProcessorTypeConfig processorConfig) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDefaultSchema(processorConfig.getSchemaName());
        return liquibase;
    }
}
