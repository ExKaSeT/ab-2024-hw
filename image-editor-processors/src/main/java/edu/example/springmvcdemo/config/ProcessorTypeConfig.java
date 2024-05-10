package edu.example.springmvcdemo.config;

import edu.example.springmvcdemo.model.ImageProcessingFilter;
import edu.example.springmvcdemo.processor.ImageProcessor;
import edu.example.springmvcdemo.processor.RgbFilter;
import edu.example.springmvcdemo.processor.RobertsCrossEdgeDetector;
import edu.example.springmvcdemo.processor.Rotate90Clockwise;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

@Configuration
@NoArgsConstructor
public class ProcessorTypeConfig {

    public static String getSchemaName(ImageProcessingFilter type) {
        return type.name() + "-processor";
    }

    /**
     * Схема в бд выставляется на основе названия обработчика, что позволяет использовать 1 базу для всех
    * */
    @EventListener
    public void setSchema(ApplicationEnvironmentPreparedEvent event) {
        var type = ImageProcessingFilter.valueOf(event.getEnvironment().getProperty("processor.type"));
        ConfigurableEnvironment environment = event.getEnvironment();
        Properties props = new Properties();
        props.put("spring.datasource.schema", ProcessorTypeConfig.getSchemaName(type));
        environment.getPropertySources().addFirst(new PropertiesPropertySource("schemaProps", props));
    }

    @Value("${processor.type}")
    private ImageProcessingFilter type;

    public String getProcessorName() {
        return type.name();
    }

    public String getSchemaName() {
        return ProcessorTypeConfig.getSchemaName(this.type);
    }

    @Bean
    public ImageProcessor getProcessor() {
        switch (type) {
            case TO_RED -> {
                return new RgbFilter(RgbFilter.ColorFilter.RED);
            }
            case TO_GREEN -> {
                return new RgbFilter(RgbFilter.ColorFilter.GREEN);
            }
            case TO_BLUE -> {
                return new RgbFilter(RgbFilter.ColorFilter.BLUE);
            }
            case ROTATE_90_CLOCKWISE -> {
                return new Rotate90Clockwise();
            }
            case BORDER_SELECTION -> {
                return new RobertsCrossEdgeDetector();
            }
            default -> throw new IllegalStateException("Specified processor is not implemented");
        }
    }
}
