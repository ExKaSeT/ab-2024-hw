package edu.example.springmvcdemo.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "imagga.tagger")
@ConditionalOnProperty(name = "processor.type", havingValue = "TAGGING")
@Data
public class ImaggaImageTaggerConfig {
    @NotBlank
    private String uploadImageUrl;
    @NotBlank
    private String getImageTagsUrl;
    @NotBlank
    private String apiKey;
    @NotBlank
    private String apiSecret;
}