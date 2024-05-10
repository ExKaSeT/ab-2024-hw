package edu.example.springmvcdemo.processor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Math.sqrt;

public class RobertsCrossEdgeDetector implements ImageProcessor {

    private static double calculateBrightness(Color pixel) {
        return 0.299 * pixel.getRed() + 0.587 * pixel.getGreen() + 0.114 * pixel.getBlue();
    }

    @Override
    public InputStream process(InputStream imageStream, String imageExtension) throws IOException {
        BufferedImage image = ImageIO.read(imageStream);
        int width = image.getWidth();
        int height = image.getHeight();

        for (int w = 0; w < width - 1; w++) {
            for (int h = 0; h < height - 1; h++) {
                double Gx = calculateBrightness(new Color(image.getRGB(w + 1, h + 1))) -
                        calculateBrightness(new Color(image.getRGB(w, h)));
                double Gy = calculateBrightness(new Color(image.getRGB(w + 1, h))) -
                        calculateBrightness(new Color(image.getRGB(w, h + 1)));

                double gradient = sqrt(Gx * Gx + Gy * Gy);
                int gradientByte = gradient > 255 ? 255 : (int) gradient;
                image.setRGB(w, h, new Color(gradientByte, gradientByte, gradientByte).getRGB());
            }
        }

        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, imageExtension, outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
