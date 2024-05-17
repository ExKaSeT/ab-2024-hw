package edu.example.springmvcdemo.config;

import edu.example.springmvcdemo.model.ImageProcessingFilter;
import edu.example.springmvcdemo.processor.ImageProcessor;
import edu.example.springmvcdemo.processor.RgbFilter;
import edu.example.springmvcdemo.processor.RobertsCrossEdgeDetector;
import edu.example.springmvcdemo.processor.Rotate90Clockwise;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
