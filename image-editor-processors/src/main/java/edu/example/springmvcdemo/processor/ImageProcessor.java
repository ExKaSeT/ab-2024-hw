package edu.example.springmvcdemo.processor;

import java.io.IOException;
import java.io.InputStream;

public interface ImageProcessor {
    InputStream process(InputStream imageStream, String imageExtension) throws IOException;
}
