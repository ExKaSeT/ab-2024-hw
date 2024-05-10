package edu.example.springmvcdemo.processor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Rotate90Clockwise implements ImageProcessor {

    @Override
    public InputStream process(InputStream imageStream, String imageExtension) throws IOException {
        BufferedImage image = ImageIO.read(imageStream);
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(height, width, image.getType());
        Graphics2D graphics2D = result.createGraphics();
        graphics2D.translate((height - width) / 2, (height - width) / 2);
        graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
        graphics2D.drawRenderedImage(image, null);

        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(result, imageExtension, outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
