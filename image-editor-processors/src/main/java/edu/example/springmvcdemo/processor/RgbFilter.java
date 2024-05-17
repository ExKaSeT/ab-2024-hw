package edu.example.springmvcdemo.processor;

import edu.example.springmvcdemo.dto.processor.StreamDataDto;
import lombok.RequiredArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class RgbFilter implements ImageProcessor {

    private final ColorFilter colorFilter;

    @Override
    public StreamDataDto process(InputStream imageStream, String imageExtension) throws IOException {
        BufferedImage image = ImageIO.read(imageStream);
        int width = image.getWidth();
        int height = image.getHeight();

        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                var color = new Color(image.getRGB(w, h));
                int colorToSet;
                switch (colorFilter) {
                    case RED -> colorToSet = new Color(color.getRed(), 0, 0).getRGB();
                    case GREEN -> colorToSet = new Color(0, color.getGreen(), 0).getRGB();
                    case BLUE -> colorToSet = new Color(0, 0, color.getBlue()).getRGB();
                    default -> throw new IllegalStateException("Specified filter is not implemented");
                }
                image.setRGB(w, h, colorToSet);
            }
        }

        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, imageExtension, outputStream);
        return new StreamDataDto(outputStream);
    }

    public enum ColorFilter {
        RED,
        GREEN,
        BLUE
    }
}
