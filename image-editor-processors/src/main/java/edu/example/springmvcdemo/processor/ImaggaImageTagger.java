package edu.example.springmvcdemo.processor;

import edu.example.springmvcdemo.dto.imagga_image_tagger.TagDto;
import edu.example.springmvcdemo.dto.processor.StreamDataDto;
import edu.example.springmvcdemo.service.ImaggaIntegration;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.AttributedString;
import java.util.List;
import java.util.stream.Collectors;

public class ImaggaImageTagger implements ImageProcessor {

    private final ImaggaIntegration imaggaIntegration;
    private final CircuitBreaker circuitBreaker;

    public ImaggaImageTagger(ImaggaIntegration imaggaIntegration, CircuitBreakerFactory circuitBreakerFactory) {
        this.imaggaIntegration = imaggaIntegration;
        this.circuitBreaker = circuitBreakerFactory.create("imagga-integration");
    }

    @Override
    public StreamDataDto process(InputStream imageStream, String imageExtension) throws IOException {
        var imageBytes = imageStream.readAllBytes();
        var imaggaImageId = circuitBreaker
                .run(() -> imaggaIntegration.uploadImage(new ByteArrayInputStream(imageBytes)));
        var tags = circuitBreaker.run(() -> imaggaIntegration.getImageTags(imaggaImageId));
        var result = drawTags(new ByteArrayInputStream(imageBytes), tags);
        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(result, imageExtension, outputStream);
        return new StreamDataDto(outputStream);
    }

    private BufferedImage drawTags(InputStream stream, List<TagDto> tags) throws IOException {
        String text = tags.stream()
                .map(tag -> String.format("%s(%.2f%%)", tag.getName(), tag.getProbability()))
                .collect(Collectors.joining(", ", " ", " "));

        BufferedImage image = ImageIO.read(stream);
        Graphics graphics = image.getGraphics();

        Font font = new Font("Arial", Font.BOLD, 28);
        AttributedString attributedText = new AttributedString(text);
        attributedText.addAttribute(TextAttribute.FONT, font);
        attributedText.addAttribute(TextAttribute.BACKGROUND, Color.BLACK);
        attributedText.addAttribute(TextAttribute.FOREGROUND, Color.WHITE);

        FontMetrics metrics = graphics.getFontMetrics(font);
        int posX = 0;
        int posY = image.getHeight() - metrics.getHeight() + metrics.getAscent();
        graphics.drawString(attributedText.getIterator(), posX, posY);

        return image;
    }
}
