package edu.example.springmvcdemo.config;

import edu.example.springmvcdemo.model.ImageProcessingFilter;
import edu.example.springmvcdemo.processor.*;
import edu.example.springmvcdemo.service.ImaggaIntegration;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@NoArgsConstructor
public class ProcessorTypeConfig {

    @Value("${processor.type}")
    private ImageProcessingFilter type;

    public String getProcessorName() {
        return type.name();
    }

    @Bean
    public ImageProcessor getProcessor(@Autowired(required = false)ImaggaIntegration imaggaIntegration,
                                       CircuitBreakerFactory circuitBreakerFactory) {
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
            case TAGGING -> {
                return new ImaggaImageTagger(imaggaIntegration, circuitBreakerFactory);
            }
            default -> throw new IllegalStateException("Specified processor is not implemented");
        }
    }
}
