package edu.example.springmvcdemo.config;

import edu.example.springmvcdemo.model.ImageProcessingFilter;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "processor")
@NoArgsConstructor
public class ProcessorTypeConfig {

    public static String getSchemaName(ImageProcessingFilter type) {
        return type.name() + "-processor";
    }

    @EventListener
    public void setSchema(ApplicationEnvironmentPreparedEvent event) {
        var type = ImageProcessingFilter.valueOf(event.getEnvironment().getProperty("processor.type"));
        ConfigurableEnvironment environment = event.getEnvironment();
        Properties props = new Properties();
        props.put("spring.datasource.schema", ProcessorTypeConfig.getSchemaName(type));
        environment.getPropertySources().addFirst(new PropertiesPropertySource("schemaProps", props));
    }

    @NotNull
    private ImageProcessingFilter type;

    public String getProcessorName() {
        return type.name();
    }

    public String getSchemaName() {
        return ProcessorTypeConfig.getSchemaName(this.type);
    }
}
